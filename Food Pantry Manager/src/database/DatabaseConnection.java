// Copyright (C) 2015 Scott Hoelsema
// Licensed under GPL v3.0; see LICENSE for full text

package database;

import java.sql.DriverManager;
import java.sql.SQLException;

import java.sql.Connection;

import javax.swing.JOptionPane;

import utils.Configuration;
import utils.Logger;

/**
 * Utility for logging in to the database system
 * 
 * @author Scott Hoelsema
 */
public class DatabaseConnection {
	private static final String SERVER_URL = "jdbc:mysql://" + Configuration.DATABASE_IP_ADDRESS + ":" + Configuration.DATABASE_PORT + "/" + Configuration.DATABASE_NAME;
	private static Connection conn;
	
	/**
	 * Log in to the database
	 * 
	 * @param username
	 *            Database username
	 * @param password
	 *            Database password
	 * @return boolean indicating the success of attempted log in
	 */
	public static boolean logIn(String username, String password) {
		try {
			conn = DriverManager.getConnection(SERVER_URL, username, password);
			return true;
		} catch (SQLException e) {
			JOptionPane.showMessageDialog(null, "Failed log in attempt.", "Error", JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
			return false;
		}
	}
	
	/**
	 * Log out of the database
	 */
	public static void logOut() {
		try {
			conn.close();
		} catch (SQLException e) {
			Logger.logThrowable(e);
			JOptionPane.showMessageDialog(null, "Problem logging out.", "Error", JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
		}
	}
	
	/**
	 * Supply the connection to the database
	 * 
	 * @return conn The connection to the database
	 */
	public static Connection getConnection() {
		return conn;
	}
}
