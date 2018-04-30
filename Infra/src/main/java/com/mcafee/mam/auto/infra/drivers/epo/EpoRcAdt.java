package com.mcafee.mam.auto.infra.drivers.epo;

import java.io.IOException;
import org.apache.log4j.Logger;
import com.mcafee.mam.auto.infra.TestException;

public final class EpoRcAdt
{
	protected EPOClient epoClient;
	private static Logger logger = Logger.getLogger(EpoRcAdt.class);

	public EpoRcAdt(EPOClient newEpoCliemt)
	{
		epoClient = newEpoCliemt;
	}

	/**
	 * 
	 * @param nodeName
	 * @return
	 * @throws IOException
	 * @throws TestException
	 */
	public boolean isSensorUp(String nodeName) throws IOException
	{
		logger.info("IsSensorUp for: " + nodeName);
		EPOCommand command = epoClient.getCommand("adt.sensor.sensorStatus");
		command.addStringParameter("nodeName", nodeName);
		return epoClient.invoke(command).toString().contains("true");
	}

	/**
	 * 
	 * @param nodeName
	 * @return
	 * @throws IOException
	 * @throws TestException
	 */
	public boolean wakeupAgent(String nodeName) throws IOException
	{
		logger.info("WakeupAgent to: " + nodeName);
		EPOCommand command = epoClient.getCommand("adt.sensor.wakeupAgent");
		command.addStringParameter("nodeName", nodeName);
		return epoClient.invoke(command).toString().contains("true");
	}
}
