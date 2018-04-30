package com.mcafee.mam.auto.infra.device;

import com.mcafee.mam.auto.infra.drivers.epo.EPOClient;
import com.mcafee.mam.auto.infra.wmi.VbsUtil;

/**
 * Represents a device on the network
 * 
 * @author Guy
 */
public class ManagedDevice extends VirtualDevice
{

	public ManagedDevice()
	{
		super.isVirtual = true;
	}

	public boolean isSensorup() throws Exception
	{
		return EPOClient.getRcAdt().isSensorUp(getName());
	}

	public boolean createPolicyAndAssign(String policyName) throws Exception
	{
		return EPOClient.getRcPolicy().createPolicyAndAssign(policyName, "My Default", "", this.getName());
	}

	public boolean assignPolicy(String policyName) throws Exception
	{
		return EPOClient.getRcPolicy().assignPolicy(policyName, this.getName());
	}

	public boolean installSensor() throws Exception
	{
		return EPOClient.getRcSensor().installSensor(getName());
	}

	public boolean uninstallSensor() throws Exception
	{
		return EPOClient.getRcSensor().uninstallSensor(getName());
	}

	public boolean addSensorToBlacklist() throws Exception
	{
		return EPOClient.getRcSensor().addToSensorBlacklist(getName());
	}

	public boolean wakeupAgent() throws Exception
	{
		return EPOClient.getRcAdt().wakeupAgent(this.getName());
	}

	public void collectAndSendProps() throws Exception
	{
		VbsUtil.collectAndSendProps(this);
	}

	public void enforcePolicies() throws Exception
	{
		VbsUtil.enforcePolicies(this);
	}

	public boolean isProcessRunning(String process) throws Exception
	{
		return VbsUtil.isProcessRunning(this, process);
	}

	public boolean isServiceRunning(String service) throws Exception
	{
		return VbsUtil.isServiceRunning(this, service);
	}
}
