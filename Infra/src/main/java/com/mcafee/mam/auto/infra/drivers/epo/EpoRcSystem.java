package com.mcafee.mam.auto.infra.drivers.epo;

import java.io.IOException;
import com.mcafee.mam.auto.infra.TestException;

public final class EpoRcSystem extends EpoRcDecorator
{
	public EpoRcSystem(EPOClient newEpoClient)
	{
		super(newEpoClient);
		// TODO Auto-generated constructor stub
	}

	/**
	 * * Deletes one or more systems from the system-tree using 'system.delete' command.
	 * 
	 * @param systemNames
	 *            - list of systems names or ids, separated by whitespace.
	 * @return command response
	 * @throws IOException
	 * @throws TestException
	 */
	public EPOResponse deleteSystem(String systemNames) throws IOException
	{
		EPOCommand command = epoClient.getCommand("system.delete");
		command.addStringParameter("names", systemNames);
		return epoClient.invoke(command);
	}

	/**
	 * 
	 * @return
	 * @throws IOException
	 * @throws TestException
	 */
	public EPOResponse deleteAllDetectedSys() throws IOException
	{
		EPOCommand command = epoClient.getCommand("detectedsystem.deleteByAge");
		command.addStringParameter("age", "0");
		command.addStringParameter("unit", "d");
		return epoClient.invoke(command);
	}

	/**
	 * * get system information using the 'system.find' command.
	 * 
	 * @param systemName
	 *            - system name to find.
	 * @return system information response.
	 * @throws IOException
	 * @throws TestException
	 */
	public EPOResponse findSystem(String systemName) throws IOException
	{
		EPOCommand command = epoClient.getCommand("system.find");
		command.addStringParameter("searchText", systemName);
		return epoClient.invoke(command);
	}

}
