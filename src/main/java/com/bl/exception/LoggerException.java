package com.bl.exception;

/**
 * This Exception is intended for any issue in the log class
 * @author Teddy
 *
 */
public class LoggerException extends ProjectException {

	
	/**
	 * Constructor that receives the message of the error on the logger
	 * @param message The message for the exception
	 */
	public LoggerException(String message) {
		super(message);
		
	}
	
	/**
	 * Constructor that receives the message of the error and the exception that triggered this one
	 * @param message The message for the exception in the logger
	 * @param e The exception that triggered this one
	 */
	public LoggerException(String message, Exception e)
	{
		super(message, e);
	}

}
