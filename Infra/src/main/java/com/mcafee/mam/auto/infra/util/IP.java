package com.mcafee.mam.auto.infra.util;

import java.net.InetAddress;
import java.net.UnknownHostException;

import com.mcafee.mam.auto.infra.TestException;
/**
 * 
 * @author gbitan
 *
 */
public class IP
{

	public static String Ipv4ToIpv6(String ipAddress)
	{
		if (isIpv6(ipAddress)) { return ipAddress.toLowerCase().substring(0, ipAddress.indexOf("/")); }
		String[] ipSplit = ipAddress.split("\\.");
		StringBuilder result = new StringBuilder("0:0:0:0:0:FFFF:");
		for (int i = 1; i <= 4; ++i)
		{
			result.append(Integer.toHexString(Integer.parseInt(ipSplit[i - 1])));
			if (i == 2)
			{
				result.append(":");
			}
		}

		return (result.toString().toUpperCase());
	}

	public static String Ipv6ToIpv4(String ipAddress)
	{
		String[] ipSplit = ipAddress.split(":");
		String str1 = ipSplit[ipSplit.length - 2].substring(0, 2);
		String str2 = ipSplit[ipSplit.length - 2].substring(2);
		String str3 = ipSplit[ipSplit.length - 1].substring(0,2);
		String str4 = ipSplit[ipSplit.length - 1].substring(2);
		return Integer.parseInt(str1, 16) + "." + 
			   Integer.parseInt(str2, 16) + "." + 
		       Integer.parseInt(str3, 16) + "." + 
			   Integer.parseInt(str4, 16);
	}

	public static boolean isIpInRange(String ipLo, String ipHi, String ipToTest) throws TestException
	{
		long lIpLo = 0;
		long lIpHi = 0;
		long lIpToTest = 0;
		try
		{
			lIpLo = ipToLong(InetAddress.getByName(ipLo));
			lIpHi = ipToLong(InetAddress.getByName(ipHi));
			lIpToTest = ipToLong(InetAddress.getByName(ipToTest));
		}
		catch (UnknownHostException e)
		{
			throw new TestException("isIpInRange: The ip's are invalid! ");
		}

		return (lIpToTest >= lIpLo && lIpToTest <= lIpHi);
	}

	private static long ipToLong(InetAddress ip)
	{
		byte[] octets = ip.getAddress();
		long result = 0;
		for (byte octet : octets)
		{
			result <<= 8;
			result |= octet & 0xff;
		}
		return result;
	}

	private static boolean isIpv6(String ipAddress)
	{
		return (ipAddress.indexOf("::") >= 0);
	}
}
