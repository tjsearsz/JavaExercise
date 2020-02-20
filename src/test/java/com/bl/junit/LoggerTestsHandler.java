package com.bl.junit;

import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;

public class LoggerTestsHandler extends Handler {

	private Level lastLevel;
	private String messageRecorded;
	
	@Override
	public void close() throws SecurityException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void flush() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void publish(LogRecord arg0) {
		this.lastLevel = arg0.getLevel();
		this.messageRecorded = arg0.getMessage();
		
	}
	
	 public Level  getLastLevel() {
	        return this.lastLevel;
	    } 
	 
	 public String getMessageRecorded()
	 {
		 return this.messageRecorded;
	 }

}
