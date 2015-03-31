// Copyright (C) 2015 Scott Hoelsema
// Licensed under GPL v3.0; see LICENSE for full text

package database;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

/**
 * Database object representing an entry in the client table, or
 * information to add an entry or update an entry in the client table
 * 
 * @author Scott Hoelsema
 */
public class Client {
	// Database fields
	private int client_id; // Primary key
	private String first_name; // First name
	private String last_name; // Last name
	private String ssn; // Social security number (cannot be null; flagged as "Withheld" if not provided)
	private Date birthday; // Birthday (can be null)
	private String address; // Address
	private String city; // City
	private String telephone; // Telephone number (can be null)
	private String gender; // Gender
	private String notes; // Notes (can be null; cannot exceed 256 characters)
	private Timestamp valid_as_of; // Timestamp identifying when this record was last updated; should only be set from Queries
	
	
	public Client()
	{
		
	}
	
	/**
	 * Constructor called in Queries to make a Client object from an entry in
	 * the client table; assumes SELECT *
	 * 
	 * @param rs
	 *            The ResultSet with the cursor at the desired record
	 */
	public Client(ResultSet rs) throws SQLException
	{
		this.client_id = rs.getInt("client_id");
		this.first_name = rs.getString("first_name");
		this.last_name = rs.getString("last_name");
		this.birthday = rs.getDate("birthday");
		this.ssn = rs.getString("ssn");
		this.address = rs.getString("address");
		this.city = rs.getString("city");
		this.telephone = rs.getString("telephone");
		this.gender = rs.getString("gender");
		this.notes = rs.getString("notes");
		this.valid_as_of = rs.getTimestamp("valid_as_of");
	}
	
	@Override
	public String toString() {
		return first_name + " " + last_name;
	}
	
	public String asInsertStatement() {
		StringBuilder sb = new StringBuilder();
		
		// Add Insert Information
		// Use double quotes in the SQL because of names like O'Brien
		sb.append("INSERT INTO `food_pantry_manager`.`client` (`client_id`,`first_name`,`last_name`,`ssn`,`address`,`city`,`telephone`,`gender`,`valid_as_of`,`birthday`,`notes`) VALUES (");
		
		// Add client_id
		sb.append(client_id);
		sb.append(",");
		
		// Add first_name
		sb.append("\"");
		sb.append(first_name.replaceAll("\"", "\"\"")); // Account for user putting quotes in first name to represent preferred name (example: Billy "Bob")
		sb.append("\",");
		
		// Add last_name
		sb.append("\"");
		sb.append(last_name);
		sb.append("\",");
		
		// Add ssn
		sb.append("\"");
		sb.append(ssn);
		sb.append("\",");
		
		// Add address
		sb.append("\"");
		sb.append(address);
		sb.append("\",");
		
		// Add city
		sb.append("\"");
		sb.append(city);
		sb.append("\",");
		
		// Add telephone
		if(telephone != null)
		{
			sb.append("\"");
			sb.append(telephone);
			sb.append("\",");
		}
		else
		{
			sb.append("NULL");
			sb.append(",");
		}
		
		// Add gender
		sb.append("\"");
		sb.append(gender);
		sb.append("\",");
		
		// Add valid_as_of
		sb.append("\"");
		sb.append(valid_as_of);
		sb.append("\",");
		
		// Add birthday
		if(birthday != null) {
			sb.append("\"");
			sb.append(birthday);
			sb.append("\",");
		} else {
			sb.append("NULL");
			sb.append(",");
		}
		
		// Add notes
		if(notes != null) {
			sb.append("\"");
			sb.append(notes);
			sb.append("\"");
		} else {
			sb.append("NULL");
		}
		
		// Close Line
		sb.append(");\n");
		
		return sb.toString();
	}
	
	public String getAddress() {
		return address;
	}
	
	public Date getBirthday() {
		return birthday;
	}
	
	public String getCity() {
		return city;
	}
	
	public int getClientID() {
		return client_id;
	}
	
	public String getFirstName() {
		return first_name;
	}
	
	public String getGender() {
		return gender;
	}
	
	public String getLastName() {
		return last_name;
	}
	
	public String getNotes() {
		return notes;
	}
	
	public String getSsn() {
		return ssn;
	}
	
	public String getTelephone() {
		return telephone;
	}
	
	public Timestamp getValidAsOf() {
		return valid_as_of;
	}
	
	public void setAddress(String address) {
		this.address = address;
	}
	
	public void setBirthday(Date birthday) {
		this.birthday = birthday;
	}
	
	public void setCity(String city) {
		this.city = city;
	}
	
	public void setClientID(int client_id) {
		this.client_id = client_id;
	}
	
	public void setFirstName(String first_name) {
		this.first_name = first_name;
	}
	
	public void setGender(String gender) {
		this.gender = gender;
	}
	
	public void setLastName(String last_name) {
		this.last_name = last_name;
	}
	
	public void setNotes(String notes) {
		this.notes = notes;
	}
	
	public void setSsn(String ssn) {
		this.ssn = ssn;
	}
	
	public void setTelephone(String telephone) {
		this.telephone = telephone;
	}
	
	public void setValidAsOf(Timestamp valid_as_of) {
		this.valid_as_of = valid_as_of;
	}
}