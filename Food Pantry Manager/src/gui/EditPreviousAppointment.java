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

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import utils.Utilities;
import database.Appointment;
import database.Updates;

public class EditPreviousAppointment extends JFrame
{
	private Appointment activeAppt;
	private JPanel northPanel = new JPanel(new GridLayout(1, 2, 5, 5)); // Pounds variables
	private JTextField pounds = new JTextField(10);
	private JPanel centerPanel = new JPanel(new BorderLayout(5, 5)); // Date and time variables
	private JPanel highCenterPanel = new JPanel(new GridLayout(2, 2, 5, 5));
	private DatePanel thisApptDate = new DatePanel(false, false);
	private JPanel northTimeSuperPanel = new JPanel();
	private JPanel northTimePanel = new JPanel(new GridLayout(1, 2, 5, 5));
	private TimeTextField northTime = new TimeTextField();
	private JPanel southPanel = new JPanel(); // Submit Variables
	private JButton updateButton = new JButton("Update");
	private boolean forcedClose = true;
	
	public EditPreviousAppointment(Appointment appt)
	{
		this.activeAppt = appt;
		
		setTitle("Edit Previous Appointment");
		Utilities.setIcon(this);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		
		BorderLayout pickUpLayout = new BorderLayout(5,5);
		setLayout(pickUpLayout);
		
		buildLayout();
		
		setData();
		
		pack();
		Utilities.centerOnScreen(this);
		setVisible(true);
	}
	
	public void buildLayout() // Build GUI Headquarters
	{
		northPanel.add(new JLabel("Pounds:"));
		northPanel.add(pounds);
		pounds.addFocusListener(new entryFieldFocusListener(pounds));
		add(northPanel, BorderLayout.NORTH);
		
		highCenterPanel.add(new JLabel("Pick Up Date & Time:"));
		highCenterPanel.add(thisApptDate);
		northTimePanel.add(northTime);
		northTime.addFocusListener(new entryFieldFocusListener(northTime));
		highCenterPanel.add(new JLabel());
		northTimeSuperPanel.add(northTimePanel);
		highCenterPanel.add(northTimeSuperPanel);
		centerPanel.add(highCenterPanel, BorderLayout.CENTER);
		
		add(centerPanel, BorderLayout.CENTER);
		
		southPanel.add(new JLabel(""));
		southPanel.add(updateButton);
		updateButton.addActionListener(new updateListener());
		add(southPanel, BorderLayout.SOUTH);
	}
	
	public void setData() {
		thisApptDate.setToTimestamp(activeAppt.getDate());
		northTime.setToTimestamp(activeAppt.getDate());
		pounds.setText(activeAppt.getPounds().toString());
	}
	
	public boolean getForcedClose() {
		return forcedClose;
	}
		
	private class updateListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			if(!checkFormat()) {
				return;
			}
			
			// Set fresh data
			activeAppt.setDate(northTime.getTimestamp(thisApptDate));
			activeAppt.setPounds(Integer.parseInt(pounds.getText()));
			Updates.updateAppointment(activeAppt);
			
			forcedClose = false;
			dispose();
		}
		
		/**
		 * Check that formatting of entered data is correct; since date and time
		 * format checking happen in DatePanel and TimeTextField, we only need to
		 * call the appropriate methods in each of those objects.
		 * 
		 * @return A boolean representing pass or fail of format checks
		 */
		public boolean checkFormat() {
			boolean noFormatProblems = true;
			
			// Check DatePanel
			if(!thisApptDate.checkFormat()) {
				noFormatProblems = false;
			}
			
			// Check TimeTextField
			if(!northTime.checkFormat()) {
				noFormatProblems = false;
			}
			
			// Check pounds
			if(pounds.getText().length()>3) { // Whoa! Nobody gets that much food at one appointment.
				JOptionPane.showMessageDialog(null, "Invalid Pounds Value - Greater Than 3 Digits", "Error", JOptionPane.ERROR_MESSAGE);
				pounds.setText("");
				noFormatProblems = false;	
			} else {
				try {
					Integer.parseInt(pounds.getText());
					noFormatProblems = true;
				} catch(NumberFormatException e1) { // Not a number
					JOptionPane.showMessageDialog(null, "Invalid Pounds Value - Use Integer", "Error", JOptionPane.ERROR_MESSAGE);
					pounds.setText("");
					noFormatProblems = false;
				}	
			}
			return noFormatProblems;
		}
	}
}