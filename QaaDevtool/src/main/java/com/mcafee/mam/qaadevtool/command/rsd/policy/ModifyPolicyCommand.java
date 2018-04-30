/*
 * Copyright (C) 2013, McAfee Inc. All Rights Reserved.
 */
package com.mcafee.mam.qaadevtool.command.rsd.policy;

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
import com.mcafee.mam.qaadevtool.command.rsd.common.DBUtils;
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
import com.mcafee.rsd.action.policy.Policy;

/**
 * @author Girish
 * 
 */
public class ModifyPolicyCommand extends VisibleCommandBase implements Command, UserAware, HelpDisplayer
{
	private static final Logger LOGGER = Logger.getLogger(ModifyPolicyCommand.class);

	// Injected fields
	private Database database;
	private Policy policy;

	private Connection connection;
	// Command Params
	private String name;
	private String category = "General";
	private String settings;

	@Override
	public void setUser(OrionUser user)
	{
		this.user = user;
	}

	@Override
	protected CommandSpec createSpec()
	{
		CommandSpec spec = new CommandSpec("adt.policy.modify.help.short-desc", "adt.policy.modify.help.long-desc");
		spec.setName(this.getResource().getString("adt.policy.modify.name", Locale.getDefault()));
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
		return "Remote Command to Modify Policy settings";
	}

	@Override
	public Object invoke() throws Exception
	{
		LOGGER.info("Request to Modify Policy settings. Params:" + toString());

		checkForErrors();
		StringBuilder results = new StringBuilder("\n");
		try
		{
			connection = database.getConnection();

			int policyTypeID = DBUtils.getpolicyTypeID(connection, category);
			throwIfInvalid(policyTypeID, "policyTypeID");

			int policySetingID = DBUtils.getPolicySettingID(connection, name, policyTypeID);
			throwIfInvalid(policySetingID, "policySetingID");

			user.setAttribute("psoid", policySetingID);
			user.setAttribute("epoReadOnlyPolicyObject", "false");

			MockHttpServletRequest request = new MockHttpServletRequest();
			MockHttpServletResponse response = new MockHttpServletResponse();
			request.setUserPrincipal(user);

			setRequestParameters(request);

			policy.savePolicy(request, response);
			connection.commit();

			// TODO can add system wakeup
			results.append("Modify Policy settings done successfully");

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

	private void setRequestParameters(MockHttpServletRequest request)
	{
		Gson gson = new Gson();
		PolicySettings policySettings = gson.fromJson(settings, PolicySettings.class);
		policySettings.checkForErrors();
		request.setParameter("EnableSensor", policySettings.isEnableSensor() ? "sensorEnabled" : "sensorDisabled");
		request.setParameter("EnableDebug", policySettings.isEnableDebug() ? "debugEnabled" : "debugDisabled");
		request.setParameter("UseDefaultIncludedSubnets", policySettings.isUseDefaultIncludedSubnets() + "");
		request.setParameter("EnableDHCP", policySettings.isEnableDHCP() ? "dhcpEnabled" : "dhcpDisabled");
		request.setParameter("RestrictReports", policySettings.isRestrictReports() + "");
		request.setParameter("SubnetScanEnabled", policySettings.isSubnetScanEnabled() + "");
		request.setParameter("OSFingerprinting", policySettings.isoSFingerprinting() + "");
		request.setParameter("scanExceptions", policySettings.isScanExceptions() + "");
		request.setParameter("RSCEnabled", policySettings.isrSCEnabled() + "");
		request.setParameter("mDNSEnabled", policySettings.ismDNSEnabled() + "");
		request.setParameter("DNSEnabled", policySettings.isdNSEnabled() + "");
		request.setParameter("ifaceOption", policySettings.getIfaceOption() == "include" ? "includeRadio" : "excludeRadio");
		request.setParameter("HiddenIncludeList", policySettings.getHiddenIncludeList());
		request.setParameter("HiddenExcludeList", policySettings.getHiddenExcludeList());
		request.setParameter("scanOption", policySettings.getScanOption());
		request.setParameter("timepickerNumbox_OSFingerprintingInterval", policySettings.getOSFingerprintingInterval() + "");
		request.setParameter("timepickerSelector_OSFingerprintingInterval", policySettings.getOSFingerprintingIntervalSelector());
		request.setParameter("timepickerNumbox_OSFingerprintingDelay", policySettings.getoSFingerprintingDelay() + "");
		request.setParameter("timepickerSelector_OSFingerprintingDelay", policySettings.getoSFingerprintingDelaySelector());
		request.setParameter("timepickerNumbox_PlatformCacheLife", policySettings.getPlatformCacheLife() + "");
		request.setParameter("timepickerSelector_PlatformCacheLife", policySettings.getPlatformCacheLifeSelector());
		request.setParameter("ServerName", policySettings.getServerName());
		request.setParameter("timepickerNumbox_FailoverSleepTime", policySettings.getFailoverSleepTime() + "");
		request.setParameter("timepickerSelector_FailoverSleepTime", policySettings.getFailoverSleepTimeSelector());
		request.setParameter("timepickerNumbox_HostFilterTimeout", policySettings.getHostFilterTimeout() + "");
		request.setParameter("timepickerSelector_HostFilterTimeout", policySettings.getHostFilterTimeoutSelector());
		request.setParameter("timepickerNumbox_SensorThrottle", policySettings.getSensorThrottle() + "");
		request.setParameter("timepickerSelector_SensorThrottle", policySettings.getSensorThrottleSelector());
		request.setParameter("HiddenWhitelist", policySettings.getHiddenWhitelist());
		request.setParameter("HiddenBlacklist", policySettings.getHiddenBlacklist());
		request.setParameter("ElectMethod", policySettings.getElectMethod());
		request.setParameter("timepickerNumbox_electResultWaitTime", policySettings.getElectResultWaitTime() + "");
		request.setParameter("timepickerSelector_electResultWaitTime", policySettings.getElectResultWaitTimeSelector());
		request.setParameter("timepickerNumbox_electionInterval", policySettings.getElectionInterval() + "");
		request.setParameter("timepickerSelector_electionInterval", policySettings.getElectionIntervalSelector());
		request.setParameter("NumActiveSensorsRadio", policySettings.getNumActiveSensorsRadio());
		request.setParameter("numActiveSensors", policySettings.getNumActiveSensors() + "");
		request.setParameter("ipv4Multicast", policySettings.getIpv4Multicast());
		request.setParameter("ipv6Multicast", policySettings.getIpv6Multicast());
		request.setParameter("sensorPort", policySettings.getSensorPort() + "");

	}

	private void throwIfInvalid(int value, String fieldName)
	{
		if (value < 1) { throw new RemoteCommandException("Invalid value for fieldName=" + fieldName + " Value=" + value); }

	}

	private void checkForErrors()
	{
		if (null == name || null == settings) { throw new RemoteCommandException("Please specify the '*' marked parameters " + toString()); }
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

				}
				else
				{ // Here we assume that the requestObj is the JSON string.
					this.settings = (String) obj;
				}
			}
			else if (obj instanceof OrionUploadFileItem)
			{
				this.settings = new String(((OrionUploadFileItem) obj).get());
			}
		}
		catch (Exception e)
		{
			LOGGER.error("Error Initializing the Parameters", e);

			throw new RemoteCommandException("Error Initializing the Parameters", e);
		}
	}

	@Override
	public String toString()
	{
		return "ModifyPolicySettingsCommand [name*=" + name + ", category=" + category + ", settings*=" + settings + "]";
	}

	public void setPolicy(Policy policy)
	{
		this.policy = policy;
	}

}
