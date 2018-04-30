package com.mcafee.mam.auto.infra.device;

import com.mcafee.mam.auto.infra.util.MAC;

/**
 * Represents a network interface of a device.
 * 
 * @author Guy
 */
public class DeviceAddress
{
	private String ip;
	private String mac;
	private String vlan;
	private String port = "";

	public String getIp()
	{
		return ip;
	}

	public String getMac()
	{
		return mac.toLowerCase();
	}

	public String getPort()
	{
		return port;
	}

	public String getVlan()
	{
		return vlan;
	}
	
	public void setVlan(String vlan)
	{
		this.vlan = vlan;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	/***
	 * Get first MAC address.
	 * 
	 * @return
	 */
	public Long getMacAsLong()
	{
		return MAC.convertSensorMacToLong(mac.toLowerCase());
	}
}
