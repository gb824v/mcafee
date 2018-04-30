/*
 * Copyright (C) 2013, McAfee Inc. All Rights Reserved.
 */
package com.mcafee.mam.qaadevtool.plugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.mcafee.orion.core.OrionCore;
import com.mcafee.orion.core.plugin.DefaultPlugin;
import com.mcafee.orion.core.plugin.Plugin;
import com.mcafee.orion.core.plugin.PluginManager;
import com.mcafee.orion.core.plugin.WebappPlugin;

/**
 * @author Girish [gdeshpan]
 * @since 5.1
 */
public class QaaPlugin extends DefaultPlugin
{
	private static final Logger m_log = Logger.getLogger(QaaPlugin.class);

	private Map<String, String[]> m_requiredPackagesMap = new HashMap<String, String[]>();
	private boolean m_initialized = false; // to avoid reloading of packages
											// multiple times
	private PluginManager m_pluginManager = null;

	/**
	 * 
	 * @param dependencies
	 *            as array of Plugin
	 * @throws Exception
	 *             as Java exception
	 */
	@Override
	public void dependenciesInitialized(Plugin[] dependencies) throws Exception
	{
		super.dependenciesInitialized(dependencies);
		if (!(dependencies instanceof Plugin[]))
		{
			m_log.error("dependencies is not an instance of Plugin[], exiting");
			return;
		}
		if (m_initialized)
		{
			m_log.info("=====================================================");
			m_log.info("Plugin has bean already initialized, exiting");
			m_log.info("=====================================================");
			return;
		}

		m_pluginManager = OrionCore.getPluginManager();
		if (m_pluginManager == null)
		{
			m_log.warn("=====================================================");
			m_log.warn("PluginManager is null, exiting");
			m_log.warn("=====================================================");
			return;
		}

		for (String tmpPluginName : m_requiredPackagesMap.keySet())
		{
			augmentSharedPackages(tmpPluginName);
		}

		m_initialized = true;
	}

	/**
	 * 
	 * @param pluginName
	 *            as PluginName
	 */
	private void augmentSharedPackages(String pluginName)
	{
		List<String> allPluginPackageList = new ArrayList<String>();

		Plugin plugin = m_pluginManager.getPlugin(pluginName);
		if (plugin != null && plugin instanceof WebappPlugin)
		{
			WebappPlugin webPlugin = (WebappPlugin) plugin;

			// already shared packages
			String[] existingSharedPackages = webPlugin.getSharedPackages();
			List<String> existingSharedPackagesAsList = Arrays
					.asList(existingSharedPackages);
			allPluginPackageList.addAll(existingSharedPackagesAsList);

			// additional required (extra) packages
			String[] pluginRequiredPackages = m_requiredPackagesMap
					.get(pluginName);
			for (String tmpExtraPackage : pluginRequiredPackages)
			{
				if (!allPluginPackageList.contains(tmpExtraPackage))
				{
					allPluginPackageList.add(tmpExtraPackage);
				}
			}
			String[] allPluginPackages = (String[]) allPluginPackageList
					.toArray(new String[allPluginPackageList.size()]);

			m_log.info("Plugin= " + pluginName
					+ "; Reloading all shared and required packages = "
					+ allPluginPackageList);

			// this sets updated packages in each web plugin
			webPlugin.setName(pluginName);
			webPlugin.setSharedPackages(allPluginPackages);
			// this is actually loads packages
			webPlugin.getClassloader().setSharedPackages(allPluginPackages);
		}
	}

	/**
	 * Excerpt example: plugin.xml <property name="requiredPackages"> <list>
	 * <value>issue:com.mcafee.test.issue.,com.mcafee.test2.issue.</value>
	 * <value>help:com.mcafee.test.help.,com.test.help.,com.test2.help.</value>
	 * </list> </property>
	 * 
	 * @param requiredPackages
	 *            as a list of required packages from plugin.xml
	 */
	public void setRequiredPackages(List<String> requiredPackages)
	{
		for (String fullPackage : requiredPackages)
		{
			String parts[] = StringUtils.split(fullPackage, ":");
			m_requiredPackagesMap.put((parts[0]).trim(),
					StringUtils.split(parts[1], ","));
		}
	}

}