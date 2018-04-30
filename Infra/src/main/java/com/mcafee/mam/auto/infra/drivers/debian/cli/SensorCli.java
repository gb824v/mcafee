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

/**
 * A driver for communicating with MAM sensor.
 * 
 * @author guy
 */
public class SensorCli extends DebianInfra
{

	private static Logger logger = Logger.getLogger(SensorCli.class);
	private ProcessStatus processes = new ProcessStatus();

	/**
	 * * performs update for all packages
	 * 
	 * @throws Exception
	 */
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
	public void update() throws Exception
	{
		execute("DEBIAN_FRONTEND=noninteractive aptitude update --quiet");
		DebianCommand upgradeCommand = new DebianCommand("DEBIAN_FRONTEND=noninteractive aptitude dist-upgrade --quiet");
		upgradeCommand.addPrompt(new Prompt("Do you want to continue\\? \\[Y/n/\\?\\]", "Y\n"));
		upgradeCommand.addPrompt(new Prompt("(Configure Interfaces)(.|\\n)*(<OK>)(.*)(<Cancel>)", "\n"));

		execute(upgradeCommand);
		DebianCommand installCommand = new DebianCommand("DEBIAN_FRONTEND=noninteractive aptitude install mam-standalone-sensor-task --quiet");
		upgradeCommand.addPrompt(new Prompt("Do you want to continue\\? \\[Y/n/\\?\\]", "Y\n"));
		execute(installCommand);
	}

	/**
	 * log package versions for all Sensor packages.
	 * 
	 * @throws Exception
	 */
	public void logSensorVersion() throws Exception
	{
		logPackageVersion("snmper");
		logPackageVersion("indise");
		logPackageVersion("everest");
		logPackageVersion("system_mam.mam");
	}

	/**
	 * log package versions for all MAM packages.
	 * 
	 * @throws Exception
	 */
	public String logCollectorVisibilityVersion() throws Exception
	{
		DebianResponse response = execute("dpkg --list | grep collector-visibility");
		return response.toString().split("//s")[2];
	}

	/**
	 * write to log package version
	 * 
	 * @param packageName
	 *            - package name to look for.
	 * @throws Exception
	 *             - if execution fails
	 */
	private void logPackageVersion(String packageName) throws Exception
	{
		DebianResponse response = execute("dpkg --list | grep " + packageName);
		logger.info(packageName + " Version " + response.toString());
	}

	/**
	 * 
	 * @throws Exception
	 */
	public void stopAllProcess() throws Exception
	{
		execute("monit stop all");
		this.processes.setStatus(showMonitSummary().toString());
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
	
	
	public DebianResponse restoreConf(String fileName)
	{
		try
		{
			logger.info("restore sensor configuration");
			return execute("/usr/lib/insightix/collector/scripts/restoreConfiguration.sh /tmp/" + fileName);
		}
		catch (Exception e)
		{
			logger.info("restore sensor configuration failed.");
			return null;
		}
		
	}
	
	public DebianResponse backupConf()
	{
		try
		{
			logger.info("backup sensor configuration");
			return execute("/usr/lib/insightix/collector/scripts/backupConfiguration.sh");
		}
		catch (Exception e)
		{
			logger.info("backup sensor configuration failed.");
			return null;
		}
		
	}
	
	public void copyFileToServer(String fileName,String server,String user, String password)
	{
		try
		{
			logger.info("coping " + fileName +" to another server");
			DebianCommand upgradeCommand = new DebianCommand(String.format("scp /var/backups/%s %s@%s:/tmp/",fileName,user,server));
			upgradeCommand.addPrompt(new Prompt(String.format("%s@%s's password:",user,server), password + "\n"));
			execute(upgradeCommand);
		}
		catch (Exception e)
		{
			logger.error("failed to copy file " + e.toString());
			
		}
		
	}
	
	public DebianResponse openTelnet(String ip ,String port) throws Exception
	{
		DebianCommand upgradeCommand = new DebianCommand(String.format("telnet %s %s ", ip, port));
		upgradeCommand.addPrompt(new Prompt("\n \n \n \n quit"));
		return execute(upgradeCommand);
	}
	
	public DebianResponse openSSH(String ip, String user, String password) throws Exception
	{
		DebianCommand upgradeCommand = new DebianCommand(String.format("ssh %s@%s ",user, ip));
		upgradeCommand.addPrompt(new Prompt(String.format("%s@%s's password:",user,ip), password + "\n"));
		return execute(upgradeCommand);
	}

}
