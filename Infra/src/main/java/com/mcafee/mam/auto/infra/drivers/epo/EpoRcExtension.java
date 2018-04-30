package com.mcafee.mam.auto.infra.drivers.epo;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import org.apache.log4j.Logger;
import com.mcafee.mam.auto.infra.TestObject;
import com.mcafee.mam.auto.infra.build.JenkinsClient;

public final class EpoRcExtension extends BuildServerBase
{
	private static Logger logger = Logger.getLogger(EpoRcExtension.class);

	public EpoRcExtension(EPOClient newEpoClient)
	{
		super(newEpoClient);
	}

	/**
	 * 
	 * @param epoExt
	 * @throws Exception
	 */
	public void installExt(EPOFile epoExt) throws Exception
	{
		installExt(epoExt, true);
	}

	/**
	 * Check EPOFile if it contain host + port its mean install from build server else from
	 * resources folder.
	 * 
	 * @param epoExt
	 * @param isForce
	 * @throws Exception
	 */
	public void installExt(EPOFile epoExt, boolean isForce) throws Exception
	{
		if (epoExt.getHost().isEmpty())
		{
			installExtFromResources(epoExt, isForce);
		}
		else
		{
			installExtFromBuildServer(epoExt, isForce);
		}
	}

	/**
	 * Check EPOFile if it contain host + port its mean upgrade from build server else from
	 * resources folder.
	 * 
	 * @param epoExt
	 * @param isForce
	 * @throws Exception
	 */
	public void upgradeExt(EPOFile epoExt) throws Exception
	{
		if (epoExt.getHost().isEmpty())
		{
			upgradeExtension(epoExt);
		}
		else
		{
			upgradeExtFromBuildServer(epoExt);
		}
	}

	/**
	 * install extenuation from resources folder.
	 * 
	 * @param epoExt
	 * @param isForce
	 * @throws Exception
	 */
	private void installExtFromResources(EPOFile epoExt, boolean isForce) throws Exception
	{
		boolean isExsist = listExtensions().isExtensionInstalled(epoExt.getName());
		if (isExsist)
		{
			if (isForce)
			{
				logger.info("Unstalling extention: " + epoExt.getName());
				try (InputStream is = epoExt.getFileAsStream())
				{
					uninstallExtension(epoExt.getName());
					TestObject.sleepSec(3);
					installExtension(epoExt);
				}
				catch (Exception e)
				{
					throw new Exception("Uninstalling extention: '" + epoExt.getName() + "' failed, (" + e.getMessage() + ").", e);
				}
			}
		}
		else
		{
			installExtension(epoExt);
		}
	}

	/**
	 * Upgrade extenuation from build server (jenkins).
	 * 
	 * @param epoExt
	 * @param isForce
	 * @throws Exception
	 */
	private void upgradeExtFromBuildServer(EPOFile epoExt) throws Exception
	{
		JenkinsClient jenkinsClient = new JenkinsClient(epoExt.getHost(), epoExt.getPort());
		boolean isExsist = listExtensions().isExtensionInstalled(epoExt.getName());
		if (isExsist)
		{
			byte[] b = getByteArrayFromInputStream(jenkinsClient.executeHttpGet(getExtBuildlLink(epoExt.getBuild(),epoExt.getVersion())));
			String extName = getExtensionName(b);
			String extVersion = getExtensionVersion(b);

			try (ByteArrayInputStream is = new ByteArrayInputStream(b))
			{
				logger.info(String.format("Upgrading extention: %s , version: %s", extName, extVersion));
				upgradeExtension(is);
			}
			catch (Exception e)
			{
				throw new Exception(String.format("Upgrade extention: %s, version: %s failed", extName, extVersion), e);
			}
		}
	}

