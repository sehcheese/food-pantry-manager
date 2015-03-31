// Copyright (C) 2015 Scott Hoelsema
// Licensed under GPL v3.0; see LICENSE for full text

package gui.panels;

import gui.PickUpFrame;
import gui.FoodPantryManager;
import gui.SelectPrinter;
import gui.supportingelements.SearchPanel;
import gui.supportingelements.SearchPanelModel;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import utils.Utilities;
import database.Appointment;
import database.Client;
import database.Household;
import database.Queries;

public class MainPanel extends JPanel implements IUpdateOnSearch{
	private FoodPantryManager fpm;
	private SearchPanel sp;
	private JTextArea display = new JTextArea(18, 60);
	private JScrollPane scrollDisplay;
	private Client activeClient;
	private String activeClientTitle;
	private JButton nextAppt;
	private JButton pickup;
	private JButton report;
	
	public MainPanel(FoodPantryManager fpm, SearchPanelModel spm)
	{		
		// Set variables
		this.fpm = fpm;
		sp = new SearchPanel(fpm, spm);
		
		// Set layout
		setLayout(new BorderLayout(5,5));
		
		// Add search panel to top of MainPanel
		add(sp, BorderLayout.NORTH);

		// Build and add text display to center of MainPanel
		display.setEditable(false);
		display.setFont(new Font("Lucida Console", Font.PLAIN, 12));
		scrollDisplay = new JScrollPane(display);
		add(scrollDisplay, BorderLayout.CENTER);

		// Build and add buttons to bottom of MainPanel
		buildButtonPanel();
	}
	
	/**
	 * Build the button panel in the south
	 */
	public void buildButtonPanel() // Build Button Panel (South)
	{
		// Create elements
		JPanel south = new JPanel(new BorderLayout(10,10));
//		backup = new JButton("Back Up Database");
		nextAppt = new JButton("Next Appointment");
		pickup = new JButton("  Pick Up  ");
		report = new JButton("Print Report");
		
		// Make west panel
		JPanel westPanel = new JPanel();
//		westPanel.add(backup, BorderLayout.WEST);
		westPanel.add(nextAppt, BorderLayout.EAST);
		
		// Make east panel
		JPanel eastPanel = new JPanel();
		eastPanel.add(report);
		
		// Add elements
		south.add(westPanel, BorderLayout.WEST);
		south.add(pickup, BorderLayout.CENTER);
		south.add(eastPanel, BorderLayout.EAST);
		
		// Set disabled by default
		nextAppt.setEnabled(false);
		pickup.setEnabled(false);
		report.setEnabled(false);
		
		// Add listeners
		nextAppt.addActionListener(new nextApptListener());
		pickup.addActionListener(new pickUpListener());
		report.addActionListener(new reportListener());
		
		// Add to bottom of MainPanel
		add(south, BorderLayout.SOUTH);
	}
	
	/**
	 * When the active client changes, this method updates the components of
	 * MainPanel with the active client's information.
	 * 
	 * @param c
	 *            The active client; null if none active
	 */
	public void updateForClient(Client c) {
		// A necessary check to prevent extra work.
		// Too many events fire because each tab tries to update every other tab on selecting a different client in the list.
		// The root problem is that SearchPanel has three different instances - one on each of three different tabs.
		// We cannot add the same SearchPanel object to each different tab - that doesn't work.
		// This could perhaps be resolved by changing the GUI so that SearchPanel is separate, but then it would have to appear above every tab,
		// eating up space in tabs that don't need the search panel.
		if (activeClient == c) return;
		
		if(c == null) { // If no client is selected (there were no search matches)...			
			// Clear text area
			display.setText("");
			
			// Disable buttons
			nextAppt.setEnabled(false);
			pickup.setEnabled(false);
			report.setEnabled(false);
		} else {
			// Update active client
			activeClient = c;
			
			// Make sure client is selected in this panel's search panel
			// The updates may be coming from selecting a client in another panel's search panel
			sp.setSelected(c);
			
			// Clear text area
			display.setText("");
			
			// Enable buttons
			nextAppt.setEnabled(true);
			pickup.setEnabled(true);
			report.setEnabled(true);
			
			// Write information for this client to display
			appendClientInformation(c); // Client information
			
			ArrayList<Household> householdMembers = Queries.getHouseholdForClient(activeClient.getClientID()); // Household information
			if(householdMembers != null) { // There was not a database or connection problem
				appendHouseholdInformation(householdMembers);
			}
			
			ArrayList<Appointment> appts = Queries.getCompletedApptsForClient(activeClient.getClientID()); // Appointment information
			if(appts != null) { // There was not a database or connection problem
				appendAppointmentInformation(appts);
			}
			
			
			// Force scrollbar to top
			display.setSelectionStart(0);
			display.setSelectionEnd(0);
		}
	}
	
