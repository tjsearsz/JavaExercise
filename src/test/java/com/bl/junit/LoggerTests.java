package com.bl.junit;

import org.junit.Assert;
import org.junit.Test;
import org.powermock.api.mockito.PowerMockito;

import com.bl.exception.LoggerException;
import com.bl.logger.JobLogger;
import com.bl.logger.LevelOfMessage;

import static org.mockito.Mockito.*;

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
				() -> JobLogger.LogMessage(null, false, true, false, LevelOfMessage.WARNING));
		Assert.assertTrue(exception.getMessage().equals("The Message cannot be null"));		
		
	}
	
	/**
	 * Unit test to verify that a empty message can't be added
	 */
	@Test
	public void MessageCannotBeBlankTest()
	{
		LoggerException exception = Assert.assertThrows(LoggerException.class, 
				() -> JobLogger.LogMessage("                ", true, false, true, LevelOfMessage.ERROR));
		Assert.assertTrue(exception.getMessage().equals("The message cannot contain only white space"));
	}
	
	/**
	 * Unit test to verify that destination for the message must be specified
	 */
	@Test
	public void LogDestinationMustBeSpecifiedTest()
	{
		LoggerException exception = Assert.assertThrows(LoggerException.class, 
				() -> JobLogger.LogMessage("This message", false, false, false, LevelOfMessage.MESSAGE));
		Assert.assertTrue(exception.getMessage().equals("Invalid configuration"));
	}
	
	/**
	 * Unit test to verify that destination for the message must be specified
	 */
	@Test
	public void TypeOfTheMessageMustBeSpecifiedTest()
	{
		LoggerException exception = Assert.assertThrows(LoggerException.class, 
				() -> JobLogger.LogMessage("This message", true, false, false, null));
		Assert.assertTrue(exception.getMessage().equals("Error or Warning or Message must be specified"));
	}
	
	
	@Test
	public void LogIntoConsoleAnErrorMessageTest()
	{
		PowerMockito.mockStatic(JobLogger.class);
		PowerMockito.doThrow(new LoggerException("There was an error trying to access to the console")).when(JobLogger.class);
		//JobLogger.LogMessage("the message", false, true, false, true, false, false);
		//LoggerException exception = Assert.assertThrows(LoggerException.class, () -> JobLogger.);		
		//LoggerException exception2 = doThrow(new LoggerException("There was an error trying to access to the console")).
		//when(JobLogger.class).LogMessage("this message", false, true, false, true, false, false);
		
		
		
	}
}
