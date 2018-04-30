package com.mcafee.mam.auto.infra.drivers.epo;

import java.io.IOException;
import org.apache.log4j.Logger;


public class EpoRcRsd extends EpoRcDecorator
{
	private static Logger logger = Logger.getLogger(EpoRcRsd.class);

	public EpoRcRsd(EPOClient newEpoClient)
	{
		super(newEpoClient);
		// TODO Auto-generated constructor stub
	}

	
	public EPOResponse addToException(String deviceName) throws IOException
	{
		logger.info("addToException rogue device: " + deviceName);
		EPOCommand command = epoClient.getCommand("detectedsystem.markAsException");
		command.addStringParameter("UIDs", deviceName);
		return epoClient.invoke(command);
	}
}
