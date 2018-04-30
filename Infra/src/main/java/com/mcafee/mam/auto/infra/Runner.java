package com.mcafee.mam.auto.infra;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.*;
import org.apache.log4j.Logger;

import com.mcafee.mam.auto.infra.TestRunnerEvent.Event;

import flexjson.JSONException;

/**
 * Represents for running the QAA tests.
 * 
 * @author danny & Guy
 */
public class Runner
{

	private static Logger logger = Logger.getLogger(Runner.class);
	private List<String> scenarios = new LinkedList<String>();
	long runStartTime = 0;

	/**
	 * * Compares Test methods using the TestStep annotation (order field).
	 */
	private static class TestStepComparator implements Comparator<Method>
	{
		@Override
		public int compare(Method t, Method t1)
		{
			try
			{
				TestStep annotation = t.getAnnotation(TestStep.class);
				TestStep annotation1 = t1.getAnnotation(TestStep.class);
				return annotation.order() - annotation1.order();
			}
			catch (Exception e)
			{
				return -1;
			}

		}
	}

	/**
	 * runs the all scenarios.
	 * 
	 * @throws Exception
	 */
	public void run() throws Exception
	{
		onRunnerStart();
		runTests();
		onRunnerFinish();
	}

	/**
	 * Adds a scenario from local resource
	 * 
	 * @param resourceName
	 *            - resource that can be found using ClassLoader.
	 * @see Scenario
	 */
	public void addScenario(String resourceName)
	{
		this.scenarios.add(resourceName);
	}

	/**
	 * * run tests (no initialization)
	 * 
	 * @throws TestException
	 */
	private void runTests() throws TestException
	{
		for (String scenarioFile : this.scenarios)
		{
			logger.info(new TestRunnerEvent(Event.ScenarioChanged));
			logger.info("Loading scenario from " + scenarioFile);
			try (InputStream inputStream = ClassLoader.getSystemResourceAsStream(scenarioFile))
			{
				Scenario scenario = Scenario.loadFrom(inputStream);
				if (scenario.getSut().isEmpty())
				{
					logger.info("Sut file is missing in the scenario: " + scenarioFile);
					break;
				}
				Iterator<TestClass> iterTests = scenario.getTests().iterator();
				while (iterTests.hasNext())
				{
					TestClass test = iterTests.next();
					try
					{
						onTestStart(test);
						try (InputStream sutStream = ClassLoader.getSystemResourceAsStream(scenario.getSut()))
						{
							test.setSUT(TestSUT.loadFrom(sutStream));
							setupStartEvent();
							test.setup();
							steps(test);
							tearDownStartEvent();
							test.tearDown();
							onTestEnd(test,scenarioFile);
							if (test.isMandatory() && test.isFailed())
							{
								logger.error(String.format("Runner stopped!!! because this test '%s': is mandatory.", test.getClass().getName()));
								break;
							}
						}
						catch (IOException io)
						{
							logger.info("Sut file " + scenario.getSut() + " in the scenario " + scenarioFile + " is incorrect.");
							onTestFailedEnd(test,scenarioFile, io);
						}
						catch (NullPointerException nullEx)
						{
							logger.info("Failed to read scenario/sut file!!!");
							onTestFailedEnd(test,scenarioFile, nullEx);
						}
						catch (JSONException ex)
						{
							logger.info("JSONException failed to pharse scenario/sut file!!!!!!");
							onTestFailedEnd(test,scenarioFile, ex);
						}
					}
					catch (Exception te)
					{
						onTestFailedEnd(test,scenarioFile, te);
					}
				}
			}
			catch (IOException ex)
			{
				logger.info("Failed to load scenario file " + scenarioFile, ex);
			}
		}
	}

	private void onTestFailedEnd(TestClass test,String scenarioFile, Exception e)
	{
		onTestFailed(test, e);
		onTestEnd(test,scenarioFile);
	}

	/**
	 * * runs steps defined in a test class.
	 * 
	 * @param test
	 *            - the test class to inspect
	 * @throws TestException
	 */
	private void steps(TestClass test) throws TestException
	{
		List<Method> methodslist = new ArrayList<Method>();

		for (Method method : test.getClass().getMethods())
		{
			if (method.isAnnotationPresent(TestStep.class))
			{
				methodslist.add(method);
			}
		}
		Method[] methods = Arrays.copyOf(methodslist.toArray(), methodslist.toArray().length, Method[].class);

		Arrays.sort(methods, new TestStepComparator());
		for (Method method : methods)
		{
			String methodName = String.format(test.getClass().getCanonicalName() + "." + method.getName());
			TestStep step = method.getAnnotation(TestStep.class);
			if (step.skip())
			{
				onStepSkipped(step, test, methodName);
			}
			else
			{
				try
				{
					stepStartEvent(step, test, methodName);
					method.invoke(test);
					onStepPassed(step, test, methodName);
				}
				catch (Exception ex)
				{
					onStepFailed(step, test, methodName, ex);
					test.setFailed(true);
					if (step.mandatory()) { throw new TestException("Step " + methodName + " Failed", ex); }
				}
			}
		}
	}

