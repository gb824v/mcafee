package com.mcafee.mam.auto.infra;

import com.mcafee.mam.auto.infra.log.html.HtmlAppender;

/**
 * Represents a test runner event. The event is sent to the log4j logger for reporting and
 * statistics analysis. The toString method implement this message as a regular event for other
 * appender.
 * 
 * @see HtmlAppender
 * @author danny
 */
public class TestRunnerEvent
{

	private TestStep step;
	private TestClass test;
	private String scenarioFile="";
	private String method;
	private TestDriver driver;
	private Exception exception;
	private long runningTime;

	public enum Event
	{
		StepDescription, StepPassed, StepFailed, StepSkipped, TestStart, TestEnded, SetupStart, tearDownStart, RunStart, RunFinished, TestFailed, ScenarioChanged, NewPackage
	}

	private final Event event;

	public TestRunnerEvent(Event event)
	{
		this.event = event;
	}

	public void setStep(TestStep step)
	{
		this.step = step;
	}

	public void setTest(TestClass test)
	{
		this.test = test;
	}

	public TestStep getStep()
	{
		return step;
	}

	public TestClass getTest()
	{
		return test;
	}

	public String getMethod()
	{
		return method;
	}

	public Event getEvent()
	{
		return event;
	}

	public void setMethod(String methodName)
	{
		this.method = methodName;
	}

	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder(this.getEvent().toString());
		if (this.test != null)
		{
			sb.append(" Test:");
			sb.append(test.getClass().getName());
		}
		if (this.driver != null)
		{
			sb.append(" Driver: ");
			sb.append(this.driver.getName());
		}
		if (this.step != null)
		{
			sb.append(" Step #");
			sb.append(this.step.order()).append(": ");
			sb.append(this.step.description());
		}
		if (this.method != null)
		{
			sb.append(" Method: ");
			sb.append(method);
		}
		if (this.exception != null)
		{
			sb.append(" Error: ");
			sb.append(this.exception.getMessage());
		}
		return sb.toString();

	}

	public void setDriver(TestDriver driver)
	{
		this.driver = driver;
	}

	public void setExeception(Exception ex)
	{
		this.exception = ex;
	}

	public Exception getException()
	{
		return exception;
	}

	public void setException(Exception exception)
	{
		this.exception = exception;
	}

	public long getRunningTime()
	{
		return runningTime;
	}

	public void setTestRunningTime(long runningTime)
	{
		this.runningTime = runningTime;
	}

	public String getScenarioFile()
	{
		return scenarioFile;
	}

	public void setScenarioFile(String scenario)
	{
		this.scenarioFile = scenario;
	}
}
