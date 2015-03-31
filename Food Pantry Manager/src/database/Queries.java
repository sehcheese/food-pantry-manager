// Copyright (C) 2015 Scott Hoelsema
// Licensed under GPL v3.0; see LICENSE for full text

package database;

import gui.LogIn;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;

import javax.swing.JOptionPane;

import utils.Logger;

/**
 * A collection of static methods that query the database
 * 
 * @author Scott Hoelsema
 */
public class Queries {
	private static Connection conn = DatabaseConnection.getConnection();
	private static final int CONNECTION_TIMEOUT_LENGTH = 2; // Timeout tolerance for testing database connection
	private static boolean accessBlocked = false; // Allows other classes to forbid querying; used by LogIn
	
	/**
	 * Refresh the connection to the database. This is used when a timeout has
	 * kicked the user off.
	 */
	public static void resetConnection() {
		conn = DatabaseConnection.getConnection();
	}
	
	/*
	 * Allows other classes to block querying. This is necessary because after a
	 * timeout attempting to load another client from the search results invokes
	 * several queries. Consequently, without this method the user would see
	 * multiple "No database connection" error dialogs and multiple login
	 * screens. This is invoked when showNoConnectionError() is invoked, which
	 * indicates that a database error has occurred. Querying is unblocked on a
	 * successful login. Note that currently (1/12/14) this is only checked on
	 * certain queries. This is very much a patch to fix the specific problem
	 * aforementioned: that after a timeout selecting another client from the
	 * search results yields multiple dialogs and log in windows.
	 */
	public static void blockQuerying() {
		accessBlocked = true;
	}
	
	/*
	 * This complements blockQuerying(), once again allowing querying.
	 */
	public static void unblockQuerying() {
		accessBlocked = false;
	}
	
	private static void showNoConnectionError() {
		blockQuerying();
		JOptionPane.showMessageDialog(null, "No database connection.", "Error", JOptionPane.ERROR_MESSAGE);
		new LogIn(false);
	}
	
	private static void showDatabaseError() {
		JOptionPane.showMessageDialog(null, "Database error.", "Error", JOptionPane.ERROR_MESSAGE);
	}
	
	/***************************\
	 * QUERIES ON client TABLE *
	\***************************/
	
