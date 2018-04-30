package com.mcafee.mam.auto.infra.log.html;

import java.io.File;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import com.hp.gagawa.java.elements.A;
import com.hp.gagawa.java.elements.Br;
import com.hp.gagawa.java.elements.Div;
import com.hp.gagawa.java.elements.Font;
import com.hp.gagawa.java.elements.H1;
import com.hp.gagawa.java.elements.H4;
import com.hp.gagawa.java.elements.Link;
import com.hp.gagawa.java.elements.Span;
import com.hp.gagawa.java.elements.Table;
import com.hp.gagawa.java.elements.Td;
import com.hp.gagawa.java.elements.Text;
import com.hp.gagawa.java.elements.Th;
import com.hp.gagawa.java.elements.Tr;

public abstract class HtmlSummary extends HtmlWrite
{
	protected int index = 1;
	protected List<TestStatistics> testOrderList;
	private final String FRAME_TITLE = "Summary Report";
	protected int totalRunningTime = 0;

	public HtmlSummary(File file)
	{
		this.testOrderList = new LinkedList<TestStatistics>();
		this.file = file;
	}

	protected void createHtmlSummaryHeader()
	{
		super.setFrameTitle(FRAME_TITLE);
		body.appendChild(new H1().appendChild(new Text(FRAME_TITLE)));
		body.appendChild(new H4().appendChild(new Text(new Date(System.currentTimeMillis()).toString())));
		Link link = new Link();
		link.setRel("stylesheet");
		link.setType("text/css");
		link.setHref("./default.css");
		head.appendChild(link);
		html.appendChild(body);
	}

	protected Div createGeneralStatisticsTable(GeneralStatistics htmlGeneralStatistics)
	{
		Div div = new Div();
		Table table = createDefaultTable("30%");
		Tr tr = new Tr();
		tr.appendChild(new Th().appendChild(new Text("General statistics")));
		tr.appendChild(new Th());
		table.appendChild(tr);
		int total = htmlGeneralStatistics.getTotalTests();
		int fails = htmlGeneralStatistics.getFails();
		if (total > 0)
		{
			table.appendChild(createTr("Total Tests:", Integer.toString(total)));
			table.appendChild(createTr("Total Fails:", Integer.toString(fails)));
			table.appendChild(createTr("Total Passed:", Integer.toString(total - fails)));
			table.appendChild(createTr("Running Time:", getTime(this.totalRunningTime)));
		}
		else
		{
			table.appendChild(createTr("Total Tests:", "0"));
			table.appendChild(createTr("Total Fails:", "0"));
			table.appendChild(createTr("Total Passed:", "0"));
			table.appendChild(createTr("Running Time:", "0"));
		}
		div.appendChild(table);
		div.appendChild(new Br());
		return div;
	}

	private Table createHtmlTestStatisticsTableHeader()
	{
		Table table = createDefaultTable("80%");
		table.appendChild(createTr());
		return table;
	}

	protected Div createHtmlSortHeader(String headerName)
	{
		Div div = new Div();
		Table table = createDefaultTable("80%");
		Tr tr = new Tr();
		tr.appendChild(new Th().appendChild(new Text(headerName)));
		table.appendChild(tr);
		div.appendChild(table);
		return div;
	}

	protected Div createHtmlTestStatisticsTable(List<TestStatistics> tsl)
	{
		Div div = new Div();
		Table table = createHtmlTestStatisticsTableHeader();
		int totalPass = 0;
		int totalFailed = 0;
		int totalSkipped = 0;
		float totalPassedPrecent = 0;
		long totalRunningTime = 0;

		for (TestStatistics ts : tsl)
		{
			totalPass += ts.getPassed();
			totalFailed += ts.getFailed();
			totalSkipped += ts.getSkipped();
			totalRunningTime += ts.getRunningTime();
			float passedPrecent = calculatePrecetnage(ts.getPassed(), ts.getPassed() + ts.getFailed());
			ts.setPassedPrecent(passedPrecent);
			table.appendChild(createTr(ts, true));
			this.index++;
		}
		totalPassedPrecent = calculatePrecetnage(totalPass, totalPass + totalFailed);
		table.appendChild(createTr(new TestStatistics("", "Total:", totalPassedPrecent, totalFailed, totalPass, totalSkipped, totalRunningTime), false));
		div.appendChild(table);
		return div;
	}

