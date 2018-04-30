package com.mcafee.mam.auto.infra.log.html;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetAddress;
import java.util.Date;

import au.com.bytecode.opencsv.CSVWriter;

public class CsvReport
{

	private CSVWriter writer = null;
	private static CsvReport instance = null;
	private File file = null;

	protected CsvReport(String logFolder)
	{
		try
		{
			this.file = new File(logFolder + File.separator + "mamResult.csv");
			createFile();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

	}

	private void createFile() throws IOException
	{
		writer = new au.com.bytecode.opencsv.CSVWriter(new FileWriter(this.file), ',');
		String[] entries = "ProductName,TestID,TestName,TestStatus,OperSystem,MachineName,Date".split(",");
		writer.writeNext(entries);
	}

	private void addLine(int testID, String testName, String status) throws IOException
	{
		String[] entries = new String[7];
		entries[0] = "MAM";
		entries[1] = Integer.toString(testID);
		entries[2] = testName;
		entries[3] = status;
		entries[4] = System.getProperty("os.name").toLowerCase();
		entries[5] = InetAddress.getLocalHost().getHostName();
		entries[6] = new Date(System.currentTimeMillis()).toString();
		writeLine(entries);

	}

	private void writeLine(String[] entries) throws IOException
	{
		writer.writeNext(entries);
	}

	public static void report(String logfolder, int testID, String testName, String status)
	{
		if (instance == null)
		{
			instance = new CsvReport(logfolder);
		}
		try
		{
			instance.addLine(testID, testName, status);
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void close()
	{
		try
		{
			if (instance != null)
			{
				instance.writer.close();
			}
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
