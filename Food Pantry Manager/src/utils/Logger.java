// Copyright (C) 2015 Scott Hoelsema
// Licensed under GPL v3.0; see LICENSE for full text

package utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Timestamp;

import javax.swing.JOptionPane;

import com.dropbox.core.DbxException;

/**
 * Log and report things such as exceptions and errors
 * 
 * @author Scott Hoelsema
 */
public class Logger {
	public static void logThrowable(Throwable throwable) {
		try {
			// Ascertain that the error log directory exists and can be written to; if not use ./Errors/
			File errorLogDirectory = new File(Configuration.errorLogDirectory);
			if(!errorLogDirectory.exists() || !errorLogDirectory.canWrite()) {
				// Notify user what the problem was
				if(!errorLogDirectory.exists())
					JOptionPane.showMessageDialog(null, "Provided error log directory does not exist. Errors will be written to an Errors folder in the same directory as the jar.", "Error", JOptionPane.ERROR_MESSAGE);
				else if(!errorLogDirectory.canWrite())
					JOptionPane.showMessageDialog(null, "You do not have write permission to the provided error log directory. Errors will be written to an Errors folder in the same directory as the jar.", "Error", JOptionPane.ERROR_MESSAGE);
				
				// Create ./Errors/ if necessary
				File currentWorkspace = new File("./");
				if(currentWorkspace.canWrite()) {
					errorLogDirectory = new File("./Errors/");
					if(!errorLogDirectory.exists()) {
						errorLogDirectory.mkdir();
					}
					
					Configuration.errorLogDirectory = "./Errors/";
				} else {
					// Provided error log directory does not exist and cannot write to working directory
					JOptionPane.showMessageDialog(null, "Provided error log directory does not exist and cannot write to working directory. Errors were not logged in files.", "Error", JOptionPane.ERROR_MESSAGE);
					return;
				}
			}
			
			// Make new file with name <Timestamp> <ExceptionName>
			Timestamp nowTM = new Timestamp(System.currentTimeMillis());
			String now = nowTM.toString().replace(':', '.');
			File errorLog = new File(Configuration.errorLogDirectory + now + "_" + throwable.getClass().getSimpleName() + ".txt");
			errorLog.createNewFile();
			
			// Write out to file
			FileOutputStream fos = new FileOutputStream(errorLog);
			fos.write(Utilities.translateToReadableDate(new Timestamp(System.currentTimeMillis()), true).getBytes());
			fos.write("\n".getBytes());
			fos.write(throwable.getMessage().getBytes());
			fos.write("\n\n".getBytes());
			for(StackTraceElement ste : throwable.getStackTrace()) {
				fos.write(ste.toString().getBytes());
				fos.write("\n".getBytes());
			}
			
			fos.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
