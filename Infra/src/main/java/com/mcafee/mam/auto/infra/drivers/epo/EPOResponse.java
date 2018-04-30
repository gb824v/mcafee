package com.mcafee.mam.auto.infra.drivers.epo;

import com.mcafee.mam.auto.infra.Response;
import com.mcafee.mam.auto.infra.TestException;

/**
 * Represents a response from the EPO server
 * 
 * @author danny
 */
public class EPOResponse extends Response
{

	/***
	 * constructs an empty response
	 */
	public EPOResponse()
	{
	}

	/***
	 * constructs a response from string received from server
	 * 
	 * @param response
	 */
	public EPOResponse(String response)
	{
		this.response.append(response);
	}

	/***
	 * verify extension exists from extension list response.
	 * 
	 * @param extensionName
	 *            - extension name to look for
	 * @param version
	 *            - extension version.
	 * @param status
	 *            - extension status
	 * @throws TestException
	 */
	public void verifyExtension(String extensionName, String version, String status) throws TestException
	{
		verify(String.format("%s\\s%s\\s%s", extensionName, version, status));
	}

	/***
	 * verify extension exists from extension list response.
	 * 
	 * @param extensionName
	 *            - extension name to look for
	 * @param status
	 *            - extension status
	 * @throws TestException
	 */
	public void verifyExtension(String extensionName, String status) throws TestException
	{
		verify(String.format("%s*.*%s", extensionName, status));
	}

	/**
	 * verify extension installed from extension list response.
	 * 
	 * @param extensionName
	 *            - extension name to look for
	 * @param version
	 *            - extension version to look for
	 * @throws TestException
	 *             - if extension is not installed.
	 */
	public void verifyExtensionInstalled(String extensionName, String version) throws TestException
	{
		verifyExtension(extensionName, version, "installed");
	}

	/**
	 * verify extension installed from extension list response.
	 * 
	 * @param extensionName
	 *            - extension name to look for
	 * @throws TestException
	 *             - if extension is not installed.
	 */
	public void verifyExtensionInstalled(String extensionName) throws TestException
	{
		verifyExtension(extensionName, "installed");
	}

	/**
	 * 
	 * @param extensionName
	 * @return
	 * @throws TestException
	 */
	public boolean isExtensionInstalled(String extensionName) throws TestException
	{
		return matches(String.format("%s *.*%s", extensionName, "installed"));
	}

	/**
	 * verify string in line. response is parsed by '\r\n'
	 * 
	 * @param index
	 *            - line number
	 * @param expected
	 *            - string to look for
	 * @throws TestException
	 *             - if expected string not equals the response at that line
	 */
	public void verifyLine(int index, String expected) throws TestException
	{
		String[] lines = response.toString().split("\r\n");
		if (lines.length < index) { throw new TestException(String.format("Line %d not found in response (total %d lines)", index, lines.length)); }
		String found = lines[index];
		if (!found.equals(expected)) { throw new TestException(String.format("Found '%s'. Expected: '%s' in line %d", found, expected, index)); }
	}

	/***
	 * verify a system is managed for a system.find response.
	 * 
	 * @throws TestException
	 *             - if system state is not managed.
	 */
	public void verifySystemManaged() throws TestException
	{
		verify("Managed State: managed");
	}

	/**
	 * creates a new EPO query response from the response contents.
	 * 
	 * @return
	 * @throws TestException
	 */
	public EPOQueryResponse parseAsQueryResponse() throws TestException
	{
		return new EPOQueryResponse(toString());
	}
}
