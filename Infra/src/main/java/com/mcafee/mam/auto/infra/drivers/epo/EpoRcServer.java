package com.mcafee.mam.auto.infra.drivers.epo;

import java.io.IOException;
import java.net.URISyntaxException;
import org.apache.log4j.Logger;
import com.mcafee.mam.auto.infra.Stopwatch;
import com.mcafee.mam.auto.infra.TestException;

public final class EpoRcServer extends EpoRcDecorator
{
	private static Logger logger = Logger.getLogger(EpoRcServer.class);

	public EpoRcServer(EPOClient newEpoClient)
	{
		super(newEpoClient);
		// TODO Auto-generated constructor stub
	}

	/**
	 * 
	 * @param name
	 * @param dbIP
	 * @param password
	 * @param port
	 * @return
	 * @throws IOException
	 * @throws TestException
	 */
	public EPOResponse registerServer(String serverName, String dbIP, String user, String password, String port, String dbName) throws IOException
	{
		return registerServer(serverName, dbIP, user, password, port, dbName, 0);
	}

	/**
	 * 
	 * @param name
	 * @param dbIP
	 * @param password
	 * @param port
	 * @return
	 * @throws IOException
	 * @throws TestException
	 */
	public EPOResponse registerServer(String serverName, String dbIP, String user, String password, String port, String dbName, int maxHours) throws IOException
	{
		logger.info("mis.addRegisteredServer: " + serverName);
		EPOCommand command = epoClient.getCommand("mis.addRegisteredServer");
		command.addStringParameter("name", serverName);
		command.addStringParameter("userName", user);
		command.addStringParameter("password", password);
		command.addStringParameter("serverAddress", dbIP);
		command.addStringParameter("port", port);
		command.addStringParameter("dataBaseName", dbName);
		command.addStringParameter("maxHours", Integer.toString(maxHours));
		command.addStringParameter("desc", "MAM_CONSOLE_6.6");
		command.addStringParameter("type", "MAMConsoleEPOConnector");
		command.addStringParameter("lastUpdate", "0");
		EPOResponse response = epoClient.invoke(command);
		return response;
	}

	/**
	 * 
	 * @return
	 * @throws IOException
	 * @throws TestException
	 */
	public EPOResponse listServerTasks() throws IOException
	{
		EPOCommand command = epoClient.getCommand("scheduler.listAllServerTasks");
		return epoClient.invoke(command);
	}

	/**
	 * 
	 * @param stfFile
	 * @return
	 * @throws IOException
	 * @throws TestException
	 * @throws URISyntaxException
	 */
	public EPOResponse createServerTask(String taskName, String serverName) throws IOException, URISyntaxException
	{

		StringBuffer cmd = new StringBuffer("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		cmd.append("<list id=\"1\"><scheduled-task id=\"2\">");
		cmd.append("<name>" + taskName + "</name><type>NORMAL</type><start-date id=\"3\">2013-05-28 07:00:01.0 UTC</start-date>");
		cmd.append("<schedule>period:yearly?h=1&amp;m=0&amp;f=1&amp;mo=JANUARY</schedule>");
		cmd.append("<command-chain id=\"4\"><command-name>scheduler.noop</command-name><commmand-params id=\"5\"/><children id=\"6\">");
		cmd.append("<command-chain id=\"7\"><command-name>MAMConsoleEPOConnector.Detectedsystem.Command</command-name>");
		cmd.append("<commmand-params id=\"8\"><entry><string>serverName</string><string>" + serverName + "</string></entry></commmand-params>");
		cmd.append("<children id=\"9\"/></command-chain></children></command-chain></scheduled-task></list>");
		EPOCommand command = epoClient.getCommand("task.importTasks");
		command.addFileParameter("file", cmd);
		return epoClient.invoke(command);
	}

	/**
	 * run ServerTask
	 * 
	 * @param teaskName
	 * @return
	 * @throws Exception
	 */
	public void runTask(String taskName) throws Exception,TestException
	{
		EPOCommand command = epoClient.getCommand("scheduler.runServerTask");
		command.addStringParameter("taskName", taskName);
		Stopwatch stopwatch = epoClient.getStopwatch();
		do
		{
			try
			{
				logger.info("Running EPO Server Task...");
				EPOResponse response = epoClient.invoke(command);
				Integer.parseInt(response.toString());
				Thread.sleep(2000);
				return;
			}
			catch (Exception ex)
			{
				stopwatch.waitFor("EPO Task to answer");
			}
		}
		while (stopwatch.hasTime());
		logger.error("Cannot run EPO Server Task");
		throw new TestException("Cannot run EPO Server Task");
	}
}
