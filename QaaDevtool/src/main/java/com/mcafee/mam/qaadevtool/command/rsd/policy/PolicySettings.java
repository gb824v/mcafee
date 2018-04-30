/*
 * Copyright (C) 2013, McAfee Inc. All Rights Reserved.
 */
package com.mcafee.mam.qaadevtool.command.rsd.policy;

import com.google.gson.Gson;
import com.mcafee.mam.qaadevtool.command.rsd.common.RemoteCommandException;
import com.mcafee.mam.qaadevtool.command.rsd.common.StaticUtils;

/**
 * @author Girish
 * 
 */
public class PolicySettings
{
	private String policyName = "";
	private boolean enableSensor = true; // EnableSensor [Boolean]
	private boolean enableDebug = false; // EnableDebug [Boolean]
	private boolean useDefaultIncludedSubnets = false; // UseDefaultIncludedSubnets
														// //
	// [Boolean]
	private boolean enableDHCP = false; // EnableDHCP [Boolean]
	private boolean restrictReports = true; // RestrictReports [Boolean]
	private boolean subnetScanEnabled = true;;// SubnetScanEnabled [Boolean]
	private boolean oSFingerprinting = true;;// OSFingerprinting [Boolean]
	private boolean scanExceptions = true;// scanExceptions [Boolean]
	private boolean rSCEnabled = false;// RSCEnabled [Boolean]
	private boolean mDNSEnabled = true;// mDNSEnabled [Boolean]
	private boolean dNSEnabled = true;// DNSEnabled [Boolean]
	private String ifaceOption = "includeRadio";// ifaceOption ["includeRadio",
												// "excludeRadio"]
	private String hiddenIncludeList = "";// HiddenIncludeList [ | separated
											// string]
	private String hiddenExcludeList = "";// HiddenExcludeList [ | separated
											// string]
	private String scanOption = "";// scanOption ["scanWhitelist",
									// "scanBlacklist" ,
									// "scanAll"]
	private int OSFingerprintingInterval;// timepickerNumbox_OSFingerprintingInterval
											// [ Integer]
	private String OSFingerprintingIntervalSelector = "";// timepickerSelector_OSFingerprintingInterval
	// ["minutes", "hours"]
	private int oSFingerprintingDelay;// timepickerNumbox_OSFingerprintingDelay
										// [ Integer]
	private String oSFingerprintingDelaySelector = "";// timepickerSelector_OSFingerprintingDelay
														// ["minutes", "hours"]
	private int platformCacheLife;// timepickerNumbox_PlatformCacheLife [
									// Integer]
	private String platformCacheLifeSelector = "";// timepickerSelector_PlatformCacheLife
													// ["minutes", "hours" ,
													// "days"]
	private String serverName;// // ServerName
	private int failoverSleepTime;// timepickerNumbox_FailoverSleepTime [
									// Integer]
	private String failoverSleepTimeSelector = "";// timepickerSelector_FailoverSleepTime
													// ["minutes", "hours"]
	private int hostFilterTimeout;// timepickerNumbox_HostFilterTimeout [
									// Integer]
	private String hostFilterTimeoutSelector = "";// timepickerSelector_HostFilterTimeout
													// ["minutes", "hours"]
	private int sensorThrottle;// timepickerNumbox_SensorThrottle [ Integer]
	private String sensorThrottleSelector = "";// timepickerSelector_SensorThrottle
												// ["minutes", "hours"]
	private String hiddenWhitelist = "";// HiddenWhitelist [ | separated string]
	private String hiddenBlacklist = "";// HiddenBlacklist [ | separated string]
	private String electMethod = "";// ElectMethod ["sensorElects",
									// "serverElects"]
	private int electResultWaitTime;// timepickerNumbox_electResultWaitTime [
									// Integer]
	private String electResultWaitTimeSelector = "";// timepickerSelector_electResultWaitTime
	// ["minutes", "hours"]
	private int electionInterval;// timepickerNumbox_electionInterval [ Integer]
	private String electionIntervalSelector = "";// timepickerSelector_electionInterval
	// ["minutes", "hours"]
	private String numActiveSensorsRadio = "radioAllActiveSensors";// NumActiveSensorsRadio
											// ["radioAllActiveSensors",
											// "radioNumActiveSensors"]
	private int numActiveSensors;// numActiveSensors [ Integer ]
	private String ipv4Multicast = "";// ipv4Multicast [ valid ipV4]
	private String ipv6Multicast = "";// ipv6Multicast [ valid ipV6]
	private int sensorPort;// sensorPort [ Integer]

