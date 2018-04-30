package com.mcafee.mam.auto.infra.drivers.debian;

import java.util.HashMap;

public class ProcessStatus
{
	private HashMap<String, String> process = new HashMap<String, String>();

	public ProcessStatus()
	{
		process.put("rootfs", "accessible");
		process.put("snmper.pid", "accessible");
		process.put("everest", "running");
		process.put("indise", "running");
		process.put("system_mam.mam", "running");
	}

	/**
	 * find a record with specific name and value
	 * 
	 * @param name
	 *            record name.
	 * @return null if no such record can be found.
	 */
	public void setStatus(String cliOutput)
	{
		String[] cliOutputArr = cliOutput.split("\n");

		/* find line of Status */
		for (int i = 0; i < cliOutputArr.length; i++)
		{
			if (cliOutputArr[i].startsWith("Process") || cliOutputArr[i].startsWith("File") || cliOutputArr[i].startsWith("System "))

			{
				String[] lineArr = cliOutputArr[i].split("'");
				String name = lineArr[1].trim();
				String status = lineArr[2].trim();
				process.put(name, status);
			}
		}

	}

	public String getStatus(String name)
	{
		return this.process.get(name);
	}

	public boolean isAllRunning()
	{
		if (process.get("rootfs").equals("accessible") && process.get("everest").equals("running") && process.get("indise").equals("running")
				&& process.get("system_mam.mam").equals("running") && process.get("snmper.pid").equals("accessible"))
		{
			return true;
		}
		else
		{
			return false;
		}
	}
}
