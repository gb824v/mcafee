package com.mcafee.mam.auto.infra.log.html;

import java.io.File;

import com.hp.gagawa.java.elements.A;
import com.hp.gagawa.java.elements.Br;
import com.hp.gagawa.java.elements.Link;
import com.hp.gagawa.java.elements.Span;
import com.hp.gagawa.java.elements.Text;

public class HtmlTestFailedList extends HtmlWrite
{
	public int index = 1;

	public HtmlTestFailedList(File file)
	{
		this.file = file;
		Link link = new Link();
		link.setRel("stylesheet");
		link.setType("text/css");
		link.setHref("./default.css");
		Span span = new Span();
		span.setCSSClass("test_report_pass");
		head.appendChild(link);
		html.appendChild(createEmptySpan("test_report_pass"));
		save();
	}

	public void addTest(TestStatistics hts)
	{
		html.appendChild(createSpan("test_list_erro", hts.getTestName(), hts.getFileName()));
		this.index++;
		save();
	}

	public Span createEmptySpan(String className)
	{
		return createSpan(className, "", "");
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
			span.appendChild(new Text("All Fails:"));
			span.appendChild(new Br());
		}
		return span;
	}

	public int getSize()
	{
		if (index > 0)
		{
			return index - 1;
		}
		else
		{
			return index;
		}
	}
}
