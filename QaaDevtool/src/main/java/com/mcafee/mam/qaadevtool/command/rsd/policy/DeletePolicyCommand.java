/*
 * Copyright (C) 2013, McAfee Inc. All Rights Reserved.
 */
package com.mcafee.mam.qaadevtool.command.rsd.policy;

import java.net.URISyntaxException;
import java.sql.Connection;
import java.util.Locale;

import org.apache.log4j.Logger;

import com.mcafee.epo.policy.services.PolicyObjectService;
import com.mcafee.mam.qaadevtool.command.rsd.common.DBUtils;
import com.mcafee.mam.qaadevtool.command.rsd.common.RemoteCommandException;
import com.mcafee.mam.qaadevtool.command.rsd.common.StaticUtils;
import com.mcafee.orion.core.auth.OrionUser;
import com.mcafee.orion.core.auth.UserAware;
import com.mcafee.orion.core.cmd.Command;
import com.mcafee.orion.core.cmd.CommandException;
import com.mcafee.orion.core.cmd.CommandSpec;
import com.mcafee.orion.core.cmd.HelpDisplayer;
import com.mcafee.orion.core.cmd.VisibleCommandBase;
import com.mcafee.orion.core.db.base.Database;

/**
 * @author Girish
 * 
 */
public class DeletePolicyCommand extends VisibleCommandBase implements Command, UserAware, HelpDisplayer
{
	private static final Logger LOGGER = Logger.getLogger(DeletePolicyCommand.class);

	// Injected fields
	private PolicyObjectService policyObjectService;
	private Database database;

	private Connection connection;

	// Command Params
	private String name;
	private String category = "General";
	private String clientList;

	@Override
	public void setUser(OrionUser user)
	{
		this.user = user;
	}

	@Override
	protected CommandSpec createSpec()
	{
		CommandSpec spec = new CommandSpec("adt.policy.delete.help.short-desc", "adt.policy.delete.help.long-desc");
		spec.setName(this.getResource().getString("adt.policy.delete.name", Locale.getDefault()));
		spec.setResource(this.getResource());

		return spec;
	}

	@Override
	public boolean authorize(OrionUser user) throws CommandException, URISyntaxException
	{
		this.user = user;
		if (user == null) { return false; }
		return true;
	}

	@Override
	public String getStatusMessage()
	{
		return "Remote Command to delete Policy";
	}

	@Override
	public Object invoke() throws Exception
	{
		LOGGER.info("Request to delete Policy. Params:" + toString());

		checkForErrors();
		StringBuilder results = new StringBuilder("\n");
		try
		{
			connection = database.getConnection();

			int policyTypeID = DBUtils.getpolicyTypeID(connection, category);
			throwIfInvalid(policyTypeID, "policyTypeID");

			int policyID = DBUtils.getPolicyID(connection, name, policyTypeID);

			if (policyID == -1) { throw new RemoteCommandException("No policy found for name=" + name); }

			policyObjectService.deletePolicyObjectById(connection, user, policyID);
			connection.commit();
			results.append("Delete Policy successfully. PolicyId=" + policyID);
			if (null != clientList)
			{
				StaticUtils.sendAgentAwakeUp(clientList, user);
			}
			results.append("deleted Policy successfully. name=" + name);

		}
		catch (Exception e)
		{
			LOGGER.error("Error Handling the request", e);
			throw e;
		}
		finally
		{
			connection.commit();
			connection.close();
		}

		return results.toString();

	}

	private void throwIfInvalid(int value, String fieldName)
	{
		if (value < 1) { throw new RemoteCommandException("Invalid value for fieldName=" + fieldName + " Value=" + value); }

	}

	private void checkForErrors()
	{
		if (null == name) { throw new RemoteCommandException("Please specify the '*' marked parameters " + toString()); }
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public void setDatabase(Database database)
	{
		this.database = database;
	}

	public void setCategory(String category)
	{
		this.category = category;
	}

	@Override
	public String toString()
	{
		return "DeletePolicyCommand [ name*=" + name + ", category=" + category + ", clientList=" + clientList + " ]";
	}

	public void setPolicyObjectService(PolicyObjectService policyObjectService)
	{
		this.policyObjectService = policyObjectService;
	}

	public void setClientList(String clientList)
	{
		this.clientList = clientList;
	}

}
