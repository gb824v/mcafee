package com.mcafee.mam.auto.infra.log.html;

import java.io.File;
import org.apache.log4j.Level;
import org.apache.log4j.spi.LoggingEvent;

import com.mcafee.mam.auto.infra.log.html.MessageEvent.MessageType;

public class HtmlReport extends HtmlWrite
{

	private HtmlReport htmlDebugReport = null;
	private MessageEvent msgEvent = null;
	private boolean wasDebug = false;

	public HtmlReport(File file)
	{
		File folder = new File(file.getParent());
		if (!folder.exists())
		{
			folder.mkdirs();
		}
		this.file = file;
		createLinkToDefaultCss();
	}

	public void addReport(LoggingEvent event)
	{
		this.msgEvent = null;
		switch (event.getLevel().toInt())
		{
			case Level.DEBUG_INT:
				this.msgEvent = new MessageEvent(event, MessageType.Dedug);
				createDebugSpan();
				break;
			case Level.ERROR_INT:
			case Level.FATAL_INT:
				this.msgEvent = new MessageEvent(event, MessageType.Error);
				createSpan(this.msgEvent);
				break;
			case Level.WARN_INT:
				this.msgEvent = new MessageEvent(event, MessageType.Warning);
				createDebugSpan();
				break;
			case Level.INFO_INT:
				this.msgEvent = new MessageEvent(event, MessageType.Info);
				createInfoSpan();
				break;
		}
	}

	public void createInfoSpan()
	{
		
		if (wasDebug)
		{
			saveAndCloseDebugFile();
			wasDebug = false;
		}
		
		if (this.msgEvent.getType().equals(MessageType.Exception))
		{
			File expLink = new File(file.getParentFile() + File.separator + ReportFile.getCountFile("exception").getName());
			HtmlReport htmlReport = new HtmlReport(expLink);
			htmlReport.createSpan(this.msgEvent);
			htmlReport.save();
			createSpan(new MessageEvent(this.msgEvent.getTime(), this.msgEvent.getExpName(), MessageType.Error, expLink.getName()));
		}
		
		else
		{
			createSpan(this.msgEvent);
		}
	}

	public void createDebugSpan()
	{
		wasDebug = true;

		if (this.htmlDebugReport == null)
		{
			String fileName = File.separator + ReportFile.getCountFile("debug").getName();
			File debugFile = new File(file.getParentFile() + fileName);
			this.htmlDebugReport = new HtmlReport(debugFile);
			createSpan(new MessageEvent(this.msgEvent.getTime(), "Debug", MessageType.Info, debugFile.getName()));
		}

		this.htmlDebugReport.createSpan(this.msgEvent);
	}

	public void createStepSpan(String msg)
	{
		createSpan(new MessageEvent(msg, MessageType.Step));
	}

	public void saveAndCloseDebugFile()
	{
		if (this.htmlDebugReport != null)
		{
			this.htmlDebugReport.save();
			this.htmlDebugReport.closeFile();
			this.htmlDebugReport = null;
		}
	}
}
