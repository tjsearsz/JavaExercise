package com.bl.junit;

import org.junit.Assert;
import org.junit.Test;

import com.bl.exception.LoggerException;
import com.bl.logger.JobLogger;

/**
 * Class that will have all the unit tests for the Logger
 * @author Teddy
 *
 */
public class LoggerTests {	
	
	/**
	 * Unit test to verify that a null message can't be added
	 */
	@Test
	public void MessageCannotBeNullTest()
	{
		LoggerException exception = Assert.assertThrows(LoggerException.class, 
				() -> JobLogger.LogMessage(null));
		Assert.assertTrue(exception.getMessage().equals("The Message cannot be null"));		
		
	}
	
	/**
	 * Unit test to verify that a empty message can't be added
	 */
	@Test
	public void MessageCannotBeBlankTest()
	{
		LoggerException exception = Assert.assertThrows(LoggerException.class, 
				() -> JobLogger.LogMessage("                "));
		Assert.assertTrue(exception.getMessage().equals("The message cannot contain only white space"));
	}
	
	/**
	 * Unit test to verify that destination for the message must be specified
	 */
	@Test
	public void LogDestinationMustBeSpecifiedTest()
	{
		LoggerException exception = Assert.assertThrows(LoggerException.class, 
				() -> JobLogger.LogMessage("this message"));
		Assert.assertTrue(exception.getMessage().equals("Invalid configuration"));
	}
	
	/**
	 * Unit test to verify that destination for the message must be specified
	 */
	@Test
	public void TypeOfTheMessageMustBeSpecifiedTest()
	{
		LoggerException exception = Assert.assertThrows(LoggerException.class, 
				() -> JobLogger.LogMessage("this message"));
		Assert.assertTrue(exception.getMessage().equals("Error or Warning or Message must be specified"));
	}
}
