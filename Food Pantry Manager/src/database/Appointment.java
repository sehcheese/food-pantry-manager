// Copyright (C) 2015 Scott Hoelsema
// Licensed under GPL v3.0; see LICENSE for full text

package database;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import utils.Utilities;

/**
 * Database object representing an entry in the appointment table, or
 * information to add an entry or update an entry in the appointment table
 * 
 * @author Scott Hoelsema
 */
public class Appointment {
	// Database fields
	private int appointment_id; // Primary key (surrogate)
	private int client_id; // Identifies client that this appointment is tied to
	private Timestamp date; // Identifies date of appointment
	private Integer pounds; // Identifies pounds received; may be null (the appointment has yet to happen)
	
	// Other
	private String clientName; // Caters to joins of client and appointment tables, which is generally done to obtain the client's name
	
	public Appointment()
	{
		
	}
	
	/**
	 * Constructor called in Queries to make a Appointment object from an entry in
	 * the appointment table; assumes SELECT *
	 * 
	 * @param rs
	 *            The ResultSet with the cursor at the desired record
	 */
	public Appointment(ResultSet rs, boolean setClientName) throws SQLException
	{
		appointment_id = rs.getInt("appointment_id");
		client_id = rs.getInt("client_id");
		date = rs.getTimestamp("date");
		if(rs.getInt("pounds") != 0) { // Assumes client never picks up 0 pounds of food
			pounds = rs.getInt("pounds");
		} else {
			pounds = null;
		}
		
		// Set client name; assumes client and appointment tables have been properly joined
		if(setClientName) {
			clientName = rs.getString("first_name") + " " + rs.getString("last_name");
		}
	}
	
	public Appointment(int appointment_id, int client_id, Timestamp date, Integer pounds)
	{
		this.appointment_id = appointment_id;
		this.client_id = client_id;
		this.date = date;
		this.pounds = pounds;
	}
	
	@Override
	public String toString() {
		return Utilities.translateToReadableDate(getDate(), true) + " (" + getPounds() + "#)";
	}
	
	public String asInsertStatement() {
		StringBuilder sb = new StringBuilder();
		
		// Add Insert Information
		sb.append("INSERT INTO `food_pantry_manager`.`appointment` (`appointment_id`,`client_id`,`date`,`pounds`) VALUES (");
		
		// Add appointment_id
		sb.append(getAppointmentID());
		sb.append(",");
		
		// Add client_id
		sb.append(getClientID());
		sb.append(",");
		
		// Add date
		sb.append("\"");
		sb.append(getDate());
		sb.append("\",");
		
		// Add pounds
		if(getPounds() != null)
		{
			sb.append(getPounds());
		}
		else
		{
			sb.append("NULL");
		}
		
		// Close Line
		sb.append(");\n");
		
		return sb.toString();
	}
	
	public int getAppointmentID() {
		return appointment_id;
	}
	
	public int getClientID() {
		return client_id;
	}
	
	/**
	 * Accessed from client side- server side should be the only one setting
	 * this variable (by means of constructor)
	 * 
	 * @return The name of the client that has this Appointment's clientID
	 */
	public String getClientName() {
		return clientName;
	}
	
	public Timestamp getDate() {
		return date;
	}
	
	public Integer getPounds() {
		return pounds;
	}
	
	public void setAppointmentID(int appointment_id) {
		this.appointment_id = appointment_id;
	}
	
	public void setClientID(int client_id) {
		this.client_id = client_id;
	}
	
	public void setDate(Timestamp date) {
		this.date = date;
	}
	
	public void setPounds(Integer pounds) {
		this.pounds = pounds;
	}
}