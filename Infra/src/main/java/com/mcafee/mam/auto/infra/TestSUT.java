package com.mcafee.mam.auto.infra;

import flexjson.JSONDeserializer;
import flexjson.JSONSerializer;
import java.io.*;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;
import com.mcafee.mam.auto.infra.device.Device;

/**
 * Represents a the test SUT file. SUT is - 'System Under Test' and contains the configuration for
 * the environment in which tests are run. This class handles 2 types of objects: a) a list of
 * TestObjects, each represents something in the 'real' world, for instance - a device. b) a list of
 * TestDrivers, which are TestObjects that contains code to manipulate the object, for instance - a
 * Debian Terminal.
 * 
 * Names are unique for each list and used to reference the objects through the getters and setters.
 * 
 * SUT file is saved in JSON format. Each entry must contain a 'class' field with the canonical name
 * of the object represented.
 * 
 * Use the 'saveTo' function to programmatically create SUT files to inspect their structure.
 * 
 * @author danny
 */
@SuppressWarnings("unchecked")
public class TestSUT
{

	private static Logger logger = Logger.getLogger(TestSUT.class);
	private List<TestDriver> drivers = new LinkedList<TestDriver>();
	private List<TestObject> objects = new LinkedList<TestObject>();

	/***
	 * gets all drivers defined in the SUT
	 * 
	 * @return
	 */
	public List<TestDriver> getDrivers()
	{
		return drivers;
	}

	/**
	 * set the list of drivers in the SUT
	 * 
	 * @param drivers
	 */
	public void setDrivers(List<TestDriver> drivers)
	{
		this.drivers = drivers;
	}

	/***
	 * returns all test objects defined in the sut.
	 * 
	 * @return
	 */
	public List<TestObject> getObjects()
	{
		return objects;
	}

	/***
	 * set the list of test objects in the sut.
	 * 
	 * @param objects
	 */
	public void setObjects(List<TestObject> objects)
	{
		this.objects = objects;
	}

	/***
	 * Saves SUT to a file
	 * 
	 * @param fileName
	 *            - filename to save to.
	 * @throws IOException
	 */
	public void saveTo(String fileName) throws IOException
	{
		JSONSerializer serializer = new JSONSerializer();
		writeFile(fileName, serializer.deepSerialize(this));
	}

	/***
	 * Read file from stream into String.
	 * 
	 * @param stream
	 *            - the stream to read from.
	 * @return String containing contents of the stream.
	 * @throws IOException
	 */
	public static String readFile(InputStream stream) throws IOException
	{
		int bufferSize = 8096;
		final char[] buffer = new char[bufferSize];
		final StringBuilder out = new StringBuilder();
		final Reader in = new InputStreamReader(stream, Charset.defaultCharset());
		try
		{
			for (;;)
			{
				int size = in.read(buffer, 0, buffer.length);
				if (size < 0)
				{
					break;
				}
				out.append(buffer, 0, size);
			}
		}
		finally
		{
			in.close();
		}

		return out.toString();
	}

	/***
	 * Reads a file from path to String
	 * 
	 * @param path
	 *            - file somewhere on disk
	 * @return - contents of the file
	 * @throws IOException
	 */
	private static String readFile(String path) throws IOException
	{
		FileInputStream stream = new FileInputStream(new File(path));
		return readFile(stream);
	}

	/***
	 * Loads a TestSUT from InputStream. The input should be a valid JSON file.
	 * 
	 * @param inputStream
	 *            - the input stream to load from.
	 * @return SUT initialized from stream.
	 * @throws IOException
	 */
	public static TestSUT loadFrom(InputStream inputStream) throws IOException
	{
		JSONDeserializer<TestSUT> deserializer = new JSONDeserializer<TestSUT>();
		return deserializer.deserialize(readFile(inputStream));
	}

