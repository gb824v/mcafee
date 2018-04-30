package com.mcafee.mam.auto.infra.log.html;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.apache.log4j.spi.LoggingEvent;

public class MessageEvent
{

	private String reportState = "";
	private String msg = "";
	private String expName = "";
	private boolean isBold;
	private String linkFileName = "";
	private MessageType type;
	private String time = "";
	private List<String> errors = null;

	public MessageEvent(LoggingEvent event, MessageType msgType)
	{
		addErrors();
		setType(msgType);
		setMsg(event.getMessage());
		setTime(event.getTimeStamp());

	}

	public MessageEvent(String msgTime, String msg, MessageType msgType, String linkFileName)
	{
		addErrors();
		this.type = msgType;
		setMsg(msg);
		this.linkFileName = linkFileName;
		this.time = msgTime;
	}

	public MessageEvent(String msg, MessageType msgType)
	{
		this.type = msgType;
		setMsg(msg);
	}

	public enum MessageType
	{

		Step,

		Info,

		Dedug,

		Warning,

		Error,

		Exception
	}

	public MessageType getType()
	{
		return type;
	}

	public void setType(MessageType msgType)
	{
		this.type = msgType;
	}

	public String getMsg()
	{
		return msg;
	}

	public void setMsg(Object eventMsg)
	{
		if (eventMsg instanceof Exception)
		{
			setType(MessageType.Exception);

			Exception ex = (Exception) eventMsg;

			this.msg = getStackTraceToString(ex);

			if (ex.getCause().getMessage() != null)
			{
				this.expName = ex.getCause().getMessage();
			}
			else
			{
				this.expName = ex.getMessage();
			}
		}
		else
		{
			switch (getType())
			{
				case Info:
					this.msg = getFormatMessage((String) eventMsg);
					this.reportState = isErrorFound() ? "test_report_warn" : "test_report_pass";
					break;
				case Error:
					this.reportState = "test_report_erro";
					this.msg = (String) eventMsg;
					break;
				case Warning:
					this.reportState = "test_report_warn";
					this.msg = (String) eventMsg;
					break;
				case Step:
					this.reportState = "test_report_pass";
					this.msg = (String) eventMsg;
					this.isBold = true;
					break;
				case Dedug:
					this.msg = getFormatMessage((String) eventMsg);
					this.reportState = isErrorFound() ? "test_report_warn" : "test_report_pass";
					break;
				default:
					break;
			}
		}

	}

	public boolean isBold()
	{
		return isBold;
	}

	public void setBold(boolean isBold)
	{
		this.isBold = isBold;
	}

	public String getLinkFileName()
	{
		return linkFileName;
	}

	public void setLinkFileName(String linkFileName)
	{
		this.linkFileName = linkFileName;
	}

	public String getReportState()
	{
		return reportState;
	}

	public String getTime()
	{
		return time;
	}

	public void setTime(long msgTime)
	{
		Calendar c = Calendar.getInstance(TimeZone.getDefault());
		c.setTimeInMillis(msgTime);
		Date d = c.getTime();
		this.time = new SimpleDateFormat("HH:mm:ss: ").format(d);
	}

	public String getExpName()
	{
		return expName;
	}

	public void setExpName(String expName)
	{
		this.expName = expName;
	}

	private boolean isErrorFound()
	{
		for (String error : this.errors)
		{
			if (this.msg.toLowerCase().indexOf(error) >= 0)
			{
				this.reportState = "test_report_warn";
				return true;
			}
		}
		return false;
	}

	private String getFormatMessage(String eventMsg)
	{
		String msg = eventMsg.replaceAll("\\r|\\n", "<br>");

		if (msg.startsWith("Recevied:"))
		{
			msg = msg.replaceAll("Recevied:", "<I>Recevied:</I>");
		}
		else if (msg.startsWith("Sending:"))
		{
			msg = msg.replaceAll("Sending:", "<I>Sending:</I>");
		}
		else if (msg.startsWith(">> ") || msg.startsWith("<< "))
		{

			if (msg.indexOf("[\\r][\\n]") >= 0)
			{
				msg = msg.replace("[\\r][\\n]", "");
			}
		}
		if (msg.endsWith("[\\r\\n]"))
		{
			msg = msg.replaceAll("[\\r\\n]", "<br>");
		}
		else if (msg.endsWith("[\n]"))
		{
			msg = msg.replaceAll("[\\n]", "");
		}
		return msg;
	}

	private String getStackTraceToString(Exception e)
	{
		StringBuilder sb = new StringBuilder();
		// dump exception stack if specified
		if (null != e)
		{
			final StackTraceElement[] traces = e.getStackTrace();
			if (null != traces && traces.length > 0)
			{
				sb.append("<b>");
				sb.append(e.getClass() + ": " + e.getMessage());
				sb.append("</b><br>");
				for (final StackTraceElement trace : traces)
				{
					sb.append("    at " + trace.getClassName() + '.' + trace.getMethodName() + '(' + trace.getFileName() + ':' + trace.getLineNumber() + ')');
					sb.append("<br>");
				}
			}
			Throwable cause = e.getCause();
			while (null != cause)
			{
				final StackTraceElement[] causeTraces = cause.getStackTrace();
				if (null != causeTraces && causeTraces.length > 0)
				{
					sb.append("Caused By:");
					sb.append("<br><b>");
					sb.append(cause.getClass() + ": " + cause.getMessage());
					sb.append("</b><br>");

					for (final StackTraceElement causeTrace : causeTraces)
					{
						sb.append("    at ").append(causeTrace.getClassName()).append('.').append(causeTrace.getMethodName()).append('(').append(causeTrace.getFileName())
								.append(':').append(causeTrace.getLineNumber()).append(')');
						sb.append("<br>");
					}
				}
				// fetch next cause
				cause = cause.getCause();
			}
		}
		return sb.toString();
	}

	protected void addErrors()
	{
		this.errors = new ArrayList<String>();
		this.errors.add(new String("error"));
		this.errors.add(new String("not supported"));
		this.errors.add(new String("cannot perform"));
		this.errors.add(new String("cannot execute"));
		this.errors.add(new String("failure"));
		this.errors.add(new String("failed"));
		this.errors.add(new String("Invalid"));
		this.errors.add(new String("no such file or directory"));
		this.errors.add(new String("permission denied"));
		this.errors.add(new String("unable to find"));
		this.errors.add(new String("unable to connect"));
	}

}
