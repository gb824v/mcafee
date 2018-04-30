package com.mcafee.mam.auto.infra;

import org.apache.log4j.Logger;

/**
 * Represents a test object that can manipulate other systems.
 * 
 * @author danny
 * @see TestObject
 */
public class TestDriver extends TestObject
{

	private static Logger logger = Logger.getLogger(TestDriver.class);

	/**
	 * * override this to initialize the test driver
	 * 
	 * @throws Exception
	 */
	public void init() throws TestException
	{
		logger.info("initializing " + getName());
	}

	/**
	 * * override this to un-initialize the driver
	 */
	public void unInit() throws TestException
	{
		logger.info("uninitializing " + getName());
	}
}
