/*
 * Copyright (C) 2013, McAfee Inc. All Rights Reserved.
 */
package com.mcafee.mam.qaadevtool.command.rsd.server;

import com.google.gson.Gson;
import com.mcafee.mam.qaadevtool.command.rsd.common.RemoteCommandException;
import com.mcafee.mam.qaadevtool.command.rsd.common.StaticUtils;
import com.mcafee.rsd.settings.RSDConfiguration;

/**
 * @author Girish
 * 
 */
public class ComplianceSettings
{
	private int lastAgentCommunication = RSDConfiguration.DEFAULT_LAST_AGENT_COMMUNICATION;
	private int lastSensorDetection = RSDConfiguration.DEFAULT_LAST_SENSOR_DETECTION;

	private int subnetGood = RSDConfiguration.DEFAULT_SUBNET_GOOD;
	private int subnetOk = RSDConfiguration.DEFAULT_SENSOR_OK;

	private int systemGood = RSDConfiguration.DEFAULT_SYSTEM_GOOD;
	private int systemOk = RSDConfiguration.DEFAULT_SYSTEM_OK;

	private int sensorGood = RSDConfiguration.DEFAULT_SENSOR_GOOD;
	private int sensorOk = RSDConfiguration.DEFAULT_SENSOR_OK;

	private String alternateEpoServerNames = "";

	public void checkForErrors()
	{
		StaticUtils.throwIfInvalid(subnetGood, "subnetGood");
		StaticUtils.throwIfInvalid(subnetOk, "subnetOk");
		StaticUtils.throwIfInvalid(systemGood, "systemGood");
		StaticUtils.throwIfInvalid(systemOk, "systemOk");
		StaticUtils.throwIfInvalid(sensorGood, "sensorGood");
		StaticUtils.throwIfInvalid(sensorOk, "sensorOk");

		if (!(subnetGood > subnetOk))
		{
			throw new RemoteCommandException("Subnet green minimum must be larger than orange minimum");
		}
		if (!(systemGood > systemOk))
		{
			throw new RemoteCommandException("System green minimum must be larger than orange minimum");
		}
		if (!(sensorGood > sensorOk))
		{
			throw new RemoteCommandException("Sensor green minimum must be larger than orange minimum");
		}
		
		if (!(lastSensorDetection > lastAgentCommunication ))
		{
			throw new RemoteCommandException("Sensor green minimum must be larger than orange minimum");
		}
	}

	public int getLastAgentCommunication()
	{
		return lastAgentCommunication;
	}

	public void setLastAgentCommunication(int lastAgentCommunication)
	{
		this.lastAgentCommunication = lastAgentCommunication;
	}

	public int getLastSensorDetection()
	{
		return lastSensorDetection;
	}

	public void setLastSensorDetection(int lastSensorDetection)
	{
		this.lastSensorDetection = lastSensorDetection;
	}

	public int getSubnetGood()
	{
		return subnetGood;
	}

	public void setSubnetGood(int subnetGood)
	{
		this.subnetGood = subnetGood;
	}

	public int getSubnetOk()
	{
		return subnetOk;
	}

	public void setSubnetOk(int subnetOk)
	{
		this.subnetOk = subnetOk;
	}

	public int getSystemGood()
	{
		return systemGood;
	}

	public void setSystemGood(int systemGood)
	{
		this.systemGood = systemGood;
	}

	public int getSystemOk()
	{
		return systemOk;
	}

	public void setSystemOk(int systemOk)
	{
		this.systemOk = systemOk;
	}

	public int getSensorGood()
	{
		return sensorGood;
	}

	public void setSensorGood(int sensorGood)
	{
		this.sensorGood = sensorGood;
	}

	public int getSensorOk()
	{
		return sensorOk;
	}

	public void setSensorOk(int sensorOk)
	{
		this.sensorOk = sensorOk;
	}

	public String getAlternateEpoServerNames()
	{
		return alternateEpoServerNames;
	}

	public void setAlternateEpoServerNames(String alternateEpoServerNames)
	{
		this.alternateEpoServerNames = alternateEpoServerNames;
	}
	
	public static void main(String[] args)
	{
		Gson gson = new Gson();
		System.out.println(gson.toJson(new ComplianceSettings()));
	}
}