	/**
	 * * occurs when a step had passed.
	 * 
	 * @param step
	 *            - the step that passed.
	 * @param test
	 *            - the test step belongs to
	 * @param methodName
	 *            - step method name.
	 */
	private void onStepPassed(TestStep step, TestClass test, String methodName)
	{
		TestRunnerEvent event = new TestRunnerEvent(Event.StepPassed);
		event.setStep(step);
		event.setTest(test);
		event.setMethod(methodName);
		logger.info(event);
	}

	/**
	 * 
	 * @param step
	 * @param test
	 * @param methodName
	 */
	private void setupStartEvent()
	{
		TestRunnerEvent event = new TestRunnerEvent(Event.SetupStart);
		logger.info(event);
	}

	/**
	 * 
	 * @param step
	 * @param test
	 * @param methodName
	 */
	private void tearDownStartEvent()
	{
		TestRunnerEvent event = new TestRunnerEvent(Event.tearDownStart);
		logger.info(event);
	}

	/**
	 * 
	 * @param step
	 * @param test
	 * @param methodName
	 */
	private void stepStartEvent(TestStep step, TestClass test, String methodName)
	{
		TestRunnerEvent event = new TestRunnerEvent(Event.StepDescription);
		event.setStep(step);
		event.setTest(test);
		event.setMethod(methodName);
		logger.info(event);
	}

	/**
	 * * occurs when a step skipped
	 * 
	 * @param step
	 *            - the step that skipped
	 * @param test
	 *            - the test step belongs to
	 * @param methodName
	 *            - step method name
	 */
	private void onStepSkipped(TestStep step, TestClass test, String methodName)
	{
		TestRunnerEvent event = new TestRunnerEvent(Event.StepSkipped);
		event.setStep(step);
		event.setTest(test);
		event.setMethod(methodName);
		logger.info(event);
	}

	/**
	 * * occurs when test step failed
	 * 
	 * @param step
	 *            - test step
	 * @param test
	 *            - test step belongs to
	 * @param methodName
	 *            - step method name
	 * @param ex
	 *            - exception that caused the fail.
	 */
	private void onStepFailed(TestStep step, TestClass test, String methodName, Exception ex)
	{
		TestRunnerEvent event = new TestRunnerEvent(Event.StepFailed);
		event.setStep(step);
		event.setTest(test);
		event.setMethod(methodName);
		event.setExeception(ex);
		logger.info(event);
		logger.info(ex);
	}

	/**
	 * occurs before test starts
	 * 
	 * @param test
	 *            - the test that starts.
	 */
	private void onTestStart(TestClass test)
	{
		TestRunnerEvent event = new TestRunnerEvent(Event.TestStart);
		event.setTest(test);
		this.runStartTime = System.currentTimeMillis();
		logger.info(event);
	}

	/**
	 * * occurs after test had finished.
	 * 
	 * @param test
	 *            - test that is finished
	 */
	private void onTestEnd(TestClass test,String scenarioFile)
	{
		TestRunnerEvent event = new TestRunnerEvent(Event.TestEnded);
		event.setScenarioFile(scenarioFile);
		event.setTest(test);
		event.setTestRunningTime(System.currentTimeMillis() - runStartTime);
		logger.info(event);
	}

	/**
	 * * occurs when test fails not on test-step.
	 * 
	 * @param test
	 *            - the test that failed
	 * @param ex
	 *            - cause.
	 */
	private void onTestFailed(TestClass test, Exception ex)
	{
		TestRunnerEvent event = new TestRunnerEvent(Event.TestFailed);
		event.setTest(test);
		event.setExeception(ex);
		event.setTestRunningTime(System.currentTimeMillis() - runStartTime);
		logger.info(event);
	}

	private void onRunnerStart()
	{
		TestRunnerEvent event = new TestRunnerEvent(Event.RunStart);
		logger.info(event);
	}

	/**
	 * * occurs after after runner finished.
	 */
	private void onRunnerFinish()
	{
		TestRunnerEvent event = new TestRunnerEvent(Event.RunFinished);
		logger.info(event);
	}
}
