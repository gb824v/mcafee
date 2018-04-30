package com.mcafee.mam.auto.infra.drivers.epo;

import com.mcafee.mam.auto.infra.TestObject;
import java.io.IOException;
import java.io.InputStream;

/**
 * A test object to specify MAM epo extension.
 * 
 * @author Guy
 */
public class EPOFile extends TestObject
{
	private String path;
	private String host = "";
	private String port = "";
	private String version = "";
	private String build = "";

	/**
	 * @return the location on disk for the extension file.
	 */
	public String getPath()
	{
		return path;
	}

	/**
	 * @param path
	 *            the location on disk for the extension file.
	 */
	public void setPath(String path)
	{
		this.path = path;
	}

	/**
	 * loads extension from path and return as file.
	 * 
	 * @return
	 * @throws IOException
	 */
	public InputStream getFileAsStream() throws IOException
	{
		return this.getClass().getClassLoader().getResourceAsStream(path);
	}

	public String getVersion()
	{
		return version;
	}

	public void setVersion(String version)
	{
		this.version = version;
	}

	public String getHost()
	{
		return host;
	}

	public void setHost(String host)
	{
		this.host = host;
	}

	public String getPort()
	{
		return port;
	}

	public void setPort(String port)
	{
		this.port = port;
	}

	public String getBuild()
	{
		return build;
	}

	public void setBuild(String build)
	{
		this.build = build;
	}

}
