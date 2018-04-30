package com.mcafee.mam.auto.infra;

import java.util.Arrays;
import org.apache.log4j.Logger;

import com.mcafee.mam.auto.infra.drivers.epo.EPOClient;
import com.mcafee.mam.auto.infra.drivers.vsphere.Vcenter;

/**
 * Represents a base class for tests. All tests derived from this class. To create a new test, users
 * should: 1. extends this class. 2. implement the 'before' and 'after' methods. 3. add methods for
 * tests. Test methods must be public and have the TestStep annotation. Test succeeds when it's
 * execution finished. To fail a test-step, throw a TestException, to fail the Test itself, throw a
 * TestException on the 'before' or 'after' methods.
 * 
 * @see TestStep annotation
 * @see TestSUT
 * @see TestException
 * @author danny
 */
public abstract class TestClass
{
	protected static Logger logger = Logger.getLogger(TestClass.class);
	private TestSUT sut;
	protected boolean mandatory = false;
	protected boolean initVcenter = true;
	protected boolean initEpo = true;
	protected boolean isFailed = false;

	public void initDrivers() throws TestException
	{
		if (!Vcenter.getInstance().isConnect())
		{
			Vcenter vc = null;
			EPOClient ec = null;
			for (TestObject obj : sut.getDrivers())
			{
				if (obj instanceof Vcenter && initVcenter)
				{
					vc = (Vcenter) obj;
					Vcenter.getInstance().init(vc.getHost(), vc.getUser(), vc.getPassword(), vc.getName());
				}
				if (obj instanceof EPOClient && initEpo)
				{
					ec = (EPOClient) obj;
					EPOClient.getInst().init(ec.getHost(), ec.getUser(), ec.getPassword(), ec.getName());
				}
			}
		}
	}

	/**
	 * sets the SUT for this test.
	 * 
	 * @param sut
	 */
	public void setSUT(TestSUT sut)
	{
		this.sut = sut;
	}

	protected <T extends TestObject> T getSutDevice(String name) throws TestException
	{
		return this.sut.getDevice(name);
	}

	protected <T extends TestDriver> T getSutDriver(String name) throws TestException
	{
		return this.sut.getDriver(name);
	}

	public <T extends TestObject> T getSutObject(String name) throws TestException
	{
		return this.sut.getObject(name);
	}

	/**
	 * * called before tests steps to initialize the test-class.
	 * 
	 * @throws TestException
	 */
	public abstract void setup() throws TestException;

	/**
	 * called after tests step had been executed. Use to un-initialize test class.
	 * 
	 * @throws TestException
	 */
	public abstract void tearDown() throws Exception;

	protected static String getMessage(String title, Object expected, boolean isPass, Object actual)
	{
		return getMessage(title, expected, actual, isPass, 0, 0);
	}

	private static String getMessage(String title, Object expected, Object actual, boolean isPass, Object expDeviation, Object ActualDeviation)
	{
		String msg = "";

		if (isPass)
		{
			if (!title.isEmpty())
			{
				title += " Verify Is As Expected, Exp: %s, Actual: %s";
			}
			else
			{
				title = "Verify Is As Expected, Exp: %s, Actual: %s";
			}

		}
		else
		{
			if (!title.isEmpty())
			{
				title += " Verify Is Not As Expected, Exp: %s, Actual: %s";
			}
			else
			{
				title = "Verify Is Not As Expected, Exp: %s, Actual: %s";
			}
		}

		if (expDeviation.equals(0))
		{
			msg = String.format(title, expected, actual);
		}
		else
		{
			msg = String.format(title + " [ Exp Dev: %s, Actual Dev: %s ]", expected, actual, expDeviation, ActualDeviation);
		}

		return msg;
	}

	/**
	 * * verify both objects are the same.
	 * 
	 * @param expected
	 *            - what's expected
	 * @param found
	 *            - what's found.
	 * @throws TestException
	 *             when 'expected' not equals 'found'.
	 */
	public static void verifyExpected(Object expected, Object actual) throws TestException
	{
		verifyExpected("", expected, actual);
	}

	/**
	 * * verify both String are the same (Ignoring Order) by given delimiter.
	 * 
	 * @param title
	 * @param expected
	 * @param actual
	 * @throws TestException
	 */

	public static void verifyArrStrExpected(String title, String expected, String actual, String delimiter) throws TestException
	{
		verifyStrArrExpected(title, expected.split(delimiter), actual.split(delimiter));
	}

	/**
	 * verify both String Array are the same (Ignoring Order).
	 * 
	 * @param title
	 * @param expected
	 * @param actual
	 * @param delimiter
	 * @throws TestException
	 */
	public static void verifyStrArrExpected(String title, String[] expected, String[] actual) throws TestException
	{
		Arrays.sort(expected);
		Arrays.sort(actual);
		if (!Arrays.equals(expected, actual))
		{
			throw new TestException(getMessage(title, expected, actual, false, 0, 0));
		}
		else
		{
			logger.info(getMessage(title, Arrays.toString(expected), true, Arrays.toString(actual)));
		}
	}

