// Copyright (C) 2015 Scott Hoelsema
// Licensed under GPL v3.0; see LICENSE for full text

package gui.panels;

import gui.FoodPantryManager;
import gui.SelectPrinter;
import gui.supportingelements.DatePanel;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import utils.Utilities;
import database.Appointment;
import database.Client;
import database.Queries;

public class ReportGeneratorPanel extends JPanel {
	private FoodPantryManager fpm;
	private JPanel north = new JPanel(new BorderLayout(5,5));
	private JPanel highNorth = new JPanel();
	private JPanel centerNorth = new JPanel();
	private JPanel lowNorth = new JPanel();
	private DatePanel date = new DatePanel(true, false);
	private JButton poundReport = new JButton("Generate Report");
	private JCheckBox includeDetail = new JCheckBox("Include Detail");
	private JComboBox<String> cityList;
	private JButton districtReport = new JButton("Generate Report");
	private JComboBox<String> numberOfMonths = new JComboBox<String>(new String[]{"36 Months", "24 Months", "12 Months", "11 Months", "10 Months", "9 Months", "8 Months", "7 Months", "6 Months"});
	private JButton stagnantClientReport = new JButton("Generate Report");
	private JTextArea display = new JTextArea(30, 75);
	private JButton print = new JButton("Print Report");
	private String reportType;
	
	public ReportGeneratorPanel(FoodPantryManager fpm)
	{
		this.fpm = fpm;
		buildReportGenerator();
	}
	
	public void buildReportGenerator()
	{
		
		buildNorthPanel();
		display.setEditable(false);
		display.getDocument().addDocumentListener(new DocumentListener() { // TODO make sure this works
			public void insertUpdate(DocumentEvent e) {
				display.setSelectionStart(0);
				display.setSelectionEnd(0);
				print.setEnabled(true);
			}
			public void removeUpdate(DocumentEvent e) {
				if(display.getText().equals("")) {
					print.setEnabled(false);
				}
			}
			public void changedUpdate(DocumentEvent e) {}
			
		});
		JScrollPane scrollDisplay = new JScrollPane(display);
		add(scrollDisplay, BorderLayout.CENTER);
		add(new JPanel().add(print), BorderLayout.SOUTH);
		print.setEnabled(false);
		print.addActionListener(new printListener());
	}
	
	public void buildNorthPanel()
	{
		highNorth.add(new JLabel("Poundage Report for"));
		date.setToCurrent();
		highNorth.add(date);
		highNorth.add(poundReport);
		highNorth.add(includeDetail);
		poundReport.addActionListener(new poundReportListener());
		
		centerNorth.add(new JLabel("List Clients by District:"));
		ArrayList<String> cities = Queries.getCities();
		cityList = new JComboBox<String>(cities.toArray(new String[cities.size()]));
		centerNorth.add(cityList);
		centerNorth.add(districtReport);
		districtReport.addActionListener(new districtReportListener());
		
		lowNorth.add(new JLabel("Report of Clients Inactive for at Least"));
		lowNorth.add(numberOfMonths);
		lowNorth.add(stagnantClientReport);
		stagnantClientReport.addActionListener(new stagnantClientReportListener());
		
		north.add(highNorth, BorderLayout.NORTH);
		north.add(centerNorth, BorderLayout.CENTER);
		north.add(lowNorth, BorderLayout.SOUTH);
		
		add(north, BorderLayout.NORTH);
	}
	
	private class poundReportListener implements ActionListener
	{
		public void actionPerformed(ActionEvent e)
		{			
			// Format Check
			if(!date.checkFormat()) {
				return;
			}
			
			// Set header for pdf document
			reportType = date.toString() + " Poundage Report"; // TODO make sure works
			
			int totalPoundage = Queries.getTotalPoundageForTimestamp(date.getTimestamp());
			if(totalPoundage == -1) { // There was a database or connection problem
				return;
			}
			
			int numClientsServed = Queries.getVisitsForTimePeriod(date.getTimestamp());
			if(numClientsServed == -1) { // There was a database or connection problem
				return;
			}
			
			ArrayList<Appointment> appts = Queries.getCompletedAppointmentsForDate(date.getTimestamp());
			if(appts == null) { // There was a database or connection problem
				return;
			}
			
			// Append to display
			display.setText("");
			display.append("Pounds Distributed " + date.toString() + ": " + totalPoundage + "\n\n");
			display.append("Total Client Visits " + date.toString() + ": " + numClientsServed + "\n\n");
			if(includeDetail.isSelected()) {
				for(Appointment a : appts) {
					display.append(a.getClientName() + " (Case ID: " + a.getClientID() + ")\n" + Utilities.translateToReadableDate(a.getDate(), false) + " (Pounds: " + a.getPounds() + ")\n\n");
				}
			}
		}	
	}
	
