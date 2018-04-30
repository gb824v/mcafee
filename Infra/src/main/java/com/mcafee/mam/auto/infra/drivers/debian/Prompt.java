package com.mcafee.mam.auto.infra.drivers.debian;

/**
 * Holds an expected response from a server per command. A prompt is a server
 * communication per running a command. For instance: the command 'cd /tmp' will
 * yield the response 'danny@ehad:/tmp' and the command 'ls -l' will yield the
 * response drwxr-xr-x 4 danny danny 4096 Mar 10 14:37 img drwxr-xr-x 6 danny
 * danny 4096 Mar 11 12:11 info.bliki.wiki ... The prompt defines the expected
 * results while providing a regular expression on the received input. For
 * instance, the string to look for can be '#' which will mark the end of
 * command and the 'response' field will hold the entire response until '#' was
 * matched.
 * 
 * @author danny
 */
public class Prompt
{

	private String pattern;
	private String response = null;

	/**
	 * constructs a new pattern
	 * 
	 * @param pattern
	 *            - regular expression to match against response.
	 */
	public Prompt(String pattern)
	{
		this.pattern = pattern;
	}

	/***
	 * constructs a new pattern
	 * 
	 * @param pattern
	 *            - the regular expression used to match
	 * @param response
	 *            - the response that was received.
	 */
	public Prompt(String pattern, String response)
	{
		this.pattern = pattern;
		this.response = response;
	}

	public String getPattern()
	{
		return this.pattern;
	}

	public String getResponse()
	{
		return this.response;
	}
}
