package com.mcafee.mam.auto.infra;

import org.apache.log4j.Logger;

/**
 * Represents an object that can be loaded through a SUT file. TestObjects can be defined in SUT.
 * Each test object contains common properties that can be manipulated in SUT files and tests. For
 * instance - timeout for operations on the TestObject. Each object has a name, that is used when
 * loading from SUT. Names should be unique.
 * 
 * @see TestSUT
 * @author danny
 */
public class TestObject
{

	private boolean silentReport = false;
	private boolean throwException = true;

	private static Logger logger = Logger.getLogger(TestObject.class);
	protected long timeout = 10 * 60 * 1000; // 10 minutes
	protected long waitInterval = 20 * 1000; // 20 seconds
	protected String name = "";

	public enum ReportingMode
	{

		Default,
		/**
		 * Be verbose (report), don't stop on errors and don't throw exceptions
		 */
		ReportAndContinue,
		/**
		 * Throw exceptions, don't report, stop execution.
		 */
		StopOnError,

		DontStopOnError
	}

	public void setReportingMode(ReportingMode mode)
	{
		switch (mode)
		{
			case Default:
			case StopOnError:
				this.silentReport = false;
				this.throwException = true;
				break;
			case ReportAndContinue:
				this.silentReport = false;
				this.throwException = false;
				break;
			case DontStopOnError:
				this.silentReport = true;
				this.throwException = false;
				break;
		}
	}

	/**
	 * * gets timeout, in milliseconds.
	 * 
	 * @return
	 */
	public long getTimeout()
	{
		return timeout;
	}

	/**
	 * * sets timeout, in milliseconds.
	 * 
	 * @param timeout
	 */
	public void setTimeout(long timeout)
	{
		this.timeout = timeout;
	}

	/**
	 * gets the interval to wait in time consuming operations.
	 * 
	 * @return
	 */
	public long getWaitInterval()
	{
		return waitInterval;
	}

	public void setWaitInterval(long timeoutInterval)
	{
		this.waitInterval = timeoutInterval;
	}

	public void setWaitIntervalSec(long timeoutInterval)
	{
		this.waitInterval = timeoutInterval * 1000;
	}

	public void setWaitIntervalToDefault()
	{
		this.waitInterval = 20 * 1000;
	}

	@Override
	public String toString()
	{
		return name;
	}

	/**
	 * 
	 * gets object name
	 * 
	 * @return
	 */
	public String getName()
	{
		return name;
	}

	/**
	 * sets the object's name.
	 * 
	 * @param name
	 */
	public void setName(String name)
	{
		this.name = name;
	}

	/**
	 * Creates a new StopWatch based on the configuration of this test object.
	 * 
	 * @return a new StopWatch class.
	 * @see Stopwatch
	 */
	public Stopwatch getStopwatch()
	{
		return new Stopwatch(this.timeout, this.waitInterval);
	}

	public static void sleepMs(long msToSleep) throws TestException
	{
		try
		{
			logger.info("Sleeping " + msToSleep + " ms...");
			Thread.sleep(msToSleep);
		}
		catch (Exception e)
		{
			throw new TestException("Sleeping " + msToSleep + " ms failed");
		}
	}

	public static void sleepSec(long secToSleep) throws TestException
	{
		String second = " second...";

		if (secToSleep > 1)
		{
			second = " seconds...";
		}

		try
		{
			logger.info("Sleeping " + secToSleep + second);
			Thread.sleep(secToSleep * 1000);
		}
		catch (Exception e)
		{
			throw new TestException("Sleeping " + secToSleep + " sec failed");
		}
	}

	public static void sleepMin(long minToSleep) throws TestException
	{
		String min = " minute...";

		if (minToSleep > 1)
		{
			min = " minutes...";
		}
		try
		{
			logger.info("Sleeping " + minToSleep + min);
			Thread.sleep(minToSleep * 1000 * 60);
		}
		catch (Exception e)
		{
			throw new TestException("Sleeping " + minToSleep + " min failed");
		}
	}

	public static void sleepHour(long hourToSleep) throws TestException
	{
		String hour = " hour...";

		if (hourToSleep > 1)
		{
			hour = " hours...";
		}
		try
		{
			logger.info("Sleeping " + hourToSleep + hour);
			Thread.sleep(hourToSleep * 1000 * 60 * 60);
		}
		catch (Exception e)
		{
			throw new TestException("Sleeping " + hourToSleep + " min failed");
		}
	}

	/**
	 * @param silentReport
	 *            the silentReport to set
	 */
	public void setSilentReport(boolean silentReport)
	{
		this.silentReport = silentReport;
	}

	/**
	 * @param throwException
	 *            the throwException to set
	 */
	public void setThrowException(boolean throwException)
	{
		this.throwException = throwException;
	}

	public boolean isSilentReport()
	{
		return silentReport;
	}

	public boolean isThrowException()
	{
		return throwException;
	}
}