	/***
	 * Loads TestSUT from file name. The input should be a valid JSON file.
	 * 
	 * @param fileName
	 *            - file name to load from.
	 * @return SUT initialized from file.
	 * @throws IOException
	 */
	public static TestSUT loadFrom(String fileName) throws IOException
	{
		logger.info("Loading sut file from " + fileName);
		JSONDeserializer<TestSUT> deserializer = new JSONDeserializer<TestSUT>();
		return deserializer.deserialize(readFile(fileName));
	}

	private void writeFile(String fileName, String content) throws IOException
	{
		File newTextFile = new File(fileName);
		FileWriter fileWriter = new FileWriter(newTextFile);
		fileWriter.write(content);
		fileWriter.close();
	}

	/***
	 * Adds a test driver to the SUT.
	 * 
	 * @param driver
	 */
	public void addDriver(TestDriver driver)
	{
		this.drivers.add(driver);
	}

	/***
	 * Gets all test objects of some type
	 * 
	 * @param <T>
	 *            - to look for.
	 * @param c
	 *            - class to look for.
	 * @return List of 0 or more objects that are exactly of class 'c'.
	 */
	@SuppressWarnings("rawtypes")
	public <T extends TestObject> List<T> getObjects(Class c)
	{
		List<T> list = new LinkedList<T>();
		for (TestObject object : this.objects)
		{
			if (object.getClass().equals(c))
			{
				list.add((T) object);
			}
		}
		return list;
	}

	public List<Device> getDevsByKeysContainAnyVal(String... fileds)
	{
		return getDevsByKeysContainVal(null, fileds);
	}

	/**
	 * 
	 * @param fileds
	 * @return
	 */
	public <T extends TestObject> List<T> getDevsByKeysContainVal(List<T> objects, String... fileds)
	{
		List<T> list = new LinkedList<T>();
		if (objects == null)
		{
			objects = (List<T>) this.objects;
		}
		for (TestObject object : objects)
		{
			if (object.getClass().equals(Device.class))
			{
				Device device = ((Device) object);
				boolean isToAdd = false;
				for (String filed : fileds)
				{
					try
					{
						for (Method method : device.getClass().getMethods())
						{
							if (method.getName().startsWith("get") && method.getName().toLowerCase().endsWith(filed.toLowerCase()))
							{
								isToAdd = method.invoke(device) != null;
								break;
							}
						}
					}
					catch (Exception e)
					{
						String devName = device.getName();
						logger.trace("Device " + devName + " didnt contain mandatory filed " + filed);
					}
				}
				if (isToAdd)
				{
					list.add((T) device);
				}
			}
		}
		return list;
	}

	public <T extends TestObject> List<T> getDevsByKeysNotContainAnyVal(String... fileds)
	{
		return getDevsByKeysNotContainAnyVal(null, fileds);
	}

	/**
	 * Gets the first object by type and name contain key without value.
	 * 
	 * @param fileds
	 * @return
	 */
	public <T extends TestObject> List<T> getDevsByKeysNotContainAnyVal(List<T> objects, String... fileds)
	{
		List<T> list = new LinkedList<T>();
		if (objects == null)
		{
			objects = (List<T>) this.objects;
		}
		for (TestObject object : objects)
		{
			if (object.getClass().equals(Device.class))
			{
				Device device = ((Device) object);
				boolean isToAdd = true;
				for (String filed : fileds)
				{
					try
					{
						for (Method method : device.getClass().getMethods())
						{
							if (method.getName().startsWith("get") && method.getName().toLowerCase().endsWith(filed.toLowerCase()))
							{
								isToAdd = method.invoke(device) == null;
								break;
							}
						}
					}
					catch (Exception e)
					{
						String devName = device.getName();
						logger.trace("Device " + devName + " didnt contain mandatory filed " + filed);
					}
				}
				if (isToAdd)
				{
					list.add((T) device);
				}
			}
		}
		return list;
	}

	public <T extends TestObject> List<T> getDevsByKeysContainVal(Map<String, String> fileds)
	{
		return getDevsByKeysContainVal(null, fileds);
	}

