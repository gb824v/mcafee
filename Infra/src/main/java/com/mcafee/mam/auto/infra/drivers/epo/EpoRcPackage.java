package com.mcafee.mam.auto.infra.drivers.epo;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.apache.log4j.Logger;

import com.mcafee.mam.auto.infra.Record;
import com.mcafee.mam.auto.infra.TestException;
import com.mcafee.mam.auto.infra.build.JenkinsClient;

public final class EpoRcPackage extends BuildServerBase
{
	private static Logger logger = Logger.getLogger(EpoRcPackage.class);

	public EpoRcPackage(EPOClient newEpoClient)
	{
		super(newEpoClient);
	}

	/**
	 * Check EPOFile if it contain host + port its mean install from build server else from
	 * resources folder.
	 * 
	 * @param epoPackage
	 * @throws Exception
	 */
	public EPOResponse checkinPackage(EPOFile epoFile) throws Exception
	{
		if (epoFile.getHost().isEmpty())
		{
			return checkinPackageFromResources(epoFile);
		}
		else
		{
			return checkinPackageFromBuildServer(epoFile);
		}
	}

	/**
	 * Install extenuation from build server (jenkins).
	 * 
	 * @param epoPackage
	 * @param isForce
	 * @throws Exception
	 */
	private EPOResponse checkinPackageFromBuildServer(EPOFile epoPkg) throws Exception
	{
		JenkinsClient jenkinsClient = new JenkinsClient(epoPkg.getHost(), epoPkg.getPort());
		byte[] b = getByteArrayFromInputStream(jenkinsClient.executeHttpGet(getPkgBuildlLink(epoPkg.getBuild(), epoPkg.getVersion())));
		try (ByteArrayInputStream is = new ByteArrayInputStream(b))
		{
			logger.info(String.format("Installing package: %s", epoPkg.getName()));
			return checkInPackage(is);
		}
	}

	/**
	 * Installs an extension from extension file via the 'ext.install' command.
	 * 
	 * @param extFile
	 *            - Extension file to install.
	 * @return command response
	 * @throws Exception
	 * @throws TestException
	 */
	protected EPOResponse checkInPackage(InputStream pkgStream) throws Exception
	{
		EPOCommand command = epoClient.getCommand("repository.checkInPackage");
		command.addFileParameter("file", pkgStream);
		command.addStringParameter("branch", "current");
		command.addStringParameter("force", "true");
		return epoClient.invoke(command);
	}

	/**
	 * Install extension by giving EPOFile object.
	 * 
	 * @param epoPackage
	 * @throws Exception
	 */
	private EPOResponse checkinPackageFromResources(EPOFile epoPackage) throws Exception
	{
		logger.info("Installing package: " + epoPackage.getName());
		try (InputStream is = epoPackage.getFileAsStream())
		{
			return checkInPackage(is);
		}
		catch (Exception e)
		{
			throw new Exception("Installing package: '" + epoPackage + "' failed, (" + e.getMessage() + ").", e);
		}
	}

	/**
	 * Install extension by giving EPOFile object.
	 * 
	 * @param epoPackage
	 * @throws Exception
	 */
	public boolean deleteRsdSensorPackage() throws Exception
	{
		String pkgName = "Rogue System Sensor";
		logger.info("deleting package: 'Rogue System Sensor'");
		Record rsd = findPackages(pkgName).parseAsQueryResponse().findRecord("productName", "Rogue System Sensor");
		if (rsd == null) { return false; }
		String id = rsd.get("productID");
		String branch = rsd.get("packageBranch");
		String type = rsd.get("packageType");
		deletePackage(id, type, branch);
		return true;
	}

	/**
	 * 
	 * @param pkgName
	 * @return
	 * @throws Exception
	 */
	private EPOResponse deletePackage(String productId, String packageType, String packageBranch) throws Exception
	{
		EPOCommand command = epoClient.getCommand("repository.deletePackage");
		command.addStringParameter("productId", productId);
		command.addStringParameter("packageType", packageType);
		command.addStringParameter("branch", packageBranch);
		return epoClient.invoke(command);
	}

	/**
	 * 
	 * @param pkgName
	 * @return
	 * @throws IOException
	 */
	public EPOResponse findPackages(String pkgName) throws IOException
	{
		EPOCommand command = epoClient.getCommand("repository.findPackages");
		command.addStringParameter("searchText", pkgName);
		return epoClient.invoke(command);
	}
}
