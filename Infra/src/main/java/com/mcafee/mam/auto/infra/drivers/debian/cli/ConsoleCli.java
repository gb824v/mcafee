package com.mcafee.mam.auto.infra.drivers.debian.cli;

import java.io.IOException;
import org.apache.log4j.Logger;

import com.mcafee.mam.auto.infra.Stopwatch;
import com.mcafee.mam.auto.infra.TestException;
import com.mcafee.mam.auto.infra.drivers.debian.DebianCommand;
import com.mcafee.mam.auto.infra.drivers.debian.DebianInfra;
import com.mcafee.mam.auto.infra.drivers.debian.DebianResponse;
import com.mcafee.mam.auto.infra.drivers.debian.ProcessStatus;
import com.mcafee.mam.auto.infra.drivers.debian.Prompt;
import com.mcafee.mam.auto.infra.log.html.FileUtils;
import com.mcafee.mam.auto.infra.util.MAC;

/**
 * A driver for communicating with MAM sensor.
 * 
 * @author guy
 */
public class ConsoleCli extends DebianInfra
{
	public static final String DB_PROPERTIES = "/usr/lib/insightix/management/conf/msconfig.properties";
	public static final String DB_CONF_FILE = "/etc/postgresql/9.1/main/postgresql.conf";
	public static final String DB_PG_HBA_FILE = "/etc/postgresql/9.1/main/pg_hba.conf";

	private static Logger logger = Logger.getLogger(ConsoleCli.class);
	private ProcessStatus processes = new ProcessStatus();

	@Override
	public void init() throws TestException
	{
		if (this.host.startsWith("${"))
		{
			String paramName = this.host.substring(this.host.indexOf("{") + 1, this.host.indexOf('}'));
			this.host = System.getProperty(paramName);
		}
		if (this.user.startsWith("${"))
		{
			String paramName = this.user.substring(this.user.indexOf("{") + 1, this.user.indexOf('}'));
			this.user = System.getProperty(paramName);
		}
		if (this.password.startsWith("${"))
		{
			String paramName = this.password.substring(this.password.indexOf("{") + 1, this.password.indexOf('}'));
			this.password = System.getProperty(paramName);
		}
	}

	public String getHashPassword(String passToHash) throws Exception
	{
		DebianCommand dCmd = new DebianCommand("/usr/lib/insightix/management/bin/createHash.sh");
		dCmd.addPrompt(new Prompt("hash:]", passToHash + "\n"));
		return execute(dCmd).toString().split("\n")[2];
	}

	/**
	 * 
	 * @throws Exception
	 */
	public void stopAllProcess() throws Exception
	{
		execute("monit stop all");
		processes.setStatus(showMonitSummary().toString());
	}

	/**
	 * 
	 * @throws Exception
	 */
	public void startAllProcess() throws Exception
	{
		execute("monit start all");
		waitForProcessesUp();
	}

	/**
	 * 
	 * @throws Exception
	 */
	public void restartAllProcesses() throws Exception
	{
		execute("monit restart all");
		waitForProcessesUp();
	}

	/**
	 * 
	 * @param process
	 * @throws Exception
	 */
	public void restartProcess(String process) throws Exception
	{
		execute("monit restart " + process);
		waitForProcessesUp();
	}

	private DebianResponse showMonitSummary() throws Exception
	{
		return execute("monit summary");
	}

	/**
	 * Tests whether sensor can be connected to.
	 * 
	 * @return true if sensor is accessible.
	 * @throws Exception
	 *             if connection attempt cannot be made.
	 */
	public boolean waitForProcessesUp() throws Exception
	{
		Stopwatch stopwatch = this.getStopwatch();
		do
		{
			try
			{
				String cliOutput = showMonitSummary().toString();
				this.processes.setStatus(cliOutput);
				if (this.processes.isAllRunning())
				{
					logger.info("reconnect: Sensor process are reconnected");
					return true;
				}
				Thread.sleep(5000);
			}
			catch (IOException ex)
			{
				logger.debug("Failed to connect to " + this.getHost() + ": " + ex.getMessage());
				stopwatch.waitFor("Sensor to answer");
			}
		}
		while (stopwatch.hasTime());
		logger.error("Cannot connect to sensor at " + this.getHost());
		throw new TestException("Cannot connect to sensor at " + this.getHost());
	}

