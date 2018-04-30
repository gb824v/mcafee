package com.mcafee.mam.auto.infra.drivers.epo;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.log4j.Logger;

import com.google.common.io.ByteStreams;

public abstract class BuildServerBase extends EpoRcDecorator
{
	public BuildServerBase(EPOClient newEpoClient)
	{
		super(newEpoClient);
	}

	private static Logger logger = Logger.getLogger(EpoRcExtension.class);
	private final String jobRSD = "RSD%20ePO%20Extension%205.0";
	private final String jobPkg = "Sensor_500";
	protected final String lastSuccessful = "lastSuccessfulBuild/artifact";
	protected final String specificPkgZipVer = "artifact/Winstaller/CreatePackage/output/RSDSensorPackage_";
	protected final String specificExtZipVer = "artifact/target/package/plugin/RSD_Extension_";
	public String getPkgBuildlLink(String build, String version)
	{
		if (version != null && !version.isEmpty())
		{
			return String.format("view/All/job/%s/%s/%s%s.zip", jobPkg, build, specificPkgZipVer, version);
		}

		else
		{
			return String.format("job/%s/%s/*zip*/archive.zip", jobPkg, lastSuccessful);
		}
	}

	public String getExtBuildlLink(String build, String version)
	{
		if (version != null && !version.isEmpty())
		{
			return String.format("job/%s/%s/%s%s.zip", jobRSD, build, specificExtZipVer, version);
		}

		else
		{
			return String.format("job/%s/%s/*zip*/archive.zip", jobRSD, lastSuccessful);
		}
	}

	protected byte[] getByteArrayFromInputStream(InputStream in) throws Exception
	{
		return getByteArrayFromInputStream(in, "rsd");
	}

	/**
	 * Get InputStream return byte[].
	 * 
	 * @param in
	 * @return
	 * @throws Exception
	 */
	protected byte[] getByteArrayFromInputStream(InputStream in, String prefix) throws Exception
	{
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		byte[] b = null;

		try (ZipInputStream zis = new ZipInputStream(in))
		{
			ZipEntry ze;
			while ((ze = zis.getNextEntry()) != null)
			{
				if (ze.getName().toLowerCase().contains(prefix) && ze.getName().endsWith("zip"))
				{
					ByteStreams.copy(zis, baos);
					baos.flush();
					b = baos.toByteArray();
					break;
				}
			}
		}
		catch (Exception e)
		{
			throw new Exception("The " + prefix + " zip file is corrapted", e);
		}
		if (b == null) { throw new IllegalStateException("Cannot find " + prefix + " zip file"); }
		return b;
	}

	/**
	 * Get Extension name from xml properties file.
	 * 
	 * @param extBytes
	 * @return
	 * @throws IllegalStateException
	 * @throws IllegalArgumentException
	 * @throws IOException
	 */
	protected String getExtensionName(byte[] extBytes) throws IllegalStateException, IllegalArgumentException, IOException
	{
		byte[] propertiesBytes = readFileFromExtention(extBytes, "extension.properties");
		Properties p = new Properties();
		p.load(new ByteArrayInputStream(propertiesBytes));
		String extName = p.getProperty("extension.name");
		if (extName == null) { throw new IllegalArgumentException("Cannot find extension name in extension.properties!"); }
		return extName;
	}

	/**
	 * Get Extension version from xml properties file.
	 * 
	 * @param extBytes
	 * @return
	 * @throws IllegalStateException
	 * @throws IllegalArgumentException
	 * @throws IOException
	 */
	protected String getExtensionVersion(byte[] extBytes) throws IllegalStateException, IllegalArgumentException, IOException
	{
		byte[] propertiesBytes = readFileFromExtention(extBytes, "extension.properties");
		Properties p = new Properties();
		p.load(new ByteArrayInputStream(propertiesBytes));
		String version = p.getProperty("extension.version");
		return version;
	}

	/**
	 * 
	 * @param extLocation
	 * @return
	 * @throws IOException
	 */
	protected byte[] getExtensionBytes(String extLocation) throws IOException
	{
		File extFile = new File(extLocation);
		logger.info(String.format("Reading extension from: %s", extFile));
		if (!extFile.isFile()) { throw new IOException(String.format("Cannot find file with extension: %s", extLocation)); }
		byte[] extBytes = Files.readAllBytes(extFile.toPath());
		return extBytes;
	}

	/**
	 * Read specific file from byte[] represent zip file.
	 * 
	 * @param extBytes
	 * @param fileName
	 * @return
	 * @throws IOException
	 */
	protected byte[] readFileFromExtention(byte[] extBytes, String fileName) throws IOException
	{
		ZipInputStream zis = null;
		try
		{
			zis = new ZipInputStream(new ByteArrayInputStream(extBytes));
			ZipEntry zp = null;
			while ((zp = zis.getNextEntry()) != null)
			{
				if (zp.getName().equals(fileName))
				{
					break;
				}
			}
			if (zp == null) { throw new IllegalStateException("Cannot find extension properties within zip file"); }
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ByteStreams.copy(zis, baos);
			return baos.toByteArray();
		}
		finally
		{
			close(zis);
		}
	}

	protected void close(Closeable closeable)
	{
		if (closeable != null)
		{
			try
			{
				closeable.close();
			}
			catch (IOException e)
			{
				// nothing
			}
		}
	}
}
