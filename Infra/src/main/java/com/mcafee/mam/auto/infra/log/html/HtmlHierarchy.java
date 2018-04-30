package com.mcafee.mam.auto.infra.log.html;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import com.hp.gagawa.java.elements.A;
import com.hp.gagawa.java.elements.Br;
import com.hp.gagawa.java.elements.Div;
import com.hp.gagawa.java.elements.H2;
import com.hp.gagawa.java.elements.Link;
import com.hp.gagawa.java.elements.Span;
import com.hp.gagawa.java.elements.Text;

public class HtmlHierarchy extends HtmlWrite
{

	private Div div = null;
	private List<TestStatistics> testStatisticsList;
	private final String FRAME_TITLE = "";

	public HtmlHierarchy(File file)
	{
		this.file = file;
		this.testStatisticsList = new LinkedList<TestStatistics>();
	}

	private void createHtmlHierarchyHeader()
	{
		this.div = new Div().setAlign("left");
		super.setFrameTitle(FRAME_TITLE);
		Link link = new Link();
		link.setRel("stylesheet");
		link.setType("text/css");
		link.setHref("./default.css");
		head.appendChild(link);
		html.appendChild(body);
	}

	private void createHtmlHirarcyList(boolean isFailed)
	{
		html.appendChild(new H2().appendChild(new Text("Summary Report")));
		String stat = "test_list_pass";
		if (isFailed)
		{
			stat = "test_list_erro";
		}
		html.appendChild(createSpan(stat, "Summary by package", "summaryPkg.html","testFrame"));
		html.appendChild(createSpan(stat, "Summary by scenario", "summaryScenario.html","testFrame"));
		html.appendChild(new Br());
		html.appendChild(createSpan(stat, "All Tests", "testsList.html","testsListFrame"));
		html.appendChild(createSpan(stat, "All Fails", "testsFailed.html","testsListFrame"));
	}

	public void build(TestStatistics testStatistics)
	{
		initPage();
		createHtmlHierarchyHeader();
		this.testStatisticsList.add(testStatistics);
		createHtmlHirarcyList(testStatistics.getFailed()>0);
		body.appendChild(div);
		save();
	}
	public Span createSpan(String className, String name, String fileName,String target)
	{
		Span span = new Span();
		span.setCSSClass(className);
		if (!name.isEmpty())
		{
			A a = new A();
			a.setHref(fileName);
			a.setTarget(target);
			a.appendChild(new Text(name));
			span.appendChild(a);
			span.appendChild(new Br());
		}
		return span;
	}
}
