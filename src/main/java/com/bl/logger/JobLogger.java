package com.bl.logger;
import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
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
		if (messageText == null || messageText.trim().length() == 0) {
			if (!logToConsole && !logToFile && !logToDatabase) {
				throw new Exception("Invalid configuration");
			}
			if ((!logError && !logMessage && !logWarning) || (!message && !warning && !error)) {
				throw new Exception("Error or Warning or Message must be specified");
			}

			Connection connection = null;
			Properties connectionProps = new Properties();
			connectionProps.put("user", dbParams.get("userName"));
			connectionProps.put("password", dbParams.get("password"));

			connection = DriverManager.getConnection("jdbc:" + dbParams.get("dbms") + "://" + dbParams.get("serverName")
					+ ":" + dbParams.get("portNumber") + "/", connectionProps);

			int t = 0;
			if (message && logMessage) {
				t = 1;
			}

			if (error && logError) {
				t = 2;
			}

			if (warning && logWarning) {
				t = 3;
			}

			Statement stmt = connection.createStatement();

			String l = null;
			File logFile = new File(dbParams.get("logFileFolder") + "/logFile.txt");
			if (!logFile.exists()) {
				logFile.createNewFile();
			}
			
			FileHandler fh = new FileHandler(dbParams.get("logFileFolder") + "/logFile.txt");
			ConsoleHandler ch = new ConsoleHandler();
			
			if (error && logError) {
				l = l + "error " + DateFormat.getDateInstance(DateFormat.LONG).format(new Date()) + messageText;
			}

			if (warning && logWarning) {
				l = l + "warning " +DateFormat.getDateInstance(DateFormat.LONG).format(new Date()) + messageText;
			}

			if (message && logMessage) {
				l = l + "message " +DateFormat.getDateInstance(DateFormat.LONG).format(new Date()) + messageText;
			}
			
			if(logToFile) {
				logger.addHandler(fh);
				logger.log(Level.INFO, messageText);
			}
			
			if(logToConsole) {
				logger.addHandler(ch);
				logger.log(Level.INFO, messageText);
			}
			
			if(logToDatabase) {
				stmt.executeUpdate("insert into Log_Values('" + message + "', " + String.valueOf(t) + ")");
			}
		}
		else
			throw new LoggerException("The Message cannot be empty");
		
	}
}
