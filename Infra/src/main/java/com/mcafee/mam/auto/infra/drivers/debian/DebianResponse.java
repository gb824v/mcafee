package com.mcafee.mam.auto.infra.drivers.debian;

import com.mcafee.mam.auto.infra.Record;
import com.mcafee.mam.auto.infra.Response;
import com.mcafee.mam.auto.infra.TestException;

/**
 * Represents an EPO query response. EPO queries are consisted of records of name value pairs.
 * 
 * @author guy
 */
public class DebianResponse extends Response
{
	public DebianResponse(String response) throws TestException
	{
		this.response = new StringBuffer();
		
		if (!response.isEmpty())
		{
			this.appendResponse(response);
		}
		else
		{
			this.clearResponse();
		}
	}

	/**
	 * constructs a new EPO query response from a given server response.
	 * 
	 * @param response
	 * @throws TestException
	 */
	public DebianResponse(String response, String format) throws TestException
	{
		if (!response.isEmpty())
		{
			this.clearResponse();
			this.appendResponse(response);
			
			String[] lines = response.split("\n");

			if (format.equals("csv"))
			{
				addCsvRecords(lines);
			}
			if (format.equals("colon"))
			{
				addCountersRecords(lines, ":");
			}
			if (format.equals("equal"))
			{
				addCountersRecords(lines, "=");
			}

		}
	}

	public void addCountersRecords(String[] lines, String spliter) throws TestException
	{
		Record record = new Record();
		for (String line : lines)
		{
			if (line.contains(spliter))
			{
				String params[] = line.split(spliter);
				if (params.length == 2)
				{
					String name = line.split(spliter)[0].trim();
					String value = line.split(spliter)[1].trim();
					record.add(name, value);
				}
			}
		}
		this.records.add(record);
	}

	public void addCsvRecords(String[] lines) throws TestException
	{
		String[] cols = null;
		Record record = null;
		for (String line : lines)
		{
			if (line.indexOf(',') >= 0)
			{
				cols = line.split(",");
				break;
			}
		}
		for (String line : lines)
		{
			record = new Record();
			if (line.startsWith("\""))
			{
				int ind = 0;
				String attrs[] = line.split(",");
				for (String attr : attrs)
				{
					record.add(cols[ind++], attr);
				}
				if (record != null) this.records.add(record);
			}

		}
	}
}
