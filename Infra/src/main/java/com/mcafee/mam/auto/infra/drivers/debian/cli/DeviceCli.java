package com.mcafee.mam.auto.infra.drivers.debian.cli;

import org.apache.log4j.Logger;

import com.mcafee.mam.auto.infra.Stopwatch;
import com.mcafee.mam.auto.infra.TestException;
import com.mcafee.mam.auto.infra.drivers.debian.DebianCommand;
import com.mcafee.mam.auto.infra.drivers.debian.DebianInfra;
import com.mcafee.mam.auto.infra.drivers.debian.DebianResponse;
import com.mcafee.mam.auto.infra.drivers.debian.Prompt;

/**
 * A driver for communicating with MAM sensor.
 * 
 * @author guy
 */
public class DeviceCli extends DebianInfra
{
	private static Logger logger = Logger.getLogger(DeviceCli.class);
	private boolean connected = false;

	/**
	 * * performs update for all packages
	 * 
	 * @throws Exception
	 */
	@Override
	public void init() throws TestException
	{
		if (this.host.startsWith("${"))
		{
			String paramName = this.host.substring(this.host.indexOf("{") + 1, this.host.indexOf('}'));
			this.host = System.getProperty(paramName);
		}
		if (this.user.startsWith("${"))
		{
			String paramName = this.user.substring(this.user.indexOf("{") + 1, this.user.indexOf('}'));
			this.user = System.getProperty(paramName);
		}
		if (this.password.startsWith("${"))
		{
			String paramName = this.password.substring(this.password.indexOf("{") + 1, this.password.indexOf('}'));
			this.password = System.getProperty(paramName);
		}
	}

	/**
	 * Delete all mails
	 * 
	 * @return
	 * @throws Exception
	 */
	public DebianResponse deleteAllMails(String user) throws Exception
	{
		return execute("rm -rf /var/mail/" + user);
	}

	/**
	 * Get specific mail message mails
	 * 
	 * @return
	 * @throws Exception
	 */
	public DebianResponse getMail(String user, String msgToFind) throws Exception
	{
		DebianResponse response = execute(String.format("cat /var/mail/%s", user));
		if (!response.toString().isEmpty())
		{
			return execute(String.format("cat /var/mail/%s | grep '%s'", user, msgToFind));
		}
		else
		{
			return response;
		}
	}

	/**
	 * Linux ssh mail (Wait for specific mail text)
	 * 
	 * @param alert
	 * @return
	 * @throws Exception
	 */
	public boolean waitForMail(String user, String msgToFind) throws Exception
	{
		logger.info("waiting for alert: " + msgToFind);
		Stopwatch stopwatch = this.getStopwatch();
		do
		{
			DebianResponse response = getMail(user, msgToFind);
			if (!response.toString().isEmpty()) { return true; }
			stopwatch.waitFor("mail text: " + msgToFind);
		}
		while (stopwatch.hasTime());
		logger.error("waiting for mail text" + msgToFind + " failed");
		if (this.isThrowException()) { throw new TestException("waiting for mail text" + msgToFind + " failed"); }
		return false;
	}

	/**
	 * Adding an alias to an existing NIC in Linux.
	 * 
	 * @param ip
	 * @return
	 * @throws Exception
	 */
	public DebianResponse addIpAliasOnEth0ClassC(String ip) throws Exception
	{
		return addIpAliasOnEth0(ip, "255.255.255.0");
	}

	/**
	 * Adding an alias to an existing NIC in Linux.
	 * 
	 * @param ip
	 * @param subnet
	 * @return
	 * @throws Exception
	 */
	public DebianResponse addIpAliasOnEth0(String ip, String subnet,int exitCode) throws Exception
	{
		return execute(String.format("ifconfig eth0:0 %s %s up", ip, subnet,exitCode));
	}
	/**
	 * Adding an alias to an existing NIC in Linux.
	 * 
	 * @param ip
	 * @param subnet
	 * @return
	 * @throws Exception
	 */
	public DebianResponse addIpAliasOnEth0(String ip, String subnet) throws Exception
	{
		return execute(String.format("ifconfig eth0:0 %s %s up", ip, subnet));
	}
	/**
	 * 
	 * @param ip
	 * @param user
	 * @param password
	 * @return
	 * @throws Exception
	 */
	public DebianResponse openTelnet(String ip ,String port) throws Exception
	{
		DebianCommand upgradeCommand = new DebianCommand(String.format("telnet %s %s ", ip, port));
		upgradeCommand.addPrompt(new Prompt("\n \n \n \n quit"));
		return execute(upgradeCommand);
	}
	/**
	 * 
	 * @param ip
	 * @param user
	 * @param password
	 * @return
	 * @throws Exception
	 */
	public DebianResponse openSSH(String ip, String user, String password) throws Exception
	{
		DebianCommand cmd = new DebianCommand(String.format("ssh %s@%s ",user, ip));
		cmd.addPrompt(new Prompt(String.format("%s@%s's password:",user,ip), password + "\n"));
		return execute(cmd);
	}
	/**
	 * Delete all sys log messages
	 * 
	 * @return
	 * @throws Exception
	 */
	public DebianResponse deleteAllSysLogMsg() throws Exception
	{
		return execute("rm -rf /var/log/messages");
	}
	/**
	 * Get specific syslog message
	 * 
	 * @param msgToFind
	 * @return
	 * @throws Exception
	 */
	public DebianResponse getSyslogMsg(String msgToFind) throws Exception
	{
		DebianResponse response = null;
		try
		{
			response = execute("cat /var/log/messages");
		}
		catch (Exception e) 
		{
			response = new DebianResponse("");
		}
		if (!response.toString().isEmpty())
		{
			return execute(String.format("cat /var/log/messages | grep '%s'", msgToFind));
		}
		else
		{
			return response;
		}
	}

	/**
	 * Linux ssh mail (Wait for specific syslog text)
	 * 
	 * @param alert
	 * @return
	 * @throws Exception
	 */
	public boolean waitForSyslogMsg(String msgToFind) throws Exception
	{
		logger.info("waiting for alert in syslog server: " + msgToFind);
		Stopwatch stopwatch = this.getStopwatch();
		do
		{
			DebianResponse response = getSyslogMsg(msgToFind);
			if (!response.toString().isEmpty()) { return true; }
			stopwatch.waitFor("syslog text: " + msgToFind);
		}
		while (stopwatch.hasTime());
		logger.error("waiting for syslog text" + msgToFind + " failed");
		if (this.isThrowException()) { throw new TestException("waiting for syslog text" + msgToFind + " failed"); }
		return false;
	}

	/**
	 * 
	 * @return
	 */
	public boolean isConnected()
	{
		return connected;
	}

	/**
	 * 
	 * @param isConnected
	 */
	public void setConnected(boolean isConnected)
	{
		this.connected = isConnected;
	}
	
	public void copyFileToServer(String fileName,String server,String user, String password)
	{
		try
		{
			logger.info("coping " + fileName +" to another server");
			DebianCommand upgradeCommand = new DebianCommand(String.format("scp /tmp/%s %s@%s:/tmp/",fileName,user,server));
			upgradeCommand.addPrompt(new Prompt(String.format("%s@%s's password:",user,server), password + "\n"));
			execute(upgradeCommand);
		}
		catch (Exception e)
		{
			logger.error("failed to copy file " + e.toString());
			
		}
		
	}

}