	/**
	 * Lookup clients that have been inactive for a certain period of time; that
	 * is, they have not completed an appointment in the last {amount of time}
	 * 
	 * @author Scott Hoelsema
	 */
	private class stagnantClientReportListener implements ActionListener
	{
		public void actionPerformed(ActionEvent e)
		{
			// Set header to be used if printed
			String selected = (String)numberOfMonths.getSelectedItem();
			reportType = selected.substring(0, selected.length()) + " Stagnant Client Report";
			
			// Determine reference date
			Date currentDatetime = new Date(System.currentTimeMillis()); Timestamp current = new Timestamp(currentDatetime.getTime());
			Timestamp reference = new Timestamp(current.getTime());	
			long monthMillis = 2628000000L;
			long yearMillis = 31556952000L;
			if(numberOfMonths.getSelectedIndex()==0) {
				reference.setTime(reference.getTime()-yearMillis - yearMillis - yearMillis);
			}
			else if(numberOfMonths.getSelectedIndex()==1) {
				reference.setTime(current.getTime() - yearMillis - yearMillis);
			}
			else if(numberOfMonths.getSelectedIndex()==2) {
				reference.setTime(reference.getTime()-yearMillis);
			}
			else if(numberOfMonths.getSelectedIndex()==3) {
				reference.setTime(current.getTime() - (monthMillis * 11));
			}
			else if(numberOfMonths.getSelectedIndex()==4) {
				reference.setTime(current.getTime() - (monthMillis * 10));
			}
			else if(numberOfMonths.getSelectedIndex()==5) {
				reference.setTime(current.getTime() - (monthMillis * 9));
			}
			else if(numberOfMonths.getSelectedIndex()==6) {
				reference.setTime(current.getTime() - (monthMillis * 8));
			}
			else if(numberOfMonths.getSelectedIndex()==7) {
				reference.setTime(current.getTime() - (monthMillis * 7));
			}
			else if(numberOfMonths.getSelectedIndex()==8) {
				reference.setTime(current.getTime() - (monthMillis * 6));
			}
				
			// Look up stagnant clients
			ArrayList<Appointment> stagnantClients = Queries.lookupInactiveClients(reference);
			
			if(stagnantClients != null) { // There was not a database or connection problem
				// Append results to display
				display.setText("");
				for(Appointment a : stagnantClients) {
					display.append(a.getClientName() + " (Case ID: "
							+ a.getClientID() + ")\n" + "Last Pick Up Date: "
							+ Utilities.translateToReadableDate(a.getDate(), true) + "\n\n");
				}
			}			
		}
	}
	
	/**
	 * Count and display all the clients for the selected city/district
	 * 
	 * @author Scott Hoelsema
	 */
	private class districtReportListener implements ActionListener
	{
		public void actionPerformed(ActionEvent e)
		{
			display.setText("");
			// Set header to be used if printed
			reportType = (String)cityList.getSelectedItem() + " Client List";
			
			// Lookup clients for selected city
			ArrayList<Client> clientsForCity = Queries.getClientsForCity((String)cityList.getSelectedItem());
			
			if(clientsForCity != null) { // There was not a database or connection problem
				// Post total number of clients in city
				display.append("Total Number of Clients in " + (String)cityList.getSelectedItem() + ": " + clientsForCity.size() + "\n\n");
				
				// Post individual client information
				for(Client c : clientsForCity) {
					// Account for absent telephone numbers
					String telephone = c.getTelephone();
					if(telephone == null) {
						telephone = "";
					} else {
						telephone = "\n" + telephone;
					}
					
					// Append information
					display.append(c.getFirstName() + " " + c.getLastName()
							+ " (Case ID: " + c.getClientID() + ")\n"
							+ c.getAddress() + ", " + c.getCity()
							+ telephone + "\n\n");
				}
			}
		}
	}
	
	public void updateCityList(String[] cities) {
		cityList.setModel(new DefaultComboBoxModel<String>(cities));
	}
	
	/**
 	 * Makes a new SelectPrinter object which prompts the user to select a printer and performs the print
 	 * 
 	 * @author Scott Hoelsema
 	 */
	private class printListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			new SelectPrinter(display, reportType, fpm);

		}
	}
}