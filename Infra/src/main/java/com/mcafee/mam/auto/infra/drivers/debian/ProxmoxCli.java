package com.mcafee.mam.auto.infra.drivers.debian;


/**
 * A driver for communicating with MAM sensor.
 * 
 * @author guy
 */
public class ProxmoxCli extends DebianInfra
{

	/**
	 * get ctid from proxmox host
	 * 
	 * @param hostName
	 * @return
	 * @throws Exception
	 */
	private int getCtid(String hostName) throws Exception
	{
		DebianResponse response = execute("vzlist | grep " + hostName);
		return Integer.parseInt(response.toString().split("\\s")[0]);
	}

	/**
	 * get IP address by given interface
	 * 
	 * @param ctid
	 * @param eth
	 * @return
	 * @throws Exception
	 */
	private void showIp(int ctid, String eth) throws Exception
	{
		execute("vzctl exec " + ctid + " ifconfig " + eth + " | grep 'inet addr:' | cut -d: -f2 | awk '{ print $1}'");
	}

	/**
	 * show eth0 IP address by given host name
	 * 
	 * @param hostName
	 * @throws Exception
	 */
	public void showEth0Ip(String hostName) throws Exception
	{
		showIp(getCtid(hostName), "eth0");
	}

	/**
	 * show eth1 IP address by given host name
	 * 
	 * @param hostName
	 * @throws Exception
	 */
	public void showEth1Ip(String hostName) throws Exception
	{
		showIp(getCtid(hostName), "eth1");

	}

	/**
	 * restart interface to proxmox ctid
	 * 
	 * @param ctid
	 * @throws Exception
	 */
	public void restNetworking(int ctid) throws Exception
	{
		DebianResponse response = execute("vzctl exec " + ctid + " /etc/init.d/networking restart");
		response.verify("Reconfiguring network interfaces...done.");
	}

	/**
	 * release IP address from DHCP
	 * 
	 * @param ctid
	 * @throws Exception
	 */
	public void releaseIPFromDHCP(int ctid) throws Exception
	{
		execute("vzctl exec " + ctid + " dhclient -r");
	}

	/**
	 * release IP address from DHCP
	 * 
	 * @param hostName
	 * @throws Exception
	 */
	public void releaseIPFromDHCP(String hostName) throws Exception
	{
		releaseIPFromDHCP(getCtid(hostName));
	}

	/**
	 * renew IP address from DHCP
	 * 
	 * @param ctid
	 * @param eth
	 * @throws Exception
	 */

	public void renewIPFromDHCP(int ctid, String eth) throws Exception
	{
		execute("vzctl exec " + ctid + " dhclient " + eth);
	}

	/**
	 * renew eth0 IP address from DHCP
	 * 
	 * @param hostName
	 * @throws Exception
	 */
	public void renewEth0IPFromDHCP(String hostName) throws Exception
	{
		renewIPFromDHCP(getCtid(hostName), "eth0");
	}

	/**
	 * renew eth1 IP address from DHCP
	 * 
	 * @param hostName
	 * @throws Exception
	 */
	public void renewEth1IPFromDHCP(String hostName) throws Exception
	{
		renewIPFromDHCP(getCtid(hostName), "eth1");
	}

	/**
	 * restart interface to  hostName
	 * 
	 * @param hostName
	 * @throws Exception
	 */
	public void restInt(String hostName) throws Exception
	{
		restNetworking(getCtid(hostName));
	}

}