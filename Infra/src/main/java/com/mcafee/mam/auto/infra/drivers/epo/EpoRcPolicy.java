package com.mcafee.mam.auto.infra.drivers.epo;

import java.io.IOException;
import java.io.InputStream;
import org.apache.log4j.Logger;

public class EpoRcPolicy extends EpoRcDecorator
{
	private static Logger logger = Logger.getLogger(EpoRcPolicy.class);

	public EpoRcPolicy(EPOClient newEpoClient)
	{
		super(newEpoClient);
		// TODO Auto-generated constructor stub
	}

	/**
	 * policyName - Name of the policy template - Template policy to be used (Duplicating policy)
	 * 
	 * @param deviceName
	 * @return
	 * @throws IOException
	 */

	public boolean createPolicy(String policyName, String template) throws IOException
	{
		return createPolicy(policyName, template, "");
	}

	/**
	 * policyName - Name of the policy template - Template policy to be used (Duplicating policy)
	 * notes - Notes for the new policy clientList - List of client nodes to which the policy to be
	 * enforced
	 * 
	 * @param deviceName
	 * @return
	 * @throws IOException
	 */
	private boolean createPolicy(String policyName, String template, String notes) throws IOException
	{
		logger.info("create policy: " + policyName);
		EPOCommand command = epoClient.getCommand("adt.policy.create");
		command.addStringParameter("name", policyName);
		command.addStringParameter("template", template);
		command.addStringParameter("notes", notes);
		command.addStringParameter("clientList", "");
		return epoClient.invoke(command).toString().contains("successfully");
	}

	/**
	 * 
	 * @param policyName
	 * @param template
	 * @param notes
	 * @param deviceName
	 * @return
	 * @throws IOException
	 */
	public boolean createPolicyAndAssign(String policyName, String template, String notes, String deviceName) throws IOException
	{
		logger.info("create policy and add it to device: " + deviceName);
		EPOCommand command = epoClient.getCommand("adt.policy.create");
		command.addStringParameter("name", policyName);
		command.addStringParameter("template", template);
		command.addStringParameter("notes", "");
		command.addStringParameter("clientList", deviceName);
		return epoClient.invoke(command).toString().contains("successfully");
	}

	/**
	 * policyName - Name of the policy template - Template policy to be used (Duplicating policy)
	 * notes - Notes for the new policy devices - List of client nodes to which the policy to be
	 * separated by , enforced
	 * 
	 * @param deviceName
	 * @return
	 * @throws IOException
	 */
	public boolean createPolicyAndAssignList(String policyName, String template, String notes, String devices) throws IOException
	{
		logger.info("create policy and add it to devices: " + devices);
		EPOCommand command = epoClient.getCommand("adt.policy.create");
		command.addStringParameter("name", policyName);
		command.addStringParameter("template", template);
		command.addStringParameter("notes", "");
		command.addStringParameter("clientList", devices);
		return epoClient.invoke(command).toString().contains("successfully");
	}

	/**
	 * 
	 * @param policyName
	 * @return
	 * @throws IOException
	 */
	public boolean deletePolicy(String policyName) throws IOException
	{
		logger.info("delete policy: " + policyName);
		EPOCommand command = epoClient.getCommand("adt.policy.delete");
		command.addStringParameter("name", policyName);
		return epoClient.invoke(command).toString().contains("successfully");
	}

	/**
	 * policyName - Name of the policy template - Template policy to be used (Duplicating policy)
	 * notes - Notes for the new policy clientList - List of client nodes to which the policy to be
	 * enforced
	 * 
	 * @param deviceName
	 * @return
	 * @throws IOException
	 */
	public boolean assignPolicy(String policyName, String devices) throws IOException
	{
		logger.info("create policy: " + policyName);
		EPOCommand command = epoClient.getCommand("adt.policy.assign");
		command.addStringParameter("name", policyName);
		command.addStringParameter("clientList", devices);
		return epoClient.invoke(command).toString().contains("successfully");
	}

	public boolean modifyPolicy(String policyName) throws Exception
	{
		return modifyPolicy(policyName, "PolicySettings.txt");
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
	public boolean modifyPolicy(String policyName, String fileName) throws Exception
	{
		if (fileName.isEmpty())
		{
			fileName = "PolicySettings.txt";
		}
		InputStream is = getClass().getClassLoader().getResourceAsStream("settings/" + fileName);
		logger.info("modify policy: " + policyName);
		EPOCommand command = epoClient.getCommand("adt.policy.modify");
		command.addStringParameter("name", policyName);
		command.addFileParameter("settings", is);
		return epoClient.invoke(command).toString().contains("successfully");
	}
}
