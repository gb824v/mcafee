package com.mcafee.mam.auto.infra.drivers.vsphere;

import java.util.ArrayList;
import java.util.List;

import com.mcafee.mam.auto.infra.Record;
import com.mcafee.mam.auto.infra.TestDriver;
import com.mcafee.mam.auto.infra.TestException;

import org.apache.log4j.Logger;

/**
 * 
 * @author Guy
 */
public final class Vcenter extends TestDriver
{
	private static Logger logger = Logger.getLogger(Vcenter.class);
	private static Vcenter instance = null;
	private VSphere2Api vm2api;
	private String host;
	private String user;
	private String password;
	private boolean isConnect = false;

	/**
	 * connects to the defined Vcenter.
	 * 
	 * @throws TestException
	 */
	public void init(String host, String user, String password, String vsClientName) throws TestException
	{
		this.host = host;
		this.user = user;
		this.password = password;
		try
		{
			if (!isConnect)
			{
				logger.info("Connecting to " + vsClientName + ": " + host);
				vm2api = new VSphere2Api(host, user, password, true);
				this.isConnect = true;
			}
		}
		catch (Exception ex)
		{
			throw new TestException("Failed to init Vcenter: " + host, ex);
		}
	}

	public static Vcenter getInstance() throws TestException
	{
		if (instance == null)
		{
			instance = new Vcenter();
		}
		return instance;
	}

	/**
	 * 
	 * @throws Exception
	 */
	public static void connect() throws Exception
	{
		try
		{
			logger.info("Connecting to Vcenter at " + instance.host);
			instance.vm2api.connect();
		}
		catch (Exception ex)
		{
			throw new TestException("Failed to init Vcenter", ex);
		}
	}

	/**
	 * 
	 * @throws Exception
	 */
	public void disconnect() throws Exception
	{
		try
		{
			logger.info("Disconnecting Vcenter: " + instance.host);
			instance.vm2api.disconnect();
		}
		catch (Exception ex)
		{
			throw new TestException("Failed to disconnect Vcenter", ex);
		}
	}

	/**
	 * 
	 * @param vmName
	 * @throws Exception
	 */
	public void reset(String vmName) throws Exception
	{
		if (vmName.isEmpty() && vmName != null)
		{
			reportInfoMessage("reset ", vmName);
			instance.vm2api.reset(vmName);
		}
		else
		{
			reportInfoMessage("vmName is invalid ", vmName);
		}
	}

	/**
	 * 
	 * @param vmName
	 * @throws Exception
	 */
	public static void resetAll() throws Exception
	{

		reportInfoMessage("reset all Vcenter devices ", "");
		instance.vm2api.reset("");
	}

	/**
	 * 
	 * @param hostnames
	 * @throws Exception
	 */
	public static void resetList(ArrayList<String> hostnames) throws Exception
	{

		reportInfoMessage("reset all Vcenter devices ", "");
		instance.vm2api.reset("");
	}

	/**
	 * 
	 * @param vmName
	 * @throws Exception
	 */
	public void shutdownAll(String... excludeList) throws Exception
	{
		instance.vm2api.shutdownAll(excludeList);
	}

	/**
	 * 
	 * @param vmName
	 * @throws Exception
	 */
	public void powerOffAll(String... excludeList) throws Exception
	{
		instance.vm2api.powerOffAll(excludeList);
	}

	/**
	 * 
	 * @param vmName
	 * @throws Exception
	 */
	public void powerOff(String vmName) throws Exception
	{
		if (instance.vm2api.isPowerOn(vmName))
		{
			reportInfoMessage("power Off", vmName);
			instance.vm2api.powerOff(vmName);
		}

	}

	/**
	 * 
	 * @param vm
	 * @throws Exception
	 */
	public void shutdown(String vmName) throws Exception
	{

		if (instance.vm2api.isPowerOn(vmName))
		{
			reportInfoMessage("Shutdown guset", vmName);
			instance.vm2api.shutdown(vmName);
		}
		else
		{
			reportInfoMessage("Guset already down", vmName);
		}
	}

	public void powerOn(String vmName) throws Exception
	{
		powerOn(false, vmName);
	}

	/**
	 * 
	 * @param Device
	 *            vm
	 * @param isWait
	 *            = for guest up.
	 * @throws Exception
	 */