	/**
	 * verify both objects are the same.
	 * 
	 * @param expected
	 *            - what's expected
	 * @param found
	 *            - what's found.
	 * @throws TestException
	 *             when 'expected' not equals 'found'.
	 */
	public static void verifyExpected(String title, Object expected, Object actual) throws TestException
	{
		verifyExpected(title, expected, actual, true);
	}

	/**
	 * verify both objects are the same.
	 * 
	 * @param expected
	 *            - what's expected
	 * @param found
	 *            - what's found.
	 * @throws TestException
	 *             when 'expected' not equals 'found'.
	 */
	public static boolean verifyExpected(String title, Object expected, Object actual, boolean isThrowException) throws TestException
	{
		boolean isPass = false;

		if (expected.toString().isEmpty())
		{
			expected = "null";
		}
		if (actual.toString().isEmpty())
		{
			actual = "null";
		}
		if (!expected.equals(actual))
		{

			if (isThrowException)
			{
				throw new TestException(getMessage(title, expected, actual, false, 0, 0));
			}
			else
			{
				logger.error(getMessage(title, expected, actual, false, 0, 0));
			}
		}
		else
		{
			logger.info(getMessage(title, expected, true, actual));
			isPass = true;
		}
		return isPass;
	}

	public static void verifyExpectedContain(String title, Object expected, Object actual) throws TestException
	{
		verifyExpectedContain(title, expected, actual, true);
	}

	/**
	 * * verify both objects are the same.
	 * 
	 * @param expected
	 *            - what's expected
	 * @param found
	 *            - what's found.
	 * @throws TestException
	 *             when 'expected' not contain 'found'.
	 */
	public static boolean verifyExpectedContain(String title, Object expected, Object actual, boolean isThrowException) throws TestException
	{
		boolean isPass = false;

		if (expected.toString().isEmpty())
		{
			expected = "null";
		}
		if (actual.toString().isEmpty())
		{
			actual = "null";
		}

		if ((actual.toString().indexOf(expected.toString()) < 0))
		{
			if (isThrowException)
			{
				throw new TestException(getMessage(title + " Contain", expected, false, actual));
			}
			else
			{
				logger.error(getMessage(title + " Contain", expected, false, actual));
			}
		}
		else
		{
			isPass = true;
			logger.info(getMessage(title + " Contain", expected, true, actual));
		}
		return isPass;
	}

	/**
	 * 
	 * @param title
	 * @param expected
	 * @param actual
	 * @throws TestException
	 */
	public static void verifyExpected(String title, int expected, int actual) throws TestException
	{
		verifyExpected(title, expected, actual, 0);
	}

	/**
	 * 
	 * @param title
	 * @param expected
	 * @param actual
	 * @throws TestException
	 */
	public static void verifyExpectedBiggerEqualThen(String title, int expected, int actual) throws TestException
	{
		if (expected < actual)
		{
			throw new TestException(getMessage(title + "Expected Bigger Then Actual", expected, false, actual));
		}
		else
		{
			logger.info(getMessage(title + "Expected Bigger Then Actual", expected, true, actual));
		}
	}

	/**
	 * 
	 * @param title
	 * @param expected
	 * @param actual
	 * @throws TestException
	 */
	public static void verifyExpectedLessEqualThen(String title, int expected, int actual) throws TestException
	{
		if (expected > actual)
		{
			throw new TestException(getMessage(title + "Expected Less Then Actual", expected, false, actual));
		}
		else
		{
			logger.info(getMessage(title + "Expected Less Then Actual", expected, true, actual));
		}
	}

	/**
	 * * verify both objects are the same.
	 * 
	 * @param expected
	 *            - what's expected
	 * @param found
	 *            - what's found.
	 * @throws TestException
	 *             when 'expected' not equals 'found'.
	 */
	public static void verifyExpected(String title, int expected, int actual, int expDeviation) throws TestException
	{
		int result = expected - actual;

		if (Math.abs(result) > expDeviation)
		{
			throw new TestException(getMessage(title, expected, actual, false, expDeviation, Math.abs(result)));
		}
		else
		{
			logger.info(getMessage(title, expected, actual, true, expDeviation, Math.abs(result)));
		}
	}

	public boolean isMandatory()
	{
		return mandatory;
	}

	public void setMandatory(boolean mandatory)
	{
		this.mandatory = mandatory;
	}

	public boolean isFailed()
	{
		return isFailed;
	}

	public void setFailed(boolean isFailed)
	{
		this.isFailed = isFailed;
	}

	public boolean isInitVcenter()
	{
		return initVcenter;
	}

	public void setInitVcenter(boolean initVcenter)
	{
		this.initVcenter = initVcenter;
	}

	public boolean isInitEpo()
	{
		return initEpo;
	}

	public void setInitEpo(boolean initEpo)
	{
		this.initEpo = initEpo;
	}
}
