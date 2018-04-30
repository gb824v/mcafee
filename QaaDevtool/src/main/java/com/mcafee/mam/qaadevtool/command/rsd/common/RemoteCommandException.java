/**
 * Girish on 29-Oct-2013
 */
package com.mcafee.mam.qaadevtool.command.rsd.common;

/**
 * @author Girish
 * 
 */
public class RemoteCommandException extends RuntimeException
{

	private static final long serialVersionUID = 1L;

	public RemoteCommandException(String message, Throwable cause)
	{
		super(message, cause);
	}

	public RemoteCommandException(String message)
	{
		super(message);
	}

	public RemoteCommandException(Throwable cause)
	{
		super(cause);
	}

}