	public static ArrayList<Client> getAllClients() {
		try {
			if(conn != null && conn.isValid(CONNECTION_TIMEOUT_LENGTH)) {
				// Prepare and execute SQL
				PreparedStatement getAllClients = conn.prepareStatement("SELECT * FROM `food_pantry_manager`.`client`;");
				ResultSet allClientsRS = getAllClients.executeQuery();
				
				// Put results into Client ArrayList and return
				ArrayList<Client> allClients = new ArrayList<Client>();
				while(allClientsRS.next()) {
					Client c = new Client(allClientsRS);
					allClients.add(c);
				}
				return allClients;
			} else {
				showNoConnectionError();
				return null;
			}
		} catch (SQLException e) {
			Logger.logThrowable(e);
			showDatabaseError();
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Search clients based on passed in criteria
	 * 
	 * @param firstName
	 *            Blank, partial or full first name
	 * @param lastName
	 *            Blank, partial or full last name
	 * @param ssn
	 *            Blank, partial or full social security number
	 * @return ArrayList of clients that match the search criteria
	 */
	public static ArrayList<Client> searchClients(String firstName, String lastName, String ssn) {
		try {
			if(conn != null && conn.isValid(CONNECTION_TIMEOUT_LENGTH)) {
				// Prepare and execute SQL
				PreparedStatement searchClients = conn.prepareStatement("SELECT * FROM `food_pantry_manager`.`client` WHERE first_name LIKE ? AND last_name LIKE ? AND ssn LIKE ? ORDER BY last_name, first_name ASC;");
				searchClients.setString(1, firstName + "%");
				searchClients.setString(2, lastName + "%");
				searchClients.setString(3, ssn + "%");
				ResultSet matchingClients = searchClients.executeQuery();
				
				// Put results into Client ArrayList and return
				ArrayList<Client> clients = new ArrayList<Client>();
				while(matchingClients.next()) {
					Client c = new Client(matchingClients);
					clients.add(c);
				}
				return clients;
			} else {
				showNoConnectionError();
				return null;
			}
		} catch (SQLException e) {
			Logger.logThrowable(e);
			showDatabaseError();
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Returns cities that exist in the database.
	 * 
	 * @return ArrayList of cities in the database
	 */
	public static ArrayList<String> getCities() {
		try {
			if(conn != null && conn.isValid(CONNECTION_TIMEOUT_LENGTH)) {
				// Prepare and execute SQL
				PreparedStatement searchClients = conn.prepareStatement("SELECT DISTINCT city FROM `food_pantry_manager`.`client`;");
				ResultSet citiesInDatabase = searchClients.executeQuery();
				
				// Put results into ArrayList and return
				ArrayList<String> cities = new ArrayList<String>();
				while(citiesInDatabase.next()) {
					cities.add(citiesInDatabase.getString("city"));
				}
				return cities;
			} else {
				showNoConnectionError();
				return null;
			}
		} catch (SQLException e) {
			Logger.logThrowable(e);
			showDatabaseError();
			e.printStackTrace();
			return null;
		}
	}
	
	public static ArrayList<Client> getClientsForCity(String city) {
		try {
			if(conn != null && conn.isValid(CONNECTION_TIMEOUT_LENGTH)) {
				// Prepare and execute SQL
				PreparedStatement searchClients = conn.prepareStatement("SELECT * FROM `food_pantry_manager`.`client` WHERE city = ? ORDER BY last_name, first_name;");
				searchClients.setString(1, city);
				ResultSet matchingClients = searchClients.executeQuery();
				
				// Put results into Client ArrayList and return
				ArrayList<Client> clients = new ArrayList<Client>();
				while(matchingClients.next()) {
					Client c = new Client(matchingClients);
					clients.add(c);
				}
				return clients;
			} else {
				showNoConnectionError();
				return null;
			}
		} catch (SQLException e) {
			Logger.logThrowable(e);
			showDatabaseError();
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Returns a Client object representing the client in the database with the given clientID
	 * 
	 * @param clientID The client ID to lookup
	 * @return Client object representing the client in the database with this clientID
	 */
	public static Client getClientByID(int clientID) {
		if(!accessBlocked) {
			try {
				if(conn != null && conn.isValid(CONNECTION_TIMEOUT_LENGTH)) {
					// Prepare and execute SQL
					PreparedStatement getClientByID = conn.prepareStatement("SELECT * FROM `food_pantry_manager`.`client` WHERE client_id = ?");
					getClientByID.setInt(1, clientID);
					ResultSet resultClient = getClientByID.executeQuery();
					
					// Put results into Client object and return
					resultClient.next();
					Client client = new Client(resultClient);
					return client;
				} else {
					showNoConnectionError();
					return null;
				}
			} catch (SQLException e) {
				Logger.logThrowable(e);
				showDatabaseError();
				e.printStackTrace();
				return null;
			}
		} else {
			return null;
		}
	}
	
	/**
	 * Checks if the given address is listed for another client in the database.
	 * If it is, it returns true; else false.
	 * 
	 * @param address
	 *            The address to compare to; must be exact
	 * @param city
	 *            The city to compare to; must be exact
	 * @param exempt
	 *            A client that is exempt from the check; used if checking for
	 *            duplicates on update of existing client
	 * @return -1 if database or connection error, 0 if no duplicates, 1 if a
	 *         duplicate exists
	 */
	public static int checkForDuplicateAddresses(String address, String city, Client exempt) {
		try {
			if(conn != null && conn.isValid(CONNECTION_TIMEOUT_LENGTH)) {
				// Prepare and execute SQL
				PreparedStatement getAllClients = conn.prepareStatement("SELECT * FROM `food_pantry_manager`.`client`");
				ResultSet allClients = getAllClients.executeQuery();
				
				// Put results into Client ArrayList and return
				while(allClients.next()) {
					if(exempt == null) {
						if(allClients.getString("address").equals(address) && allClients.getString("city").equals(city)) {
							return 1;
						}
					} else {
						if(allClients.getString("address").equals(address) && allClients.getString("city").equals(city) && exempt.getClientID() != allClients.getInt("client_id")) {
							return 1;
						}
					}
					
				}
				return 0;
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
	 * QUERIES ON appointment TABLE *
	\********************************/
	
	public static ArrayList<Appointment> getAllAppointments() {
		try {
			if(conn != null && conn.isValid(CONNECTION_TIMEOUT_LENGTH)) {
				// Prepare and execute SQL
				PreparedStatement getAllAppointments = conn.prepareStatement("SELECT * FROM `food_pantry_manager`.`appointment`;");
				ResultSet allApptsRS = getAllAppointments.executeQuery();
				
				// Put results into Client ArrayList and return
				ArrayList<Appointment> allAppts = new ArrayList<Appointment>();
				while(allApptsRS.next()) {
					Appointment a = new Appointment(allApptsRS, false);
					allAppts.add(a);
				}
				return allAppts;
			} else {
				showNoConnectionError();
				return null;
			}
		} catch (SQLException e) {
			Logger.logThrowable(e);
			showDatabaseError();
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Get the next appointment given a client
	 * 
	 * @param clientID
	 *            The client for whom we are looking up the next appointment
	 * @return Appointment object for this client's next appointment
	 */
	public static Appointment getNextAppointment(int clientID) {
		if(!accessBlocked) {
			try {
				if(conn != null && conn.isValid(CONNECTION_TIMEOUT_LENGTH)) {
					// Prepare and execute SQL
					PreparedStatement nextApptLookup = conn.prepareStatement("SELECT * FROM `food_pantry_manager`.`appointment` WHERE client_id = ? AND pounds IS NULL;");
					nextApptLookup.setInt(1, clientID);
					ResultSet nextAppt = nextApptLookup.executeQuery();
					
					// Put results into Appointment object and return
					nextAppt.next();
					return new Appointment(nextAppt, false);
				} else {
					showNoConnectionError();
					return null;
				}
			} catch (SQLException e) {
				Logger.logThrowable(e);
				showDatabaseError();
				e.printStackTrace();
				return null;
			}
		} else {
			return null;
		}
	}
	
	/**
	 * Return all the completed appointments associated with a given client;
	 * this does not include the client's next appointment
	 * 
	 * @param clientID
	 *            The client for whom to lookup appointments
	 * @return An ArrayList of appointments
	 */
	public static ArrayList<Appointment> getCompletedApptsForClient(int clientID) {
		if(!accessBlocked) {
			try {
				if(conn != null && conn.isValid(CONNECTION_TIMEOUT_LENGTH)) {
					// Prepare and execute SQL
					PreparedStatement apptsForClientLookup = conn.prepareStatement("SELECT * FROM `food_pantry_manager`.`appointment` WHERE client_id = ? AND pounds IS NOT NULL ORDER BY appointment_id DESC");
					apptsForClientLookup.setInt(1, clientID);
					ResultSet clientAppts = apptsForClientLookup.executeQuery();
					
					// Put results into Appointment ArrayList and return
					ArrayList<Appointment> appts = new ArrayList<Appointment>();
					while(clientAppts.next()) {
						Appointment appt = new Appointment(clientAppts, false);
						appts.add(appt);
					}
					return appts;
				} else {
					showNoConnectionError();
					return null;
				}
			} catch (SQLException e) {
				Logger.logThrowable(e);
				showDatabaseError();
				e.printStackTrace();
				return null;
			}
		} else {
			return null;
		}
	}
	
	/**
	 * Returns total number of pounds distributed in the passed in
	 * date/date range
	 * 
	 * @param ts
	 *            String in format YYYY-MM-DD, may contain SQL wildcards
	 * @return int identifying the total number of pounds distributed in the passed
	 *         in date/date range
	 */
	public static int getTotalPoundageForTimestamp(String ts) {
		try {
			if(conn != null && conn.isValid(CONNECTION_TIMEOUT_LENGTH)) {
				// Prepare and execute SQL
				PreparedStatement getTotalPoundage = conn.prepareStatement("SELECT SUM(pounds) FROM `food_pantry_manager`.`appointment` WHERE date LIKE ?;");
				getTotalPoundage.setString(1, ts);
				ResultSet sumPounds = getTotalPoundage.executeQuery();
				
				// Return the sum of the pounds for the given timestamp
				sumPounds.next();
				return sumPounds.getInt("SUM(pounds)");
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
	
	/**
	 * Returns total number of distinct clients served in the passed in
	 * date/date range
	 * 
	 * @param ts
	 *            String in format YYYY-MM-DD, may contain SQL wildcards
	 * @return int identifying the total number of clients served in the passed
	 *         in date/date range
	 */
	public static int getVisitsForTimePeriod(String ts) {
		try {
			if(conn != null && conn.isValid(CONNECTION_TIMEOUT_LENGTH)) {
				// Prepare and execute SQL
				PreparedStatement getNumClientsServed = conn.prepareStatement("SELECT COUNT(appointment_id) FROM `food_pantry_manager`.`appointment` WHERE date LIKE ? AND pounds IS NOT NULL;");
				getNumClientsServed.setString(1, ts);
				ResultSet numClientsServed = getNumClientsServed.executeQuery();
				
				// Return number of visits for the given time period
				numClientsServed.next();
				return numClientsServed.getInt("COUNT(appointment_id)");
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
	
	/**
	 * Returns clients served in the passed in
	 * date/date range
	 * 
	 * @param ts
	 *            String in format YYYY-MM-DD, may contain SQL wildcards
	 * @return ArrayList containing appointments completed in given date range
	 */
	public static ArrayList<Appointment> getCompletedAppointmentsForDate(String ts) {
		try {
			if(conn != null && conn.isValid(CONNECTION_TIMEOUT_LENGTH)) {
				// Prepare and execute SQL
				PreparedStatement clientsServed = conn.prepareStatement("SELECT * FROM `food_pantry_manager`.`appointment`, `food_pantry_manager`.`client` WHERE date LIKE ? AND appointment.client_id = client.client_id AND pounds IS NOT NULL ORDER BY date ASC;");
				clientsServed.setString(1, ts);
				ResultSet matchingClients = clientsServed.executeQuery();
				
				// Put results into Client ArrayList and return
				ArrayList<Appointment> clientsForPeriod = new ArrayList<Appointment>();
				while(matchingClients.next()) {
					Appointment a = new Appointment(matchingClients, true);
					clientsForPeriod.add(a);
				}
				return clientsForPeriod;
			} else {
				showNoConnectionError();
				return null;
			}
		} catch (SQLException e) {
			Logger.logThrowable(e);
			showDatabaseError();
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Returns appointments scheduled in the given date/date range.
	 * 
	 * @param ts
	 *            String in format YYYY-MM-DD, may contain SQL wildcards
	 * @return ArrayList containing appointments scheduled in given date range
	 */
	public static ArrayList<Appointment> getAppointmentsForDate(String ts) {
		try {
			if(conn != null && conn.isValid(CONNECTION_TIMEOUT_LENGTH)) {
				// Prepare and execute SQL
				PreparedStatement clientsServed = conn.prepareStatement("SELECT * FROM `food_pantry_manager`.`appointment`, `food_pantry_manager`.`client` WHERE date LIKE ? AND appointment.client_id = client.client_id ORDER BY date ASC;");
				clientsServed.setString(1, ts);
				ResultSet matchingClients = clientsServed.executeQuery();
				
				// Put results into Client ArrayList and return
				ArrayList<Appointment> clientsForPeriod = new ArrayList<Appointment>();
				while(matchingClients.next()) {
					Appointment a = new Appointment(matchingClients, true);
					clientsForPeriod.add(a);
				}
				return clientsForPeriod;
			} else {
				showNoConnectionError();
				return null;
			}
		} catch (SQLException e) {
			Logger.logThrowable(e);
			showDatabaseError();
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Lookup clients whose last visit was before the passed in Timestamp
	 * 
	 * @param dateThreshold
	 *            The earliest a client could have a completed appointment and
	 *            still be considered active
	 * @return An ArrayList of clients who are inactive by the criterion given
	 */
	public static ArrayList<Appointment> lookupInactiveClients(Timestamp dateThreshold) {
		try {
			if(conn != null && conn.isValid(CONNECTION_TIMEOUT_LENGTH)) {
				// Prepare and execute SQL
				PreparedStatement inactiveClients = conn.prepareStatement("SELECT appointment_id, pounds, client.client_id, first_name, last_name, MAX(date) AS date FROM `food_pantry_manager`.`appointment`,`food_pantry_manager`.`client` WHERE pounds IS NOT NULL AND appointment.client_id = client.client_id GROUP BY appointment.client_id HAVING MAX(date) < ? ORDER BY date ASC;");
				inactiveClients.setTimestamp(1, dateThreshold);
				ResultSet matchingClients = inactiveClients.executeQuery();
				
				// Put results into Appointment ArrayList and return
				ArrayList<Appointment> inactive = new ArrayList<Appointment>();
				while(matchingClients.next()) {
					Appointment a = new Appointment(matchingClients, true);
					inactive.add(a);
				}
				return inactive;
			} else {
				showNoConnectionError();
				return null;
			}
		} catch (SQLException e) {
			Logger.logThrowable(e);
			showDatabaseError();
			e.printStackTrace();
			return null;
		}
	}
	
	/******************************\
	 * QUERIES ON household TABLE *
	\******************************/
	
	public static ArrayList<Household> getAllHouseholds() {
		try {
			if(conn != null && conn.isValid(CONNECTION_TIMEOUT_LENGTH)) {
				// Prepare and execute SQL
				PreparedStatement getAllHouseholds = conn.prepareStatement("SELECT * FROM `food_pantry_manager`.`household`;");
				ResultSet allHouseholdsRS = getAllHouseholds.executeQuery();
				
				// Put results into Client ArrayList and return
				ArrayList<Household> allHouseholds = new ArrayList<Household>();
				while(allHouseholdsRS.next()) {
					Household h = new Household(allHouseholdsRS);
					allHouseholds.add(h);
				}
				return allHouseholds;
			} else {
				showNoConnectionError();
				return null;
			}
		} catch (SQLException e) {
			Logger.logThrowable(e);
			showDatabaseError();
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Get household members for the given client
	 * 
	 * @param clientID
	 *            The client for whom we are looking up the household members
	 * @return ArrayList of household members belonging to this client
	 */
	public static ArrayList<Household> getHouseholdForClient(int clientID) {
		if(!accessBlocked) {
			try {
				if(conn != null && conn.isValid(CONNECTION_TIMEOUT_LENGTH)) {
					// Prepare and execute SQL
					PreparedStatement getHouseholdMembers = conn.prepareStatement("SELECT * FROM `food_pantry_manager`.`household` WHERE client_id = ?");
					getHouseholdMembers.setInt(1, clientID);
					ResultSet householdMembers = getHouseholdMembers.executeQuery();
					
					// Put results into Client ArrayList and return
					ArrayList<Household> clients = new ArrayList<Household>();
					while(householdMembers.next()) {
						Household h = new Household(householdMembers);
						clients.add(h);
					}
					return clients;
				} else {
					showNoConnectionError();
					return null;
				}
			} catch (SQLException e) {
				Logger.logThrowable(e);
				showDatabaseError();
				e.printStackTrace();
				return null;
			}
		} else {
			return null;
		}
	}
}