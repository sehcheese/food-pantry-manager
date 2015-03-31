// Copyright (C) 2015 Scott Hoelsema
// Licensed under GPL v3.0; see LICENSE for full text

package database;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Database object representing an entry in the household table, or
 * information to add an entry or update an entry in the household table
 * 
 * @author Scott Hoelsema
 */
public class Household {
	// Database fields
	private int household_member_id; // Primary key
	private int client_id; // Identifies client that this household member is tied to
	private String name; // Household member name
	private Date birthday; // Household member birthday (can be null)
	private String gender; // Household member gender
	private String relationship; // Household member relationship
	
	public Household()
	{
		
	}
	
	/**
	 * Constructor called in Queries to make a Household object from an entry in
	 * the household table; this object represents one member of the household;
	 * assumes SELECT *
	 * 
	 * @param rs
	 *            The ResultSet with the cursor at the desired record
	 */
	public Household(ResultSet rs) throws SQLException
	{
		household_member_id = rs.getInt("household_member_id");
		client_id = rs.getInt("client_id");
		name = rs.getString("name");
		birthday = rs.getDate("birthday");
		gender = rs.getString("gender");
		relationship = rs.getString("relationship");
	}
	
	@Override
	public String toString() {
		return getName() + ": " + getRelationship();
	}
	
	public String asInsertStatement() {
		StringBuilder sb = new StringBuilder();
		
		// Add Insert Information
		sb.append("INSERT INTO `food_pantry_manager`.`household` (`client_id`,`household_member_id`,`name`,`birthday`,`gender`,`relationship`) VALUES (");
		
		// Add ClientID
		sb.append(client_id);
		sb.append(",");
		
		// Add HouseholdMemberID
		sb.append(household_member_id);
		sb.append(",");
		
		// Add Name
		sb.append("\"");
		sb.append(name);
		sb.append("\",");
		
		// Add Birthday
		if(birthday != null) {
			sb.append("\"");
			sb.append(birthday);
			sb.append("\",");
		} else {
			sb.append("NULL");
			sb.append(",");
		}
		
		// Add Gender
		sb.append("\"");
		sb.append(gender);
		sb.append("\",");
		
		// Add Relationship
		sb.append("\"");
		sb.append(relationship);
		sb.append("\"");
		
		// Close Line
		sb.append(");\n");
		
		return sb.toString();
	}
	
	public Date getBirthday() {
		return birthday;
	}
	
	public int getClientID() {
		return client_id;
	}
	
	public String getGender() {
		return gender;
	}
	
	public int getHouseholdMemberID() {
		return household_member_id;
	}
	
	public String getName() {
		return name;
	}
	
	public String getRelationship() {
		return relationship;
	}
	
	public void setBirthday(Date birthday) {
		this.birthday = birthday;
	}
	
	public void setClientID(int client_id) {
		this.client_id = client_id;
	}
	
	public void setGender(String gender) {
		this.gender = gender;
	}
	
	public void setHouseholdMemberID(int household_member_id) {
		this.household_member_id = household_member_id;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public void setRelationship(String relationship) {
		this.relationship = relationship;
	}
}
