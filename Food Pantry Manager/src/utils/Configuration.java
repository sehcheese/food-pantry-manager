// Copyright (C) 2015 Scott Hoelsema
// Licensed under GPL v3.0; see LICENSE for full text

package utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.util.Properties;

import javax.swing.JOptionPane;

public class Configuration {
	
	private static final Properties properties = new Properties();
	
	static {
		// Get handle on configuration file - should be located in the same directory as the running JAR, or in the parent directory of the classpath
		InputStream configurationFileInputStream = null;
		try {
			File classDirectory = new File(Configuration.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath());
			File configurationFile = new File(classDirectory.getParent() + "/configuration.cfg");
			configurationFileInputStream = new FileInputStream(configurationFile);
		} catch (FileNotFoundException e1) {
			JOptionPane.showMessageDialog(null, "Configuration file not found. It should be located in the same directory as the JAR.", "Error", JOptionPane.ERROR_MESSAGE);
			e1.printStackTrace();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
					
		// Establish reader to read configuration file
		BufferedReader br = new BufferedReader(new InputStreamReader(configurationFileInputStream));
		
		// Read in the configurations
		try {
			properties.load(br);
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null, "Configuration file I/O exception.", "Error", JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
		}
		
		// Close file
		try {
			configurationFileInputStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	// Set variables according to configurations file
	public static final String APP_NAME = properties.getProperty("application.name");
	public static final String APP_LOGO_PATH = properties.getProperty("application.logo_path");
	
	public static final String DATABASE_IP_ADDRESS = properties.getProperty("database.ip_address");
	public static final String DATABASE_PORT = properties.getProperty("database.port");
	public static final String DATABASE_NAME = properties.getProperty("database.name");
	
	public static String errorLogDirectory = properties.getProperty("application.error_log_directory");
	public static boolean isLocalOrNetworkBackup = Boolean.valueOf(properties.getProperty("application.do_local_backup"));
	public static String localOrNetworkBackupDirectory = properties.getProperty("application.local_backup_directory");
	public static boolean isDropBoxBackup = Boolean.valueOf(properties.getProperty("application.do_dropbox_backup"));
	public static String dropBoxApiKey = properties.getProperty("application.dropbox_api_key");
	
	public static final int DEFAULT_WEEKS_TO_NEXT_APPOINTMENT = Integer.valueOf(properties.getProperty("application.default_weeks_to_next_appointment"));
}
