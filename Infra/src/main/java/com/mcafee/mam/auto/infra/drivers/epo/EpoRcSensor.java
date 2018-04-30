package com.mcafee.mam.auto.infra.drivers.epo;

//import org.apache.log4j.Logger;


import org.apache.log4j.Logger;

import com.mcafee.mam.auto.infra.TestException;

public final class EpoRcSensor extends BuildServerBase
{
	private static Logger logger = Logger.getLogger(EpoRcSensor.class);

	public EpoRcSensor(EPOClient newEpoClient)
	{
		super(newEpoClient);
	}

	/**
	 * @param extFile
	 *            - Extension file to install.
	 * @return command response
	 * @throws Exception
	 * @throws TestException
	 */
	public boolean installSensor(String devName) throws Exception
	{
		logger.info("Installing Sensor on MA device: " + devName);
		EPOCommand command = epoClient.getCommand("adt.sensor.addRemoveSensor");
		command.addStringParameter("CTask", "Install RSD");
		command.addStringParameter("nodeName", devName);
		return epoClient.invoke(command).toString().contains("true");
	}

	/**
	 * @param extFile
	 *            - Extension file to install.
	 * @return command response
	 * @throws Exception
	 * @throws TestException
	 */
	public boolean uninstallSensor(String devName) throws Exception
	{
		logger.info("Unnstalling Sensor on MA device: " + devName);
		EPOCommand command = epoClient.getCommand("adt.sensor.addRemoveSensor");
		command.addStringParameter("CTask", "Remove RSD");
		command.addStringParameter("nodeName", devName);
		return epoClient.invoke(command).toString().contains("true");
	}

	public boolean addToSensorBlacklist(String devName) throws Exception
	{
		logger.info("addToSensorBlacklist on MA device: " + devName);
		EPOCommand command = epoClient.getCommand("system.addToSensorBlacklist");
		command.addStringParameter("names", devName);
		return epoClient.invoke(command).toString().contains("true");
	}

//	public boolean removeSensorFromBlacklist(String devName) throws Exception
//	{
//		EPOCommand command = epoClient.getCommand("system.addToSensorBlacklist");
//		command.addStringParameter("names", devName);
//		return epoClient.invoke(command).toString().contains("true");
//	}
}
