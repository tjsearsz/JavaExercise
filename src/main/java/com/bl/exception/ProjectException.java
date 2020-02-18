package com.bl.exception;

/**
 * Class that represents all the custom exceptions for this project
 * @author Teddy
 *
 */
public class ProjectException extends Exception {
	
	/**
	 * Constructor that receives the message of the error
	 * @param message The message for the exception
	 */
	public ProjectException(String message)
	{
		super(message);
	}
	
	/**
	 * Constructor that receives the message of the error and the exception that triggered this one
	 * @param message The message for the exception
	 * @param e The exception that triggered this one
	 */
	public ProjectException(String message, Exception e)
	{
		super(message, e);
	}

}
