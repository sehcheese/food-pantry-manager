// Copyright (C) 2015 Scott Hoelsema
// Licensed under GPL v3.0; see LICENSE for full text

package database;

import gui.LogIn;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;

import javax.swing.JOptionPane;

import utils.Logger;

/**
 * A collection of static methods that update the database
 * 
 * @author Scott Hoelsema
 */
public class Updates {
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
	 * UPDATES ON client TABLE *
	\***************************/
	
	/**
	 * Update a client's record in the database
	 * 
	 * @param c The client object with updated information - should have every field set, not just changed ones.
	 * @return boolean indicating the success of the attempted update
	 */
	public static boolean updateClient(Client c) {
		try {
			if(conn != null && conn.isValid(CONNECTION_TIMEOUT_LENGTH)) {
				// Perform a pickup on the appointment supplied
				PreparedStatement updateClientRecord = conn.prepareStatement("UPDATE `food_pantry_manager`.`client` SET `first_name`=?, `last_name`=?, `ssn`=?, `address`=?, `city`=?, `telephone`=?, `gender`=?, `birthday`=?, `notes`=? WHERE `client_id`=?;");
				updateClientRecord.setString(1, c.getFirstName());
				updateClientRecord.setString(2, c.getLastName());
				updateClientRecord.setString(3, c.getSsn()); // SSN will not be null; if not provided, this reads "Withheld"
				updateClientRecord.setString(4, c.getAddress());
				updateClientRecord.setString(5, c.getCity());
				if(c.getTelephone() != null) {
					updateClientRecord.setString(6, c.getTelephone());
				} else {
					updateClientRecord.setNull(6, Types.VARCHAR);
				}
				updateClientRecord.setString(7, c.getGender());
				if(c.getBirthday() != null) {
					updateClientRecord.setDate(8, c.getBirthday());
				} else {
					updateClientRecord.setNull(8, Types.DATE);
				}
				if(c.getNotes() != null) {
					updateClientRecord.setString(9, c.getNotes());
				} else {
					updateClientRecord.setNull(9, Types.NULL);
				}
				updateClientRecord.setInt(10, c.getClientID());
				updateClientRecord.executeUpdate();
				return true; // Successful update
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
	
	public static boolean deleteClient(Client c) {
		try {
			if(conn != null && conn.isValid(CONNECTION_TIMEOUT_LENGTH)) {
				PreparedStatement deleteClient = conn.prepareStatement("DELETE FROM `food_pantry_manager`.`client` WHERE `client_id`=?;");
				deleteClient.setInt(1, c.getClientID());
				deleteClient.executeUpdate();
				return true; // Successful delete
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
	
	/********************************\
	 * UPDATES ON appointment TABLE *
	\********************************/
	
	/**
	 * Fill in the rest of the information about an appointment scheduled earlier by Inserts.addNextAppointment
	 * 
	 * @param pickupAppt An Appointment object with (at least) appointmentID, date, and pounds set 
	 * @return boolean indicating the success of attempting to do the database operations involved in doing a pickup
	 */
	public static boolean doPickup(Appointment pickupAppt) {
		try {
			if(conn != null && conn.isValid(CONNECTION_TIMEOUT_LENGTH)) {
				// Perform a pickup on the appointment supplied
				PreparedStatement submitPickUp = conn.prepareStatement("UPDATE `food_pantry_manager`.`appointment` SET `date`=?, `pounds`=? WHERE `appointment_id`=?;");
				submitPickUp.setTimestamp(1, pickupAppt.getDate());
				submitPickUp.setInt(2, pickupAppt.getPounds());
				submitPickUp.setInt(3, pickupAppt.getAppointmentID());
				submitPickUp.executeUpdate();
				return true; // Successful update
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
	
	/**
	 * Change the date and/or pounds for any appointment
	 * 
	 * @param appt
	 *            The Appointment object with a valid AppointmentID and date
	 *            and/or pound information
	 * @return boolean identifying the success of this method
	 */
	public static boolean updateAppointment(Appointment appt) {
		try {
			if(conn != null && conn.isValid(CONNECTION_TIMEOUT_LENGTH)) {
				// Perform an update on the appointment supplied
				PreparedStatement updateAppt = conn.prepareStatement("UPDATE `food_pantry_manager`.`appointment` SET `date`=?, `pounds`=? WHERE `appointment_id`=?;");
				if(appt.getDate() != null) {
					updateAppt.setTimestamp(1, appt.getDate());
				} else {
					updateAppt.setNull(1, Types.TIMESTAMP);
				}
				if(appt.getPounds() != null) {
					updateAppt.setInt(2, appt.getPounds());
				} else {
					updateAppt.setNull(2, Types.INTEGER);
				}
				updateAppt.setInt(3, appt.getAppointmentID());
				updateAppt.executeUpdate();
				return true; // Successful update
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
	
	/**
	 * Delete an appointment
	 * 
	 * @param appt
	 *            The appointment to be deleted; must have an AppointmentID
	 * @return boolean identifying the success of the operation
	 */
	public static boolean deleteAppointment(Appointment appt) {
		try {
			if(conn != null && conn.isValid(CONNECTION_TIMEOUT_LENGTH)) {
				// Perform an update on the appointment supplied
				PreparedStatement deleteAppt = conn.prepareStatement("DELETE FROM `food_pantry_manager`.`appointment` WHERE `appointment_id`=?;");
				deleteAppt.setInt(1, appt.getAppointmentID());
				deleteAppt.executeUpdate();
				return true; // Successful delete
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
	 * UPDATES ON household TABLE *
	\******************************/
	
	/**
	 * Delete a household member from the database
	 * 
	 * @param hm A Household object indicating the household member to delete (only needs id)
	 * @return boolean indicating the success of attempting to delete the household member
	 */
	public static boolean deleteHouseholdMember(Household hm) {
		try {
			if(conn != null && conn.isValid(CONNECTION_TIMEOUT_LENGTH)) {
				// Perform an update on the appointment supplied
				PreparedStatement deleteAppt = conn.prepareStatement("DELETE FROM `food_pantry_manager`.`household` WHERE `household_member_id`=?;");
				deleteAppt.setInt(1, hm.getHouseholdMemberID());
				deleteAppt.executeUpdate();
				return true; // Successful delete
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