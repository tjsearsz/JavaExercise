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
	
	private static boolean logToFile;
	private static boolean logToConsole;
	private static boolean logMessage;
	private static boolean logWarning;
	private static boolean logError;
	private static boolean logToDatabase;
	private static boolean initialized;
	private static Map dbParams;
	private static Logger logger;

	/**
	 * Constructor of the class that receives all the needed elements to specify
	 * what and where will be logged
	 * @param logToFileParam Indicates whether we will log in a file
	 * @param logToConsoleParam Indicates whether we will log in the console
	 * @param logToDatabaseParam Indicates whether we will log in the database
	 * @param logMessageParam Indicates whether we will log an informative message
	 * @param logWarningParam Indicates whether we will log a warning message
	 * @param logErrorParam Indicates whether we will log an error message
	 * @param dbParamsMap Indicates the parameters needed to log into the database
	 */
	public JobLogger(boolean logToFileParam, boolean logToConsoleParam, boolean logToDatabaseParam,
			boolean logMessageParam, boolean logWarningParam, boolean logErrorParam, Map dbParamsMap) {
		logger = Logger.getLogger("MyLog");  
		logError = logErrorParam;
		logMessage = logMessageParam;
		logWarning = logWarningParam;
		logToDatabase = logToDatabaseParam;
		logToFile = logToFileParam;
		logToConsole = logToConsoleParam;
		dbParams = dbParamsMap;
	}	
	
	/**
	 * Setter for the Log to file flag
	 * @param logToFile Flag to indicate whether we will log in a file
	 */
	public void setLogToFile(boolean logToFile) {
		this.logToFile = logToFile;
	}

	/**
	 * Setter for the Log to Console flag
	 * @param logToConsole Flag to indicate whether we will log in a console
	 */
	public void setLogToConsole(boolean logToConsole) {
		this.logToConsole = logToConsole;
	}

	/**
	 * Setter for the message of informative type
	 * @param logMessage the flag to indicate whether the message is informative
	 */
	public void setLogMessage(boolean logMessage) {
		this.logMessage = logMessage;
	}

	/**
	 * Setter for the message of warning type
	 * @param logMessage the flag to indicate whether the message is a warning
	 */
	public void setLogWarning(boolean logWarning) {
		this.logWarning = logWarning;
	}

	/**
	 * Setter for the message of error type
	 * @param logMessage the flag to indicate whether the message is an error
	 */
	public void setLogError(boolean logError) {
		this.logError = logError;
	}
	
	/**
	 * Setter for the Log to Database flag
	 * @param logToDatabase The flag to indicate whether we will log in the DataBase
	 */
	public void setLogToDatabase(boolean logToDatabase) {
		this.logToDatabase = logToDatabase;
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
	public static void LogMessage(String messageText) throws LoggerException {
		
		//if its a valid message (not null, and not only empty spaces) we will log
		if (messageText == null || messageText.trim().length() == 0) 
		{		
			//If we have at least one destination of the log message
			if (!logToConsole && !logToFile && !logToDatabase) 
			{			
				//If we have specified at least one type for the message
				if (!logError && !logMessage && !logWarning)  
				{
					if(logToDatabase)
						LogIntoDataBase(messageText);
					
					//String used to log a message into the console or file
					String logMessageText = null;
					Level MessageLevel = null;
					
					if (logError) {
						logMessageText ="error " + DateFormat.getDateInstance(DateFormat.LONG).format(new Date()) + messageText;
						MessageLevel = Level.SEVERE;
					}
					else if (logWarning) {
						logMessageText = "warning " +DateFormat.getDateInstance(DateFormat.LONG).format(new Date()) + messageText;
						MessageLevel = Level.WARNING;
					}
					else
					{
						logMessageText = "message " +DateFormat.getDateInstance(DateFormat.LONG).format(new Date()) + messageText;
						MessageLevel = Level.INFO;
					}
					
					if(logToFile)
						LogIntoFile(messageText);
					if(logToConsole)
						LogIntoConsole(messageText);
				}
				else
					throw new LoggerException("Error or Warning or Message must be specified");
			}
			else
				throw new LoggerException("Invalid configuration");
		}
		else
			throw new LoggerException("The Message cannot be empty");
		
	}
	
	/**
	 * Method that holds the logic to log into a file
	 * @param messageText The message we want to add
	 * @throws LoggerException The exception that has been thrown during the process of logging
	 */
	private static void LogIntoFile(String messageText) throws LoggerException
	{
		//File path
		File logFile = new File(dbParams.get("logFileFolder") + "/logFile.txt");		
		try
		{
			//Creating the file if it doesn't exist
			if (!logFile.exists()) {
				logFile.createNewFile();
			}
			
			//Creating the handler and logging into the file
			FileHandler fh = new FileHandler(dbParams.get("logFileFolder") + "/logFile.txt");
			logger.addHandler(fh);
			logger.log(Level.INFO, messageText);
		}
		catch(IOException e)
		{
			throw new LoggerException("An error has occurred trying to create, open or write in a file", e);
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
	private static void LogIntoConsole(String messageText) throws LoggerException
	{
		//Performing the process of loggin into the console
		try
		{
			ConsoleHandler ch = new ConsoleHandler();
			logger.addHandler(ch);
			logger.log(Level.INFO, messageText);
		}
		catch(SecurityException e)
		{
			throw new LoggerException("There was an error trying to access to the console", e);
		}		
	}
	
	/**
	 * Method that holds the logic to add a message into the database
	 * @param messageText the message we want to add
	 * @throws LoggerException The exception that has been thrown during the process of logging
	 */
	private static void LogIntoDataBase(String messageText) throws LoggerException
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
