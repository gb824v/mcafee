package com.mcafee.mam.auto.infra.log.html;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import com.hp.gagawa.java.elements.A;
import com.hp.gagawa.java.elements.B;
import com.hp.gagawa.java.elements.Body;
import com.hp.gagawa.java.elements.Br;
import com.hp.gagawa.java.elements.Font;
import com.hp.gagawa.java.elements.Head;
import com.hp.gagawa.java.elements.Html;
import com.hp.gagawa.java.elements.Link;
import com.hp.gagawa.java.elements.Span;
import com.hp.gagawa.java.elements.Text;
import com.hp.gagawa.java.elements.Title;

/**
 * @author gbitan
 * 
 */
public abstract class HtmlWrite
{
	protected Html html = null;
	protected Head head = null;
	protected Body body = null;
	protected Title title = null;
	protected File file = null;
	private BufferedWriter writer = null;

	public HtmlWrite()
	{
		initPage();
	}

	protected void initPage()
	{
		this.html = new Html();
		this.head = new Head();
		this.title = new Title();
		this.body = new Body();
		head.appendChild(title);
		html.appendChild(head);
	}

	protected void setFrameTitle(String frameTitle)
	{
		this.title.appendChild(new Text(frameTitle));
	}

	protected void save()
	{
		try
		{
			this.writer = null;
			this.writer = new BufferedWriter(new FileWriter(this.file.getPath()));
			writer.append(html.write());
			writer.flush();
		}
		catch (IOException e)
		{
			e.printStackTrace(System.err);
		}
	}

	protected void createLinkToDefaultCss()
	{
		Link link = new Link();
		link.setRel("stylesheet");
		link.setType("text/css");
		link.setHref("../default.css");
		head.appendChild(link);
	}

	protected void createSpan(MessageEvent msgEvent)
	{
		A a = null;

		if (!msgEvent.getLinkFileName().isEmpty())
		{
			a = new A();
			a.setHref(msgEvent.getLinkFileName());
			a.setTarget("testFrame");
			a.appendChild(new Text(msgEvent.getMsg()));
		}
		Span span1 = new Span();
		span1.setCSSClass(msgEvent.getReportState());
		if (!msgEvent.getTime().isEmpty())
		{
			Span span2 = new Span().setCSSClass("time_stamp");
			span2.appendChild(new Text(msgEvent.getTime()));
			span1.appendChild(span2);
		}
		if (a != null)
		{
			span1.appendChild(a);
		}
		else
		{
			if (msgEvent.isBold())
			{
				Font font = new Font().setSize("4");
				span1.appendChild(new Br());
				span1.appendChild(new B().appendChild(font.appendChild(new Text(msgEvent.getMsg()))));
				span1.appendChild(new Br());
			}
			else
			{
				span1.appendChild(new Text(msgEvent.getMsg()));
				span1.appendChild(new Br());
			}
		}
		span1.appendChild(new Br());
		html.appendChild(span1);
		if (!msgEvent.getLinkFileName().isEmpty())
		{
			html.appendChild(new Br());
		}
	}

	protected void closeFile()
	{
		try
		{
			if (this.writer != null)
			{
				this.writer.close();
			}
		}
		catch (IOException e)
		{
			e.printStackTrace(System.err);
		}
	}

	public File getFile()
	{
		return file;
	}

	public void setFile(File file)
	{
		this.file = file;
	}
}
