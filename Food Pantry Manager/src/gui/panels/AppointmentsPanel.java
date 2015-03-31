// Copyright (C) 2015 Scott Hoelsema
// Licensed under GPL v3.0; see LICENSE for full text

package gui.panels;

import gui.PickUpFrame;
import gui.FoodPantryManager;
import gui.supportingelements.DatePanel;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Calendar;

import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import utils.Utilities;
import database.Appointment;
import database.Queries;

public class AppointmentsPanel extends JPanel {
	private FoodPantryManager fpm;
	private DatePanel date;
	private JButton submit;
	private JButton today;
	private JButton tomorrow;
	private JList<Appointment> apptList;
	private JButton pickup;
	
	public AppointmentsPanel(FoodPantryManager fpm)
	{
		// Set variables
		this.fpm = fpm;
		
		buildGUI();
	}
	
	private void buildGUI() {
		// Set Layout
		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		
		// Initialize GUI elements
		date = new DatePanel(false, false);
		submit = new JButton("Submit");
		today = new JButton("Today");
		tomorrow = new JButton("Tomorrow");
		apptList = new JList<Appointment>();
		pickup = new JButton("Pick Up");
		
		// Add listeners
		addListeners();
		
		// Add to JPanel
		JPanel dateControlPanel = new JPanel();
		dateControlPanel.add(date);
		dateControlPanel.add(submit);
		dateControlPanel.add(today);
		dateControlPanel.add(tomorrow);
		add(dateControlPanel);
		
		JPanel apptListPanel = new JPanel();
		apptList.setCellRenderer(new DefaultListCellRenderer() { // Make the items in the JList appear as we would like
			@Override
			public Component getListCellRendererComponent(JList<? extends Object> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
				// Set the Appointment object as component
				Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
				
				// Set text to display
				String apptTime = Utilities.twelveHourClockFormat(((Appointment)value).getDate().toString().substring(11,16));
				String space = "                  ";
				if(apptTime.length() == 4) {
					setText(apptTime + space + "  " + ((Appointment)value).getClientName());
				} else {
					setText(apptTime + space + ((Appointment)value).getClientName());
				}
				
				
				// Gray out if appointment already completed
				if(((Appointment)value).getPounds() != null) {
					c.setEnabled(false);
				}
				return c;
			}
			
		});
		JScrollPane jsp = new JScrollPane(apptList);
		jsp.setPreferredSize(new Dimension(300, 360));
		apptListPanel.add(jsp);
		add(apptListPanel);
		
		JPanel actionsPanel = new JPanel();
		actionsPanel.add(pickup);
		add(actionsPanel);
		
		// By default, set to today
		date.setToCurrent();
		setApptsForDate(new Date(System.currentTimeMillis()));
		
	}
	
	private void addListeners() {
		submit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(!date.checkFormat()) {
					return;
				}
				setApptsForDate(date.getSQLDate());
			}
			
		});
		
		today.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				date.setToCurrent(); // Set DatePanel to today
				setApptsForDate(new Date(System.currentTimeMillis())); // List today's appts
			}
		});
		
		tomorrow.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Calendar cal = Calendar.getInstance(); // Get calendar object
				cal.add(Calendar.DATE, 1); // Set calendar to tomorrow
				Date tomorrow = new Date(cal.getTimeInMillis());
				date.setToSQLDate(tomorrow); // Set DatePanel to tomorrow
				setApptsForDate(tomorrow); // List tomorrow's appts
			}
		});
		
		pickup.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				new PickUpFrame(Queries.getClientByID(apptList.getSelectedValue().getClientID()), fpm);
			}
		});
	}
	
	public void setApptsForDate(Date date) {
		ArrayList<Appointment> appts = Queries.getAppointmentsForDate(date.toString() + "%");
		apptList.setListData(appts.toArray(new Appointment[appts.size()]));
	}
	
	public void refresh() {
		setApptsForDate(date.getSQLDate());
	}
}