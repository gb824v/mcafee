package com.mcafee.mam.qaadevtool.command.sensor;

import java.io.Serializable;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang.StringUtils;
import com.mcafee.epo.task.model.ClientTask;
import com.mcafee.epo.task.model.ClientTaskSettings;
import com.mcafee.epo.task.services.ClientTaskService;
import com.mcafee.orion.core.auth.AuthorizationException;
import com.mcafee.orion.core.auth.OrionUser;
import com.mcafee.orion.core.cmd.Auditable;
import com.mcafee.orion.core.cmd.Chainable;
import com.mcafee.orion.core.cmd.CommandException;
import com.mcafee.orion.core.cmd.CommandSpec;
import com.mcafee.orion.core.cmd.OutputDescriptor;
import com.mcafee.orion.core.cmd.SchedulableCommandBase;
import com.mcafee.orion.core.data.ListDataSource;
import com.mcafee.orion.core.db.ConnectionBean;
import com.mcafee.orion.core.db.base.Database;
import com.mcafee.orion.core.db.base.DatabaseUtil;
import com.mcafee.orion.core.ext.InstallableExtensionItem;
import com.mcafee.orion.core.query.OrionQuery;
import com.mcafee.orion.core.query.cmd.QueryOutputDescriptor;
import com.mcafee.orion.core.servlet.util.UserUtil;
import com.mcafee.orion.core.util.resource.LocaleAware;
import com.mcafee.orion.core.cmd.CommandInvoker;
import com.mcafee.orion.core.audit.AuditLogWriter;
import com.mcafee.orion.core.audit.AuditLogWriterData;

@SuppressWarnings("deprecation")
public class AddRemoveSensor extends SchedulableCommandBase implements ConnectionBean, Chainable, Auditable, LocaleAware
{
	public static final String COMMAND_NAME = "adt.sensor.addRemoveSensor";
	private ClientTaskService clientTaskService = null;
	private Connection connection = null;
	private OrionUser user;
	private String clientTaskA = null;
	private String nodeName;
	protected InstallableExtensionItem m_pkgItem;
	private CommandInvoker commandInvoker = null;
	private AuditLogWriter auditLogWriter = null;
	private ListDataSource<?> dataSource = null;
	private Database database;
	private String msg = "";
	private boolean success = false;
	private String errorMessage = null;

	public String getStatusMessage()
	{
		return "AddRemoveSensor from remote computer";
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
		if (StringUtils.isBlank(this.clientTaskA)) { throw new CommandException("ClientTask not specified"); }
		if (StringUtils.isBlank(this.nodeName)) { throw new CommandException("nodeName is mandatory !!!"); }
		ClientTask chosenCt = this.clientTaskService.getClientTaskByName(this.connection, this.user, "RSDMETA", "SensorDeployment", this.clientTaskA, true);
		if (chosenCt == null) { throw new CommandException("ClientTask not found: clientTask=" + this.clientTaskA); }

		ClientTaskSettings cts = chosenCt.getTaskSettings();
		boolean install = (cts.getSections().containsKey("Install\\RSD_____4700")) && ("1".equals(cts.getSetting("Install", "NumInstalls")));

		Map<String, Serializable> map = new HashMap<String, Serializable>();
		map.put("parentID", getNodeId(this.nodeName));
		map.put("clientTaskID", Integer.valueOf(chosenCt.getTaskObjectId()));
		if (install)
		{
			try
			{
				getCommandInvoker().invoke("rsd.installSensor", map, getUser());
				return "status: true";
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
				return "status: false";
			}
		}
		else
		{
			try
			{
				getCommandInvoker().invoke("rsd.uninstallSensor", map, getUser());
				return "status: true";
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
				return "status: false";
			}
		}
	}

	public int getNodeId(String nodeName) throws Exception
	{
		int autoId = -1;
		Connection con = null;
		PreparedStatement stmt = null;
		try
		{
			con = this.database.getConnection();
			stmt = con.prepareStatement("SELECT AutoID FROM EPOLeafNode WHERE NodeName = ?");
			stmt.setString(1, nodeName);
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

	public OutputDescriptor getOutputDescriptor()
	{
		return null;
	}

	public ListDataSource<?> getDataSource()
	{
		return this.dataSource;
	}

	public void setDataSource(ListDataSource<?> dataSource)
	{
		this.dataSource = dataSource;
	}

	public void setInputObject(Object obj)
	{
		if ((obj instanceof ListDataSource))
		{
			this.dataSource = ((ListDataSource<?>) obj);
		}
	}

	public boolean isSuccess()
	{
		return this.success;
	}

	public void setSuccess(boolean success)
	{
		this.success = success;
	}

	public String getErrorMessage()
	{
		return this.errorMessage;
	}

	public void setErrorMessage(String errorMessage)
	{
		this.errorMessage = errorMessage;
	}

	public Connection getConnection()
	{
		return this.connection;
	}

	public void setConnection(Connection connection)
	{
		this.connection = connection;
	}

	public AuditLogWriter getAuditLogWriter()
	{
		return this.auditLogWriter;
	}

	public boolean authorize(OrionUser user) throws CommandException, URISyntaxException
	{
		return (user != null);
	}

	public void setAuditLogWriter(AuditLogWriter auditLogWriter)
	{
		this.auditLogWriter = auditLogWriter;
	}

	public CommandInvoker getCommandInvoker()
	{
		return this.commandInvoker;
	}

	public void setCommandInvoker(CommandInvoker commandInvoker)
	{
		this.commandInvoker = commandInvoker;
	}

	public String getDetailLogName()
	{
		return getDisplayName();
	}

	public int getPriority()
	{
		return isSuccess() ? 2 : 1;
	}

	public String getDescription()
	{
		return this.msg;
	}

	public void setClientTaskService(ClientTaskService clientTaskService)
	{
		this.clientTaskService = clientTaskService;
	}

	public void setCTask(String clientTaskName)
	{
		this.clientTaskA = clientTaskName;
	}

	public void setValidateMessage(String validateMessage)
	{
		// /this.validateMessage = validateMessage;
	}

	public void setDatabase(Database database)
	{
		this.database = database;
	}

	public void setNodeName(String nodeName)
	{
		this.nodeName = nodeName;
	}

	@Override
	public String getValidateMessage()
	{
		// TODO Auto-generated method stub
		return null;
	}

	public boolean canAccept(OutputDescriptor descriptor)
	{
		if ((descriptor instanceof QueryOutputDescriptor))
		{
			QueryOutputDescriptor qod = (QueryOutputDescriptor) descriptor;

			OrionQuery query = qod.getOrionQuery();
			String name = query.getName();

			if (qod.isChartDataSource())
			{
				setValidateMessage(getResource().formatString("rsd.sensorInstaller.invalidQueryType", getLocale(), new Object[] { name }));

				return false;
			}

			String target = query.getTarget();
			if (!"EPOLeafNode".equals(target))
			{
				setValidateMessage(getResource().formatString("rsd.sensorInstaller.invalidTableType", getLocale(), new Object[] { name }));

				return false;
			}

			setValidateMessage(null);
			return true;
		}

		setValidateMessage(getResource().getString("rsd.sensorInstaller.invalidOutputType", getLocale()));
		return false;
	}

	public boolean canAcceptToSchedule(OutputDescriptor descriptor)
	{
		return descriptor instanceof QueryOutputDescriptor;
	}

}
