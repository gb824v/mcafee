package com.mcafee.mam.auto.infra.log.html;

import com.mcafee.mam.auto.infra.TestClass;
import com.mcafee.mam.auto.infra.TestRunnerEvent;
import com.mcafee.mam.auto.infra.TestStep;
import com.mcafee.mam.auto.infra.TestRunnerEvent.Event;

import java.io.File;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.spi.LoggingEvent;

public class HtmlAppender extends AppenderSkeleton
{

	private TestStatistics testStatistics = null;
	private GeneralStatistics htmlGeneralStatistics = null;
	private HtmlIndex htmlIndex = null;
	private HtmlReport htmlReport = null;
	private HtmlTestList htmlTestList = null;
	private HtmlTestFailedList htmlTestFailedList = null;
	private HtmlPackageSummary htmlPkgSummary = null;
	private HtmlScenarioSummary htmlScenarioSummary = null;
	private HtmlHierarchy htmlHierarchy = null;
	private String logFolder = "log";
	private long index = 0;
	private boolean saveArchive = false;

	@Override
	public void activateOptions()
	{
		FileUtils.init(this.logFolder, saveArchive);
		this.logFolder += "/current";
		this.htmlTestList = new HtmlTestList(new File(logFolder + File.separator + "testsList.html"));
		this.htmlTestFailedList = new HtmlTestFailedList(new File(logFolder + File.separator + "testsFailed.html"));
		this.htmlPkgSummary = new HtmlPackageSummary(new File(logFolder + File.separator + "summaryPkg.html"));
		this.htmlScenarioSummary = new HtmlScenarioSummary(new File(logFolder + File.separator + "summaryScenario.html"));
		this.htmlHierarchy = new HtmlHierarchy(new File(logFolder + File.separator + "hierarchy.html"));
		this.htmlIndex = new HtmlIndex(new File(logFolder + File.separator + "index.html"));
	}

	public HtmlAppender()
	{
		this.testStatistics = new TestStatistics();
		this.htmlGeneralStatistics = new GeneralStatistics();
	}

	@Override
	public void append(LoggingEvent loggingEvent)
	{
		if (loggingEvent.getMessage() instanceof TestRunnerEvent)
		{
			TestRunnerEvent runnerEvent = (TestRunnerEvent) loggingEvent.getMessage();
			Event event = runnerEvent.getEvent();
			switch (event)
			{
				case RunStart:
					FileUtils.addResourceFiles("html", "default.css", logFolder);
					break;
				case TestStart:
					testStartedEvent(runnerEvent.getTest());
					break;
				case TestEnded:
					String pkgName = runnerEvent.getTest().getClass().getPackage().getName();
					String scenarioFile = runnerEvent.getScenarioFile();
					testEndedEvent(runnerEvent.getRunningTime(), pkgName, scenarioFile);
					break;
				case StepDescription:
					save();
					TestStep step = runnerEvent.getStep();
					htmlReport.createStepSpan(String.format("Step #%d: %s", step.order(), step.description()));
					break;
				case SetupStart:
					save();
					htmlReport.createStepSpan("Setup");
					break;
				case tearDownStart:
					save();
					htmlReport.createStepSpan("TearDown");
					break;
				case StepPassed:
					save();
					this.testStatistics.stepPassed();
					break;
				case TestFailed:
				case StepFailed:
					save();
					this.testStatistics.stepFailed();
					break;
				case StepSkipped:
					this.testStatistics.stepSkipped();
					break;
				case RunFinished:
					runnerFinishedEvent();
					CsvReport.close();
					break;
				default:
					break;

			}
		}
		else
		{
			if (this.htmlReport != null)
			{
				htmlReport.addReport(loggingEvent);
				if (index++ % 10 == 0)
				{
					save();
				}
				if (index++ % 200 == 0)
				{
					htmlReport.closeFile();
				}
			}
		}
	}

	private void save()
	{
		if (this.htmlReport != null)
		{
			this.htmlReport.save();
		}
	}

	private void testStartedEvent(TestClass test)
	{
		save();
		ReportFile reportFile = new ReportFile(test.getClass(), this.logFolder);
		this.htmlReport = new HtmlReport(reportFile.getFile());
		addTestToContainer(reportFile.getTestName(), reportFile.getARefLink());

	}

	private void runnerFinishedEvent()
	{
		this.htmlTestList.closeFile();
		this.htmlTestFailedList.closeFile();
		this.htmlIndex.closeFile();
		this.htmlPkgSummary.closeFile();
		this.htmlScenarioSummary.closeFile();
		this.htmlHierarchy.closeFile();

		if (this.htmlReport != null)
		{
			this.htmlReport.closeFile();
		}
	}

	private void testEndedEvent(long runningTime, String packageName, String scenarioName)
	{
		this.testStatistics.setRunningTime(runningTime);
		this.testStatistics.setPackageName(packageName);
		this.testStatistics.setScenarioFile(scenarioName);
		this.htmlTestList.addTest(this.testStatistics);

		if (testStatistics.getFailed() > 0)
		{
			this.htmlGeneralStatistics.testFail();
			this.htmlTestFailedList.addTest(this.testStatistics);
		}
		this.htmlGeneralStatistics.setTotalTests(htmlTestList.getSize());
		this.htmlPkgSummary.build(this.htmlGeneralStatistics, this.testStatistics);
		this.htmlScenarioSummary.build(this.htmlGeneralStatistics, this.testStatistics);
		this.htmlHierarchy.build(this.testStatistics);
		this.testStatistics = new TestStatistics();
		save();
	}

	public void addTestToContainer(String testName, String aRefLink)
	{
		this.testStatistics.setTestName(testName);
		this.testStatistics.setFilePath(aRefLink);
	}

	@Override
	public void close()
	{
	}

	@Override
	public boolean requiresLayout()
	{
		return false;
	}

	public void setSaveArchive(boolean saveArchive)
	{
		this.saveArchive = saveArchive;
	}

}
