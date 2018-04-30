package com.mcafee.mam.auto.infra;
import com.mcafee.mam.auto.infra.Runner;

/**
 * QAA runner application main
 * 
 */
public class App
{
	public static void main(String[] args) throws Exception
	{
		if (args.length == 0)
		{
			System.err.println("Usage: [scenario1] [scenario2] [...]");
			return;
		}
		Runner testRunner = new Runner();
		for (int i = 0; i < args.length; i++)
		{
			testRunner.addScenario(args[i]);
		}
		testRunner.run();
	}
}