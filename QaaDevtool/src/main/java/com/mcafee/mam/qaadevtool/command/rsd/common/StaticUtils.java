package com.mcafee.mam.qaadevtool.command.rsd.common;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang.StringUtils;
import org.springframework.mock.web.MockHttpServletRequest;
import com.mcafee.orion.core.OrionCore;
import com.mcafee.orion.core.auth.OrionUser;
import com.mcafee.orion.core.cmd.CommandException;

public class StaticUtils
{

	private static final String PATTERN_IPV4 = "^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." + "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\."
			+ "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." + "([01]?\\d\\d?|2[0-4]\\d|25[0-5])$";
	private static final String PATTERN_IPV6 = "^((([0-9A-Fa-f]{1,4}:){7}[0-9A-Fa-f]{1,4})|(([0-9A-Fa-f]{1,4}:){6}:[0-9A-Fa-f]{1,4})|(([0-9A-Fa-f]{1,4}:){5}:([0-9A-Fa-f]{1,4}:)?[0-9A-Fa-f]{1,4})|(([0-9A-Fa-f]{1,4}:){4}:([0-9A-Fa-f]{1,4}:){0,2}[0-9A-Fa-f]{1,4})|(([0-9A-Fa-f]{1,4}:){3}:([0-9A-Fa-f]{1,4}:){0,3}[0-9A-Fa-f]{1,4})|(([0-9A-Fa-f]{1,4}:){2}:([0-9A-Fa-f]{1,4}:){0,4}[0-9A-Fa-f]{1,4})|(([0-9A-Fa-f]{1,4}:){6}((\\b((25[0-5])|(1\\d{2})|(2[0-4]\\d)|(\\d{1,2}))\\b)\\.){3}(\\b((25[0-5])|(1\\d{2})|(2[0-4]\\d)|(\\d{1,2}))\\b))|(([0-9A-Fa-f]{1,4}:){0,5}:((\\b((25[0-5])|(1\\d{2})|(2[0-4]\\d)|(\\d{1,2}))\\b)\\.){3}(\\b((25[0-5])|(1\\d{2})|(2[0-4]\\d)|(\\d{1,2}))\\b))|(::([0-9A-Fa-f]{1,4}:){0,5}((\\b((25[0-5])|(1\\d{2})|(2[0-4]\\d)|(\\d{1,2}))\\b)\\.){3}(\\b((25[0-5])|(1\\d{2})|(2[0-4]\\d)|(\\d{1,2}))\\b))|([0-9A-Fa-f]{1,4}::([0-9A-Fa-f]{1,4}:){0,5}[0-9A-Fa-f]{1,4})|(::([0-9A-Fa-f]{1,4}:){0,6}[0-9A-Fa-f]{1,4})|(([0-9A-Fa-f]{1,4}:){1,7}:))$";

	private static final String PATTERN_BELOW_100 = "^(0?[1-9]|[1-9][0-9])$";

	public static boolean validateIPV4(final String ip)
	{

		Pattern pattern = Pattern.compile(PATTERN_IPV4);
		Matcher matcher = pattern.matcher(ip);
		return matcher.matches();
	}

	public static boolean validateIPV6(final String ip)
	{

		Pattern pattern = Pattern.compile(PATTERN_IPV6);
		Matcher matcher = pattern.matcher(ip);
		return matcher.matches();
	}

	public static boolean validateIPs(final String inputIPs, boolean isIPV4)
	{
		String[] ips = inputIPs.split("\\|");

		Pattern pattern = null;

		if (isIPV4)
		{
			pattern = Pattern.compile(PATTERN_IPV4);
		} else
		{
			pattern = Pattern.compile(PATTERN_IPV6);
		}

		for (String ip : ips)
		{
			Matcher matcher = pattern.matcher(ip);
			if (!matcher.matches())
			{
				return false;
			}
		}
		return true;
	}

	public static boolean validateIPsWithPort(String hiddenIncludeList)
	{

		String[] ips = hiddenIncludeList.split("\\|");

		Pattern pattern = null;

		for (String ip : ips)
		{
			String[] tokens = ip.split("/");
			pattern = Pattern.compile(PATTERN_IPV4);
			Matcher matcher = pattern.matcher(tokens[0]);
			if (!matcher.matches())
			{
				return false;
			}

			pattern = Pattern.compile(PATTERN_BELOW_100);
			matcher = pattern.matcher(tokens[1]);
			if (!matcher.matches())
			{
				return false;
			}
		}
		return true;

	}

	public static void main1(String[] args)
	{
		System.out.println(validateIPV4("192.168.1.33"));
		System.out.println(validateIPV6("ff02::110"));
		System.out.println(validateIPsWithPort("192.168.1.33/10|192.168.1.33/10|192.168.1.00/01"));
	}

	public static void main(String[] args)
	{
		MockHttpServletRequest request = new MockHttpServletRequest();

		request.getParameterMap().put("Test", new String[]
		{"sersdf"});

		System.out.println(request.getParameterMap());

	}
	public static void throwIfInvalid(Object value, String fieldName)
	{
		if (null == value)
		{
			throw new RemoteCommandException("Invalid value for fieldName=" + fieldName + " Value=" + value);
		} else if (value instanceof Integer && ((Integer) value) < 1)
		{
			throw new RemoteCommandException("Invalid value for fieldName=" + fieldName + " Value=" + value);
		} else if (value instanceof Collection<?> && ((Collection<?>) value).isEmpty())
		{
			throw new RemoteCommandException("Invalid value for fieldName=" + fieldName + " Value=" + value);
		}

	}
	public static List<String> convertCSVToList(String str)
	{
		if (StringUtils.isBlank(str))
		{
			return new ArrayList<String>();
		}

		return Arrays.asList(str.split(","));
	}

	public static String getCommanSeperated(List<?> values)
	{
		StringBuilder titles = new StringBuilder();
		for (Object value : values)
		{
			titles.append("'" + value + "'" + ",");
		}

		return titles.substring(0, titles.length() - 1);
	}

	public static void sendAgentAwakeUp(String clientList, OrionUser user) throws CommandException
	{
		Map<String, Object> commandParamMap = new Hashtable<String, Object>();
		commandParamMap.put("names", clientList);
		commandParamMap.put("forceFullPolicyUpdate", true);
		OrionCore.getCommandInvoker().invoke("system.wakeupAgent", commandParamMap, user);
	}

	public static void assignPolicyToNodes(String clientList, int policyTypeID, int policyID, OrionUser user, String productId)
			throws CommandException
	{
		Map<String, String> commandParamMap = new Hashtable<String, String>();
		commandParamMap.put("names", clientList);
		commandParamMap.put("typeId", policyTypeID + "");
		commandParamMap.put("objectId", policyID + "");
		commandParamMap.put("productId", productId);
		OrionCore.getCommandInvoker().invoke("policy.assignToSystem", commandParamMap, user);
	}

}
