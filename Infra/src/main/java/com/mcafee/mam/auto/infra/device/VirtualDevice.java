package com.mcafee.mam.auto.infra.device;

import java.util.List;

import com.mcafee.mam.auto.infra.Record;
import com.mcafee.mam.auto.infra.drivers.vsphere.Vcenter;

/**
 * Represents a device on the network
 * 
 * @author Guy
 */
public class VirtualDevice extends Device
{
	private String mainSnapshot;
	private String snapshot1;
	private String snapshot2;
	private String snapshot3;
	private String mainConnection;
	private String connection2;

	public VirtualDevice()
	{
		super.isVirtual = true;
	}

	public void reset() throws Exception
	{
		Vcenter.getInstance().reset(name);
	}

	public void powerOff() throws Exception
	{
		Vcenter.getInstance().powerOff(name);
	}

	public void shutdown() throws Exception
	{
		Vcenter.getInstance().shutdown(name);

	}

	public void powerOn() throws Exception
	{
		Vcenter.getInstance().powerOn(name);
	}

	public void powerOn(boolean isWait) throws Exception
	{
		Vcenter.getInstance().powerOn(isWait, name);

	}

	public void isPowerOn() throws Exception
	{
		Vcenter.getInstance().isPowerOn(name);
	}

	public void reboot() throws Exception
	{
		Vcenter.getInstance().reboot(name);
	}

	public void runProgram(String guestProgramPath) throws Exception
	{
		Vcenter.getInstance().runProgram(name, userName, password, guestProgramPath);
	}

	public void login() throws Exception
	{
		Vcenter.getInstance().login(name, userName, password);
	}

	public void revertToMainConnection() throws Exception
	{
		Vcenter.getInstance().revert2Connection(name, addresses.get(0).getMac(), mainConnection);
	}

	public void revertToConnection2() throws Exception
	{
		Vcenter.getInstance().revert2Connection(name, addresses.get(0).getMac(), connection2);
	}
	public String getVmGuestIp(String vmName) throws Exception
	{
		return getVmGuestNetInfo(vmName).get(0).get("ip.v4");
	}
	
	public List<Record> getVmGuestNetInfo(String name) throws Exception
	{
		return Vcenter.getInstance().getVmGuestNetInfo(name);
	}

	public void copyFile(String localFilePath, String guestFilePath) throws Exception
	{
		Vcenter.getInstance().copyFile(name, userName, password, localFilePath, guestFilePath);

	}

	public void runWgetToGoogle() throws Exception
	{
		Vcenter.getInstance().runWgetToGoogle(name, userName, password);

	}

	public void revertToMainSnap() throws Exception
	{
		revertToMainSnap(false, false);
	}
	
	public void revertToMainSnap(boolean isForce, boolean isWait) throws Exception
	{
		Vcenter.getInstance().revert2Snapshot(name, mainSnapshot, isForce, isWait);
	}

	public void revertToSnap2(boolean isForce, boolean isWait) throws Exception
	{
		Vcenter.getInstance().revert2Snapshot(name, snapshot2, isForce, isWait);
	}

	public void revertToSnap3(boolean isForce, boolean isWait) throws Exception
	{
		Vcenter.getInstance().revert2Snapshot(name, snapshot3, isForce, isWait);
	}

	public String getMainSnapshot()
	{
		return mainSnapshot;
	}

	public void setMainSnapshot(String mainSnapshot)
	{
		this.mainSnapshot = mainSnapshot;
	}

	public String getSnapshot1()
	{
		return snapshot1;
	}

	public void setSnapshot1(String snapshot1)
	{
		this.snapshot1 = snapshot1;
	}

	public String getSnapshot2()
	{
		return snapshot2;
	}

	public void setSnapshot2(String snapshot2)
	{
		this.snapshot2 = snapshot2;
	}

	public String getSnapshot3()
	{
		return snapshot3;
	}

	public void setSnapshot3(String snapshot3)
	{
		this.snapshot3 = snapshot3;
	}

	public String getMainConnection()
	{
		return mainConnection;
	}

	public void setMainConnection(String mainConnection)
	{
		this.mainConnection = mainConnection;
	}

	public String getConnection2()
	{
		return connection2;
	}

	public void setConnection2(String connection2)
	{
		this.connection2 = connection2;
	}
}
