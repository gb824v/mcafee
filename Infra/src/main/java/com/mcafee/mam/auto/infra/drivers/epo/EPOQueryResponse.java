package com.mcafee.mam.auto.infra.drivers.epo;

import com.mcafee.mam.auto.infra.Record;
import com.mcafee.mam.auto.infra.TestException;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents an EPO query response. EPO queries are consisted of records of
 * name value pairs.
 * 
 * @author danny
 */
public class EPOQueryResponse
{

	private List<Record> records = new ArrayList<Record>();
	private List<Record> filterRecords = new ArrayList<Record>();

	/**
	 * returns a list of all records in query
	 * 
	 * @return
	 */
	public List<Record> getRecords()
	{
		return records;
	}

	/**
	 * constructs a new EPO query response from a given server response.
	 * 
	 * @param response
	 * @throws TestException
	 */
	public EPOQueryResponse(String response) throws TestException
	{
		if (!response.isEmpty())
		{
			String[] lines = response.split("\r\n");
			Record record = new Record();
			for (String line : lines)
			{
				if (line.isEmpty())
				{
					this.records.add(record);
					record = new Record();
				}
				else
				{
					int pos = line.indexOf(':');
					String name = line.substring(0, pos).trim();
					String value = line.substring(pos + 1).trim();
					record.add(name, value);
				}
			}
			this.records.add(record);
		}
	}

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
	 *            record name.
	 * @param value
	 *            value in record
	 * @return null if no such record can be found.
	 */
	public Record findInFilteredRecord(String name, String value)
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
	 * @param value
	 *            value in record
	 * @return null if no such record can be found.
	 */
	public Record findInFilteredRecord(String name1, String value1, String name2, String value2)
	{
		for (Record record : this.filterRecords)
		{
			String val = record.get(name1);

			if (val != null)
			{
				if (value1.equals(val))
				{
					val = record.get(name2);

					if (val != null)
					{
						if (value2.equals(val)) { return record; }
					}
				}
			}
		}
		return null;
	}

	/**
	 * find a record with specific name and value
	 * 
	 * @return
	 */
	public void filterRecords(String name, String value)
	{

		for (Record record : this.records)
		{
			String val = record.get(name);

			if (value.equals(val))
			{
				filterRecords.add(record);
			}

		}
	}
}