	public void powerOn(boolean isWait, String vmName) throws Exception
	{
		if (!instance.vm2api.isPowerOn(vmName))
		{
			reportInfoMessage("power On", vmName);
			instance.vm2api.powerOn(vmName);
		}
		else
		{
			reportInfoMessage("Already On", vmName);
		}
		if (isWait)
		{
			reportInfoMessage("waiting for vm to reconnected...", vmName);
			instance.vm2api.waitForVmConnected(vmName);
			reportInfoMessage("vm connected.", vmName);
		}
	}

	/**
	 * 
	 * @param vm
	 * @return
	 * @throws Exception
	 */
	public boolean isPowerOn(String vmName) throws Exception
	{
		return instance.vm2api.isPowerOn(vmName);
	}

	/**
	 * 
	 * @param vmName
	 * @throws Exception
	 */
	public void reboot(String vmName) throws Exception
	{
		reportInfoMessage("reboot", vmName);
		instance.vm2api.reboot(vmName);
	}

	/**
	 * 
	 * @param device
	 * @param guestProgramPath
	 * @throws Exception
	 */

	public void runProgram(String vmName, String userName, String password, String guestProgramPath) throws Exception
	{
		reportInfoMessage("runProgram " + guestProgramPath, vmName);
		instance.vm2api.runProgram(vmName, userName, password, guestProgramPath);
	}

	/**
	 * 
	 * @param device
	 * @throws Exception
	 */

	public void login(String vmName, String userName, String password) throws Exception
	{
		reportInfoMessage("login ", vmName);
		instance.vm2api.login(vmName, userName, password);
	}

	/**
	 * 
	 * @param device
	 * @param netIndex
	 * @throws Exception
	 */
	public void revert2Connection(String vmName, String mac, String connection) throws Exception
	{
		reportInfoMessage("updateExsistingVmNic (mac):" + mac + " to connection", connection);
		instance.vm2api.updateExsistingVmNic(vmName, mac, connection);
	}

	/**
	 * 
	 * @param device
	 * @return
	 * @throws Exception
	 */
	public List<Record> getVmGuestNetInfo(String vmName) throws Exception
	{
		return instance.vm2api.getVmGuestNetInfo(vmName);
	}

	/**
	 * 
	 * @param device
	 * @param localFilePath
	 * @param guestFilePath
	 * @throws Exception
	 */
	public void copyFile(String vmName, String userName, String password, String localFilePath, String guestFilePath) throws Exception
	{
		reportInfoMessage("copyFile from: " + localFilePath + " to: " + guestFilePath, vmName);
		instance.vm2api.copyFile(vmName, userName, password, localFilePath, guestFilePath);
	}

	/**
	 * 
	 * @param vmName
	 * @throws Exception
	 */
	public void runWgetToGoogle(String vmName, String userName, String password) throws Exception
	{
		String guestProgPath = "c:/runIE.cmd";
		reportInfoMessage("runProgram " + guestProgPath, vmName);
		instance.vm2api.runProgram(vmName, userName, password, guestProgPath);
	}

	/**
	 * 
	 * @param vmName
	 * @param snapshot
	 * @throws Exception
	 */
	public void revert2Snapshot(String vmName, String snapshot, boolean isForce, boolean isWait) throws Exception
	{

		if (snapshot != null)
		{
			try
			{
				reportInfoMessage("Revert To Snapshot [ " + snapshot + " ]", vmName);
				instance.vm2api.revertSnapshot(vmName, snapshot, isForce);
				Thread.sleep(1000);
			}
			catch (Exception e)
			{
				throw new TestException(String.format(" Reverting Snapshot - [ %s ] Failure %n", snapshot));
			}
		}
		if (isWait)
		{
			reportInfoMessage("waiting for vm to reconnected...", vmName);
			instance.vm2api.waitForVmConnected(vmName);
			reportInfoMessage("vm connected.", vmName);
		}
	}

	/**
	 * 
	 * @param message
	 * @param vmName
	 */
	private static void reportInfoMessage(String message, String vmName)
	{
		if (logger.isInfoEnabled())
		{
			logger.info(String.format("Vcenter: '%s' Vm: '%s': %s", instance.host, vmName, message));
		}
	}

	public String getHost()
	{
		return host;
	}

	public void setHost(String host)
	{
		this.host = host;
	}

	public String getUser()
	{
		return user;
	}

	public void setUser(String user)
	{
		this.user = user;
	}

	public String getPassword()
	{
		return password;
	}

	public void setPassword(String password)
	{
		this.password = password;
	}

	public boolean isConnect()
	{
		return isConnect;
	}

	public void setConnect(boolean isConnect)
	{
		this.isConnect = isConnect;
	}
}
