package com.bl.junit;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.AfterClass;
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
	 * Unit test to verify that the DbParam for logging in the file cannot be null
	 */
	@Test	
	public void FileParameterCannotBeNullTest()
	{		
		LoggerException exception = Assert.assertThrows(LoggerException.class, 
				() -> JobLogger.LogMessage("This is a message", true, false, false, LevelOfMessage.WARNING, null));
		Assert.assertTrue(exception.getMessage().equals("File parameter cannot be blank"));				
		
	}
	
	/**
	 * Unit test to verify that the File path must be specified
	 */
	@Test	
	public void FileParameterMustBeSpecifiedTest()
	{	
		final Map<String, String> dbParams = new HashMap<String, String>();
		
		//Asserting logFileFolder value must exist
		LoggerException exception = Assert.assertThrows(LoggerException.class, 
				() -> JobLogger.LogMessage("This is a message", true, false, false, LevelOfMessage.WARNING, dbParams));
		Assert.assertTrue(exception.getMessage().equals("File parameter has not been specified"));		
		
		//Asserting logFileFolder cannot be null
		dbParams.put("logFileFolder", null);		
		exception = Assert.assertThrows(LoggerException.class, 
				() -> JobLogger.LogMessage("This is a message", true, false, false, LevelOfMessage.WARNING, dbParams));
		Assert.assertTrue(exception.getMessage().equals("File parameter has not been specified"));
	}
	
	/**
	 * Unit test to verify that the FilePath cannot be anything except a variable that can be treated as a string
	 */
	@Test	
	public void FileParameterCanOnlyBeStringRelatedTypeTest()
	{	
		//Trying to use a non string as a file path
		final Map<String, Exception> dbParams1 = new HashMap<String, Exception>();
		dbParams1.put("logFileFolder", new Exception());
		
		//Asserting that an error is thrown
		LoggerException exception = Assert.assertThrows(LoggerException.class, 
				() -> JobLogger.LogMessage("This is a message", true, false, false, LevelOfMessage.WARNING, dbParams1));
		Assert.assertTrue(exception.getMessage().equals("File parameter must be a valid location"));
		
		//Trying to use a non string as a file path
		final Map<String, Integer> dbParams2 = new HashMap<String, Integer>();
		dbParams2.put("logFileFolder", 1);
		
		//Asserting that an error is thrown
		exception = Assert.assertThrows(LoggerException.class, 
				() -> JobLogger.LogMessage("This is a message", true, false, false, LevelOfMessage.WARNING, dbParams2));
		Assert.assertTrue(exception.getMessage().equals("File parameter must be a valid location"));
		
		//Trying to use a non string as a file path
		final Map<String, Boolean> dbParams3 = new HashMap<String, Boolean>();
		dbParams3.put("logFileFolder", true);
		
		//Asserting that an error is thrown
		exception = Assert.assertThrows(LoggerException.class, 
				() -> JobLogger.LogMessage("This is a message", true, false, false, LevelOfMessage.WARNING, dbParams3));
		Assert.assertTrue(exception.getMessage().equals("File parameter must be a valid location"));		
		
	}
	
	/**
	 * Unit test to verify that a correct file path has been used (it exists)
	 */
	@Test
	public void ValidFilePathMustExistTest()
	{
		//Trying to use a a non existant filepath
		final Map<String, String> dbParams = new HashMap<String, String>();
		dbParams.put("logFileFolder", "1294askefow");
		
		//Asserting that an error is thrown
		LoggerException exception = Assert.assertThrows(LoggerException.class, 
				() -> JobLogger.LogMessage("This is a message", true, false, false, LevelOfMessage.WARNING, dbParams));
		Assert.assertTrue(exception.getMessage().equals("An error has occurred trying to create, open a file"));
		
		//Trying to use a a non existant filepath
		dbParams.put("logFileFolder", "          ");
		
		//Asserting that an error is thrown
		exception = Assert.assertThrows(LoggerException.class, 
				() -> JobLogger.LogMessage("This is a message", true, false, false, LevelOfMessage.WARNING, dbParams));
		Assert.assertTrue(exception.getMessage().equals("An error has occurred trying to create, open a file"));
	}
	
	/**
	 * Unit test to verify that every type of message can be logged in the console 
	 * @throws IOException 
	 * @throws DocumentException 
	 * @throws JDOMException 
	 */
	@Test
	public void LogAllTypesOfMessagesIntoAfileTest() throws LoggerException, FileNotFoundException
	{
		//Using an real filepath
		final Map<String, String> dbParams = new HashMap<String, String>();
		dbParams.put("logFileFolder", System.getProperty("user.home"));
		
		String auxText = null;		
		
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
		JobLogger.LogMessage("This a warning message", true, false, false, LevelOfMessage.WARNING, dbParams);
		
		//Getting the file after creating it the first time
		//logFile = new File(dbParams.get("logFileFolder") + "/logFile.txt");
			
	/* Scanner scanner = new Scanner(logFile)		   ;		    while (scanner.hasNextLine()) {		        String line = scanner.nextLine()		       ;		        if(line.contains("WARNING")) { 		            System.out.println("ho hum, i found it on line m);		        }		    
	    
	    scanner.close();}*/
		
		//Asserting that the level recorded is the same
		Assert.assertTrue(handler.getLevelRecorded().intValue() == Level.WARNING.intValue());
		
		//Asserting that the message is the same
		auxText ="warning "+DateFormat.getDateInstance(DateFormat.LONG).format(new Date())+" "+"This a warning message";
		Assert.assertTrue(handler.getMessageRecorded().equals(auxText));		
		       
		/****************************************************************************************************/
		/********************************************Error Message ******************************************/
		//Executing the log process
		JobLogger.LogMessage("This an error message", true, false, false, LevelOfMessage.ERROR, dbParams);		
		
		//Asserting that the level recorded is the same
		Assert.assertTrue(handler.getLevelRecorded().intValue() == Level.SEVERE.intValue());		
		
		//Asserting that the message is the same
		auxText = "error "+DateFormat.getDateInstance(DateFormat.LONG).format(new Date())+" "+"This an error message";
		Assert.assertTrue(handler.getMessageRecorded().equals(auxText));
		
		/****************************************************************************************************/
		/*******************************************Information Message *************************************/
		//Executing the log process
		JobLogger.LogMessage("This an info message", true, false, false, LevelOfMessage.MESSAGE, dbParams);
				
		//Asserting that the level recorded is the same
		Assert.assertTrue(handler.getLevelRecorded().intValue() == Level.INFO.intValue());		
		
		//Asserting that the message is the same
		auxText ="message "+DateFormat.getDateInstance(DateFormat.LONG).format(new Date()) + " " +"This an info message";
		Assert.assertTrue(handler.getMessageRecorded().equals(auxText));
		/****************************************************************************************************/
		
		//Removing the handler to avoid memory leak
		logger.removeHandler(handler);
	}
	
	/**
	 * Unit test to verify that the DbParam for logging in the database cannot be null
	 */
	@Test	
	public void DataBaseParametersCannotBeNullTest()
	{		
		LoggerException exception = Assert.assertThrows(LoggerException.class, 
				() -> JobLogger.LogMessage("This is a message", false, false, true, LevelOfMessage.WARNING, null));
		Assert.assertTrue(exception.getMessage().equals("DataBase parameters cannot be blank"));				
		
	}
	
	/**
	 * Unit test to verify that all the required Database parameters must be specified
	 */
	@Test
	public void AllDataBaseParametersMustBeSpecifiedTest()
	{
		//Preparing all the cases
		final Map<String, String> dbParams = new HashMap<String, String>();
		final Map<String, String> dbParams1 = new HashMap<String, String>();
		dbParams1.put("userName", null);
		dbParams1.put("password", "information");
		dbParams1.put("dbms", "information");
		dbParams1.put("serverName", "information");
		dbParams1.put("portNumber", "information");
		final Map<String, String> dbParams2 = new HashMap<String, String>();
		dbParams2.put("userName", "information");
		dbParams2.put("password", null);
		dbParams2.put("dbms", "information");
		dbParams2.put("serverName", "information");
		dbParams2.put("portNumber", "information");
		final Map<String, String> dbParams3 = new HashMap<String, String>();
		dbParams3.put("userName", "information");
		dbParams3.put("password", "information");
		dbParams3.put("dbms", null);
		dbParams3.put("serverName", "information");
		dbParams3.put("portNumber", "information");
		final Map<String, String> dbParams4 = new HashMap<String, String>();
		dbParams4.put("userName", "information");
		dbParams4.put("password", "information");
		dbParams4.put("dbms", "information");
		dbParams4.put("serverName", null);
		dbParams4.put("portNumber", "information");
		final Map<String, String> dbParams5 = new HashMap<String, String>();
		dbParams5.put("userName", "information");
		dbParams5.put("password", "information");
		dbParams5.put("dbms", "information");
		dbParams5.put("serverName", "information");
		dbParams5.put("portNumber", null);
		final Map<String, String> dbParams6 = new HashMap<String, String>();		
		dbParams6.put("password", "information");
		dbParams6.put("dbms", "information");
		dbParams6.put("serverName", "information");
		dbParams6.put("portNumber", "information");
		final Map<String, String> dbParams7 = new HashMap<String, String>();
		dbParams7.put("userName", "information");		
		dbParams7.put("dbms", "information");
		dbParams7.put("serverName", "information");
		dbParams7.put("portNumber", "information");
		final Map<String, String> dbParams8 = new HashMap<String, String>();
		dbParams8.put("userName", "information");
		dbParams8.put("password", "information");		
		dbParams8.put("serverName", "information");
		dbParams8.put("portNumber", "information");
		final Map<String, String> dbParams9 = new HashMap<String, String>();
		dbParams9.put("userName", "information");
		dbParams9.put("password", "information");
		dbParams9.put("dbms", "information");		
		dbParams9.put("portNumber", "information");
		final Map<String, String> dbParams10 = new HashMap<String, String>();
		dbParams10.put("userName", "information");
		dbParams10.put("password", "information");
		dbParams10.put("dbms", "information");
		dbParams10.put("serverName", "information");		
		
		//Asserting logFileFolder value must exist
		LoggerException exception = Assert.assertThrows(LoggerException.class, 
				() -> JobLogger.LogMessage("This is a message", false, false, true, LevelOfMessage.WARNING, dbParams));
		Assert.assertTrue(exception.getMessage().equals("Not all the required database parameters have been specified"));		
		
		//Asserting logFileFolder cannot be null		
		exception = Assert.assertThrows(LoggerException.class, 
				() -> JobLogger.LogMessage("This is a message", false, false, true, LevelOfMessage.WARNING, dbParams1));
		Assert.assertTrue(exception.getMessage().equals("Not all the required database parameters have been specified"));
		
		//Asserting logFileFolder cannot be null		
		exception = Assert.assertThrows(LoggerException.class, 
				() -> JobLogger.LogMessage("This is a message", false, false, true, LevelOfMessage.WARNING, dbParams2));
		Assert.assertTrue(exception.getMessage().equals("Not all the required database parameters have been specified"));
				
		//Asserting logFileFolder cannot be null		
		exception = Assert.assertThrows(LoggerException.class, 
				() -> JobLogger.LogMessage("This is a message", false, false, true, LevelOfMessage.WARNING, dbParams3));
		Assert.assertTrue(exception.getMessage().equals("Not all the required database parameters have been specified"));
				
		//Asserting logFileFolder cannot be null		
		exception = Assert.assertThrows(LoggerException.class, 
				() -> JobLogger.LogMessage("This is a message", false, false, true, LevelOfMessage.WARNING, dbParams4));
		Assert.assertTrue(exception.getMessage().equals("Not all the required database parameters have been specified"));
		
		//Asserting logFileFolder cannot be null		
		exception = Assert.assertThrows(LoggerException.class, 
				() -> JobLogger.LogMessage("This is a message", false, false, true, LevelOfMessage.WARNING, dbParams5));
		Assert.assertTrue(exception.getMessage().equals("Not all the required database parameters have been specified"));
		
		//Asserting logFileFolder cannot be null		
		exception = Assert.assertThrows(LoggerException.class, 
				() -> JobLogger.LogMessage("This is a message", false, false, true, LevelOfMessage.WARNING, dbParams6));
		Assert.assertTrue(exception.getMessage().equals("Not all the required database parameters have been specified"));
		
		//Asserting logFileFolder cannot be null		
		exception = Assert.assertThrows(LoggerException.class, 
				() -> JobLogger.LogMessage("This is a message", false, false, true, LevelOfMessage.WARNING, dbParams7));
		Assert.assertTrue(exception.getMessage().equals("Not all the required database parameters have been specified"));
		
		//Asserting logFileFolder cannot be null		
		exception = Assert.assertThrows(LoggerException.class, 
				() -> JobLogger.LogMessage("This is a message", false, false, true, LevelOfMessage.WARNING, dbParams8));
		Assert.assertTrue(exception.getMessage().equals("Not all the required database parameters have been specified"));
		
		//Asserting logFileFolder cannot be null		
		exception = Assert.assertThrows(LoggerException.class, 
				() -> JobLogger.LogMessage("This is a message", false, false, true, LevelOfMessage.WARNING, dbParams9));
		Assert.assertTrue(exception.getMessage().equals("Not all the required database parameters have been specified"));
		
		//Asserting logFileFolder cannot be null		
		exception = Assert.assertThrows(LoggerException.class, 
				() -> JobLogger.LogMessage("This is a message", false, false, true, LevelOfMessage.WARNING, dbParams10));
		Assert.assertTrue(exception.getMessage().equals("Not all the required database parameters have been specified"));
	}
	
	/**
	 * Method to clean anything required
	 */
	@AfterClass
	public static void TearDown()
	{
		//File path
		File logFile = new File(System.getProperty("user.home") + "/logFile.txt");
		if(logFile.exists())
			logFile.delete();
	}
}
