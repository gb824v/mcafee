/*
 * Copyright (C) 2013, McAfee Inc. All Rights Reserved.
 */
package com.mcafee.mam.qaadevtool.command.rsd.server;

import com.google.gson.Gson;
import com.mcafee.mam.qaadevtool.command.rsd.common.RemoteCommandException;
import com.mcafee.mam.qaadevtool.command.rsd.common.StaticUtils;

/**
 * @author Girish
 * 
 */
public class SensorSettings
{
	private int sensorTimeout = 1;
	private String timeoutUnits = "days";
	
	private int sensorsPerSubnet;
	private int maxActive;
	
	private int maxTimePeriod = 1;
	private String periodUnits = "hours";
	
	private String noscanText = ""; // String [Line separated MAC addresses or OUIs]
	private String ipnoscanText = ""; // String [Line separated IP addresses or
									// Subset masks]
	public void checkForErrors()
	{
		StaticUtils.throwIfInvalid(sensorTimeout, "sensorTimeout");
		//StaticUtils.throwIfInvalid(maxActive, "maxActive");
		StaticUtils.throwIfInvalid(maxTimePeriod, "maxTimePeriod");

		if (!"hours".equalsIgnoreCase(timeoutUnits) && !"days".equalsIgnoreCase(timeoutUnits) && !"minutes".equalsIgnoreCase(timeoutUnits))
		{
			throw new RemoteCommandException("Invalid input for timeoutUnits=" + timeoutUnits + ". Possible values [minutes, hours, days] ");
		}

		if (!"hours".equalsIgnoreCase(periodUnits) && !"minutes".equalsIgnoreCase(timeoutUnits))
		{
			throw new RemoteCommandException("Invalid input for periodUnits=" + periodUnits + ". Possible values [minutes , hours] ");
		}

		int maxInt = 10080;

		if ("hours".equalsIgnoreCase(timeoutUnits))
		{
			maxInt = 168;
		} else if ("days".equalsIgnoreCase(timeoutUnits))
		{
			maxInt = 7;
		}

		if (sensorTimeout > maxInt)
		{
			throw new RemoteCommandException("This value must be a whole number less than or equal to 7 days. ");
		}

		maxInt = 1440;
		if ("hours".equalsIgnoreCase(periodUnits))
		{
			maxInt = 24;
		}

		if (maxActive > maxInt)
		{
			throw new RemoteCommandException("Minute(s) Hour(s)   This value must be a whole number less than or equal to 1 day. ");
		}
	}

	public int getSensorTimeout()
	{
		return sensorTimeout;
	}
	public void setSensorTimeout(int sensorTimeout)
	{
		this.sensorTimeout = sensorTimeout;
	}
	public int getSensorsPerSubnet()
	{
		return sensorsPerSubnet;
	}
	public void setSensorsPerSubnet(int sensorsPerSubnet)
	{
		this.sensorsPerSubnet = sensorsPerSubnet;
	}
	public int getMaxActive()
	{
		return maxActive;
	}
	public void setMaxActive(int maxActive)
	{
		this.maxActive = maxActive;
	}
	public String getPeriodUnits()
	{
		return periodUnits;
	}
	public void setPeriodUnits(String periodUnits)
	{
		this.periodUnits = periodUnits;
	}
	public int getMaxTimePeriod()
	{
		return maxTimePeriod;
	}
	public void setMaxTimePeriod(int maxTimePeriod)
	{
		this.maxTimePeriod = maxTimePeriod;
	}
	public String getTimeoutUnits()
	{
		return timeoutUnits;
	}
	public void setTimeoutUnits(String timeoutUnits)
	{
		this.timeoutUnits = timeoutUnits;
	}
	public String getNoscanText()
	{
		return noscanText;
	}
	public void setNoscanText(String noscanText)
	{
		this.noscanText = noscanText;
	}
	public String getIpnoscanText()
	{
		return ipnoscanText;
	}
	public void setIpnoscanText(String ipnoscanText)
	{
		this.ipnoscanText = ipnoscanText;
	}

	public static void main(String[] args)
	{
		Gson gson = new Gson();
		System.out.println(gson.toJson(new SensorSettings()));
	}
}
