package com.bl.logger;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.SQLTimeoutException;
import java.sql.Statement;
import java.text.DateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Properties;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.bl.exception.LoggerException;

/**
 * This class is intended to log (Informative, warning or error) messages
 * in the console, File or Database
 * @author Belatrix
 *
 */
public class JobLogger {
	
	//Atttributes of the class
	private static Logger logger;
	private static String finalMessage;
	private static Level levelOfMessage;

	/**
	 * Empty Constructor of the class	 
	 */
	public JobLogger() {				
	}	
	
	/**
	 * This methods logs a message on the required destiny and the type needed
	 * @param messageText The text of the message we will log
	 * @param logToFile Flag to indicate whether we will log into a file
	 * @param logToConsole Flag to indicate whether we will log into the console
	 * @param logToDatabase Flag to indicate whether we will log into a database
	 * @param level Flag to indicate the type of the message
	 * @param dbParams the database parameters for inserting data (if apply)
	 * @throws LoggerException The exception that has been thrown during the process of logging
	 */
	public static void LogMessage(String messageText, 
			boolean logToFile, boolean logToConsole, boolean logToDatabase,
			LevelOfMessage level, Map dbParams) throws LoggerException {		
		
		//if its a valid message (not null, and not only empty spaces) we will log
		if (messageText != null) 
		{	
			//if the message is not only white space
			if (messageText.trim().length() != 0)
			{
				//If we have at least one destination of the log message
				if (logToConsole || logToFile || logToDatabase) 
				{			
					//If we have specified at least one type for the message
					if (level != null)  
					{
						//Getting the logger
						logger = Logger.getLogger("MyLog");
						
						//Inserting into the place where it's needed
						if(logToDatabase)
							LogIntoDataBase(messageText, level, dbParams);					
						if(logToFile)
							LogIntoFile(messageText, level, dbParams);
						if(logToConsole)
							LogIntoConsole(messageText, level);
					}
					else
						throw new LoggerException("Error or Warning or Message must be specified");
				}
				else
					throw new LoggerException("Invalid configuration");
			}
			else
				throw new LoggerException("The message cannot contain only white space");
		}
		else
			throw new LoggerException("The Message cannot be null");
		
	}
	
	/**
	 * The method that holds the logic to prepare the final message (message + time) and the type
	 * @param messageText The message we want to add
	 * @param level The type of the message we will output
	 */
	private static void SetMessageAndLevel(String messageText, LevelOfMessage level)
	{
		switch(level)
		{
			case ERROR:
				finalMessage ="error " + DateFormat.getDateInstance(DateFormat.LONG).format(new Date()) + " " + messageText;
				levelOfMessage = Level.SEVERE;
				break;
			case WARNING:
				finalMessage = "warning " +DateFormat.getDateInstance(DateFormat.LONG).format(new Date()) + " " + messageText;
				levelOfMessage = Level.WARNING;
				break;
			default:
				finalMessage = "message " +DateFormat.getDateInstance(DateFormat.LONG).format(new Date()) + " " + messageText;
				levelOfMessage = Level.INFO;
				break;				
		}
		
	}
	
	/**
	 * Method that holds the logic to log into a file
	 * @param messageText The message we want to add
	 * @param level The type of the message we will output
	 * @param dbParams the file parameters used to insert the message
	 * @throws LoggerException The exception that has been thrown during the process of logging
	 */
	private static void LogIntoFile(String messageText, LevelOfMessage level, Map dbParams) throws LoggerException
	{
		//Validating the parameter is not null
		if (dbParams != null)
		{	
			//Validating the file parameter has been specified
			if (dbParams.containsKey("logFileFolder") && dbParams.get("logFileFolder") != null)
			{
				//Validating the value of the path can be casted to a string (cannot be a class or any other thing)
				if (dbParams.get("logFileFolder") instanceof String)
				{
					//File path
					File logFile = new File(dbParams.get("logFileFolder") + "/logFile.txt");		
					try
					{
						//Creating the file if it doesn't exist
						if (!logFile.exists())
							logFile.createNewFile();			
						
						//Creating the handler and logging into the file
						FileHandler fh = new FileHandler(dbParams.get("logFileFolder") + "/logFile.txt");
						logger.addHandler(fh);
						
						//Setting the level/final message and logging
						SetMessageAndLevel(messageText, level);			
						logger.log(levelOfMessage, finalMessage);
						
						//Removing the handler to avoid leak of memory
						logger.removeHandler(fh);
						
						//Closing the Handler
						fh.close();			
					}
					catch(IOException e)
					{
						throw new LoggerException("An error has occurred trying to create, open a file", e);
					}
					catch(SecurityException e)
					{
						throw new LoggerException("A security error has occurred with the file", e);
					}
				}
				else
					throw new LoggerException("File parameter must be a valid location");
			}
			else
				throw new LoggerException("File parameter has not been specified");
		}
		else
			throw new LoggerException("File parameter cannot be blank");
	}
	
