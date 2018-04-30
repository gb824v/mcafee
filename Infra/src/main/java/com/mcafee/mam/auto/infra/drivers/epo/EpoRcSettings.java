package com.mcafee.mam.auto.infra.drivers.epo;

import java.io.InputStream;
import org.apache.log4j.Logger;

public class EpoRcSettings extends EpoRcDecorator
{
	private static Logger logger = Logger.getLogger(EpoRcSettings.class);

	public EpoRcSettings(EPOClient newEpoClient)
	{
		super(newEpoClient);
		// TODO Auto-generated constructor stub
	}

	public boolean modifySensor() throws Exception
	{
		return modifySensor("SensorSettings.txt");
	}

	/**
	 * policyName - Name of the policy template - Template policy to be used (Duplicating policy)
	 * notes - Notes for the new policy clientList - List of client nodes to which the policy to be
	 * enforced
	 * 
	 * @param deviceName
	 * @return
	 * @throws Exception
	 */
	public boolean modifySensor(String fileName) throws Exception
	{
		if (fileName.isEmpty())
		{
			fileName = "SensorSettings.txt";
		}
		InputStream is = getClass().getClassLoader().getResourceAsStream("settings/" + fileName);
		logger.info("modify epo sensor setting.");
		EPOCommand command = epoClient.getCommand("adt.sensorsettings.modify");
		command.addFileParameter("settings", is);
		return epoClient.invoke(command).toString().contains("successfully");
	}

	public boolean modifyCompliance() throws Exception
	{
		return modifyCompliance("ComplianceSettings.txt");
	}

	/**
	 * policyName - Name of the policy template - Template policy to be used (Duplicating policy)
	 * notes - Notes for the new policy clientList - List of client nodes to which the policy to be
	 * enforced
	 * 
	 * @param deviceName
	 * @return
	 * @throws Exception
	 */
	public boolean modifyCompliance(String fileName) throws Exception
	{
		if (fileName.isEmpty())
		{
			fileName = "ComplianceSettings.txt";
		}
		InputStream is = getClass().getClassLoader().getResourceAsStream("settings/" + fileName);
		logger.info("modify epo sensor setting.");
		EPOCommand command = epoClient.getCommand("adt.compliancesettings.modify");
		command.addFileParameter("settings", is);
		return epoClient.invoke(command).toString().contains("successfully");
	}
}