	/**
	 * Install extenuation from build server (jenkins).
	 * 
	 * @param epoExt
	 * @param isForce
	 * @throws Exception
	 */
	private void installExtFromBuildServer(EPOFile epoExt, boolean isForce) throws Exception
	{
		JenkinsClient jenkinsClient = new JenkinsClient(epoExt.getHost(), epoExt.getPort());
		boolean isExsist = listExtensions().isExtensionInstalled(epoExt.getName());
		if (isExsist)
		{
			if (isForce)
			{
				byte[] b = getByteArrayFromInputStream(jenkinsClient.executeHttpGet(getExtBuildlLink(epoExt.getBuild(),epoExt.getVersion())));
				String extName = getExtensionName(b);
				String extVersion = getExtensionVersion(b);
				logger.info(String.format("Unstalling extention: %S", extName));
				uninstallExtension(extName);
				TestObject.sleepSec(3);
				try (ByteArrayInputStream is = new ByteArrayInputStream(b))
				{
					logger.info(String.format("Installing extention: %s , version: %s", extName, extVersion));
					installExtension(is);
				}
				catch (Exception e)
				{
					throw new Exception(String.format("Installing extention: %s, version: %s failed", extName, extVersion), e);
				}
			}
		}
		else
		{
			byte[] b = getByteArrayFromInputStream(jenkinsClient.executeHttpGet(getExtBuildlLink(epoExt.getBuild(),epoExt.getVersion())));
			String extName = getExtensionName(b);
			String extVersion = getExtensionVersion(b);
			try (ByteArrayInputStream is = new ByteArrayInputStream(b))
			{
				logger.info(String.format("Installing extention: %s , version: %s", extName, extVersion));
				installExtension(is);
			}
		}
	}

	/**
	 * Remove an extension installation from EPO using 'ext.uninstall' command.
	 * 
	 * @param extentionName
	 *            - extension to remove.
	 * @return command response
	 * @throws IOException
	 * @throws TestException
	 */
	public EPOResponse uninstallExtension(String name) throws IOException
	{
		EPOCommand command = epoClient.getCommand("ext.uninstall");
		command.addStringParameter("UID", name);
		return epoClient.invoke(command);
	}

	/**
	 * List all installed extensions via the 'ext.list' command.
	 * 
	 * @return list of extensions.
	 * @throws IOException
	 * @throws TestException
	 */
	public EPOResponse listExtensions() throws IOException
	{
		EPOCommand command = epoClient.getCommand("ext.list");
		return epoClient.invoke(command);
	}

	/**
	 * Installs an extension from extension file via the 'ext.install' command.
	 * 
	 * @param extFile
	 *            - Extension file to install.
	 * @return command response
	 * @throws IOException
	 * @throws TestException
	 */
	protected EPOResponse installExtension(File extFile) throws IOException
	{
		EPOCommand command = epoClient.getCommand("ext.install");
		command.addFileParameter("extension", extFile);
		return epoClient.invoke(command);
	}

	/**
	 * Installs an extension from extension file via the 'ext.install' command.
	 * 
	 * @param extFile
	 *            - Extension file to install.
	 * @return command response
	 * @throws IOException
	 * @throws TestException
	 */
	protected EPOResponse installExtension(InputStream extFile) throws IOException
	{
		EPOCommand command = epoClient.getCommand("ext.install");
		command.addFileParameter("extension", extFile);
		return epoClient.invoke(command);
	}

	/**
	 * Upgrading an extension from extension file via the 'ext.install' command.
	 * 
	 * @param extFile
	 *            - Extension file to upgrade.
	 * @return command response
	 * @throws IOException
	 * @throws TestException
	 */
	protected EPOResponse upgradeExtension(InputStream extFile) throws IOException
	{
		EPOCommand command = epoClient.getCommand("ext.upgrade");
		command.addFileParameter("extension", extFile);
		return epoClient.invoke(command);
	}

	/**
	 * Upgrade extension by giving EPOFile object.
	 * 
	 * @param epoExt
	 * @throws Exception
	 */
	private void upgradeExtension(EPOFile epoExt) throws Exception
	{
		logger.info("Upgrading extention: " + epoExt.getName());
		try (InputStream is = epoExt.getFileAsStream())
		{
			upgradeExtension(is);
			TestObject.sleepSec(2);
			listExtensions().verifyExtensionInstalled(epoExt.getName());
		}
		catch (Exception e)
		{
			logger.error("Installing extention: '" + epoExt + "' failed, (" + e.getMessage() + ").", e);
		}
	}

	/**
	 * Install extension by giving EPOFile object.
	 * 
	 * @param epoExt
	 * @throws Exception
	 */
	private void installExtension(EPOFile epoExt) throws Exception
	{
		logger.info("Installing extention: " + epoExt.getName());
		try (InputStream is = epoExt.getFileAsStream())
		{
			installExtension(is);
			TestObject.sleepSec(2);
			listExtensions().verifyExtensionInstalled(epoExt.getName());
		}
		catch (Exception e)
		{
			throw new Exception("Installing extention: '" + epoExt + "' failed, (" + e.getMessage() + ").", e);
		}
	}
}
