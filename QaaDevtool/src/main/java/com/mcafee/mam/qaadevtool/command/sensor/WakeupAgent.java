package com.mcafee.mam.qaadevtool.command.sensor;

import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang.StringUtils;
import com.mcafee.epo.core.services.EPOConsoleRequestServices;
import com.mcafee.orion.core.audit.AuditLogWriterData;
import com.mcafee.orion.core.auth.AuthorizationException;
import com.mcafee.orion.core.auth.OrionUser;
import com.mcafee.orion.core.cmd.Auditable;
import com.mcafee.orion.core.cmd.Command;
import com.mcafee.orion.core.cmd.CommandException;
import com.mcafee.orion.core.cmd.CommandSpec;
import com.mcafee.orion.core.cmd.ResultDisplayer;
import com.mcafee.orion.core.cmd.VisibleCommandBase;
import com.mcafee.orion.core.db.ConnectionBean;
import com.mcafee.orion.core.db.base.Database;
import com.mcafee.orion.core.db.base.DatabaseUtil;
import com.mcafee.orion.core.servlet.util.UserUtil;
import com.mcafee.orion.core.cmd.CommandInvoker;
import com.mcafee.orion.core.audit.AuditLogWriter;

public class WakeupAgent extends VisibleCommandBase implements Command, ConnectionBean, Auditable, ResultDisplayer
{
	public static final String COMMAND_NAME = "adt.sensor.wakeupAgent";
	private Connection connection = null;
	private OrionUser user;
	private Database database;
	private String nodeName;
	private CommandInvoker commandInvoker = null;
	private AuditLogWriter auditLogWriter = null;

	public String getStatusMessage()
	{
		return "WakeupAgent";
	}

	protected CommandSpec createSpec()
	{
		CommandSpec spec = new CommandSpec("getProductProperties.help.short-desc", "getProductProperties.help.long-desc");
		spec.setName(COMMAND_NAME);
		spec.setResource(getResource());
		return spec;
	}

	public String invoke() throws Exception
	{
		if (StringUtils.isBlank(this.nodeName)) { throw new CommandException("nodeName is mandatory !!!"); }
		int nodeId = getNodeId();
		if (nodeId > 0) { return agentWakeup(nodeId) ? "true" : "failed"; }
		return "status: false";
	}

	public int getNodeId() throws Exception
	{
		int autoId = -1;
		Connection con = null;
		PreparedStatement stmt = null;
		try
		{
			con = this.database.getConnection();
			stmt = con.prepareStatement("SELECT AutoID FROM EPOLeafNode WHERE NodeName = ?");
			stmt.setString(1, this.nodeName);
			ResultSet rs = stmt.executeQuery();
			if (rs.next())
			{
				autoId = rs.getInt(1);
			}
		}
		finally
		{
			DatabaseUtil.close(stmt);
			DatabaseUtil.close(con);
		}

		return autoId;
	}

	protected boolean agentWakeup(int leafNodeID)
	{
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("fullProps", true);
		map.put("randomMinutes", 0);
		map.put("attempts", 1);
		map.put("retryIntervalInSeconds", 30);
		map.put("abortAfterMinutes", 1);
		map.put("computerIds", Integer.toString(leafNodeID));
		map.put("inputType", EPOConsoleRequestServices.INPUT_CSV_COMPIDS); // input type
		map.put("useAllHandlers", true);
		map.put("forceFullPolicyUpdate", true);
		try
		{
			getCommandInvoker().invoke("system.wakeupAgent", map, getUser());
			return true;
		}
		catch (AuthorizationException ae)
		{

			String message = getResource().formatString("rsd.install.sensor.install.noagentperm", getLocale(), user.getName());
			AuditLogWriterData data = new AuditLogWriterData();
			data.setCmdName(getDisplayName());
			data.setPriority(Auditable.PRIORITY_MED);
			data.setStartAndEndTime(new Timestamp(System.currentTimeMillis()));
			data.setSuccess(false);
			data.setUser(getUser());
			data.setMessage(message);
			getAuditLogWriter().writeAuditLogEntry(data);
		}
		catch (CommandException ce)
		{
			// TODO: do we really care if the wakeup fails?
		}
		return false;
	}

	public boolean authorize(OrionUser user) throws CommandException, URISyntaxException
	{
		return (user != null);
	}

	public int getPriority()
	{
		return Auditable.PRIORITY_LOW;
	}

	public Connection getConnection()
	{
		return connection;
	}

	public void setConnection(Connection connection)
	{
		this.connection = connection;
	}

	public OrionUser getUser()
	{
		try
		{
			user = UserUtil.getInternalAdminUser(getConnection());
		}
		catch (java.sql.SQLException sqe)
		{
		}
		return user;
	}

	public void setUser(OrionUser user)
	{

		this.user = user;
	}

	public void setDatabase(Database database)
	{
		this.database = database;
	}

	public void setNodeName(String nodeName)
	{
		this.nodeName = nodeName;
	}

	public CommandInvoker getCommandInvoker()
	{
		return commandInvoker;
	}

	public void setCommandInvoker(CommandInvoker commandInvoker)
	{
		this.commandInvoker = commandInvoker;
	}

	public AuditLogWriter getAuditLogWriter()
	{
		return auditLogWriter;
	}

	public void setAuditLogWriter(AuditLogWriter auditLogWriter)
	{
		this.auditLogWriter = auditLogWriter;
	}

}
