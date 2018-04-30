package com.mcafee.mam.auto.infra.log.html;

import java.io.File;

/**
 * 
 * @author guy
 */
public class ReportFile
{

	private static int reportCounter = 1;
	private Class<?> klass;
	private final String baseFolder;

	public ReportFile(Class<?> klass, String baseFolder)
	{
		count();
		this.klass = klass;
		this.baseFolder = baseFolder;
		File folder = new File(getFolderName());
		if (!folder.exists())
		{
			folder.mkdirs();
		}
	}

	public ReportFile(String baseFolder)
	{
		count();
		this.baseFolder = baseFolder;
		File folder = new File(getFolderName());
		if (!folder.exists())
		{
			folder.mkdirs();
		}
	}

	public String getTestName()
	{
		return this.klass.getSimpleName();
	}

	public String getARefLink()
	{
		return getRelativeName() + "/report.html";
	}

	private synchronized String getFolderName()
	{
		return this.baseFolder + File.separator + getRelativeName();

	}

	public File getFile()
	{
		return new File(getFolderName() + File.separator + "report.html");
	}

	public static File getCountFile(String prefix)
	{
		return new File(prefix + ++reportCounter + ".html");
	}

	private synchronized void count()
	{
		reportCounter++;
	}

	private String getRelativeName()
	{
		return this.klass.getCanonicalName() + String.format("_%d", reportCounter);
	}
}
