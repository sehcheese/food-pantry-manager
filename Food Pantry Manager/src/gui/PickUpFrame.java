// Copyright (C) 2015 Scott Hoelsema
// Licensed under GPL v3.0; see LICENSE for full text

package gui;

import gui.listeners.entryFieldFocusListener;
import gui.supportingelements.DatePanel;
import gui.supportingelements.TimeTextField;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import utils.Utilities;
import database.Appointment;
import database.Client;
import database.Inserts;
import database.Queries;
import database.Updates;

public class PickUpFrame extends JFrame {
	private FoodPantryManager fpm;
	private Client activeClient;
	private JPanel northPanel; // Pounds Variables
	private JTextField pounds;
	private JPanel centerPanel;
	private JPanel highCenterPanel;
	private DatePanel thisApptDatePanel;
	private JPanel thisApptTimeSuperPanel;
	private JPanel thisApptTimePanel;
	private TimeTextField thisApptTime;
	private JPanel lowCenterPanel;
	private DatePanel nextApptDatePanel;
	private JPanel nextApptTimeSuperPanel;
	private JPanel nextApptTimePanel;
	private TimeTextField nextApptTime;
	private JPanel southPanel;
	private JButton submitButton;
	
	public PickUpFrame(Client activeClient, FoodPantryManager fpm)
	{
		// Set variables
		this.fpm = fpm;
		this.activeClient = activeClient;
		
		// Window settings
		setTitle("Pick Up Information");
		Utilities.setIcon(this);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setLayout(new BorderLayout(5,5));
		
		buildLayout();
		
		Utilities.centerOnScreen(this);
		setVisible(true);
	}

	/**
	 * Build GUI and set defaults
	 */
	public void buildLayout()
	{
		// Create elements
		northPanel = new JPanel(new GridLayout(1,2,5,5)); // Holds pounds
		JLabel poundsLabel = new JLabel("Pounds:");
		pounds = new JTextField(10);
		
		centerPanel = new JPanel(new BorderLayout(5,5)); // Parent of highCenterPanel and LowCenterPanel, date and time info
		
		highCenterPanel = new JPanel(new GridLayout(2,2,5,5)); // Parent of label and thisApptTimeAndDatePanel
		JLabel thisApptLabel = new JLabel("Pick Up Date & Time:");
		thisApptTimeSuperPanel = new JPanel(); // Parent of thisApptDatePanel and thisApptTimePanel
		thisApptDatePanel = new DatePanel(false, false);
		thisApptTimePanel = new JPanel(new GridLayout(1,2,5,5));
		thisApptTime = new TimeTextField();
		
		lowCenterPanel = new JPanel(new GridLayout(2,2,5,5)); // Parent of label and nextApptTimeAndDatePanel
		JLabel nextApptLabel = new JLabel("Next Appoitment:");
		nextApptTimeSuperPanel = new JPanel(); // Parent of nextApptDatePanel and nextApptTimePanel
		nextApptDatePanel = new DatePanel(false, false);
		nextApptTimePanel = new JPanel(new GridLayout(1,2,5,5));
		nextApptTime = new TimeTextField();
		
		southPanel = new JPanel();
		submitButton = new JButton("Submit");
		
		// Add elements to northPanel
		northPanel.add(poundsLabel);
		pounds.addFocusListener(new entryFieldFocusListener(pounds));
		northPanel.add(pounds);
		
		
		// Add elements to centerPanel - this appointment section
		highCenterPanel.add(thisApptLabel); // Add label
		thisApptDatePanel.setToCurrent();
		highCenterPanel.add(thisApptDatePanel); // Add DatePanel
		thisApptTime.addFocusListener(new entryFieldFocusListener(thisApptTime));
		thisApptTime.setToCurrent();
		thisApptTimePanel.add(thisApptTime);
		highCenterPanel.add(new JLabel()); // Add filler
		thisApptTimeSuperPanel.add(thisApptTimePanel);
		highCenterPanel.add(thisApptTimeSuperPanel); // Add TimeTextField
		centerPanel.add(highCenterPanel, BorderLayout.CENTER);
		
		// Add elements to centerPanel - next appointment section
		lowCenterPanel.add(nextApptLabel); // Add label
		nextApptDatePanel.setAheadFromCurrent(4); // Pass in number of weeks to put appointment ahead by default
		lowCenterPanel.add(nextApptDatePanel); // Add DatePanel
		nextApptTime.addFocusListener(new entryFieldFocusListener(nextApptTime));
		nextApptTime.setToNearestHalfHour(); // Set to nearest half hour
		nextApptTimePanel.add(nextApptTime);
		lowCenterPanel.add(new JLabel()); // Add filler
		nextApptTimeSuperPanel.add(nextApptTimePanel);
		lowCenterPanel.add(nextApptTimeSuperPanel); // Add TimeTextField
		centerPanel.add(lowCenterPanel, BorderLayout.SOUTH);
		
		// Add elements to southPanel
		southPanel.add(new JLabel()); // Add filler JLabel
		submitButton.addActionListener(new submitListener());
		southPanel.add(submitButton); // Add submit button
		
		// Add panels to PickUpFrame
		add(northPanel, BorderLayout.NORTH);
		add(centerPanel, BorderLayout.CENTER);
		add(southPanel, BorderLayout.SOUTH);
		
		// Pack
		pack();
	}
	
