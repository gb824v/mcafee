/*
 * Copyright (C) 2013, McAfee Inc. All Rights Reserved.
 */
package com.mcafee.mam.qaadevtool.command.rsd.response;

import java.net.URISyntaxException;
import java.sql.Connection;
import java.util.Locale;

import org.apache.log4j.Logger;

import com.mcafee.mam.qaadevtool.command.rsd.common.RemoteCommandException;
import com.mcafee.orion.core.auth.OrionUser;
import com.mcafee.orion.core.auth.UserAware;
import com.mcafee.orion.core.cmd.Command;
import com.mcafee.orion.core.cmd.CommandException;
import com.mcafee.orion.core.cmd.CommandSpec;
import com.mcafee.orion.core.cmd.HelpDisplayer;
import com.mcafee.orion.core.cmd.VisibleCommandBase;
import com.mcafee.orion.core.db.base.Database;
import com.mcafee.orion.response.criteria.Rule;
import com.mcafee.orion.response.criteria.service.RuleService;

/**
 * @author Girish
 * 
 */
public class DeleteAutomaticResponseCommand extends VisibleCommandBase implements Command, UserAware, HelpDisplayer
{
	private static final Logger LOGGER = Logger.getLogger(DeleteAutomaticResponseCommand.class);

	// Injected fields
	private RuleService ruleService;
	private Database database;

	private Connection connection;
	// Command Params
	private String names;
	@Override
	public void setUser(OrionUser user)
	{
		this.user = user;
	}

	@Override
	protected CommandSpec createSpec()
	{
		CommandSpec spec = new CommandSpec("adt.response.delete.help.short-desc", "adt.response.delete.help.long-desc");
		spec.setName(this.getResource().getString("adt.response.delete.name", Locale.getDefault()));
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
		return "Remote Command to Create Automatic Response";
	}

	@Override
	public Object invoke() throws Exception
	{
		LOGGER.info("Request to  Create Automatic Response. Params:" + toString());
		checkForErrors();

		StringBuilder results = new StringBuilder("\n");
		try
		{

			connection = database.getConnection();
			StringBuilder ruleIds = new StringBuilder();

			for (String name : names.split(","))
			{
				Rule rule = ruleService.findByName(user, name);

				if (null == rule)
				{
					throw new RemoteCommandException("No rule found for name=" + name);
				}
				ruleService.delete(user, rule);
				ruleIds.append(rule.getId() + ",");
			}

			results.append("Successfully deleted Automatic Response");

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
		if (null == names)
		{
			throw new RemoteCommandException("Please specify the '*' marked parameters " + toString());
		}
	}

	public void setDatabase(Database database)
	{
		this.database = database;
	}

	@Override
	public String toString()
	{
		return "DeleteAutomaticResponseCommand [names*=" + names + "]";
	}

	public void setNames(String names)
	{
		this.names = names;
	}

	public void setRuleService(RuleService ruleService)
	{
		this.ruleService = ruleService;
	}

}
