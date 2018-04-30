package com.mcafee.mam.auto.infra.device;

import com.mcafee.mam.auto.infra.TestObject;
import com.mcafee.mam.auto.infra.drivers.debian.cli.DeviceCli;

import java.util.LinkedList;
import java.util.List;

/**
 * Represents a device on the network
 * 
 * @author Guy
 */
public class Device extends TestObject
{
	private String dns;
	private String netbios;
	private String domain;
	private String authorized;
	private String vendorid;
	private String capability;
	private List<String> users;
	private List<String> os;
	private List<String> cpe;
	private List<String> ports;
	private String hotfixes;
	private String location;
	private String firewall;
	private DeviceCli cli;
	private String SwitchPort;
	private List<String> vlanid;
	private String canonicalName = "";
	protected String userName;
	protected String password;
	
	protected boolean isVirtual = false;

	protected List<DeviceAddress> addresses = new LinkedList<DeviceAddress>();

	public String getDns()
	{
		return dns;
	}

	public String getNetbios()
	{
		return netbios;
	}

	public String getDomain()
	{
		return domain;
	}

	public String getAuthorized()
	{
		return authorized;
	}

	public String getVendorid()
	{
		return vendorid;
	}

	public String getCapability()
	{
		return capability;
	}

	public List<String> getUsers()
	{
		return users;
	}

	public List<String> getOs()
	{
		return os;
	}

	public List<String> getCpe()
	{
		return cpe;
	}

	public List<String> getPorts()
	{
		return ports;
	}

	public String getHotfixes()
	{
		return hotfixes;
	}

	public String getLocation()
	{
		return location;
	}

	public String getFirewall()
	{
		return firewall;
	}

	public DeviceCli getCli()
	{
		return cli;
	}

	public String getSwitchPort()
	{
		return SwitchPort;
	}

	public List<String> getVlanid()
	{
		return vlanid;
	}

	public List<DeviceAddress> getAddresses()
	{
		return addresses;
	}

	public String getMac()
	{
		return addresses.get(0).getMac();
	}

	public long getMacAsLong()
	{
		return addresses.get(0).getMacAsLong();
	}

	public String getIp()
	{
		return addresses.get(0).getIp();
	}
	
	public void setIp(String ip)
	{
		addresses.get(0).setIp(ip);
	}

	public void setDns(String dns)
	{
		this.dns = dns;
	}

	public void setNetbios(String netbios)
	{
		this.netbios = netbios;
	}

	public void setDomain(String domain)
	{
		this.domain = domain;
	}

	public void setAuthorized(String authorized)
	{
		this.authorized = authorized;
	}

	public void setVendorid(String vendorid)
	{
		this.vendorid = vendorid;
	}

	public void setCapability(String capability)
	{
		this.capability = capability;
	}

	public void setUsers(List<String> users)
	{
		this.users = users;
	}

	public void setOs(List<String> os)
	{
		this.os = os;
	}

	public void setCpe(List<String> cpe)
	{
		this.cpe = cpe;
	}

	public void setPorts(List<String> ports)
	{
		this.ports = ports;
	}

	public void setHotfixes(String hotfixes)
	{
		this.hotfixes = hotfixes;
	}

	public void setLocation(String location)
	{
		this.location = location;
	}

	public void setFirewall(String firewall)
	{
		this.firewall = firewall;
	}

	public void setCli(DeviceCli cli)
	{
		this.cli = cli;
	}

	public void setSwitchPort(String switchPort)
	{
		SwitchPort = switchPort;
	}

	public void setVlanid(List<String> vlanid)
	{
		this.vlanid = vlanid;
	}

	public void setAddresses(List<DeviceAddress> addresses)
	{
		this.addresses = addresses;
	}

	public String getCanonicalName()
	{
		return canonicalName;
	}

	public void setCanonicalName(String canonicalName)
	{
		this.canonicalName = canonicalName;
	}

	public String getUserName()
	{
		return userName;
	}

	public String getPassword()
	{
		return password;
	}

	public void setUserName(String userName)
	{
		this.userName = userName;
	}

	public void setPassword(String password)
	{
		this.password = password;
	}
	
}
