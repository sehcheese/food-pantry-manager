// Copyright (C) 2015 Scott Hoelsema
// Licensed under GPL v3.0; see LICENSE for full text

package gui;

import gui.panels.AppointmentsPanel;
import gui.panels.EditAppointmentPanel;
import gui.panels.EditClientPanel;
import gui.panels.MainPanel;
import gui.panels.NewClientPanel;
import gui.panels.ReportGeneratorPanel;
import gui.supportingelements.SearchPanelModel;

import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import javax.swing.ProgressMonitor;

import database.Client;
import database.DatabaseConnection;
import database.Queries;
import utils.Backup;
import utils.Configuration;
import utils.Utilities;

/**
 * Set up and control the GUI
 * 
 * @author Scott Hoelsema
 */
public class FoodPantryManager extends JFrame {
	private MainPanel mp;
	private AppointmentsPanel ap;
	private EditAppointmentPanel eap;
	private EditClientPanel ecp;
	private NewClientPanel ncp;
	private ReportGeneratorPanel rgp;
	private WindowListener wl;
	
	public FoodPantryManager()
	{
		setUpWindow();
		establishElements();
	}
	
	/**
	 * Set up the window (icon, size, title, etc.)
	 */
	private void setUpWindow() {
		Utilities.setIcon(this);
		setTitle(Configuration.APP_NAME);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setSize(875, 650);
		Utilities.centerOnScreen(this);
		setVisible(true);
		
		// Automatically backup database on close of Food Pantry Manager
		wl = new WindowListener() {
			@Override
			public void windowOpened(WindowEvent e) {}
			
			@Override
			public void windowClosing(WindowEvent e) {
				if(Configuration.isLocalOrNetworkBackup || Configuration.isDropBoxBackup) {
					ProgressMonitor pm = new ProgressMonitor(FoodPantryManager.this,"Backing up " + Configuration.APP_NAME + ". Please wait.","", 0, 170);
					pm.setMillisToDecideToPopup(0);
					pm.setMillisToPopup(0);
					
					Backup bu = new Backup(pm);
					bu.start();					
				} else {
					// Close database connection
					DatabaseConnection.logOut();
				}
			}
			
			@Override
			public void windowClosed(WindowEvent e) {}
			@Override
			public void windowIconified(WindowEvent e) {}
			@Override
			public void windowDeiconified(WindowEvent e) {}
			@Override
			public void windowActivated(WindowEvent e) {}
			@Override
			public void windowDeactivated(WindowEvent e) {}
		};
		this.addWindowListener(wl);
	}
	
	private void establishElements() {
		SearchPanelModel spm = new SearchPanelModel();
		
		// Make new JPanels that will be tabs
		mp = new MainPanel(this, spm);
		ap = new AppointmentsPanel(this);
		eap = new EditAppointmentPanel(this, spm);
		ecp = new EditClientPanel(this, spm);
		ncp = new NewClientPanel(this);
		rgp = new ReportGeneratorPanel(this);
		
		// Add tabs to window
		JTabbedPane tabbedPane = new JTabbedPane();
		tabbedPane.addTab("  Main  ", mp);
		tabbedPane.addTab("  Appointments  ", ap);
		tabbedPane.addTab("  Edit Appointment  ", eap);
		tabbedPane.addTab("  Edit Client  ", ecp);
		tabbedPane.addTab("  New Client  ", ncp);
		tabbedPane.addTab("  Report Generator  ", rgp);
		add(tabbedPane);
	}
	
	public void updatePanelsOnSearchSelectionChange(Client c) {
		mp.updateForClient(c);
		eap.updateForClient(c);
		ecp.updateForClient(c);
	}
	
	public MainPanel getMainPanel() {
		return mp;
	}
	
	public AppointmentsPanel getAppointmentsPanel() {
		return ap;
	}
	
	public EditAppointmentPanel getEditAppointmentsPanel() {
		return eap;
	}
	
	public EditClientPanel getEditClientPanel() {
		return ecp;
	}
	
	/**
	 * Somewhere a new city has been added; update the panels with city lists so that they are up to date.
	 * Called from places where a new city may have been added to the database.
	 */
	public void updateCityLists() {
		// Get list of cities
		ArrayList<String> citiesArrayList = Queries.getCities();
		rgp.updateCityList(citiesArrayList.toArray(new String[citiesArrayList.size()])); // Update in Report Generator Panel
		
		// Add "Other" to the list so panels can offer option to add unlisted city 
		citiesArrayList.add("Other");
		String[] cities = citiesArrayList.toArray(new String[citiesArrayList.size()]);
		ecp.updateCityList(cities);
		ncp.updateCityList(cities);
	}
	
	/**
	 * The entry point for Food Pantry Manager; this attempts log in,
	 * and if successful, will build Food Pantry Manager
	 * 
	 * @param args
	 *            Arguments from the command line
	 */
 	public static void main(String[] args) {
 		new LogIn(true);
	}
}