	/**
	 * Submit Listener: Updates target date entry with pounds and actual pick up
	 * date, creates new target date entry, updates display
	 * 
	 * @author Scott Hoelsema
	 */
	private class submitListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			// First, make sure our formats are all set.
			if(!checkFormat()) {
				return;
			}
			
			// Lookup the client's next appointment
			Appointment thisAppt = Queries.getNextAppointment(activeClient.getClientID());
			
			if(thisAppt != null) { // There was not a database or connection problem looking up the client's next appointment
				// Check if appointment may have already been entered - is there a completed appointment on the same day?
				ArrayList<Appointment> appts = Queries.getCompletedApptsForClient(activeClient.getClientID());
				for(Appointment a : appts) {
					if(a.getDate().toString().substring(0,11).equals(thisApptTime.getTimestamp(thisApptDatePanel).toString().substring(0,11))) { // If appointment attempting to be completed is on the same day as an existing completed appointment. get confirmation to continue
						int selectedOption = JOptionPane.showConfirmDialog(null, "A completed appointment for this day already exists. You may have already entered this appointment into the system. Continue?", "Confirm", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
						if(selectedOption!=0) {
							return;
						}
						break;
					}
				}
				
				// Check if current datetime is before scheduled pickup datetime
				if(thisAppt.getDate().getTime() > System.currentTimeMillis()) {
					int selectedOption = JOptionPane.showConfirmDialog(null, "Current date and time are before scheduled pick up time. Continue?", "Confirm", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
					if(selectedOption!=0) {
						return;
					}
				}
				
				// Update the entry containing the target pickup date to the actual pickup date and pounds picked up
				thisAppt.setDate(thisApptTime.getTimestamp(thisApptDatePanel));
				thisAppt.setPounds(Integer.parseInt(pounds.getText()));
				boolean successfulUpdateThisAppt = Updates.doPickup(thisAppt);
				
				if(successfulUpdateThisAppt) { // There was not a database or connection problem completing the appointment
					// Insert a new entry with the target date for the next appointment, but empty pound and date columns
					Appointment nextAppt = new Appointment();
					nextAppt.setClientID(thisAppt.getClientID());
					nextAppt.setDate(nextApptTime.getTimestamp(nextApptDatePanel));
					boolean successfulAddNextAppt = Inserts.addNextAppointment(nextAppt);
					
					if(successfulAddNextAppt) { // There was not a database or connection problem adding the next appointment
						// I don't think this it's really necessary to do a fresh query,
						// but here's the code should you, dear reader, think otherwise.
						/* It's possible that some other things about this client have
						 * changed besides the appointment we just completed, so we'll go
						 * ahead and do a fresh query. */
						/*Client c = Queries.getClientByID(activeClient.getClientID());
						if(c != null) { // There was not a database or connection problem
							mp.updateForClient(c);
						}*/
						
						// Update display for this client so it shows the appointment we just entered
						fpm.getMainPanel().updateForClient(activeClient);
						
						// Refresh EditAppointmentPanel so that it displays the appointment just completed
						fpm.getEditAppointmentsPanel().refresh();
						
						// Refresh AppointmentsPanel so that it displays this appointment as completed
						fpm.getAppointmentsPanel().refresh();
					}
				}
			}
			
			dispose();
		}
	}
	
	/**
	 * Check that formatting of entered data is correct; since date and time
	 * format checking happen in DatePanel and TimeTextField, we only need to
	 * call the appropriate methods in each of those objects.
	 * 
	 * @return A boolean representing pass or fail of format checks
	 */
	public boolean checkFormat() {
		// Check DatePanels
		if(!thisApptDatePanel.checkFormat()) {
			return false;
		}
		if(!nextApptDatePanel.checkFormat()) {
			return false;
		}
		
		// Check TimeTextFields
		if(!thisApptTime.checkFormat()) {
			return false;
		}
		if(!nextApptTime.checkFormat()) {
			return false;
		}
		
		// Check pounds
		if(pounds.getText().length()>3) { // Whoa! Nobody gets that much food at one appointment.
			JOptionPane.showMessageDialog(null, "Invalid Pounds Value - Greater Than 3 Digits", "Error", JOptionPane.ERROR_MESSAGE);
			pounds.setText("");
			return false;	
		} else {
			try {
				Integer.parseInt(pounds.getText());
				return true;
			} catch(NumberFormatException e1) { // Not a number
				JOptionPane.showMessageDialog(null, "Invalid Pounds Value - Use Integer", "Error", JOptionPane.ERROR_MESSAGE);
				pounds.setText("");
				return false;
			}	
		}
	}
}