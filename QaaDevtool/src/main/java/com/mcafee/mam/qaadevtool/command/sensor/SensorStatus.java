package com.mcafee.mam.qaadevtool.command.sensor;

import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import org.apache.commons.lang.StringUtils;
import com.mcafee.epo.computermgmt.ui.action.systemdetail.PropertyBean;
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
import com.mcafee.orion.core.ui.BasicDisplayContext;
import com.mcafee.orion.core.ui.PropertyFormatter;
import com.mcafee.orion.core.ui.Verbosity;
import com.mcafee.orion.core.cmd.CommandInvoker;
import com.mcafee.orion.core.audit.AuditLogWriter;

public class SensorStatus extends VisibleCommandBase implements Command, ConnectionBean, Auditable, ResultDisplayer
{
	public static final String COMMAND_NAME = "adt.sensor.sensorStatus";
	private Connection connection = null;
	private OrionUser user;
	private Database database;
	private String nodeName;
	private String productCode = null;
	private PropertyFormatter productPropertyFormatter;
	private String lastEPOCommunication = "";
	private CommandInvoker commandInvoker = null;
	private AuditLogWriter auditLogWriter = null;

	public String getStatusMessage()
	{
		return "Get RSD 5.0 Sensor Status";
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
		if (null == this.productCode)
		{
			this.productCode = "MAMPLUGN1000";
		}
		if (StringUtils.isBlank(this.nodeName)) { throw new CommandException("nodeName is mandatory !!!"); }
		int nodeId = getNodeId();
		if (nodeId > 0)
		{
			String beforeWakeUp = getLastEpoCommunicationTime(nodeId);
			if (agentWakeup(nodeId)) return getLastEpoCommunicationTime(nodeId).equals(beforeWakeUp) ? "status:false" : "status:true";
		}
		else
		{
			throw new CommandException("Couldn't find nodeName in Epo DB !!!");
		}
		return "status: false";
	}

	public String getLastEpoCommunicationTime(int nodeId) throws Exception
	{
		populateProductsProperties(nodeId, getLocale());
		return this.lastEPOCommunication;
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

	private LinkedHashMap<String, Object> populateProductsProperties(int nodeId, Locale locale) throws Exception
	{
		LinkedHashMap<String, Object> allProductsProperties = new LinkedHashMap<String, Object>();
		Connection con = null;
		PreparedStatement stmt = null;
		try
		{
			BasicDisplayContext headerContext = new BasicDisplayContext(locale, Verbosity.SHORT);
			BasicDisplayContext propertyContext = new BasicDisplayContext(locale, Verbosity.LONG);

			con = this.database.getConnection();
			stmt = con.prepareStatement("SELECT DISTINCT EPOProductProperties.*, " + "EPOProductFamilies.ProductProperties AS fmProperties,"
					+ "EPOProductFamilies.ProductFamily AS productfamily," + "EPOProductFamilies.FamilyDispName AS fmDispName,"
					+ "EPOSoftware.SoftwareName AS swSoftwareName FROM EPOProductProperties "
					+ "LEFT JOIN EPOProductFamilies ON EPOProductFamilies.ProductCode=EPOProductProperties.ProductCode "
					+ "LEFT JOIN EPOSoftware ON EPOSoftware.ProductCode=EPOProductProperties.ProductCode WHERE [EPOProductProperties].[ParentID]= ?");

			stmt.setInt(1, nodeId);
			ResultSet rs = stmt.executeQuery();

			while (rs.next())
			{
				LinkedHashMap<String, LinkedHashMap<String, PropertyBean>> rd = new LinkedHashMap<String, LinkedHashMap<String, PropertyBean>>();
				String prodCode = rs.getString("ProductCode");
				if ((!StringUtils.isBlank(this.productCode)) && (!this.productCode.equalsIgnoreCase(prodCode)))
				{
					continue;
				}

				LinkedHashMap<String, PropertyBean> prodProps = new LinkedHashMap<String, PropertyBean>();
				rd.put("ProductProperties", prodProps);

				String familyDispName = rs.getString("fmDispName");

				if (null == familyDispName)
				{
					familyDispName = rs.getString("swSoftwareName");
				}
				if (null == familyDispName)
				{
					familyDispName = rs.getString("ProductCode");
				}

				String familyValue = rs.getString("ProductFamily");
				if (null == familyValue)
				{
					familyValue = rs.getString("ProductCode");
				}

				prodProps.put("ProductFamily", new PropertyBean("ProductFamily", familyDispName, familyValue, familyValue));

				prodProps.put("ProductCode", new PropertyBean("ProductCode", prodCode, prodCode, prodCode));

				String relevantProperties = rs.getString("fmProperties");
				if (null == relevantProperties)
				{
					relevantProperties = "ProductVersion";
				}

				for (String s : relevantProperties.split(","))
				{
					s = s.trim();
					Object o = rs.getObject(s);

					prodProps.put(s.toLowerCase(), new PropertyBean(s.toLowerCase(), this.productPropertyFormatter.getHeader(s.toLowerCase(), headerContext),
							this.productPropertyFormatter.formatPropertyValue(s, o, propertyContext), o == null ? null : o.toString()));
				}

				PreparedStatement stmt2 = con.prepareStatement("SELECT DISTINCT psl.Section," + "psl.Setting,psv.Value FROM EPOProductSettingValues psv "
						+ "JOIN EPOProductProperties pp ON psv.ParentID=pp.AutoID " + "JOIN EPOProductSettingLabels psl ON psv.LabelID=psl.AutoID "
						+ "WHERE psl.AutoID=psv.LabelID and pp.ProductCode=?");

				stmt2.setString(1, prodCode);
				ResultSet rs2 = stmt2.executeQuery();
				while (rs2.next())
				{
					String section = rs2.getString(1);
					String propName = rs2.getString(2);
					String propValue = rs2.getString(3);
					if (section.equals("General") && propName.equals("LastEPOCommunication"))
					{
						this.lastEPOCommunication = propValue;
					}
					prodProps.put(section + "." + propName, new PropertyBean(section.toLowerCase(), this.productPropertyFormatter.getHeader(propName.toLowerCase(), headerContext),
							this.productPropertyFormatter.formatPropertyValue(section, propValue, propertyContext), propValue));
				}

				DatabaseUtil.close(stmt2);

				allProductsProperties.put(null == familyDispName ? "N/A" : familyDispName, rd);
			}
		}
		finally
		{
			DatabaseUtil.close(stmt);
			DatabaseUtil.close(con);
		}

		return allProductsProperties;
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

	public void setProductPropertyFormatter(PropertyFormatter productPropertyFormatter)
	{
		this.productPropertyFormatter = productPropertyFormatter;
	}

	public void setNodeName(String nodeName)
	{
		this.nodeName = nodeName;
	}

	public void setProductCode(String productCode)
	{
		this.productCode = productCode;
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
