// Copyright (C) 2015 Scott Hoelsema
// Licensed under GPL v3.0; see LICENSE for full text

package database;

import gui.LogIn;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import javax.swing.JOptionPane;

import utils.Logger;

/**
 * A collection of static methods that update the database
 * 
 * @author Scott Hoelsema
 */
public class Inserts {
	private static Connection conn = DatabaseConnection.getConnection();
	private static final int CONNECTION_TIMEOUT_LENGTH = 2; // Timeout tolerance for testing database connection
	
	public static void resetConnection() {
		conn = DatabaseConnection.getConnection();
	}
	
	private static void showNoConnectionError() {
		JOptionPane.showMessageDialog(null, "No database connection.", "Error", JOptionPane.ERROR_MESSAGE);
		new LogIn(false);
	}
	
	private static void showDatabaseError() {
		JOptionPane.showMessageDialog(null, "Database error.", "Error", JOptionPane.ERROR_MESSAGE);
	}

	/***************************\
	 * INSERTS ON client TABLE *
	\***************************/
	
	/**
	 * Insert a new client. Returns the generated ClientID of the new client.
	 * 
	 * @param c
	 *            The client to insert
	 * @return The generated ClientID of the new client
	 */
	public static int insertClient(Client c) {
		try {
			if(conn != null && conn.isValid(CONNECTION_TIMEOUT_LENGTH)) {
				PreparedStatement submitNewClient = conn.prepareStatement("INSERT INTO `food_pantry_manager`.`client`(first_name, last_name, ssn, address, city, telephone, gender, birthday, notes) VALUES(?,?,?,?,?,?,?,?,?);", new String[]{"client_id"});
				submitNewClient.setString(1, c.getFirstName());
				submitNewClient.setString(2, c.getLastName());
				submitNewClient.setString(3, c.getSsn()); // SSN will not be null; if not provided, this reads "Withheld"
				submitNewClient.setString(4, c.getAddress());
				submitNewClient.setString(5, c.getCity());
				if(c.getTelephone() != null) {
					submitNewClient.setString(6, c.getTelephone());
				} else {
					submitNewClient.setNull(6, Types.VARCHAR);
				}
				submitNewClient.setString(7, c.getGender());
				if(c.getBirthday() != null) {
					submitNewClient.setDate(8, c.getBirthday());
				} else {
					submitNewClient.setNull(8, Types.DATE);
				}
				if(c.getNotes() != null) {
					submitNewClient.setString(9, c.getNotes());
				} else {
					submitNewClient.setNull(9, Types.NULL);
				}
				submitNewClient.executeUpdate();
				
				ResultSet key = submitNewClient.getGeneratedKeys();
				key.next();
				return key.getInt(1);
			} else {
				showNoConnectionError();
				return -1;
			}
		} catch (SQLException e) {
			Logger.logThrowable(e);
			showDatabaseError();
			e.printStackTrace();
			return -1;
		}
	}
	
	/********************************\
	 * INSERTS ON appointment TABLE *
	\********************************/
	
	/**
	 * Add new appointment; this appointment will happen in the future so we
	 * will not set pounds right now
	 * 
	 * @param appt Appointment object with (at least) a clientID and date set
	 */
	public static boolean addNextAppointment(Appointment appt) {
		try {
			if(conn != null && conn.isValid(CONNECTION_TIMEOUT_LENGTH)) {
				PreparedStatement submitPickUp = conn.prepareStatement("INSERT INTO `food_pantry_manager`.`appointment`(client_id, date) VALUES(?,?);");
				submitPickUp.setInt(1, appt.getClientID());
				submitPickUp.setTimestamp(2, appt.getDate());
				submitPickUp.executeUpdate();
				return true; // Successful insert
			} else {
				showNoConnectionError();
				return false;
			}
		} catch (SQLException e) {
			Logger.logThrowable(e);
			showDatabaseError();
			e.printStackTrace();
			return false;
		}
	}
	
	/******************************\
	 * INSERTS ON household TABLE *
	\******************************/
	
	public static boolean addHouseholdMember(Household hm) {
		try {
			if(conn != null && conn.isValid(CONNECTION_TIMEOUT_LENGTH)) {
				PreparedStatement insertHouseholdMember = conn.prepareStatement("INSERT INTO `food_pantry_manager`.`household`(client_id, name, birthday, gender, relationship) VALUES(?,?,?,?,?);");
				insertHouseholdMember.setInt(1, hm.getClientID());
				insertHouseholdMember.setString(2, hm.getName());
				if(hm.getBirthday() != null) {
					insertHouseholdMember.setDate(3, hm.getBirthday());
				} else {
					insertHouseholdMember.setNull(3, Types.DATE);
				}
				insertHouseholdMember.setString(4, hm.getGender());
				insertHouseholdMember.setString(5, hm.getRelationship());
				insertHouseholdMember.executeUpdate();
				return true; // Successful insert
			} else {
				showNoConnectionError();
				return false;
			}
		} catch (SQLException e) {
			Logger.logThrowable(e);
			showDatabaseError();
			e.printStackTrace();
			return false;
		}
	}
}
