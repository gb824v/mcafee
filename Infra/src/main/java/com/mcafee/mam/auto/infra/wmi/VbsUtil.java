package com.mcafee.mam.auto.infra.wmi;

import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;
import com.google.common.io.Files;
import com.mcafee.mam.auto.infra.TestDriver;
import com.mcafee.mam.auto.infra.device.Device;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public final class VbsUtil extends TestDriver
{
	private static final String cmdAgent = "C:/Program Files (x86)/McAfee/Common Framework/CmdAgent.exe";

	public static String collectAndSendProps(Device device) throws IOException, InterruptedException
	{
		if (System.getProperty("os.name").toLowerCase().indexOf("win") >= 0)
		{
			return createAndRunScript(device, "wmiRunApp", cmdAgent + " /p");
		}
		else
		{
			return invokeLinuxCmd(device, cmdAgent, "/p");
		}
	}

	public static String enforcePolicies(Device device) throws IOException, InterruptedException
	{
		if (System.getProperty("os.name").toLowerCase().indexOf("win") >= 0)
		{
			return createAndRunScript(device, "wmiRunApp", cmdAgent + " /e");
		}
		else
		{
			return invokeLinuxCmd(device, cmdAgent, "/e");
		}
	}

	private static String taskList(Device device) throws IOException, InterruptedException
	{
		if (System.getProperty("os.name").toLowerCase().indexOf("win") >= 0)
		{
			return createAndRunScript(device, "wmiTasksList", "");
		}
		else
		{
			return invokeLinuxCmd(device, "tasklist", "");
		}
	}

	public static boolean isProcessRunning(Device device, String process) throws IOException, InterruptedException
	{
		return taskList(device).contains(process);
	}

	public static boolean isServiceRunning(Device device, String service) throws IOException, InterruptedException
	{
		return serviceList(device).contains(service + " State: Running");
	}

	private static String serviceList(Device device) throws IOException, InterruptedException
	{
		if (System.getProperty("os.name").toLowerCase().indexOf("win") >= 0)
		{
			return createAndRunScript(device, "wmiServiceList", "");
		}
		else
		{
			return invokeLinuxCmd(device, "servicelist", "");
		}
	}

	private static String invoke(String cmd) throws IOException, InterruptedException
	{
		return CharStreams.toString(new InputStreamReader(Runtime.getRuntime().exec(cmd).getInputStream(), Charsets.UTF_8));
	}

	private static String createAndRunScript(Device device, String scriptName, String cmd) throws IOException, InterruptedException
	{
		InputStream intasklistvbs = VbsUtil.class.getClassLoader().getResourceAsStream("vbs/" + scriptName + ".vbs");
		String ip = device.getIp();
		String user = device.getUserName();
		String pass = device.getPassword();
		String domain = device.getDomain();
		String contents = CharStreams.toString(new InputStreamReader(intasklistvbs, Charsets.UTF_8));
		contents = contents.replaceAll("strComputer =", "strComputer = \"" + ip + "\"");
		contents = contents.replaceAll("strDomain =", "strDomain = \"" + domain + "\"");
		contents = contents.replaceAll("strUser =", "strUser = \"" + user + "\"");
		contents = contents.replaceAll("strPassword =", "strPassword = \"" + pass + "\"");
		if (!cmd.isEmpty())
		{
			contents = contents.replaceAll("strCmd =", "strCmd = \"" + cmd + "\"");
		}
		File newFile = new File(VbsUtil.class.getClassLoader().getResource("vbs").getPath() + "/" + scriptName + "New.vbs");
		Files.write(contents.getBytes(), newFile);
		String res = invoke("cscript " + newFile.getAbsolutePath());
		newFile.delete();
		return res;
	}

	private static String invokeLinuxCmd(Device device, String process, String argv) throws IOException, InterruptedException
	{
		String ip = device.getIp();
		String user = device.getUserName();
		String pass = device.getPassword();
		return invoke(String.format("winexe -U %s%'%s' \\\\%s \"%s\" %s", user, pass, ip, process, argv));
	}
}
