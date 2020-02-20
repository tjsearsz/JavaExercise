package com.bl.junit;

import java.text.DateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import com.bl.exception.LoggerException;
import com.bl.logger.JobLogger;
import com.bl.logger.LevelOfMessage;

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
				() -> JobLogger.LogMessage(null, false, true, false, LevelOfMessage.WARNING, null));
		Assert.assertTrue(exception.getMessage().equals("The Message cannot be null"));		
		
	}
	
	/**
	 * Unit test to verify that a empty message can't be added
	 */
	@Test	
	public void MessageCannotBeBlankTest()
	{
		LoggerException exception = Assert.assertThrows(LoggerException.class, 
				() -> JobLogger.LogMessage("                ", true, false, true, LevelOfMessage.ERROR, null));
		Assert.assertTrue(exception.getMessage().equals("The message cannot contain only white space"));
	}
	
	/**
	 * Unit test to verify that destination for the message must be specified
	 */
	@Test	
	public void LogDestinationMustBeSpecifiedTest()
	{
		LoggerException exception = Assert.assertThrows(LoggerException.class, 
				() -> JobLogger.LogMessage("This message", false, false, false, LevelOfMessage.MESSAGE, null));
		Assert.assertTrue(exception.getMessage().equals("Invalid configuration"));
	}
	
	/**
	 * Unit test to verify that destination for the message must be specified
	 */
	@Test	
	public void TypeOfTheMessageMustBeSpecifiedTest()
	{
		LoggerException exception = Assert.assertThrows(LoggerException.class, 
				() -> JobLogger.LogMessage("This message", true, false, false, null, null));
		Assert.assertTrue(exception.getMessage().equals("Error or Warning or Message must be specified"));
	}
	
	/**
	 * Unit test to verify that every type of message can be logged in the console
	 * @throws LoggerException
	 */
	@Test
	public void LogAllTypesOfMessagesIntoTheConsoleTest() throws LoggerException
	{
		//Getting the logger
		Logger logger = Logger.getLogger("MyLog");
		
		//Instantiating our custom handler
		final LoggerTestsHandler handler = new LoggerTestsHandler();
		
		//Allowing handler to take all types of level
		handler.setLevel(Level.ALL);		
		
		//With this we ensure that we don't take logs for other handlers already predefined (parents) 
		logger.setUseParentHandlers(false);
		
		//Adding the handler in the logger
		logger.addHandler(handler);
		
		/*******************************************Warning Message ****************************************/
		//Executing the log process
		JobLogger.LogMessage("This a warning message", false, true, false, LevelOfMessage.WARNING, null);		
		
		//Asserting that the level recorded is the same
		Assert.assertTrue(handler.getLevelRecorded().intValue() == Level.WARNING.intValue());
		
		//Asserting that the message is the same
		Assert.assertTrue(handler.getMessageRecorded().equals("warning " +
		DateFormat.getDateInstance(DateFormat.LONG).format(new Date()) + " " + "This a warning message"));
		/****************************************************************************************************/
		/********************************************Error Message ******************************************/
		//Executing the log process
		JobLogger.LogMessage("This an error message", false, true, false, LevelOfMessage.ERROR, null);		
		
		//Asserting that the level recorded is the same
		Assert.assertTrue(handler.getLevelRecorded().intValue() == Level.SEVERE.intValue());		
		
		//Asserting that the message is the same
		Assert.assertTrue(handler.getMessageRecorded().equals("error " + 
		DateFormat.getDateInstance(DateFormat.LONG).format(new Date()) + " " + "This an error message"));
		/****************************************************************************************************/
		/*******************************************Information Message *************************************/
		//Executing the log process
		JobLogger.LogMessage("This an info message", false, true, false, LevelOfMessage.MESSAGE, null);		
		
		//Asserting that the level recorded is the same
		Assert.assertTrue(handler.getLevelRecorded().intValue() == Level.INFO.intValue());		
		
		//Asserting that the message is the same
		Assert.assertTrue(handler.getMessageRecorded().equals("message " + 
		DateFormat.getDateInstance(DateFormat.LONG).format(new Date()) + " " + "This an info message"));
		/****************************************************************************************************/
		
		//Removing the handler to avoid memory leak
		logger.removeHandler(handler);
	}
	
	/**
	 * Unit test to verify that every type of message can be logged in the console
	 * @throws LoggerException
	 */
	@Test
	public void LogAllTypesOfMessagesIntoTheFileTest() throws LoggerException
	{
		
	}
	
	@Test
	@Ignore
	public void LogIntoConsoleAnErrorMessageTest()
	{
		//PowerMockito.mockStatic(JobLogger.class);
		//PowerMockito.doThrow(new LoggerException("There was an error trying to access to the console")).when(JobLogger.class);
		//JobLogger.LogMessage("the message", false, true, false, true, false, false);
		//LoggerException exception = Assert.assertThrows(LoggerException.class, () -> JobLogger.);		
		//LoggerException exception2 = doThrow(new LoggerException("There was an error trying to access to the console")).
		//when(JobLogger.class).LogMessage("this message", false, true, false, true, false, false);
		
		
		
	}
}
