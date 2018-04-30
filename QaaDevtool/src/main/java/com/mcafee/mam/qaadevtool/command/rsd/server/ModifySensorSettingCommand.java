/*
 * Copyright (C) 2013, McAfee Inc. All Rights Reserved.
 */
package com.mcafee.mam.qaadevtool.command.rsd.server;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.util.Locale;

import org.apache.log4j.Logger;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import com.google.gson.Gson;
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
import com.mcafee.rsd.action.setting.SensorSettings;

/**
 * @author Girish
 * 
 */
public class ModifySensorSettingCommand extends VisibleCommandBase implements Command, UserAware, HelpDisplayer
{
	private static final Logger LOGGER = Logger.getLogger(ModifySensorSettingCommand.class);

	// Injected fields
	private SensorSettings sensorSettings;
	private Database database;

	private Connection connection;
	// Command Params
	private String settings;

	@Override
	public void setUser(OrionUser user)
	{
		this.user = user;
	}

	@Override
	protected CommandSpec createSpec()
	{
		CommandSpec spec = new CommandSpec("adt.sensorsettings.modify.help.short-desc", "adt.sensorsettings.modify.help.long-desc");
		spec.setName(this.getResource().getString("adt.sensorsettings.modify.name", Locale.getDefault()));
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
		return "Remote Command to Change Sensor Settings";
	}

	@Override
	public Object invoke() throws Exception
	{
		LOGGER.info("Request to Change Sensor Settings. Params:" + toString());
		checkForErrors();
		StringBuilder results = new StringBuilder("\n");
		try
		{
			connection = database.getConnection();
			MockHttpServletRequest request = new MockHttpServletRequest();
			MockHttpServletResponse response = new MockHttpServletResponse();
			request.setUserPrincipal(user);

			setRequestParameters(request);
			sensorSettings.setConnection(connection);
			sensorSettings.save(request, response);

			results.append("Sensor Settings changed successfully");

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

	private void setRequestParameters(MockHttpServletRequest request)
	{
		Gson gson = new Gson();
		com.mcafee.mam.qaadevtool.command.rsd.server.SensorSettings sensorSettings = gson.fromJson(settings,
				com.mcafee.mam.qaadevtool.command.rsd.server.SensorSettings.class);

		sensorSettings.checkForErrors();
		request.setParameter("sensorTimeout", sensorSettings.getSensorTimeout() + "");
		request.setParameter("sensorsPerSubnet", sensorSettings.getSensorsPerSubnet() + "");
		request.setParameter("maxActive", sensorSettings.getMaxActive() + "");
		request.setParameter("maxTimePeriod", sensorSettings.getMaxTimePeriod() + "");

		request.setParameter("timeoutUnits", sensorSettings.getTimeoutUnits());
		request.setParameter("periodUnits", sensorSettings.getPeriodUnits());
		request.setParameter("noscanText", sensorSettings.getNoscanText());
		request.setParameter("ipnoscanText", sensorSettings.getIpnoscanText());

	}

	public void setSettings(Object obj)
	{
		try
		{
			if (obj instanceof String)
			{
				File file = new File((String) obj);

				if (file.exists())
				{
					BufferedReader reader = new BufferedReader(new FileReader(file));
					String line = reader.readLine();

					StringBuilder builder = new StringBuilder("");
					while (line != null)
					{
						builder.append(line);
						line = reader.readLine();
					}
					this.settings = builder.toString();
					reader.close();

				} else
				{ // Here we assume that the requestObj is the JSON string.
					this.settings = (String) obj;
				}
			} else if (obj instanceof OrionUploadFileItem)
			{
				this.settings = new String(((OrionUploadFileItem) obj).get());
			}
		} catch (Exception e)
		{
			LOGGER.error("Error Initializing the Parameters", e);

			throw new RemoteCommandException("Error Initializing the Parameters", e);
		}
	}

	public void setSensorSettings(SensorSettings sensorSettings)
	{
		this.sensorSettings = sensorSettings;
	}

	public void setDatabase(Database database)
	{
		this.database = database;
	}

	@Override
	public String toString()
	{
		return "ModifySensorSettingCommand [settings*=" + settings + "]";
	}

}