	public boolean isEnableSensor()
	{
		return enableSensor;
	}
	public void setEnableSensor(boolean enableSensor)
	{
		this.enableSensor = enableSensor;
	}
	public boolean isEnableDebug()
	{
		return enableDebug;
	}
	public void setEnableDebug(boolean enableDebug)
	{
		this.enableDebug = enableDebug;
	}
	public boolean isUseDefaultIncludedSubnets()
	{
		return useDefaultIncludedSubnets;
	}
	public void setUseDefaultIncludedSubnets(boolean useDefaultIncludedSubnets)
	{
		this.useDefaultIncludedSubnets = useDefaultIncludedSubnets;
	}
	public boolean isRestrictReports()
	{
		return restrictReports;
	}
	public void setRestrictReports(boolean restrictReports)
	{
		this.restrictReports = restrictReports;
	}
	public boolean isSubnetScanEnabled()
	{
		return subnetScanEnabled;
	}
	public void setSubnetScanEnabled(boolean subnetScanEnabled)
	{
		this.subnetScanEnabled = subnetScanEnabled;
	}
	public boolean isoSFingerprinting()
	{
		return oSFingerprinting;
	}
	public void setoSFingerprinting(boolean oSFingerprinting)
	{
		this.oSFingerprinting = oSFingerprinting;
	}
	public boolean isScanExceptions()
	{
		return scanExceptions;
	}
	public void setScanExceptions(boolean scanExceptions)
	{
		this.scanExceptions = scanExceptions;
	}
	public boolean isrSCEnabled()
	{
		return rSCEnabled;
	}
	public void setrSCEnabled(boolean rSCEnabled)
	{
		this.rSCEnabled = rSCEnabled;
	}
	public boolean ismDNSEnabled()
	{
		return mDNSEnabled;
	}
	public void setmDNSEnabled(boolean mDNSEnabled)
	{
		this.mDNSEnabled = mDNSEnabled;
	}
	public boolean isdNSEnabled()
	{
		return dNSEnabled;
	}
	public void setdNSEnabled(boolean dNSEnabled)
	{
		this.dNSEnabled = dNSEnabled;
	}
	public String getIfaceOption()
	{
		return ifaceOption;
	}
	public void setIfaceOption(String ifaceOption)
	{
		this.ifaceOption = ifaceOption;
	}
	public String getHiddenIncludeList()
	{
		return hiddenIncludeList;
	}
	public void setHiddenIncludeList(String hiddenIncludeList)
	{
		if (!StaticUtils.validateIPsWithPort(hiddenIncludeList))
		{
			throw new RemoteCommandException(hiddenIncludeList);
		}
		this.hiddenIncludeList = hiddenIncludeList;
	}
	public String getHiddenExcludeList()
	{
		return hiddenExcludeList;
	}
	public void setHiddenExcludeList(String hiddenExcludeList)
	{
		if (!StaticUtils.validateIPsWithPort(hiddenIncludeList))
		{
			throw new RemoteCommandException(hiddenIncludeList);
		}
		this.hiddenExcludeList = hiddenExcludeList;
	}
	public String getScanOption()
	{
		return scanOption;
	}
	public void setScanOption(String scanOption)
	{
		this.scanOption = scanOption;
	}
	public int getOSFingerprintingInterval()
	{
		return OSFingerprintingInterval;
	}
	public void setOSFingerprintingInterval(int oSFingerprintingInterval)
	{
		OSFingerprintingInterval = oSFingerprintingInterval;
	}
	public String getOSFingerprintingIntervalSelector()
	{
		return OSFingerprintingIntervalSelector;
	}
	public void setOSFingerprintingIntervalSelector(String oSFingerprintingIntervalSelector)
	{
		OSFingerprintingIntervalSelector = oSFingerprintingIntervalSelector;
	}
	public int getoSFingerprintingDelay()
	{
		return oSFingerprintingDelay;
	}
	public void setoSFingerprintingDelay(int oSFingerprintingDelay)
	{
		this.oSFingerprintingDelay = oSFingerprintingDelay;
	}
	public String getoSFingerprintingDelaySelector()
	{
		return oSFingerprintingDelaySelector;
	}
	public void setoSFingerprintingDelaySelector(String oSFingerprintingDelaySelector)
	{
		this.oSFingerprintingDelaySelector = oSFingerprintingDelaySelector;
	}
	public int getPlatformCacheLife()
	{
		return platformCacheLife;
	}
	public void setPlatformCacheLife(int platformCacheLife)
	{
		this.platformCacheLife = platformCacheLife;
	}
	public String getPlatformCacheLifeSelector()
	{
		return platformCacheLifeSelector;
	}
	public void setPlatformCacheLifeSelector(String platformCacheLifeSelector)
	{
		this.platformCacheLifeSelector = platformCacheLifeSelector;
	}
	public int getFailoverSleepTime()
	{
		return failoverSleepTime;
	}
	public void setFailoverSleepTime(int failoverSleepTime)
	{
		this.failoverSleepTime = failoverSleepTime;
	}
	public String getFailoverSleepTimeSelector()
	{
		return failoverSleepTimeSelector;
	}
	public void setFailoverSleepTimeSelector(String failoverSleepTimeSelector)
	{
		this.failoverSleepTimeSelector = failoverSleepTimeSelector;
	}
	public int getHostFilterTimeout()
	{
		return hostFilterTimeout;
	}
	public void setHostFilterTimeout(int hostFilterTimeout)
	{
		this.hostFilterTimeout = hostFilterTimeout;
	}
	public String getHostFilterTimeoutSelector()
	{
		return hostFilterTimeoutSelector;
	}
	public void setHostFilterTimeoutSelector(String hostFilterTimeoutSelector)
	{
		this.hostFilterTimeoutSelector = hostFilterTimeoutSelector;
	}
	public int getSensorThrottle()
	{
		return sensorThrottle;
	}
	public void setSensorThrottle(int sensorThrottle)
	{
		this.sensorThrottle = sensorThrottle;
	}
	public String getSensorThrottleSelector()
	{
		return sensorThrottleSelector;
	}
	public void setSensorThrottleSelector(String sensorThrottleSelector)
	{
		this.sensorThrottleSelector = sensorThrottleSelector;
	}
	public String getHiddenWhitelist()
	{
		return hiddenWhitelist;
	}
	public void setHiddenWhitelist(String hiddenWhitelist)
	{
		if (!StaticUtils.validateIPsWithPort(hiddenIncludeList))
		{
			throw new RemoteCommandException(hiddenIncludeList);
		}
		this.hiddenWhitelist = hiddenWhitelist;
	}
	public String getHiddenBlacklist()
	{
		return hiddenBlacklist;
	}
	public void setHiddenBlacklist(String hiddenBlacklist)
	{
		if (!StaticUtils.validateIPsWithPort(hiddenIncludeList))
		{
			throw new RemoteCommandException(hiddenIncludeList);
		}
		this.hiddenBlacklist = hiddenBlacklist;
	}
	public String getElectMethod()
	{
		return electMethod;
	}
	public void setElectMethod(String electMethod)
	{
		this.electMethod = electMethod;
	}
	public int getElectResultWaitTime()
	{
		return electResultWaitTime;
	}
	public void setElectResultWaitTime(int electResultWaitTime)
	{
		this.electResultWaitTime = electResultWaitTime;
	}
	public String getElectResultWaitTimeSelector()
	{
		return electResultWaitTimeSelector;
	}
	public void setElectResultWaitTimeSelector(String electResultWaitTimeSelector)
	{
		this.electResultWaitTimeSelector = electResultWaitTimeSelector;
	}
	public int getElectionInterval()
	{
		return electionInterval;
	}
	public void setElectionInterval(int electionInterval)
	{
		this.electionInterval = electionInterval;
	}
	public String getElectionIntervalSelector()
	{
		return electionIntervalSelector;
	}
	public void setElectionIntervalSelector(String electionIntervalSelector)
	{
		this.electionIntervalSelector = electionIntervalSelector;
	}
	public String getNumActiveSensorsRadio()
	{
		return numActiveSensorsRadio;
	}
	public void setNumActiveSensorsRadio(String numActiveSensorsRadio)
	{
		this.numActiveSensorsRadio = numActiveSensorsRadio;
	}
	public int getNumActiveSensors()
	{
		return numActiveSensors;
	}
	public void setNumActiveSensors(int numActiveSensors)
	{
		this.numActiveSensors = numActiveSensors;
	}
	public String getIpv4Multicast()
	{
		return ipv4Multicast;
	}
	public void setIpv4Multicast(String ipv4Multicast)
	{
		if (!StaticUtils.validateIPs(ipv4Multicast, true))
		{
			throw new RemoteCommandException(ipv4Multicast);
		}
		this.ipv4Multicast = ipv4Multicast;
	}
	public String getIpv6Multicast()
	{
		return ipv6Multicast;
	}
	public void setIpv6Multicast(String ipv6Multicast)
	{
		if (!StaticUtils.validateIPs(ipv6Multicast, false))
		{
			throw new RemoteCommandException(ipv6Multicast);
		}
		this.ipv6Multicast = ipv6Multicast;
	}
	public int getSensorPort()
	{
		return sensorPort;
	}
	public void setSensorPort(int sensorPort)
	{
		this.sensorPort = sensorPort;
	}
	public static void main(String[] args)
	{
		Gson gson = new Gson();
		System.out.println(gson.toJson(new PolicySettings()));
	}
	public String getPolicyName()
	{
		return policyName;
	}
	public void setPolicyName(String policyName)
	{
		this.policyName = policyName;
	}
	public boolean isEnableDHCP()
	{
		return enableDHCP;
	}
	public void setEnableDHCP(boolean enableDHCP)
	{
		this.enableDHCP = enableDHCP;
	}
	public String getServerName()
	{
		return serverName;
	}
	public void setServerName(String serverName)
	{
		this.serverName = serverName;
	}
	
	public void checkForErrors()
	{
		StaticUtils.throwIfInvalid(serverName, "serverName");
		StaticUtils.throwIfInvalid(ipv4Multicast, "ipv4Multicast");
		StaticUtils.throwIfInvalid(ipv6Multicast, "ipv6Multicast");
		StaticUtils.throwIfInvalid(ipv4Multicast, "ipv4Multicast");
		StaticUtils.throwIfInvalid(ipv4Multicast, "ipv4Multicast");
	}
}