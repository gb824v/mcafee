package com.mcafee.mam.auto.infra.util;
/**
 * 
 * @author gbitan
 *
 */
public class MAC
{
	static byte[] addressBytes;
	
	public static long convertSensorMacToLong(String mac)
	{
		initMacProperties(mac, ":");
		long calculatedLongValue = convertBytesToLong();
		return calculatedLongValue;
	}
	public static long convertConsoleMacToLong(String mac)
    {
        String[] MacFilterArr = mac.split(":");
        String MacAsLong = "";
        for (int i =0 ; i<MacFilterArr.length ; i++)
            MacAsLong = MacAsLong + MacFilterArr[i];
        return Long.parseLong(MacAsLong,16);
    }
	private static void initMacProperties(String macString, String delim)
	{
		int delimLen = delim.length();
		int curPos = 0;
		addressBytes = new byte[6];
		for (int i = 0; i < addressBytes.length; i++)
		{
			addressBytes[i] = (byte) Integer.parseInt(macString.substring(curPos, curPos + 2), 16);
			curPos += 2 + delimLen;
		}
	}
	/**
	 * retrieves a long value representing this mac, but in reverse <BR>
	 * please note: this is not interchangable with getAsLong!!!
	 * 
	 * @return a long value representing this mac, but in reverse
	 */
	private static long convertBytesToLong()
	{
		byte[] cDataRev = new byte[8];
		cDataRev[0] = 0;
		cDataRev[1] = 0;
		cDataRev[2] = addressBytes[5];
		cDataRev[3] = addressBytes[4];
		cDataRev[4] = addressBytes[3];
		cDataRev[5] = addressBytes[2];
		cDataRev[6] = addressBytes[1];
		cDataRev[7] = addressBytes[0];
		return convertQWordToLong(cDataRev);
	}

	private static Long convertQWordToLong(byte[] cData){
        return convertQWordToLong(cData,0);
    }

	private static Long convertQWordToLong(byte[] cData, int fromPos)
	{
		return convertNBytesToLong(cData, fromPos, 8);

	}

	private static Long convertNBytesToLong(byte[] cData, int fromPos, int n)
	{
		long tempLong = 0;
		for (int i = 0; i < n; ++i)
		{
			tempLong <<= 8;
			tempLong |= cData[i + fromPos] & 0xFF;
		}
		return new Long(tempLong);

	}
}