	/**
	 * Gets the first object by type and name contain key with any value.
	 * 
	 * @param fileds
	 * @return
	 */
	public <T extends TestObject> List<T> getDevsByKeysContainVal(List<T> objects, Map<String, String> fileds)
	{
		List<T> list = new LinkedList<T>();
		if (objects == null)
		{
			objects = (List<T>) this.objects;
		}
		for (TestObject object : objects)
		{
			if (object.getClass().equals(Device.class))
			{
				Device device = ((Device) object);
				boolean isToAdd = false;
				for (String filed : fileds.keySet())
				{
					try
					{
						for (Method method : device.getClass().getMethods())
						{
							if (method.getName().startsWith("get") && method.getName().toLowerCase().endsWith(filed.toLowerCase()))
							{
								isToAdd = method.invoke(device).toString().contains(fileds.get(filed));
								break;
							}
						}
					}
					catch (Exception e)
					{
						String devName = device.getName();
						logger.trace("Device " + devName + " didnt contain mandatory filed " + filed);
					}
				}
				if (isToAdd)
				{
					list.add((T) device);
				}
			}
		}
		return list;
	}

	public <T extends TestObject> List<T> getDevsByKeysNotContainVal(Map<String, String> fileds)
	{
		return getDevsByKeysNotContainVal(null, fileds);
	}

	/**
	 * Gets the first object by type and name not contain key & value .
	 * 
	 * @param fileds
	 * @return
	 */
	public <T extends TestObject> List<T> getDevsByKeysNotContainVal(List<T> objects, Map<String, String> fileds)
	{
		List<T> list = new LinkedList<T>();
		if (objects == null)
		{
			objects = (List<T>) this.objects;
		}
		for (TestObject object : objects)
		{
			if (object.getClass().equals(Device.class))
			{
				Device device = ((Device) object);
				boolean isToAdd = true;
				for (String filed : fileds.keySet())
				{
					try
					{
						for (Method method : device.getClass().getMethods())
						{
							if (method.getName().startsWith("get") && method.getName().toLowerCase().endsWith(filed.toLowerCase()))
							{
								isToAdd = !method.invoke(device).toString().contains(fileds.get(filed));
								break;
							}
						}
					}
					catch (Exception e)
					{
						String devName = device.getName();
						logger.trace("Device " + devName + " didnt contain mandatory filed " + filed);
					}
				}
				if (isToAdd)
				{
					list.add((T) device);
				}
			}
		}
		return list;
	}

	/**
	 * Gets the first object by type and name.
	 * 
	 * @param <T>
	 *            - type of object to look for.
	 * @param name
	 *            - name of object to look for.
	 * @return first object if found.
	 * @throws TestException
	 *             if object not found.
	 */
	public <T extends TestObject> T getObject(String name) throws TestException
	{
		Iterator<TestObject> iterator = this.objects.iterator();
		while (iterator.hasNext())
		{
			TestObject to = iterator.next();
			if (to.getName().equals(name)) { return (T) to; }
		}
		throw new TestException("Cannot find object: " + name);
	}

	/**
	 * Gets the first driver by type and name.
	 * 
	 * @param <T>
	 *            - type of driver to look for.
	 * @param name
	 *            - name of driver to look for.
	 * @return first driver if found.
	 * @throws TestException
	 *             if driver not found.
	 */

	public <T extends TestDriver> T getDriver(String name) throws TestException
	{
		Iterator<TestDriver> iterator = this.drivers.iterator();
		while (iterator.hasNext())
		{
			TestDriver driver = iterator.next();
			if (driver.getName().equals(name)) { return (T) driver; }
		}
		throw new TestException("Cannot find driver " + name);
	}

	/**
	 * 
	 * @param name
	 * @return
	 * @throws TestException
	 */
	public <T extends TestObject> T getDevice(String name) throws TestException
	{
		return getObject(name);
	}
}
