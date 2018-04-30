package com.mcafee.mam.auto.infra.drivers.epo;

import java.io.IOException;

import org.apache.log4j.Logger;

import com.mcafee.mam.auto.infra.TestException;


public class EpoRcQueriesAndReports extends EpoRcDecorator
{
	private static Logger logger = Logger.getLogger(EpoRcQueriesAndReports.class);

	public EpoRcQueriesAndReports(EPOClient newEpoClient)
	{
		super(newEpoClient);
		// TODO Auto-generated constructor stub
	}

	/**
	 * * list all EPO queries via the 'core.listQueries' command.
	 * 
	 * @return list of queries response.
	 * @throws IOException
	 * @throws TestException
	 */
	public EPOResponse listQueries() throws IOException
	{
		EPOCommand command = epoClient.getCommand("core.listQueries");
		return epoClient.invoke(command);
	}

	/**
	 * * get system information using the 'system.find' command.
	 * 
	 * @return rsd system information response.
	 * @throws IOException
	 * @throws TestException
	 */
	public EPOResponse rsdDetectedSystems() throws IOException
	{
		EPOCommand command = epoClient.getCommand("core.executeQuery");
		command.addStringParameter("target", "RSDDetectedSystems");
		return epoClient.invoke(command);
	}

	/**
	 * * get system information using the 'system.find' command.
	 * 
	 * @return rsd Interfaces information response.
	 * @throws IOException
	 * @throws TestException
	 */
	public EPOResponse getRsdInterfaces() throws IOException
	{
		EPOCommand command = epoClient.getCommand("core.executeQuery");
		command.addStringParameter("target", "RSDInterfaces");
		return epoClient.invoke(command);
	}

	public EPOQueryResponse execQueryByName(String queryName) throws IOException,TestException
	{
		logger.info(String.format("Executing '%s' Query", queryName));
		String queryID = listQueries().parseAsQueryResponse().findRecord("Name", queryName).get("Id");
		EPOResponse res = execQueryById(queryID);
		return res.parseAsQueryResponse();
	}

	/**
	 * executes a query via the 'core.executeQuery' command
	 * 
	 * @param queryID
	 *            - query id to execute.
	 * @return epo query response
	 * @throws IOException
	 * @throws TestException
	 */
	public EPOResponse execQueryById(String queryID) throws IOException
	{
		EPOCommand command = epoClient.getCommand("core.executeQuery");
		command.addStringParameter("queryId", queryID);
		return epoClient.invoke(command);
	}
}
