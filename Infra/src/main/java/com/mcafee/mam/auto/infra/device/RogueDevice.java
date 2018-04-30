package com.mcafee.mam.auto.infra.device;

import com.mcafee.mam.auto.infra.drivers.epo.EPOClient;

/**
 * Represents a device on the network
 * 
 * @author Guy
 */
public class RogueDevice extends VirtualDevice
{

	public RogueDevice()
	{
		super.isVirtual = true;
	}

	public void rsdFindSys() throws Exception
	{
		EPOClient.getRcSystem().findSystem(this.getName());
	}

	public void addToException() throws Exception
	{
		EPOClient.getRcRsd().addToException(this.getCanonicalName());
	}
	
}
