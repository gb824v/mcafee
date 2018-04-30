package com.mcafee.mam.auto.infra;

import org.apache.log4j.Logger;

/**
 * Utility class to provide a stop watch for tests timeout from configuration.
 * The idea of this, is to provide a common interface for configurating, waiting
 * and logging through-out the tests.
 *
 * @author danny
 */
public class Stopwatch {

    private static Logger logger = Logger.getLogger(Stopwatch.class);
    private long timeLeft;
    private long interval;

    /**
     * *
     * Constructs a new stop watch.
     *
     * @param timeout
     * @param interval
     */
    public Stopwatch(long timeout, long interval) {
        this.timeLeft = timeout;
        this.interval = interval;
    }

    /**
     * *
     * returns true if there is time left to wait another interval.
     *
     * @return
     */
    public boolean hasTime() {
        return (timeLeft >= interval);
    }

    /**
     * *
     * Sleeps 'interval' time, waiting for something to happen.
     *
     * @param forWhat - describe what are you waiting for.
     * @throws InterruptedException
     */
    public void waitFor(String forWhat) throws Exception {
    	
    	try
    	{
    	logger.debug("Waiting " + (interval / 1000) + " seconds for " + forWhat);
        Thread.sleep(interval);
        consumeInterval();
    	}
    	catch(Exception e)
    	{
    		throw new Exception("waitFor " + forWhat + " failed.");
    	}
    }

    /**
     * *
     * Decrease the time left with interval and return the interval value.
     *
     * @return
     */
    public long consumeInterval() {
        this.timeLeft = this.timeLeft - interval;
        return this.interval;
    }
}
