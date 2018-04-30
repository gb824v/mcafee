package com.mcafee.mam.auto.infra.drivers.debian;

import expect4j.Closure;
import expect4j.ExpectState;
import expect4j.matches.Match;
import expect4j.matches.RegExpMatch;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import org.apache.oro.text.regex.MalformedPatternException;

/**
 * a terminal command that is targeted to run on MAM debian machines.
 * 
 * @author danny & Guy
 */
public class DebianCommand implements Closure
{

	private StringBuilder cleanResult = new StringBuilder();
	private StringBuilder result = new StringBuilder();
	private List<Prompt> prompts = new ArrayList<Prompt>();
	private int exitCode = 0;
	private final String command;
	private Prompt lastMatch;

	/**
	 * constructs a new command
	 * 
	 * @param command
	 *            - command text
	 * @throws MalformedPatternException
	 */
	public DebianCommand(String command) throws MalformedPatternException
	{
		this.command = command;
		this.exitCode = 0;
		initPrompts();
	}

	/**
	 * constructs a new command
	 * 
	 * @param command
	 *            - command text
	 * @throws MalformedPatternException
	 */
	public DebianCommand(String command, int exitCode) throws MalformedPatternException
	{
		this.command = command;
		this.exitCode = exitCode;
		initPrompts();
	}

	private void initPrompts()
	{
		this.prompts.add(new Prompt("# "));
		this.prompts.add(new Prompt("~# "));
		this.prompts.add(new Prompt("~$  "));
		this.prompts.add(new Prompt("\\> "));
		this.prompts.add(new Prompt("root@mam:~# "));
	}

	/**
	 * implements the closure for expecting prompts from server response. This method is called
	 * after command had executed and one of the expected prompts had received. The received buffer
	 * is appended to the result and the 'lastmatch' field is set with the prompt that had been
	 * found.
	 * 
	 * @param es
	 * @throws Exception
	 */
	@Override
	public void run(ExpectState es) throws Exception
	{
		this.lastMatch = this.prompts.get(es.getPairIndex());
		this.result.append(es.getBuffer());
	}

	/**
	 * @return builds and get the list of patterns (=expected prompts) for this command.
	 */
	public List<Match> getPatterns() throws MalformedPatternException
	{
		List<Match> patterns = new LinkedList<Match>();
		Iterator<Prompt> iter = this.prompts.iterator();
		while (iter.hasNext())
		{
			patterns.add(new RegExpMatch(iter.next().getPattern(), this));
		}
		return patterns;

	}

	/**
	 * expected exit code for successful operation.
	 * 
	 * @return this is always 0
	 */
	public String getExpectedExitCode()
	{
		return String.valueOf(this.exitCode);
	}

	/***
	 * returns the command to execute.
	 * 
	 * @return
	 */
	public String getCommand()
	{
		return this.command;
	}

	@Override
	public String toString()
	{
		return this.command;
	}

	/***
	 * adds an expected prompt
	 * 
	 * @param prompt
	 */
	public void addPrompt(Prompt prompt)
	{
		this.prompts.add(prompt);
	}

	/***
	 * remove an expected prompt
	 * 
	 * @param prompt
	 */
	public void removePrompt(String prompt)
	{
		for (int i = 0; i < this.prompts.size(); i++)
		{
			if (this.prompts.get(i).getPattern().equals(prompt)) 
			{
				this.prompts.remove(i);
			}
		}
	}

	/***
	 * get the prompt that matched after the command had finished.
	 * 
	 * @return
	 */
	public Prompt getLastMatch()
	{
		return this.lastMatch;
	}

	/***
	 * get the response received from terminal for the last successful match.
	 * 
	 * @return null if nothing was matched.
	 */
	public String getResponse()
	{
		if (this.lastMatch != null) { return this.lastMatch.getResponse(); }
		return null;
	}

	/***
	 * returns the terminal response.
	 * 
	 * @return
	 */
	public String getResult()
	{
		return this.result.toString();
	}

	/***
	 * returns the terminal response without command.
	 * 
	 * @return
	 */
	public String getCleanResult()
	{
		String results[] = this.result.toString().split("\\r\\n");

		if (results.length > 2)
		{
			for (int i = 1; i < results.length - 1; i++)
			{
				this.cleanResult.append(results[i].trim().concat("\n"));
			}
		}
		else if (results.length == 2)
		{
			this.cleanResult.append("");
		}
		else
		{
			this.cleanResult = this.result;
		}
		return this.cleanResult.toString();
	}
}