	/**
	 * Method that holds the logic to log into the console
	 * @param messageText The message we want to add
	 * @param level The type of message we will output
	 * @throws LoggerException The exception that has been thrown during the process of logging
	 */
	private static void LogIntoConsole(String messageText, LevelOfMessage level) throws LoggerException
	{
		//Performing the process of logging into the console
		try
		{
			//Preparing the console handler
			ConsoleHandler ch = new ConsoleHandler();
			logger.addHandler(ch);
			
			//Setting the level/final message and logging
			SetMessageAndLevel(messageText, level);			
			logger.log(levelOfMessage, finalMessage);
			
			//Removing the handler to avoid leak of memory
			logger.removeHandler(ch);
			
			//Closing the handler
			ch.close();
		}
		catch(SecurityException e)
		{
			throw new LoggerException("There was an error trying to access to the console", e);
		}		
	}	
	
	/**
	 * Method that holds the logic to add a message into the database
	 * @param messageText the message we want to add
	 * @param level The type of message we will output	 
	 * @param dbParams The database parameters used to insert the message
	 * @throws LoggerException The exception that has been thrown during the process of logging
	 */
	private static void LogIntoDataBase(String messageText, LevelOfMessage level, Map dbParams) 
			throws LoggerException
	{		
		//Validating database parameters doesn't come null
		if (dbParams != null)
		{
			//If we have all the required parameters for the database, we can open a connection
			if ((dbParams.containsKey("userName")  && dbParams.get("userName")  != null) && 
				 (dbParams.containsKey("password")  && dbParams.get("password")  != null) &&
				 (dbParams.containsKey("dbms")      && dbParams.get("dbms")      != null) &&
				 (dbParams.containsKey("serverName")&& dbParams.get("serverName")!= null))
			{
				//Validating the value of the parameters can be casted to a string (cannot be a class or any other thing)
				if (dbParams.get("userName") instanceof String &&
					dbParams.get("password") instanceof String &&
					dbParams.get("dbms") instanceof String &&
					dbParams.get("serverName") instanceof String)
				{
					Connection connection = null;
					Properties connectionProps = new Properties();
					try
					{
						//Placing the credentials for the connection
						connectionProps.put("user", dbParams.get("userName"));
						connectionProps.put("password", dbParams.get("password"));
			
						//Creating a connection with the credentials given
						connection = DriverManager.getConnection("jdbc:" + dbParams.get("dbms") + ":" + dbParams.get("serverName")
								+ "/test;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false", connectionProps);
						
						//Depending on the type of the message we will insert in database, it will have a code
						String typeOfMessage = "0";
						switch(level)
						{
							case ERROR:
								typeOfMessage = "2";
								break;
							case WARNING:
								typeOfMessage = "3";
								break;
							default:
								typeOfMessage = "1";
								break;				
						}					
			
						//Executing DB operation
						Statement stmt = connection.createStatement();								
						stmt.executeUpdate("INSERT INTO LOG VALUES('" + messageText + "', '" + typeOfMessage + "')");
						
						//Closing connection and statement
						connection.close();
						stmt.close();
	
					}
					catch (SQLTimeoutException e)
					{
						//If we get a timeout when trying to establish a DB connection
						throw new LoggerException("Timeout occurred when attempting to establish a DB connection", e);
					}
					catch (SQLException e)
					{
						//If an error on the DB happens we will catch it
						throw new LoggerException("Cannot create database connection or perform DML instruction, "
								+ "Please check your Data Base parameters", e);
					}
				}
				else
					throw new LoggerException("Database parameters must be valid data");
			}
			else
				throw new LoggerException("Not all the required database parameters have been specified");
		}
		else
			throw new LoggerException ("DataBase parameters cannot be blank");
	}
}
