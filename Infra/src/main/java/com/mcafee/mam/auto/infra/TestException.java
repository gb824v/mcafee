package com.mcafee.mam.auto.infra;

/**
 * Represents an error while test.
 *
 * @author danny
 */
public class TestException extends Exception {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/***
     * constructs a new test exception
     * @param message - error message
     * @param th - cause
     */
    public TestException(String message, Throwable th) {
        super(message, th);
    }

    /***
     * constructs a new test exception without a cause.
     * @param message - error message
     */
    public TestException(String message) {
        super(message);
    }
}
