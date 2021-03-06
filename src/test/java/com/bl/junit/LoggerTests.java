package com.bl.junit;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.Assert;
import org.junit.BeforeClass;
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
	 * Method to clean anything required
	 * @throws SQLException 
	 */
	@BeforeClass
	public static void SetUp() throws SQLException
	{
		//Deleting the file created in case we performed that test
		File logFile = new File(System.getProperty("user.home") + "/logFile.txt");
		if(logFile.exists())
			logFile.delete();
		
		//Using real data base parameters
		Map<String, String> dbParams = new HashMap<String, String>();
		dbParams.put("userName", "username");
		dbParams.put("password", "dragon");
		dbParams.put("dbms", "h2");
		dbParams.put("serverName", System.getProperty("user.home"));
		
		//Deleting all rows in case we have performed that test
		Connection connection = null;
		Properties connectionProps = new Properties();
		
		//Placing the credentials for the connection
		connectionProps.put("user", dbParams.get("userName"));
		connectionProps.put("password", dbParams.get("password"));
		
		//Creating a connection with the credentials given
		connection = DriverManager.getConnection("jdbc:" + dbParams.get("dbms") + ":" + dbParams.get("serverName")
				+ "/test;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false", connectionProps);
		
		//Executing DB operation
		Statement stmt = connection.createStatement();								
		stmt.executeUpdate("DELETE FROM LOG");
		connection.commit();
		
		//Closing connection and statement
		connection.close();
		stmt.close();
		
		//Placing values to null so they can be collected by JGC
		logFile = null;
		dbParams = null;
		connection = null;
		connectionProps = null;
		stmt = null;
	}
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
		//Using a real filepath
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
		
		//Checking the file for the information
		Assert.assertTrue(HasInformationLoggedIntoTheFile(Level.WARNING));
		
		//Asserting that the level recorded is the same
		Assert.assertTrue(handler.getLevelRecorded().intValue() == Level.WARNING.intValue());
		
		//Asserting that the message is the same
		auxText ="warning "+DateFormat.getDateInstance(DateFormat.LONG).format(new Date())+" "+"This a warning message";
		Assert.assertTrue(handler.getMessageRecorded().equals(auxText));		
		       
		/****************************************************************************************************/
		/********************************************Error Message ******************************************/
		//Executing the log process
		JobLogger.LogMessage("This an error message", true, false, false, LevelOfMessage.ERROR, dbParams);
		
		//Checking the file for the information
		Assert.assertTrue(HasInformationLoggedIntoTheFile(Level.SEVERE));
		
		//Asserting that the level recorded is the same
		Assert.assertTrue(handler.getLevelRecorded().intValue() == Level.SEVERE.intValue());		
		
		//Asserting that the message is the same
		auxText = "error "+DateFormat.getDateInstance(DateFormat.LONG).format(new Date())+" "+"This an error message";
		Assert.assertTrue(handler.getMessageRecorded().equals(auxText));
		
		/****************************************************************************************************/
		/*******************************************Information Message *************************************/
		//Executing the log process
		JobLogger.LogMessage("This an info message", true, false, false, LevelOfMessage.MESSAGE, dbParams);
		
		//Checking the file for the information
		Assert.assertTrue(HasInformationLoggedIntoTheFile(Level.INFO));
				
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
	 * Method to validate that the information recently saved in the file has been inserted correctly
	 * @param level The level of the message we have inserted
	 * @return true if we found it in the file otherwise false
	 * @throws FileNotFoundException In case we haven't found the file
	 */
	private boolean HasInformationLoggedIntoTheFile(Level level) throws FileNotFoundException
	{
		//Getting the file after creating it the first time
		File logFile = new File(System.getProperty("user.home") + "/logFile.txt");
		boolean found = false;
		
		//Checking if the information has been inserted
		Scanner scanner = new Scanner(logFile);
	    while (scanner.hasNextLine()) {
	        String line = scanner.nextLine();
	        if(line.contains(level.getName()))
	        {
	            found = true;
	            break;
	        }
	    }	    
	    scanner.close();	    
	    return found;
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
		final Map<String, String> dbParams2 = new HashMap<String, String>();
		dbParams2.put("userName", "information");
		dbParams2.put("password", null);
		dbParams2.put("dbms", "information");
		dbParams2.put("serverName", "information");		
		final Map<String, String> dbParams3 = new HashMap<String, String>();
		dbParams3.put("userName", "information");
		dbParams3.put("password", "information");
		dbParams3.put("dbms", null);
		dbParams3.put("serverName", "information");		
		final Map<String, String> dbParams4 = new HashMap<String, String>();
		dbParams4.put("userName", "information");
		dbParams4.put("password", "information");
		dbParams4.put("dbms", "information");
		dbParams4.put("serverName", null);			
		final Map<String, String> dbParams6 = new HashMap<String, String>();		
		dbParams6.put("password", "information");
		dbParams6.put("dbms", "information");
		dbParams6.put("serverName", "information");		
		final Map<String, String> dbParams7 = new HashMap<String, String>();
		dbParams7.put("userName", "information");		
		dbParams7.put("dbms", "information");
		dbParams7.put("serverName", "information");		
		final Map<String, String> dbParams8 = new HashMap<String, String>();
		dbParams8.put("userName", "information");
		dbParams8.put("password", "information");		
		dbParams8.put("serverName", "information");		
		final Map<String, String> dbParams9 = new HashMap<String, String>();
		dbParams9.put("userName", "information");
		dbParams9.put("password", "information");
		dbParams9.put("dbms", "information");			
		
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
	}
	
	/**
	 * Unit test to verify that the database parameters cannot be anything except a variable that can be treated as a string
	 */
	@Test	
	public void DataBaseParametersCanOnlyBeStringRelatedTypeTest()
	{	
		//Trying to use a non string as a file path
		final Map<String, Object> dbParams1 = new HashMap<String, Object>();
		dbParams1.put("userName", new Exception());
		dbParams1.put("password", "information");
		dbParams1.put("dbms", "information");
		dbParams1.put("serverName", "information");				
		
		//Asserting that an error is thrown
		LoggerException exception = Assert.assertThrows(LoggerException.class, 
				() -> JobLogger.LogMessage("This is a message", false, false, true, LevelOfMessage.WARNING, dbParams1));
		Assert.assertTrue(exception.getMessage().equals("Database parameters must be valid data"));
		
		dbParams1.put("userName", "information");
		dbParams1.put("password", new Exception());
		dbParams1.put("dbms", "information");
		dbParams1.put("serverName", "information");			
		
		//Asserting that an error is thrown
		exception = Assert.assertThrows(LoggerException.class, 
				() -> JobLogger.LogMessage("This is a message", false, false, true, LevelOfMessage.WARNING, dbParams1));
		Assert.assertTrue(exception.getMessage().equals("Database parameters must be valid data"));
		
		dbParams1.put("userName", "information");
		dbParams1.put("password", "information");
		dbParams1.put("dbms", new Exception());
		dbParams1.put("serverName", "information");			
		
		//Asserting that an error is thrown
		exception = Assert.assertThrows(LoggerException.class, 
				() -> JobLogger.LogMessage("This is a message", false, false, true, LevelOfMessage.WARNING, dbParams1));
		Assert.assertTrue(exception.getMessage().equals("Database parameters must be valid data"));
		
		dbParams1.put("userName", "information");
		dbParams1.put("password", "information");
		dbParams1.put("dbms", "information");
		dbParams1.put("serverName", new Exception());			
		
		//Asserting that an error is thrown
		exception = Assert.assertThrows(LoggerException.class, 
				() -> JobLogger.LogMessage("This is a message", false, false, true, LevelOfMessage.WARNING, dbParams1));
		Assert.assertTrue(exception.getMessage().equals("Database parameters must be valid data"));		
		
		//Trying to use a non string as a file path
		final Map<String, Object> dbParams2 = new HashMap<String, Object>();
		dbParams2.put("userName", 1);
		dbParams2.put("password", "information");
		dbParams2.put("dbms", "information");
		dbParams2.put("serverName", "information");				
		
		//Asserting that an error is thrown
		exception = Assert.assertThrows(LoggerException.class, 
				() -> JobLogger.LogMessage("This is a message", false, false, true, LevelOfMessage.WARNING, dbParams2));
		Assert.assertTrue(exception.getMessage().equals("Database parameters must be valid data"));
		
		dbParams2.put("userName", "information");
		dbParams2.put("password", 1);
		dbParams2.put("dbms", "information");
		dbParams2.put("serverName", "information");			
		
		//Asserting that an error is thrown
		exception = Assert.assertThrows(LoggerException.class, 
				() -> JobLogger.LogMessage("This is a message", false, false, true, LevelOfMessage.WARNING, dbParams2));
		Assert.assertTrue(exception.getMessage().equals("Database parameters must be valid data"));
		
		dbParams2.put("userName", "information");
		dbParams2.put("password", "information");
		dbParams2.put("dbms", 1);
		dbParams2.put("serverName", "information");			
		
		//Asserting that an error is thrown
		exception = Assert.assertThrows(LoggerException.class, 
				() -> JobLogger.LogMessage("This is a message", false, false, true, LevelOfMessage.WARNING, dbParams2));
		Assert.assertTrue(exception.getMessage().equals("Database parameters must be valid data"));
		
		dbParams2.put("userName", "information");
		dbParams2.put("password", "information");
		dbParams2.put("dbms", "information");
		dbParams2.put("serverName", 1);			
		
		//Asserting that an error is thrown
		exception = Assert.assertThrows(LoggerException.class, 
				() -> JobLogger.LogMessage("This is a message", false, false, true, LevelOfMessage.WARNING, dbParams2));
		Assert.assertTrue(exception.getMessage().equals("Database parameters must be valid data"));				
				
		//Trying to use a non string as a file path
		final Map<String, Object> dbParams3 = new HashMap<String, Object>();				
		dbParams3.put("userName", true);
		dbParams3.put("password", "information");
		dbParams3.put("dbms", "information");
		dbParams3.put("serverName", "information");			
		
		//Asserting that an error is thrown
		exception = Assert.assertThrows(LoggerException.class, 
				() -> JobLogger.LogMessage("This is a message", false, false, true, LevelOfMessage.WARNING, dbParams3));
		Assert.assertTrue(exception.getMessage().equals("Database parameters must be valid data"));
		
		dbParams3.put("userName", "information");
		dbParams3.put("password", true);
		dbParams3.put("dbms", "information");
		dbParams3.put("serverName", "information");		
		
		//Asserting that an error is thrown
		exception = Assert.assertThrows(LoggerException.class, 
				() -> JobLogger.LogMessage("This is a message", false, false, true, LevelOfMessage.WARNING, dbParams3));
		Assert.assertTrue(exception.getMessage().equals("Database parameters must be valid data"));
		
		dbParams3.put("userName", "information");
		dbParams3.put("password", "information");
		dbParams3.put("dbms", true);
		dbParams3.put("serverName", "information");		
		
		//Asserting that an error is thrown
		exception = Assert.assertThrows(LoggerException.class, 
				() -> JobLogger.LogMessage("This is a message", false, false, true, LevelOfMessage.WARNING, dbParams3));
		Assert.assertTrue(exception.getMessage().equals("Database parameters must be valid data"));
		
		dbParams3.put("userName", "information");
		dbParams3.put("password", "information");
		dbParams3.put("dbms", "information");
		dbParams3.put("serverName", true);			
		
		//Asserting that an error is thrown
		exception = Assert.assertThrows(LoggerException.class, 
				() -> JobLogger.LogMessage("This is a message", false, false, true, LevelOfMessage.WARNING, dbParams3));
		Assert.assertTrue(exception.getMessage().equals("Database parameters must be valid data"));				
	}
	
	/**
	 * Unit test to verify that every type of message can be logged into the DataBase
	 * @throws LoggerException LoggerException in case any special error appear
	 * @throws SQLException exception when we are validating directly into the data
	 */
	@Test	
	public void LogAllTypesOfMessagesIntoDataBaseTest() throws LoggerException, SQLException
	{
		//Using real data base parameters
		final Map<String, String> dbParams = new HashMap<String, String>();
		dbParams.put("userName", "username");
		dbParams.put("password", "dragon");
		dbParams.put("dbms", "h2");
		dbParams.put("serverName", System.getProperty("user.home"));		
		
		
		/*******************************************Warning Message ****************************************/
		//Executing the log process
		JobLogger.LogMessage("This a warning message", false, false, true, LevelOfMessage.WARNING, dbParams);		
		
		//Asserting that the level recorded is the same
		Assert.assertTrue(HasInformationBeenInsertedIntoDatabase(3));		
		
		/****************************************************************************************************/
		/********************************************Error Message ******************************************/
		//Executing the log process
		JobLogger.LogMessage("This an error message", false, false, true, LevelOfMessage.ERROR, dbParams);		
		
		//Asserting that the level recorded is the same
		Assert.assertTrue(HasInformationBeenInsertedIntoDatabase(2));
		
		/****************************************************************************************************/
		/*******************************************Information Message *************************************/
		//Executing the log process
		JobLogger.LogMessage("This an info message", false, false, true, LevelOfMessage.MESSAGE, dbParams);		
		
		//Asserting that the level recorded is the same
		Assert.assertTrue(HasInformationBeenInsertedIntoDatabase(1));
		
		/****************************************************************************************************/		
	}
	
	/**
	 * Private method to check if the message 
	 * @param level the level of the message we need to assert
	 * @return true if the information has been inserted successfully, otherwise false
	 * @throws SQLException error in case we are querying he database
	 */
	private boolean HasInformationBeenInsertedIntoDatabase(int level) throws SQLException
	{
		//Using real data base parameters
		Map<String, String> dbParams = new HashMap<String, String>();
		dbParams.put("userName", "username");
		dbParams.put("password", "dragon");
		dbParams.put("dbms", "h2");
		dbParams.put("serverName", System.getProperty("user.home"));
		int amount = 0;
		
		//Deleting all rows in case we have performed that test
		Connection connection = null;
		Properties connectionProps = new Properties();
		
		//Placing the credentials for the connection
		connectionProps.put("user", dbParams.get("userName"));
		connectionProps.put("password", dbParams.get("password"));
		
		//Creating a connection with the credentials given
		connection = DriverManager.getConnection("jdbc:" + dbParams.get("dbms") + ":" + dbParams.get("serverName")
				+ "/test;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false", connectionProps);
		
		//Executing DB operation
		Statement stmt = connection.createStatement();
		String query = "SELECT COUNT(*) AMOUNT FROM LOG G WHERE G.LEVEL = '" + level + "'";
		ResultSet resultSet = stmt.executeQuery(query);
		
		//extracting the message
		while(resultSet.next())
		{
			amount = resultSet.getInt("AMOUNT");
		}

		//Closing connection and statement
		connection.close();
		stmt.close();
		
		//Returning the answer
		return amount == 1 ? true:false;
	}
	
	/**
	 * Unit test to verify that the logger handles a SQL error due to invalid Data base parameters
	 */
	@Test
	public void HandlingUnsuccessfulConnectionDueToInvalidDBParamsTest()
	{
		//Trying to use a non string as a file path
		final Map<String, String> dbParams = new HashMap<String, String>();
		dbParams.put("userName", "information");
		dbParams.put("password", "information");
		dbParams.put("dbms", "information");
		dbParams.put("serverName", "information");
		dbParams.put("portNumber", "information");
		
		//Asserting that an error is thrown
		LoggerException exception = Assert.assertThrows(LoggerException.class, 
				() -> JobLogger.LogMessage("This is a message", false, false, true, LevelOfMessage.WARNING, dbParams));
		Assert.assertTrue(exception.getMessage().equals("Cannot create database connection or perform DML instruction, "
				+ "Please check your Data Base parameters"));
	}
}
