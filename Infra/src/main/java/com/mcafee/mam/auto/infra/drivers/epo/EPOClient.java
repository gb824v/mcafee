package com.mcafee.mam.auto.infra.drivers.epo;

import com.mcafee.mam.auto.infra.Stopwatch;
import com.mcafee.mam.auto.infra.TestDriver;
import com.mcafee.mam.auto.infra.TestException;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import org.apache.log4j.Logger;

/**
 * A driver for communicating with EPO. This client uses EPO 'web based' API at
 * https://epo:port/remote ...
 * 
 * @see com.mcafee.orion.remote.client.CommandClient
 * @author Guy
 */
public final class EPOClient extends TestDriver
{
	private static Logger logger = Logger.getLogger(EPOClient.class);
	private static EPOClient instance = null;
	private boolean isConnect = false;
	private int port = 8443;
	private String user = "";
	private String password = "";
	private String host = "";
	private String machineName;
	private EpoRcExtension epoRcExt = null;
	private EpoRcQueriesAndReports epoRcQueriesAndReports = null;
	private EpoRcRsd epoRcRsd = null;
	private EpoRcServer epoRcServer = null;
	private EpoRcSystem epoRcSystem = null;
	private EpoRcAdt epoRcAdt = null;
	private EpoRcPackage epoRcPkg = null;
	private EpoRcSensor epoRcSensor = null;
	private EpoRcPolicy epoRcPolicy = null;
	private EpoRcSettings epoRcSettings = null;

	public static EPOClient getInst() throws TestException
	{
		if (instance == null)
		{
			instance = new EPOClient();
		}
		return instance;
	}

	public static EpoRcExtension getRcExt() throws TestException
	{
		if (getInst().epoRcExt == null)
		{
			getInst().epoRcExt = new EpoRcExtension(instance);
		}
		return getInst().epoRcExt;
	}

	public static EpoRcPackage getRcPackage() throws TestException
	{
		if (getInst().epoRcPkg == null)
		{
			getInst().epoRcPkg = new EpoRcPackage(instance);
		}
		return getInst().epoRcPkg;
	}

	public static EpoRcSensor getRcSensor() throws TestException
	{
		if (getInst().epoRcSensor == null)
		{
			getInst().epoRcSensor = new EpoRcSensor(instance);
		}
		return getInst().epoRcSensor;
	}

	public static EpoRcPolicy getRcPolicy() throws TestException
	{
		if (getInst().epoRcPolicy == null)
		{
			getInst().epoRcPolicy = new EpoRcPolicy(instance);
		}
		return getInst().epoRcPolicy;
	}

	public static EpoRcSettings getRcSettings() throws TestException
	{
		if (getInst().epoRcSettings == null)
		{
			getInst().epoRcSettings = new EpoRcSettings(instance);
		}
		return getInst().epoRcSettings;
	}

	public static EpoRcRsd getRcRsd() throws TestException
	{
		if (getInst().epoRcRsd == null)
		{
			getInst().epoRcRsd = new EpoRcRsd(instance);
		}
		return getInst().epoRcRsd;
	}

	public static EpoRcQueriesAndReports getRcQueriesAndReports() throws TestException
	{
		if (getInst().epoRcQueriesAndReports == null)
		{
			getInst().epoRcQueriesAndReports = new EpoRcQueriesAndReports(instance);
		}
		return getInst().epoRcQueriesAndReports;
	}

	public static EpoRcServer getRcServer() throws TestException
	{
		if (getInst().epoRcServer == null)
		{
			getInst().epoRcServer = new EpoRcServer(instance);
		}
		return getInst().epoRcServer;
	}

	public static EpoRcSystem getRcSystem() throws TestException
	{
		if (getInst().epoRcSystem == null)
		{
			getInst().epoRcSystem = new EpoRcSystem(instance);
		}
		return getInst().epoRcSystem;
	}

	public static EpoRcAdt getRcAdt() throws TestException
	{
		if (getInst().epoRcAdt == null)
		{
			getInst().epoRcAdt = new EpoRcAdt(instance);
		}
		return getInst().epoRcAdt;
	}

	/**
	 * Tests whether epo can be connected to.
	 * 
	 * @return true if epo is accessible.
	 * @throws Exception
	 *             if connection attempt cannot be made.
	 */
	private boolean tryConnect() throws Exception
	{
		Stopwatch stopwatch = this.getStopwatch();
		InetSocketAddress address = new InetSocketAddress(this.getHost(), this.getPort());
		do
		{
			try
			{
				logger.info("Connecting to EPO at " + address.toString().replace("/", ""));
				Socket socket = new Socket();
				socket.connect(address, (int) this.getTimeout());
				socket.close();
				this.isConnect = true;
				return true;
			}
			catch (IOException ex)
			{
				logger.debug("Failed to connect to " + address.toString() + ": " + ex.getMessage());
				stopwatch.waitFor("EPO to answer");
			}
		}
		while (stopwatch.hasTime());
		logger.error("Cannot connect to ePO at " + this.host);
		throw new TestException("Cannot connect to ePO at " + this.host);
	}

	public void init(String host, String user, String password, String ecClientName) throws TestException
	{
		this.host = host;
		this.user = user;
		this.password = password;
		if (!isConnect)
		{
			try
			{
				tryConnect();
			}
			catch (Exception e)
			{
				throw new TestException(e.getMessage());
			}
		}
	}

	/**
	 * * returns a new EPO command for a given command name.
	 * 
	 * @param name
	 *            command name to create
	 * @return EPO command
	 */
	public EPOCommand getCommand(String name)
	{
		EPOCommand command = new EPOCommand(name);
		command.setHost(String.format("%s:%d", this.host, this.port));
		command.setPassword(this.password);
		command.setUser(this.user);
		return command;
	}

	/**
	 * invokes EPO command and returns server response.
	 * 
	 * @param command
	 * @param isSilent
	 * @param isThrowException
	 * @return
	 * @throws IOException
	 */

	protected EPOResponse invoke(EPOCommand command) throws IOException
	{
		EPOResponse response;
		try
		{
			logger.debug("invoking epo command: " + command.toString());
			response = new EPOResponse(command.invoke());
			return response;
		}
		catch (IOException e)
		{
			if (isThrowException()) { throw new IOException(e.getMessage()); }
		}

		return null;
	}

	/**
	 * * show the commands help via the 'core.help' command.
	 * 
	 * @return epo response for command help
	 * @throws IOException
	 * @throws TestException
	 */
	public EPOResponse coreHelp() throws IOException, TestException
	{
		EPOCommand command = getCommand("core.help");
		return EPOClient.instance.invoke(command);
	}

	public String getMcahineName()
	{
		return machineName;
	}

	public void setMcahineName(String mcahineName)
	{
		this.machineName = mcahineName;
	}

	/**
	 * @return the port for EPO connection.
	 */
	public int getPort()
	{
		return port;
	}

	/**
	 * @param port
	 *            the port to set
	 */
	public void setPort(int port)
	{
		this.port = port;
	}

	public String getUser()
	{
		return user;
	}

	public void setUser(String user)
	{
		this.user = user;
	}

	/**
	 * @return the epo admin password
	 */
	public String getPassword()
	{
		return password;
	}

	/**
	 * @param password
	 *            the epo admin password
	 */
	public void setPassword(String password)
	{
		this.password = password;
	}

	/**
	 * @return the epo host
	 */
	public String getHost()
	{
		return host;
	}

	/**
	 * @param host
	 *            the epo host
	 */
	public void setHost(String host)
	{
		this.host = host;
	}
}