	/**
	 * Helper method for updateForClient that posts client information to the display
	 * 
	 * @param c
	 */
	private void appendClientInformation(Client c) {
		// Append name, ssn, and address
		activeClientTitle = c.getFirstName() + " " + c.getLastName() + " (Case ID: " + c.getClientID() + ")\n";
		display.append(activeClientTitle);
		display.append("SSN: " + c.getSsn() + "\n");
		display.append(c.getAddress() + ", " + c.getCity() + "\n");
		
		// Append telephone and birthday, if provided
		if(c.getTelephone() != null) {
			display.append(c.getTelephone() + "\n");
		}
		if(c.getBirthday() != null) {
			long yearMillis = 31556952000L;
			Date currentDatetime = new Date(System.currentTimeMillis()); Timestamp current = new Timestamp(currentDatetime.getTime());
			int clientAge = (int)((current.getTime() - c.getBirthday().getTime())/yearMillis);
			display.append("Birthday: " + Utilities.translateToReadableDate(c.getBirthday()) + " (Age: " + clientAge + ")\n");
		}
		
		// Append when information was last updated
		display.append("Information valid as of: " + Utilities.translateToReadableDate(c.getValidAsOf(), false));
		
		// Append notes if exist
		if(c.getNotes() != null) {
			display.append("\n-----------------------------------------------------------------\n");
			display.append("Notes:\n");
			display.append(c.getNotes());
		}
		
		// Append separator
		display.append("\n-----------------------------------------------------------------\n");
	}
	
	/**
	 * Helper method for updateForClient that posts household information to the display
	 */
	private void appendHouseholdInformation(ArrayList<Household> household) {
		int childCount = 0;
		int adultCount = 1; // Start at one; assume client himself is an adult
		long yearMillis = 31556952000L;
		ArrayList<String> appendNames = new ArrayList<String>();
		for(Household hm : household) {
			int householdMemberAge;
			if(hm.getBirthday() != null) {
				householdMemberAge = (int)((System.currentTimeMillis() - hm.getBirthday().getTime())/yearMillis);
				appendNames.add(hm.getName() + ": " + hm.getRelationship() + "   (Birthday: " + Utilities.translateToReadableDate(hm.getBirthday()) + ")\n");
			} else {
				householdMemberAge = 9999;
				appendNames.add(hm.getName() + ": " + hm.getRelationship() + "\n");
			}
			
			if(householdMemberAge < 18) {
				childCount++;
			} else { // Assumes entries with unlisted ages ('9999') are adults
				adultCount++;
			}
		}
		
		display.append("Household Members: (Adults: " + adultCount + " | Children: " + childCount + ")\n");
		for(String s : appendNames)
		{
			display.append(s);
		}
		display.append("-----------------------------------------------------------------\n");
	}
	
	/**
	 * Helper method for updateForClient that posts appointment information to the display
	 */
	private void appendAppointmentInformation(ArrayList<Appointment> appts) {
		display.append("Pick Up Date:               Pounds:\n\n");
		int poundsTotal = 0;
		for(Appointment appt : appts) {
			// Add appointment date
			String apptDate = Utilities.translateToReadableDate(appt.getDate(), false);
			display.append(apptDate);
			
			// Add appropriate amount of whitespace then pounds picked up at that appointment
			for(int addWhitespace = 28 - apptDate.length(); addWhitespace>0; addWhitespace--) {
				display.append(" ");
			}
			int pounds = appt.getPounds(); // Though this column can be null (returns Integer rather than int), we know it exists because of the criteria in the query
			display.append(Integer.toString(pounds));
			display.append("\n\n");
			
			// Add pounds from this appointment to the total
			poundsTotal = poundsTotal + pounds;
		}
		display.append("Total Pounds Received by Client: " + poundsTotal);
	}
	
	public SearchPanel getSp() {
		return sp;
	}
	
	/**
	 * Display active client's next appointment date
	 * 
	 * @author Scott Hoelsema
	 */
 	private class nextApptListener implements ActionListener
	{
		public void actionPerformed(ActionEvent e)
		{
			Appointment nextAppt = Queries.getNextAppointment(activeClient.getClientID());
			if(nextAppt != null) { // There was not a database or connection problem
				JOptionPane.showMessageDialog(null, Utilities.translateToReadableDate(nextAppt.getDate(), true), "Next Appointment", JOptionPane.INFORMATION_MESSAGE);
			}
		}
	}
	
 	/**
 	 * Makes a new SelectPrinter object which prompts the user to select a printer and performs the print
 	 * 
 	 * @author Scott Hoelsema
 	 */
	private class reportListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			new SelectPrinter(display, activeClientTitle, fpm);
		}
	}
	
	/**
	 * Make a PickUpFrame on click of "Pick Up"
	 * 
	 * @author Scott Hoelsema
	 */
	private class pickUpListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			new PickUpFrame(activeClient, fpm);
		}
	}
}