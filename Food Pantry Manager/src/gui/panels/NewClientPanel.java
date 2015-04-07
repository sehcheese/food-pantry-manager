// Copyright (C) 2015 Scott Hoelsema
// Licensed under GPL v3.0; see LICENSE for full text

package gui.panels;

import gui.FoodPantryManager;
import gui.listeners.entryFieldFocusListener;
import gui.supportingelements.DatePanel;
import gui.supportingelements.TimeTextField;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.Border;

import database.Appointment;
import database.Client;
import database.Inserts;
import database.Queries;

public class NewClientPanel extends JPanel {
	private FoodPantryManager fpm;
	private JPanel northPanel;
	private JTextField firstName;
	private JTextField lastName;
	private JTextField ssn;
	private JTextField address;
	private JComboBox<String> cityList;
	private JTextField otherLocation;
	private JTextField telephone;
	private JComboBox<String> sex;
	private DatePanel birthdayDatePanel;
	private JPanel southPanel;
	private DatePanel firstApptDatePanel;
	private JPanel timePanel;
	private TimeTextField apptTime;
	private JButton newClientSubmit;
	
	public NewClientPanel(FoodPantryManager fpm)
	{
		this.fpm = fpm;
		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		buildNewClient();
	}

	public void buildNewClient() {
		// Create elements
		northPanel = new JPanel(new GridLayout(9,1,5,5)); // Client information...
		Border northBorder = BorderFactory.createTitledBorder("Client Information");
		JLabel firstNameLabel = new JLabel("First Name:");
		firstName = new JTextField(15);
		JLabel lastNameLabel = new JLabel("Last Name:");
		lastName = new JTextField(15);
		JLabel ssnLabel = new JLabel("Social Security Number:");
		ssn = new JTextField("0, 4, or 9 Digits",15);
		JLabel addressLabel = new JLabel("Address:");
		address = new JTextField(15);
		JLabel cityListLabel = new JLabel("City:");
		ArrayList<String> cities = Queries.getCities();
		cities.add("Other");
		cityList = new JComboBox<String>(cities.toArray(new String[cities.size()]));
		otherLocation = new JTextField("Other City",15);
		JLabel telephoneLabel = new JLabel("Telephone:");
		telephone = new JTextField("XxxYyyZzzz",15);
		JLabel sexLabel = new JLabel("Gender:");
		sex = new JComboBox<String>();
		sex.addItem("Male");
		sex.addItem("Female");
		JLabel birthdayLabel = new JLabel("Birthday:");
		birthdayDatePanel = new DatePanel(false, true);
		southPanel = new JPanel(new GridLayout(3,2,5,5)); // First appointment information...
		Border southBorder = BorderFactory.createTitledBorder("First Appointment");
		JLabel firstApptLabel = new JLabel("Date:");
		firstApptDatePanel = new DatePanel(false, true);
		timePanel = new JPanel();
		apptTime = new TimeTextField();
		newClientSubmit = new JButton("Submit");
		
		// Add elements to northPanel
		northPanel.setBorder(northBorder);
		northPanel.add(firstNameLabel);
		firstName.addFocusListener(new entryFieldFocusListener(firstName));
		northPanel.add(firstName); // Add firstName
		northPanel.add(lastNameLabel);
		lastName.addFocusListener(new entryFieldFocusListener(lastName));
		northPanel.add(lastName); // Add lastName
		northPanel.add(ssnLabel);
		ssn.addFocusListener(new entryFieldFocusListener(ssn));
		northPanel.add(ssn); // Add ssn
		northPanel.add(addressLabel);
		address.addFocusListener(new entryFieldFocusListener(address));
		northPanel.add(address); // Add address
		northPanel.add(cityListLabel);
		cityList.addItemListener(new cityListListener());
		northPanel.add(cityList); // Add cityList
		northPanel.add(new JLabel()); // Add filler
		// cityListListener will not trigger if only 1 item in list (which would be "Other") because the state never changes; 
		// therefore if there is only one item in the list we need to set the original state of otherLocation to be enabled
		if(cities.size() < 2) 
			otherLocation.setEnabled(true);
		else
			otherLocation.setEnabled(false);
		otherLocation.addFocusListener(new entryFieldFocusListener(otherLocation));
		northPanel.add(otherLocation); // Add otherLocation
		northPanel.add(telephoneLabel);
		telephone.addFocusListener(new entryFieldFocusListener(telephone));
		northPanel.add(telephone); // Add telephone
		northPanel.add(sexLabel);
		northPanel.add(sex); // Add sex
		northPanel.add(birthdayLabel);
		northPanel.add(birthdayDatePanel); // Add birthdayDatePanel
		northPanel.setMaximumSize(new Dimension(450, 340)); // northPanel size
		
		// Add elements to southPanel
		southPanel.setBorder(southBorder);
		southPanel.add(firstApptLabel);
		firstApptDatePanel.setToCurrent();
		southPanel.add(firstApptDatePanel); // Add firstApptDatePanel
		southPanel.add(new JLabel()); // Add filler TODO should be label?
		apptTime.addFocusListener(new entryFieldFocusListener(apptTime));
		timePanel.add(apptTime);
		southPanel.add(timePanel); // Add apptTime
		southPanel.add(new JLabel()); // Add filler
		newClientSubmit.addActionListener(new submitListener());
		southPanel.add(newClientSubmit); // Add newClientSubmit
		southPanel.setMaximumSize(new Dimension(450, 150)); // southPanel size
		
		// Add panels to NewClientPanel
		add(northPanel, BorderLayout.NORTH);
		add(southPanel, BorderLayout.SOUTH);
	}
	
