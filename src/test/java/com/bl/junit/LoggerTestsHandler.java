package com.bl.junit;

import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;

/**
 * Custom handler to capture all the logs we will perform in our unit tests
 * @author Teddy
 *
 */
public class LoggerTestsHandler extends Handler {

	private Level levelRecorded;
	private String messageRecorded;
	
	@Override
	public void close() throws SecurityException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void flush() {
		// TODO Auto-generated method stub
		
	}

	/**
	 * Method where we capture the log message and level we are performing
	 * @param record the specific log record we are manipulating
	 */
	@Override
	public void publish(LogRecord record) {
		this.levelRecorded = record.getLevel();
		this.messageRecorded = record.getMessage();
		
	}
	
	/**
	 * Getter for the level we  have logged
	 * @return the Level logged
	 */
	public Level  getLevelRecorded() 
	{
		return this.levelRecorded;
	} 
	
	/**
	 * Getter for the message we have logged
	 * @return the message logged
	 */
	public String getMessageRecorded()
	{
		return this.messageRecorded;
	}

}
