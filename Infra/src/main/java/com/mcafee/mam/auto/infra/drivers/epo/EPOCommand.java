package com.mcafee.mam.auto.infra.drivers.epo;

import com.mcafee.orion.remote.client.BinaryParam;
import com.mcafee.orion.remote.client.CommandUtil;
import com.mcafee.orion.remote.client.StringParam;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Represents EPO command that can be executed via the orion.remote client.
 * 
 * @see com.mcafee.orion.remote.client.CommandClient
 * @author Guy
 */
public class EPOCommand
{

	/**
	 * defines the EPO response output format.
	 */
	public enum OutputFormat
	{
		TERSE, VERBOSE, XML, JSON
	}

	private String user = "";
	private String password = "";
	private String host = "";
	private List<StringParam> stringParams = new LinkedList<StringParam>();
	private List<BinaryParam> binParams = new LinkedList<BinaryParam>();
	private String name;

	/***
	 * constructs a new command with a given name.
	 * 
	 * @param name
	 */
	public EPOCommand(String name)
	{
		this.name = name;
	}

	/***
	 * formats command name and parameters to string
	 * 
	 * @return
	 */
	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder(this.name);
		Iterator<StringParam> iter = this.stringParams.iterator();
		while (iter.hasNext())
		{
			StringParam param = iter.next();
			sb.append(" ").append(param.getName()).append("=").append(param.getValue());
		}
		sb.append(" ");
		Iterator<BinaryParam> iter1 = this.binParams.iterator();
		while (iter1.hasNext())
		{
			BinaryParam binParam = iter1.next();
			sb.append(" ");
			sb.append(binParam.getName()).append("(");
			sb.append(binParam.getValue().length).append(" bytes)");
		}
		return sb.toString();
	}

	/***
	 * invoke the command and return string containing invocation results.
	 * 
	 * @return
	 * @throws IOException
	 *             - if cannot execute or command had failed.
	 */
	public String invoke() throws IOException
	{
		return CommandUtil.postCommand(getHost(), getUser(), getPassword(), getName(), "https", stringParams, binParams);
	}

	/***
	 * adds a string parameter to the command
	 * 
	 * @param name
	 *            - parameter name
	 * @param value
	 *            - parameter value.
	 */
	public void addStringParameter(String name, String value)
	{
		this.stringParams.add(new StringParam(name, value));
	}

	/***
	 * adds string or file parameter
	 * 
	 * @param name
	 *            - parameter name
	 * @param value
	 *            - parameter value. if starts with 'file://' a file will be loaded from URL.
	 * @throws Exception
	 */
	public void addParameter(String name, String value) throws Exception
	{
		if (value.startsWith("file://"))
		{
			addFileParameter(name, value);
		}
		else
		{
			addStringParameter(name, value);
		}
	}

	/***
	 * adds file parameter
	 * 
	 * @param name
	 *            - parameter name
	 * @param filaName
	 *            - location of file on disk.
	 * @throws URISyntaxException
	 * @throws IOException
	 */
	public void addFileParameter(String name, String filaName) throws URISyntaxException, IOException
	{
		URI uri = new URI(filaName);
		File file = null;
		try
		{
			file = new File(uri);
		}
		catch (IllegalArgumentException localIllegalArgumentException)
		{
			throw new IOException("Illegal Argument Exception Caught Creating File Based on URI: \r\n" + localIllegalArgumentException.getLocalizedMessage());
		}
		addFileParameter(filaName, file);
	}

	/**
	 * adds file parameter
	 * 
	 * @param name
	 *            - parameter name
	 * @param file
	 *            - file to add
	 * @throws IOException
	 */
	public void addFileParameter(String name, File file) throws IOException
	{
		this.binParams.add(new BinaryParam(name, loadAsBuffer(file)));
	}
	/**
	 * adds file parameter
	 * 
	 * @param name
	 *            - parameter name
	 * @param is
	 *            - InputStream to add
	 * @throws IOException
	 */
	public void addFileParameter(String name,InputStream is) throws IOException
	{
		this.binParams.add(new BinaryParam(name, loadAsBuffer(is)));
	}
	/**
	 * adds file parameter
	 * 
	 * @param name
	 *            - parameter name
	 * @param file
	 *            - file to add
	 * @throws IOException
	 */
	public void addFileParameter(String name, StringBuffer cmd) throws IOException
	{
		this.binParams.add(new BinaryParam(name, cmd.toString().getBytes()));
	}

	private static byte[] loadAsBuffer(File paramFile) throws IOException
	{
		FileInputStream localFileInputStream = null;
		byte[] arrayOfByte;
		try
		{
			localFileInputStream = new FileInputStream(paramFile);
			arrayOfByte = loadAsBuffer(localFileInputStream);
		}
		finally
		{
			if (localFileInputStream != null)
			{
				localFileInputStream.close();
			}
		}
		return arrayOfByte;
	}
	private static byte[] loadAsBuffer(InputStream paramInputStream) throws IOException
	{
		ByteArrayOutputStream localByteArrayOutputStream = new ByteArrayOutputStream();
		byte[] arrayOfByte = new byte[1024];
		int i;
		while ((i = paramInputStream.read(arrayOfByte)) != -1)
		{
			localByteArrayOutputStream.write(arrayOfByte, 0, i);
		}

		return localByteArrayOutputStream.toByteArray();
	}

	/**
	 * Sets the response output format.
	 * 
	 * @param format
	 */
	public void addOutputFormat(OutputFormat format)
	{
		this.addStringParameter(":output", format.toString().toLowerCase());
	}

	public String getUser()
	{
		return user;
	}

	public void setUser(String user)
	{
		this.user = user;
	}

	/**
	 * @return the password
	 */
	public String getPassword()
	{
		return password;
	}

	/**
	 * @param password
	 *            the password to set
	 */
	public void setPassword(String password)
	{
		this.password = password;
	}

	/**
	 * @return the host
	 */
	public String getHost()
	{
		return host;
	}

	/**
	 * @param host
	 *            the host to set
	 */
	public void setHost(String host)
	{
		this.host = host;
	}

	/**
	 * @return the name
	 */
	public String getName()
	{
		return name;
	}

	/**
	 * @param name
	 *            the name to set
	 */
	public void setName(String name)
	{
		this.name = name;
	}
}