	/**
	 * 
	 * @param passed
	 * @param total
	 * @return
	 */
	private float calculatePrecetnage(int passed, int total)
	{
		return 100 * (float) passed / (float) total;
	}

	private Table createDefaultTable(String width)
	{
		Table table = new Table();
		table.setWidth(width);
		table.setBorder("1");
		table.setCellspacing("2");
		table.setCellpadding("2");
		table.setStyle("'border-collapse:collapse;border:none;mso-border-alt:solid black .75pt;mso-padding-alt:0in 5.4pt 0in 5.4pt'");
		return table;
	}

	private Tr createTr(String name, String value)
	{
		Tr tr = new Tr();
		tr.appendChild(new Td().appendChild(new Text(name)));
		tr.appendChild(new Td().appendChild(new Text(value)));
		return tr;
	}

	private Tr createTr()
	{
		Tr tr = new Tr();
		tr.appendChild(new Td().appendChild(new Text("Test")));
		tr.appendChild(new Td().appendChild(new Text("Passed(%)")));
		tr.appendChild(new Td().appendChild(new Text("Passed")));
		tr.appendChild(new Td().appendChild(new Text("Failed")));
		tr.appendChild(new Td().appendChild(new Text("Skipped")));
		tr.appendChild(new Td().appendChild(new Text("Running Time")));
		return tr;
	}

	private Tr createTr(TestStatistics ts, boolean isTest)
	{
		Tr tr = new Tr();
		if (!isTest)
		{
			tr.appendChild(new Td().appendChild(new Text(ts.getTestName())));
		}
		if (ts.getFailed() > 0)
		{
			if (isTest)
			{
				tr.appendChild(new Td().appendChild(createSpan("test_list_erro", ts.getTestName(), ts.getFileName())));
			}
			Font font = new Font();
			font.setColor("RED");
			tr.appendChild(new Td().appendChild(font.appendChild(new Text(ts.getPassedPrecent()))));
		}
		else
		{
			if (isTest)
			{
				tr.appendChild(new Td().appendChild(createSpan("test_list_pass", ts.getTestName(), ts.getFileName())));
			}
			Font font = new Font();
			font.setColor("BLUE");
			tr.appendChild(new Td().appendChild(font.appendChild(new Text(ts.getPassedPrecent()))));
		}
		tr.appendChild(new Td().appendChild(new Text(ts.getPassedAsString())));
		tr.appendChild(new Td().appendChild(new Text(ts.getFailedAsString())));
		tr.appendChild(new Td().appendChild(new Text(ts.getSkippedAsString())));
		tr.appendChild(new Td().appendChild(new Text(getTime(ts.getRunningTime()))));
		return tr;
	}

	public Span createSpan(String className, String testName, String fileName)
	{
		Span span = new Span();
		span.setCSSClass(className);

		if (!testName.isEmpty())
		{
			A a = new A();
			a.setHref(fileName);
			a.setTarget("testFrame");
			a.appendChild(new Text(index + " " + testName));
			span.appendChild(a);
			span.appendChild(new Br());
		}
		else
		{
			span.appendChild(new Text("All Tests:"));
			span.appendChild(new Br());
		}
		return span;
	}

	private String getTime(long milliseconds)
	{
		int seconds = (int) (milliseconds / 1000) % 60;
		int minutes = (int) ((milliseconds / (1000 * 60)) % 60);
		int hours = (int) ((milliseconds / (1000 * 60 * 60)) % 24);
		String res = "";
		if (milliseconds >= 1000)
		{
			if (hours > 0)
			{
				res += hours + " hour ";
			}
			if (minutes > 0)
			{
				res += minutes + " min ";
			}
			if (seconds > 0)
			{
				res += seconds + " sec ";
			}
		}
		else
		{
			return milliseconds + " ms";
		}
		return res;
	}
}
