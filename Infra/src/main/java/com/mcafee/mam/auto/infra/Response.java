package com.mcafee.mam.auto.infra;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.mcafee.mam.auto.infra.Record;

/**
 * 
 * @author Guy
 */
public abstract class Response
{
	private static Logger logger = Logger.getLogger(Response.class);
	protected List<Record> records = new ArrayList<Record>();
	protected StringBuffer response = new StringBuffer();
	public static Map<String, Pattern> compiledPatterns = Collections.synchronizedMap(new HashMap<String, Pattern>());

	/**
	 * returns the number of records in query
	 * 
	 * @return
	 */
	public int recordsCount()
	{
		return this.records.size();
	}

	/**
	 * find a record with specific name and value
	 * 
	 * @param name
	 *            record name.
	 * @param value
	 *            value in record
	 * @return null if no such record can be found.
	 */
	public Record findRecord(String name, String value)
	{
		for (Record record : this.records)
		{
			String val = record.get(name);
			if (val != null)
			{
				if (value.equals(val)) { return record; }
			}
		}
		return null;
	}

	/**
	 * find a record with specific name and value
	 * 
	 * @param name
	 *            record name.
	 * @return null if no such record can be found.
	 */
	public Record findRecord(String name)
	{
		for (Record record : this.records)
		{
			String val = record.get(name);
			if (val != null) { return record; }
		}
		return null;
	}

	/**
	 * find a record with specific name and value
	 * 
	 * @param name
	 *            record value.
	 * @return null if no such record can be found.
	 */
	public String getRecordValue(String name)
	{
		for (Record record : this.records)
		{
			String val = record.get(name);
			if (val != null) { return record.get(name); }
		}
		return null;
	}

	public List<Record> getRecords()
	{
		return records;
	}

	@Override
	public String toString()
	{
		return this.response.toString();
	}

	/**
	 * sets response received from the driver.
	 * 
	 * @param response
	 */
	public void appendResponse(String response)
	{
		this.response.append(response);
	}

	public void clearResponse()
	{
		this.response = new StringBuffer();
	}

	/***
	 * returns true of response matches regular expression.
	 * 
	 * @param regEx
	 * @return
	 */
	public boolean matches(String regEx)
	{
		if (this.response == null)
		{
			if (regEx != null) { return false; }
		}
		return getPattern(regEx).matcher(this.response).find();
	}

	/***
	 * throws a TestException if response does not match the provided regular expression.
	 * 
	 * @param regexp
	 * @throws TestException
	 */
	public void verify(String regexp) throws TestException
	{
		if (!matches(regexp))
		{
			throw new TestException(String.format("Response '%s' does not match pattern '%s'", this.response, regexp));
		}
		else
		{
			logger.info(String.format("Response match pattern '%s'", regexp));
			logger.debug(String.format("Response '%s' match pattern '%s'", this.response, regexp));
		}
	}

	private Pattern getPattern(String regEx)
	{
		Pattern pattern;
		if (compiledPatterns.containsKey(regEx))
		{
			pattern = compiledPatterns.get(regEx);
		}
		else
		{
			pattern = Pattern.compile(regEx);
			compiledPatterns.put(regEx, pattern);
		}
		return pattern;
	}
}
