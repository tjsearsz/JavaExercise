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
	
	private static Map dbParams;
	private static Logger logger;
	private static String finalMessage;
	private static Level levelOfMessage;

	/**
	 * Empty Constructor of the class	 
	 */
	public JobLogger() {
		logger = Logger.getLogger("MyLog");		
	}
	
	/**
	 * Setter for the DataBase parameters needed to log into the database
	 * @param dbParams The parameters required to access and open database connection
	 */
	public void setDbParams(Map dbParams) {
		this.dbParams = dbParams;
	}

	/**
	 * This method Logs a message on the required place
	 * @param messageText The text of the message we will log
	 * @throws LoggerException The exception that has been thrown during the process of logging
	 */
	public static void LogMessage(String messageText, 
			boolean logToFile, boolean logToConsole, boolean logToDatabase,
			boolean logWarning, boolean logError, boolean  logMessage) throws LoggerException {
		
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
					if (logError || logMessage || logWarning)  
					{
						//Inserting into the place where its needed
						if(logToDatabase)
							LogIntoDataBase(messageText, logError, logMessage);					
						if(logToFile)
							LogIntoFile(messageText, logError, logWarning);
						if(logToConsole)
							LogIntoConsole(messageText, logError, logWarning);
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
	 * @param logError The flag that indicates whether the message will be saved as error
	 * @param logWarning The flag that indicates whether the message will be saved as a warning
	 */
	private static void SetMessageAndLevel(String messageText, boolean logError, boolean logWarning)
	{
		if (logError) {
			finalMessage ="error " + DateFormat.getDateInstance(DateFormat.LONG).format(new Date()) + messageText;
			levelOfMessage = Level.SEVERE;
		}
		else if (logWarning) {
			finalMessage = "warning " +DateFormat.getDateInstance(DateFormat.LONG).format(new Date()) + messageText;
			levelOfMessage = Level.WARNING;
		}
		else
		{
			finalMessage = "message " +DateFormat.getDateInstance(DateFormat.LONG).format(new Date()) + messageText;
			levelOfMessage = Level.INFO;
		}
	}
	
	/**
	 * Method that holds the logic to log into a file
	 * @param messageText The message we want to add
	 * @throws LoggerException The exception that has been thrown during the process of logging
	 */
	private static void LogIntoFile(String messageText, boolean logError, boolean logWarning) throws LoggerException
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
			SetMessageAndLevel(messageText, logError, logWarning);			
			logger.log(levelOfMessage, finalMessage);
			
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
	
	/**
	 * Method that holds the logic to log into the console
	 * @param messageText The message we want to add
	 * @throws LoggerException The exception that has been thrown during the process of logging
	 */
	private static void LogIntoConsole(String messageText, boolean logError, boolean logWarning) throws LoggerException
	{
		//Performing the process of logging into the console
		try
		{
			//Preparing the console handler
			ConsoleHandler ch = new ConsoleHandler();
			logger.addHandler(ch);
			
			//Setting the level/final message and logging
			SetMessageAndLevel(messageText, logError, logWarning);			
			logger.log(levelOfMessage, finalMessage);
		}
		catch(SecurityException e)
		{
			throw new LoggerException("There was an error trying to access to the console", e);
		}		
	}	
	
	/**
	 * Method that holds the logic to add a message into the database
	 * @param messageText the message we want to add
	 * @param logError The flag that indicates whether the message will be saved as error
	 * @param logMessage The flag that indicates whether the message will be saved as informative
	 * @throws LoggerException The exception that has been thrown during the process of logging
	 */
	private static void LogIntoDataBase(String messageText, boolean logError, boolean logMessage) 
			throws LoggerException
	{		
			Connection connection = null;
			Properties connectionProps = new Properties();
			
			//If we have all the required parameters for the database, we can open a connection
			if (dbParams != null && 
					((dbParams.containsKey("userName")  && dbParams.get("userName")  != null) && 
					 (dbParams.containsKey("password")  && dbParams.get("password")  != null) &&
					 (dbParams.containsKey("dbms")      && dbParams.get("dbms")      != null) &&
					 (dbParams.containsKey("serverName")&& dbParams.get("servername")!= null) &&
					 (dbParams.containsKey("portNumber")&& dbParams.get("portNumber")!= null)))
			{
				
				try
				{
					//Placing the credentials for the connection
					connectionProps.put("user", dbParams.get("userName"));
					connectionProps.put("password", dbParams.get("password"));
		
					//Creating a connection with the credentials given
					connection = DriverManager.getConnection("jdbc:" + dbParams.get("dbms") + "://" + dbParams.get("serverName")
							+ ":" + dbParams.get("portNumber") + "/", connectionProps);
					
					//Depending on the type of the message we will insert in database, it will have a code
					char typeOfMessage = 0;
					if (logMessage)
						typeOfMessage = 1;								
					else if (logError)
						typeOfMessage = 2;								
					else
						typeOfMessage = 3;
		
					//Executing DB operation
					Statement stmt = connection.createStatement();								
					stmt.executeUpdate("insert into Log_Values('" + messageText + "', " + typeOfMessage + ")");								

				}
				catch (SQLTimeoutException e)
				{
					//If we get a timeout when trying to establish a DB connection
					throw new LoggerException("Timeout occurred when attempting to establish a DB connection", e);
				}
				catch (SQLException e)
				{
					//If an error on the DB happens we will catch it
					throw new LoggerException("An error on the database has occurred", e);
				}										
			}
			else
				throw new LoggerException("Not all the required database parameters have been specified");
	}
}
