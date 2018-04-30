package com.mcafee.mam.auto.infra.drivers.debian;

import com.jcraft.jsch.ChannelShell;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.mcafee.mam.auto.infra.Stopwatch;
import com.mcafee.mam.auto.infra.TestDriver;
import com.mcafee.mam.auto.infra.TestException;

import expect4j.Expect4j;
import expect4j.matches.Match;
import java.util.List;
import org.apache.log4j.Logger;

/**
 * A driver for communicating with MAM sensor.
 * 
 * @author danny
 */
public abstract class DebianInfra extends TestDriver
{

	private static Logger logger = Logger.getLogger(DebianInfra.class);
	protected String user;
	protected String password;
	protected String host;
	private int port = 22;
	private Session session;
	private ChannelShell channel;
	private Expect4j expect;
	boolean isConnect = false;

	/**
	 * connects to sensor via ssh.
	 * 
	 * @throws Exception
	 */
	protected void connect() throws Exception
	{
		logger.info("Connecting to: " + getHost() + ":" + getPort());
		JSch jsch = new JSch();
		this.session = jsch.getSession(user, host, port);
		this.session.setPassword(this.password);
		this.session.setConfig("StrictHostKeyChecking", "no");
		session.connect((int) this.getTimeout());
		this.channel = (ChannelShell) session.openChannel("shell");
		this.channel.setPty(true);
		this.channel.setPtyType("VT100");
		this.expect = new Expect4j(channel.getInputStream(), channel.getOutputStream());
		this.expect.setDefaultTimeout(this.getTimeout());
		channel.connect();
		expect.expect(new DebianCommand("").getPatterns());
		logger.debug(expect.getLastState().getBuffer());
		this.isConnect = true;
	}

	/**
	 * disconnects if connected
	 */
	@Override
	public void unInit()
	{
		disconnect();
	}

	/**
	 * executes a command
	 * 
	 * @param command
	 *            command to execute
	 * @return SensorResponse
	 * @throws Exception
	 */
	protected DebianResponse execute(String command) throws Exception
	{
		return execute(new DebianCommand(command));
	}

	protected DebianResponse execute(StringBuffer command, int exitCode) throws Exception
	{
		return execute(new DebianCommand(command.toString(), exitCode));
	}

	/**
	 * executes a command
	 * 
	 * @param command
	 *            command to execute
	 * @return SensorResponse
	 * @throws Exception
	 */
	protected DebianResponse execute(StringBuffer command) throws Exception
	{
		return execute(new DebianCommand(command.toString()));
	}

	/**
	 * execute a command
	 * 
	 * @param command
	 *            - command to execute
	 * @return sensor response
	 * @throws Exception
	 */
	protected DebianResponse execute(DebianCommand command) throws Exception
	{
		verifyConnected();
		logger.debug("Sending: " + command.toString());
		expect.send(command.getCommand());
		expect.send("\n");
		List<Match> patterns = command.getPatterns();
		expect.expect(patterns);
		String response = command.getResponse();
		while (response != null)
		{
			logger.debug("Sending:" + response);
			expect.send(response);
			expect.expect(patterns);
			response = command.getResponse();
		}
		String result = command.getCleanResult();
		logger.debug("Recevied: " + result);
		String expectedExitCode = command.getExpectedExitCode();
		verifyExitCode(expectedExitCode);
		return new DebianResponse(result.toString());
	}

	/**
	 * 
	 * @return
	 */
	public String getUser()
	{
		return user;
	}

	/**
	 * 
	 * @param user
	 */
	public void setUser(String user)
	{
		this.user = user;
	}

	/**
	 * @return the password
	 */
	public String getPassword()
	{
		return password;
	}

	/**
	 * @param password
	 *            the password to set
	 */
	public void setPassword(String password)
	{
		this.password = password;
	}

	/**
	 * @return the host
	 */
	public String getHost()
	{
		return host;
		
	}

	/**
	 * @param host
	 *            the host to set
	 */
	public void setHost(String host)
	{
		this.host = host;
	}

	/**
	 * @return the port
	 */
	protected int getPort()
	{
		return port;
	}

	/**
	 * @param port
	 *            the port to set
	 */
	public void setPort(int port)
	{
		this.port = port;
	}

	/**
	 * checks the exist code for the last command using the 'echo $?' command.
	 * 
	 * @param expectedExitCode
	 * @throws Exception
	 *             - if operation failed or exit code doesn't match
	 */
	protected void verifyExitCode(String expectedExitCode) throws Exception
	{
		DebianCommand exitCodeCommand = new DebianCommand("echo $?");
		expect.send(exitCodeCommand.getCommand());
		expect.send("\n");
		expect.expect(exitCodeCommand.getPatterns());
		String[] exitCode = exitCodeCommand.getResult().split("\r\n");
		if (exitCode.length > 0)
		{
			String code = exitCode[1];
			if (code.equals(expectedExitCode)) { return; }
		}
		throw new TestException("Command failed with exit code " + exitCodeCommand.getResult());
	}

