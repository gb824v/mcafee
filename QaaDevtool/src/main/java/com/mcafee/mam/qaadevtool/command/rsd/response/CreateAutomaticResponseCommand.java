/*
 * Copyright (C) 2013, McAfee Inc. All Rights Reserved.
 */
package com.mcafee.mam.qaadevtool.command.rsd.response;

import java.net.URISyntaxException;
import java.sql.Connection;
import java.util.List;
import java.util.Locale;

import org.apache.log4j.Logger;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import com.mcafee.mam.qaadevtool.command.rsd.common.RemoteCommandException;
import com.mcafee.orion.core.auth.OrionUser;
import com.mcafee.orion.core.auth.UserAware;
import com.mcafee.orion.core.cmd.Command;
import com.mcafee.orion.core.cmd.CommandException;
import com.mcafee.orion.core.cmd.CommandSpec;
import com.mcafee.orion.core.cmd.HelpDisplayer;
import com.mcafee.orion.core.cmd.VisibleCommandBase;
import com.mcafee.orion.core.db.base.Database;
import com.mcafee.orion.core.util.OrionUploadFileItem;
import com.mcafee.orion.response.criteria.Rule;
import com.mcafee.orion.response.mvc.ImportResponseAction;

/**
 * @author Girish
 * 
 */
public class CreateAutomaticResponseCommand extends VisibleCommandBase implements Command, UserAware, HelpDisplayer
{
	private static final Logger LOGGER = Logger.getLogger(CreateAutomaticResponseCommand.class);

	// Injected fields
	private ImportResponseAction importResponseAction;
	private Database database;

	private Connection connection;
	// Command Params
	private OrionUploadFileItem settings;

	@Override
	public void setUser(OrionUser user)
	{
		this.user = user;
	}

	@Override
	protected CommandSpec createSpec()
	{
		CommandSpec spec = new CommandSpec("adt.response.create.help.short-desc", "adt.response.create.help.long-desc");
		spec.setName(this.getResource().getString("adt.response.create.name", Locale.getDefault()));
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
		return "Remote Command to Create Automatic Response for Deploying MA";
	}

	@Override
	public Object invoke() throws Exception
	{
		LOGGER.info("Request to  Create Automatic Response for Deploying MA. Params:" + toString());
		checkForErrors();
		
		StringBuilder results = new StringBuilder("\n");
		try
		{

			connection = database.getConnection();
			MockHttpServletRequest request = new MockHttpServletRequest();
			MockHttpServletResponse response = new MockHttpServletResponse();
			request.setUserPrincipal(user);
			//importResponseAction.setUser(user);
			importResponseAction.setFile(settings);
			importResponseAction.confirmImportResponse(request, response);

			if (null != request.getAttribute("friendlyErrorMessage"))
			{
				throw new RemoteCommandException((String) request.getAttribute("friendlyErrorMessage"));

			}

			setRequestParams(request);
			importResponseAction.importResponse(request, response);
			results.append("Successfully created Automatic Response for Deploying MA");

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
		if (null == settings)
		{
			throw new RemoteCommandException("Please specify the '*' marked parameters " + toString());
		}
	}
	private void setRequestParams(MockHttpServletRequest request)
	{
		@SuppressWarnings("unchecked")
		List<Rule> rules = (List<Rule>) request.getAttribute("rules");
		request.setParameter("ruleCount", rules.size() + "");
		for (int i = 1; i <= rules.size(); i++)
		{
			request.setParameter("enableRule_" + i, "true");
		}

	}
	public void setSettings(OrionUploadFileItem fileItem)
	{
		this.settings = fileItem;
	}

	public void setDatabase(Database database)
	{
		this.database = database;
	}

	public void setImportResponseAction(ImportResponseAction importResponseAction)
	{
		this.importResponseAction = importResponseAction;
	}

	@Override
	public String toString()
	{
		return "CreateAutomaticResponseCommand [settings*=" + settings + "]";
	}

}