	public void prepareEPO()
	{
		try
		{
			logger.info("Preparing ePo");
			execute("/usr/lib/insightix/assetreports/bin/epoPrepare.sh");
		}
		catch (Exception e)
		{
			logger.info("Preparing ePo failed.");
		}
	}

	public String getDBuuidByMac(String mac) throws TestException
	{
		long lmac = MAC.convertConsoleMacToLong(mac);
		String res = "";
		try
		{
			String cmd = buildSQL("select uuid from device_uuid_map_view", "mac=" + lmac, null);
			res = execute(cmd).toString().split("\n")[3];
			if (res.contains("0 rows"))
			{
				throw new TestException(String.format("Can't find device: %s (%s) in console db", mac, lmac));
			}
			else
			{
				return res;
			}
		}
		catch (Exception e)
		{
			throw new TestException(String.format("Can't find device: %s (%s) in console db", mac, lmac));
		}
	}

	private String buildSQL(String select, String where, String conAnd) throws Exception
	{
		if ((where == null || where == "") && conAnd == null)
		{
			return "echo \"" + select + "\" | su postgres -c \"psql mc\"";
		}
		else if (where == null || where == "" && conAnd != null && conAnd != "")
		{
			return "echo \"" + select + " and " + conAnd + "\" | su postgres -c \"psql mc\"";
		}
		else
		{
			return "echo \"" + select + " where " + where + "\" | su postgres -c \"psql mc\"";
		}

	}

	public void backupDB(String backupName)
	{
		try
		{
			logger.info("backupConsoleDB to backupName: " + backupName);
			String date = FileUtils.getDateForFile();
			execute(String.format("/usr/lib/insightix/management/bin/backup.sh " + "/usr/%s_%s", date, backupName));
		}
		catch (Exception e)
		{
			logger.info("backup: " + backupName + " Failed.");
		}
	}

	public void restoreDB(String backupName)
	{
		try
		{
			logger.info("restoreConsoleDB from backupName: " + backupName);
			DebianCommand cmd = new DebianCommand(String.format("/usr/lib/insightix/management/bin/restore.sh " + "/usr/backup/%s", backupName));
			cmd.removePrompt("\\> ");
			execute(cmd);
		}
		catch (Exception e)
		{
			logger.info("restore: " + backupName + " Failed.");
		}
	}

	public DebianResponse getDbCredentials() throws Exception
	{
		String res = execute("cat " + DB_PROPERTIES + " | grep db").toString();
		return new DebianResponse(res, "equal");
	}

	public DebianResponse restartDB() throws Exception
	{
		return execute("/etc/init.d/postgresql restart");
	}

	private boolean isStringInFile(String strToFind, String filePath) throws Exception
	{
		DebianResponse response = null;
		try
		{
			response = execute("echo -n Total: && grep -c " + "\"" + strToFind + "\" " + filePath);
		}
		catch (Exception e)
		{
			return false;
		}

		if (response.toString().indexOf("Total:1") >= 0)

		{
			return true;
		}
		else
		{
			return false;
		}
	}

	private void replaceStringInFile(String strToFind, String strToReplace, String filePath) throws Exception
	{
		execute("sed -i \"s/" + strToFind + "/" + strToReplace + "/\"  " + filePath);
	}

	private DebianResponse addStringToFile(String strToAdd, String filePath) throws Exception
	{
		return execute("sed -i \"" + strToAdd + "\" " + filePath);
	}

	public void prepareDB() throws Exception
	{

		boolean isNeedRestart = false;

		if (isStringInFile("#listen_addresses = 'localhost'", DB_CONF_FILE))
		{
			replaceStringInFile("#listen_addresses = 'localhost'", "listen_addresses = '*'", DB_CONF_FILE);
			isNeedRestart = true;
		}
		else if (isStringInFile("#listen_addresses = '*'", DB_CONF_FILE))
		{
			replaceStringInFile("#listen_addresses", "listen_addresses", DB_CONF_FILE);
			isNeedRestart = true;
		}
		if (!isStringInFile("host\\s*all\\s*all\\s*0.0.0.0/0\\s*md5", DB_PG_HBA_FILE))
		{
			addStringToFile("$ a\\ host    all     all     0.0.0.0/0     md5", DB_PG_HBA_FILE);
			isNeedRestart = true;
		}
		if (isNeedRestart)
		{
			restartDB();
			restartAllProcesses();
		}
	}

}
