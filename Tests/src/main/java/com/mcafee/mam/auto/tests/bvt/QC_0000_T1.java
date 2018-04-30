package com.mcafee.mam.auto.tests.bvt;

import java.io.File;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.google.common.io.Resources;
import com.mcafee.mam.auto.infra.TestClass;
import com.mcafee.mam.auto.infra.TestException;
import com.mcafee.mam.auto.infra.device.ManagedDevice;
import com.mcafee.mam.auto.infra.device.RogueDevice;
import com.mcafee.mam.auto.infra.drivers.epo.EPOClient;
import com.mcafee.mam.auto.infra.drivers.epo.EPOFile;

public class QC_0000_T1 extends TestClass
{
	@Override
	public void setup() throws TestException
	{
		initDrivers();
		ManagedDevice win7pro = getSutDevice("win7pro");
		RogueDevice win8 = getSutDevice("win2008");
		try
		{
			EPOClient.getRcExt().installExt((EPOFile) getSutObject("QaaDevtool"),false);
			//EPOClient.getRcExt().installExt((EPOFile) getSutObject("rsd"),true);
			EPOClient.getRcPackage().checkinPackage((EPOFile) getSutObject("pkg"));
			EPOClient.getRcAdt().wakeupAgent("WIN7PRO");
		    //win7pro.installSensor();
			//win7pro.wakeupAgent();
			//win7pro.isSensorup();
			//win7pro.addSensorToBlacklist();
			//win7pro.wakeupAgent();
			//win7pro.collectAndSendProps();
			//System.out.println(win7pro.isProcessRunning("balash.exe"));
			//System.out.println(win7pro.isServiceRunning("SensrSvc"));
			//win7pro.enforcePolicies();
			//win8.addToException();
			// EPOClient.getRcPackage().deleteRsdSensorPackage();
			// win7pro.powerOn();
			//win7pro.revertToMainSnap();
			//EPOClient.getRcPolicy().modifyPolicy("My Default 2");
			//EPOClient.getRcSettings().modifyCompliance();
			//EPOClient.getRcSettings().modifySensor();
			//win7pro.revertToSnap2(true, false);
			// win7pro.revertToSnap3(true, false);
			// win7pro.isSensorup();
		}
		catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void tearDown() throws Exception
	{
	}
}
