package com.mcafee.mam.auto.infra.log.html;

/**
 * holds statistics regarding tests.
 * 
 * @author guy
 */
public class TestStatistics
{

	private String testName = "";
	private String packageName = "";
	private String scenarioFile = "";
	private int failed = 0;
	private int passed = 0;
	private int skipped = 0;
	private String fileName = "";
	private long runningTime = 0;
	private float passedPrecent = 0;

	public TestStatistics(String fileName, String testName, float passedPrecent, int failed, int passed, int skipped, long runningTime)
	{
		super();
		this.failed = failed;
		this.passed = passed;
		this.skipped = skipped;
		this.testName = testName;
		this.runningTime = runningTime;
		this.passedPrecent = passedPrecent;
	}

	public TestStatistics()
	{
	}

	public int getFailed()
	{
		return failed;
	}

	public String getFailedAsString()
	{
		return Integer.toString(failed);
	}

	public void stepFailed()
	{
		this.failed++;
	}

	public int getPassed()
	{
		return passed;
	}

	public String getPassedAsString()
	{
		return Integer.toString(passed);
	}

	public void stepPassed()
	{
		this.passed++;
	}

	public int stepSkipped()
	{
		return skipped++;
	}

	public String getSkippedAsString()
	{
		return Integer.toString(skipped);
	}

	public int getSkipped()
	{
		return skipped;
	}

	public String getTestName()
	{
		return testName;
	}

	public void setTestName(String testName)
	{
		this.testName = testName;
	}

	public String getFileName()
	{
		return fileName;
	}

	public void setFilePath(String fileName)
	{
		this.fileName = fileName;
	}

	public long getRunningTime()
	{
		return runningTime;
	}

	public void setRunningTime(long runningTime)
	{
		this.runningTime = runningTime;
	}

	public String getPassedPrecentAsString()
	{
		return String.format("%.2f", passedPrecent);
	}

	public float getPassedPrecent()
	{
		if (passedPrecent>0)
		{
		return passedPrecent;
		}
		else
		{
			return 0;
		}
	}

	public void setPassedPrecent(float passedPrecent)
	{
		this.passedPrecent = passedPrecent;
	}

	public String getPkgName()
	{
		return packageName;
	}

	public void setPackageName(String packageName)
	{
		this.packageName = packageName;
	}

	public String getScenarioFile()
	{
		return scenarioFile;
	}

	public void setScenarioFile(String scenarioFile)
	{
		this.scenarioFile = scenarioFile;
	}
}
