/*
 * Copyright (C) 2013, McAfee Inc. All Rights Reserved.
 */
package com.mcafee.mam.qaadevtool.command.rsd.policy;

import java.net.URISyntaxException;
import java.sql.Connection;
import java.util.Locale;

import org.apache.log4j.Logger;

import com.mcafee.epo.policy.model.PolicyObject;
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
public class CreatePolicyCommand extends VisibleCommandBase implements Command, UserAware, HelpDisplayer
{
	private static final Logger LOGGER = Logger.getLogger(CreatePolicyCommand.class);

	private static final String DEFAULT_PRODUCT_ID = "RSDMETA";

	// Injected fields
	private PolicyObjectService policyObjectService;
	private Database database;

	private Connection connection;
	// Command Params
	private String name;
	private String template = "McAfee Default";
	private String category = "General";
	private String notes;
	private String clientList;

	@Override
	public void setUser(OrionUser user)
	{
		this.user = user;
	}

	@Override
	protected CommandSpec createSpec()
	{
		CommandSpec spec = new CommandSpec("adt.policy.create.help.short-desc", "adt.policy.create.help.long-desc");
		spec.setName(this.getResource().getString("adt.policy.create.name", Locale.getDefault()));
		spec.setResource(this.getResource());

		return spec;
	}

	@Override
	public boolean authorize(OrionUser user) throws CommandException, URISyntaxException
	{
		this.user = user;
		if (user == null)
		{
			return false;
		}
		return true;
	}

	@Override
	public String getStatusMessage()
	{
		return "Remote Command to create Policy";
	}

	@Override
	public Object invoke() throws Exception
	{
		LOGGER.info("Request to create Policy. Params:" + toString());

		checkForErrors();
		StringBuilder results = new StringBuilder("\n");
		try
		{
			connection = database.getConnection();

			int policyTypeID = DBUtils.getpolicyTypeID(connection, category);
			StaticUtils.throwIfInvalid(policyTypeID, "policyTypeID");

			int policyID = DBUtils.getPolicyID(connection, template, policyTypeID);
			StaticUtils.throwIfInvalid(policyID, "policyID");

			PolicyObject policyObject = policyObjectService.copyPolicyObjectById(connection, user, policyID, name, notes, user.getId(), true);
			connection.commit();
			results.append("Created Policy successfully. PolicyId=" + policyObject.getId());

			if (null != clientList)
			{
				StaticUtils.assignPolicyToNodes(clientList, policyTypeID, policyID, user, DEFAULT_PRODUCT_ID);

				StaticUtils.sendAgentAwakeUp(clientList, user);
			}

		} catch (Exception e)
		{
			LOGGER.error("Error Handling the request", e);
			throw e;
			
		} finally
		{
			connection.commit();
			connection.close();
		}

		return results.toString();

	}
	private void checkForErrors()
	{
		if (null == name)
		{
			throw new RemoteCommandException("Please specify the '*' marked parameters " + toString());
		}
	}
	public void setName(String name)
	{
		this.name = name;
	}

	public void setTemplate(String template)
	{
		this.template = template;
	}

	public void setNotes(String notes)
	{
		this.notes = notes;
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
		return "CreatePolicyCommand [name*=" + name + ", template=" + template + ", category=" + category + ", clientList=" + clientList + ", notes="
				+ notes + "]";
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