	/**
	 * Try to connect to sensor until timeout.
	 * 
	 * @throws Exception
	 *             - if cannot connect to sensor within timeout.
	 */
	public void tryConnect() throws Exception
	{
		Stopwatch stopwatch = this.getStopwatch();
		while (stopwatch.hasTime())
		{
			try
			{
				connect();
				return;
			}
			catch (Exception ex)
			{
				logger.debug("tryConnect",ex);
				stopwatch.waitFor("debian cli to get ready");
			}
		}
		throw new TestException("Timeout while trying to connect to terminal");
	}

	/**
	 * disconnect all
	 * 
	 * @throws
	 */
	protected void disconnect()
	{
		if (this.expect != null)
		{
			this.expect.close();
			this.expect = null;
		}
		if (this.channel != null)
		{
			this.channel.disconnect();
			this.channel = null;
		}
		if (this.session != null)
		{
			this.session.disconnect();
			this.session = null;
		}
		isConnect = false;
	}

	/**
	 * make sure channel is connected. if not connected, try to reconnect
	 * 
	 * @throws Exception
	 */
	protected void verifyConnected() throws Exception
	{
		if (this.channel == null)
		{
			tryConnect();
		}
		else
		{
			if (!this.channel.isConnected())
			{
				reconnect();
			}
		}
	}

	/**
	 * reboots the sensor and blocks until reconnect.
	 * 
	 * @throws Exception
	 */
	public void reboot() throws Exception
	{
		execute("reboot");
		reconnect();
	}


	/**
	 * ping from specific interface to ipv6.
	 * 
	 * @param ip
	 * @param fromEth
	 * @throws Exception
	 */
	public void ping(String ip) throws Exception
	{
		DebianResponse response = execute(String.format("ping -c 3 %s ",ip));
		response.verify("64 bytes from");
	}
	/**
	 * ping from specific interface to ipv6.
	 * 
	 * @param ip
	 * @param fromEth
	 * @throws Exception
	 */
	public boolean isPing(String ip) throws Exception
	{
		DebianResponse response = execute(String.format("ping -c 3 %s ",ip));
		return response.toString().contains("64 bytes from");
	}
	/**
	 * ping from specific interface to ipv6.
	 * 
	 * @param ip
	 * @param fromEth
	 * @throws Exception
	 */
	protected void ping6(String ip, String fromEth) throws Exception
	{
		DebianResponse response = execute("ping6 -c 3 " + ip + "%" + fromEth);
		response.verify("64 bytes from");
	}
	/**
	 * Show all Sensor logs
	 * 
	 * @throws Exception
	 */
	public void showAllLogs() throws Exception
	{
	}

	/**
	 * wget to specific url
	 * 
	 * @param address
	 * @throws Exception
	 */
	public void wget(String address) throws Exception
	{
		DebianResponse response = execute("wget " + address);
		logger.info("wget: " + response.toString());
	}

	/**
	 * Sniff (tshark)
	 * 
	 * @param intNum
	 * @param filter
	 * @param duSec
	 *            = Stop writing to a capture file after value seconds have elapsed.
	 * @throws Exception
	 */
	public DebianResponse tshark(int intNum, String filter, int duSec) throws Exception
	{
		StringBuffer cmd = new StringBuffer("PCAP_USE_PFRING=yes tshark -T fields ");
		cmd.append("-e ip.src -e ip.dst -e tcp.dstport -e udp.dstport -E separator=, -E quote=d -E header=y ");
		cmd.append("-a duration:" + duSec);
		cmd.append(" -f '" + filter + "' ");
		cmd.append("-i " + intNum );
		String xmlResponse = execute(cmd).toString();
		logger.debug("tshark: " + xmlResponse);
		return new DebianResponse(xmlResponse, "csv");
	}

	/**
	 * tcpreplay tool should be installed on debian machine
	 * 
	 * @param fileName
	 * @return
	 * @throws Exception
	 */
	public DebianResponse injectTraffic(String fileName) throws Exception
	{
		return injectTraffic(0, "/usr/local/WirelessDumpFiles", fileName);
	}

	/**
	 * tcpreplay tool should be installed on debian machine
	 * 
	 * @param intNum
	 *            interface number
	 * @param path
	 * @param fileName
	 * @return
	 * @throws Exception
	 */
	public DebianResponse injectTraffic(int intNum, String path, String fileName) throws Exception
	{
		String countersResponse = execute(String.format("tcpreplay --intf1=eth%s %s/%s", intNum, path, fileName)).toString();
		logger.debug("tcpreplay: " + countersResponse);
		return new DebianResponse(countersResponse, "colon");
	}

	/**
	 * delete sensor logs
	 * 
	 * @throws Exception
	 */
	public void cleanLogs() throws Exception
	{
		execute("rm -rf /var/log/");

	}

	/**
	 * disconnect and connect
	 * 
	 * @throws Exception
	 */
	protected void reconnect() throws Exception
	{
		disconnect();
		Thread.sleep(2000);
		tryConnect();
	}

	public boolean isConnect()
	{
		return isConnect;
	}

	public void setConnect(boolean isConnect)
	{
		this.isConnect = isConnect;
	}

}