	private class submitListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			if(!performFormatChecks()) {
				return;
			} else {
				if(cityList.getSelectedIndex() == cityList.getModel().getSize() - 1) { // If "Other" is selected in cityList
					int result = Queries.checkForDuplicateAddresses(address.getText(), otherLocation.getText(), null);
					
					if(result == -1) { // There was a database or connection error
						return;
					} else if(result == 1) { // A duplicate address was found
						JOptionPane.showMessageDialog(null, "Duplicate addresses are not allowed.", "Error", JOptionPane.ERROR_MESSAGE);
						return;
					}
				} else { // A listed city is selected
					int result = Queries.checkForDuplicateAddresses(address.getText(), (String)cityList.getSelectedItem(), null);
					
					if(result == -1) { // There was a database or connection error
						return;
					} else if(result == 1) { // A duplicate address was found
						JOptionPane.showMessageDialog(null, "Duplicate addresses are not allowed.", "Error", JOptionPane.ERROR_MESSAGE);
						return;
					}
				}
				
				// Insert new client
				Client c = new Client();
				c.setFirstName(firstName.getText()); // Set firstName
				c.setLastName(lastName.getText()); // Set lastName
				if(ssn.getText().length() == 4 || ssn.getText().length() == 9) { // Set SSN
					c.setSsn(ssn.getText());
				} else {
					c.setSsn("Withheld");
				}
				if(birthdayDatePanel.getSQLDate() != null) { // Set birthday
					c.setBirthday(birthdayDatePanel.getSQLDate());
				} else {
					c.setBirthday(null);
				}
				c.setAddress(address.getText()); // Set address
				if(cityList.getSelectedIndex() == cityList.getModel().getSize() - 1) { // Set city
					c.setCity(otherLocation.getText()); // "Other" was selected, so get the city name manually entered
				} else {
					c.setCity((String)cityList.getSelectedItem());
				}
				if(!telephone.getText().equals("") && !telephone.getText().equals("XxxYyyZzzz")) { // Set telephone; put in pretty format
					String telephoneString = telephone.getText();
					telephoneString = "(" + telephoneString.substring(0,3) + ") " + telephoneString.substring(3,6) + "-" + telephoneString.substring(6);
					c.setTelephone(telephoneString);
				} else {
					c.setTelephone(null);
				}
				c.setGender((String)sex.getSelectedItem()); // Set gender
				/*** If notes for client added, do c.setNotes(String notes) here ***/
				
				int clientID = Inserts.insertClient(c); // Perform insert of client
				
				if(clientID != -1) { // There was no database or connection problem
					// Insert first appointment for new client
					Appointment firstAppt = new Appointment();
					firstAppt.setClientID(clientID);
					firstAppt.setDate(apptTime.getTimestamp(firstApptDatePanel));
					boolean firstApptAdded = Inserts.addNextAppointment(firstAppt); // Perform insert of appointment
					
					if(firstApptAdded) { // There was no database or connection problem
						// Update city lists if a new city was added
						if(otherLocation.isEnabled()) {
							fpm.updateCityLists();
						}
						
						// Alert success
						JOptionPane.showMessageDialog(null, "Successful addition of client!", "Success", JOptionPane.INFORMATION_MESSAGE);
												
						// Clear data that was just entered
						clearData();
					}
				}
			}
		}
	}
	
	/**
	 * Check format of user entry. Display one or more messages telling them
	 * what the problem(s) were. Return a boolean identifying if there were any
	 * format problems.
	 * 
	 * @return A boolean identifying if there are any format problems
	 */
	public boolean performFormatChecks()
	{
		boolean noFormatProblems = true;
		
		if(!birthdayDatePanel.checkFormat()) {
			noFormatProblems = false;
		}
		if(!firstApptDatePanel.checkFormat()) {
			noFormatProblems = false;
		}
		
		// Check TimeTextField
		if(!apptTime.checkFormat()) {
			noFormatProblems = false;
		}
		
		// Check first for fields left empty
		if(firstName.getText().equals("") || lastName.getText().equals("") || address.getText().equals("")) {
			JOptionPane.showMessageDialog(null, "Only the Social Security Number and Telephone may be left empty.", "Error", JOptionPane.ERROR_MESSAGE);
			noFormatProblems = false;
		} else { // Now check if Social Security number entered properly
			if(ssn.getText().length()!=9 && ssn.getText().length()!=4 && ssn.getText().length() != 0 && !ssn.getText().equals("0, 4, or 9 Digits")) {
				JOptionPane.showMessageDialog(null, "Invalid Social Security Number entry.", "Error", JOptionPane.ERROR_MESSAGE);
				ssn.setText("0, 4, or 9 Digits");
				noFormatProblems = false;
			} else {
				try {
					if(ssn.getText().length()==9 || ssn.getText().length()==4) {
						Integer.parseInt(ssn.getText());
					}
				} catch(NumberFormatException e1) {
					JOptionPane.showMessageDialog(null, "Invalid Social Security Number entry.", "Error", JOptionPane.ERROR_MESSAGE);
					ssn.setText("0, 4, or 9 Digits");
					noFormatProblems = false;
				}
			}
			
			// Now check telephone entered properly
			if(telephone.getText().length()!=10 && !telephone.getText().equals("XxxYyyZzzz") && !telephone.getText().equals("")) {
				JOptionPane.showMessageDialog(null, "Invalid telephone number entry.", "Error", JOptionPane.ERROR_MESSAGE);
				telephone.setText("XxxYyyZzzz");
				noFormatProblems = false;
			} else {
				if(telephone.getText().equals("") || telephone.getText().equals("XxxYyyZzzz")) {
					
				} else {
					try {
						Long.parseLong(telephone.getText());
					} catch(NumberFormatException e1) {
						JOptionPane.showMessageDialog(null, "Invalid telephone number entry.", "Error", JOptionPane.ERROR_MESSAGE);
						telephone.setText("XxxYyyZzzz");
						noFormatProblems = false;
					}
				}
			}
		}
		return noFormatProblems;
	}
	
	private void clearData() {
		firstName.setText("");
		lastName.setText("");
		ssn.setText("0, 4, or 9 Digits");
		address.setText("");
		cityList.setSelectedIndex(0);
		otherLocation.setText("Other City");
		telephone.setText("XxxYyyZzzz");
		sex.setSelectedIndex(0);
		birthdayDatePanel.clear();
		firstApptDatePanel.setToCurrent();
		apptTime.clear();
	}
	
	public void updateCityList(String[] cities) {
		cityList.setModel(new DefaultComboBoxModel<String>(cities));
		cityList.getListeners(ItemListener.class)[0].itemStateChanged(null); // Fire event so that the Other City text field will become disabled, as it should. The model has been changed, but this has not been caught by the itemlistener for cityList.
	}
	
	/**
	 * Makes otherLocation enabled if "Other" is selected in cityList
	 * 
	 * @author Scott Hoelsema
	 */
	private class cityListListener implements ItemListener {
		public void itemStateChanged(ItemEvent e) {
			if(cityList.getSelectedIndex() == cityList.getModel().getSize() - 1) { // Assumes "Other" is last option
				otherLocation.setEnabled(true);
			} else {
				otherLocation.setEnabled(false);
			}
		}
	}
}