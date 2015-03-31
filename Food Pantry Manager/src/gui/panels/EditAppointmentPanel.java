// Copyright (C) 2015 Scott Hoelsema
// Licensed under GPL v3.0; see LICENSE for full text

package gui.panels;

import gui.EditPreviousAppointment;
import gui.FoodPantryManager;
import gui.listeners.entryFieldFocusListener;
import gui.supportingelements.DatePanel;
import gui.supportingelements.SearchPanel;
import gui.supportingelements.SearchPanelModel;
import gui.supportingelements.TimeTextField;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.border.Border;

import database.Appointment;
import database.Client;
import database.Queries;
import database.Updates;

public class EditAppointmentPanel extends JPanel implements IUpdateOnSearch {
	private FoodPantryManager fpm;
	
	private SearchPanel sp;
	
	private Client activeClient;
	
	private EditPreviousAppointment epa;
	
	private JPanel center = new JPanel(new BorderLayout()); // Edit Next Appointment Subpanel
	private JPanel highCenter = new JPanel(new GridLayout(1,2,5,5));
	private JPanel lowCenter = new JPanel(new GridLayout(1,2,5,5));
	private DatePanel date = new DatePanel(false, false);
	private JPanel timePanel = new JPanel();
	private TimeTextField time = new TimeTextField();
	private JButton submit = new JButton("Submit");
	
	private JPanel south = new JPanel(new BorderLayout(5,5)); // Extended Appointment Edit Subpanel
	private JList<Appointment> selectAppt = new JList<Appointment>();
	private JButton edit = new JButton ("Edit");
	private JButton delete = new JButton("Delete");
	
	/**
	 * Build the GUI and listeners for the Edit Appointment panel
	 * 
	 * @param sp The search panel to put at the top
	 */
	public EditAppointmentPanel(FoodPantryManager fpm, SearchPanelModel spm)
	{
		// Set variables
		this.fpm = fpm;
		this.sp = new SearchPanel(fpm, spm);
		
		// Add search panel to top of MainPanel
		add(sp, BorderLayout.NORTH);
		
		buildEditAppointment();
	}
	
	/**
	 * GUI Building Headquarters
	 */
	public void buildEditAppointment() {
		buildNextApptEditPanel();
		buildExtendedEditPanel();
	}
	
	/**
	 * Build panel for editing active client's next appointment
	 */
	public void buildNextApptEditPanel() { // Build Next Appointment Edit Panel (Center)
		Border centerBorder = BorderFactory.createTitledBorder("Edit Next Appointment");
		center.setBorder(centerBorder);
		
		highCenter.add(new JLabel("Next Appointment Target Date:"));
		date.setEnabled(false);
		highCenter.add(date);
		center.add(highCenter, BorderLayout.NORTH);
		lowCenter.add(new JLabel(""));
		time.setEnabled(false);
		timePanel.add(time);
		time.setText("xx:xx");
		time.addFocusListener(new entryFieldFocusListener(time));
		lowCenter.add(timePanel);
		center.add(lowCenter, BorderLayout.CENTER);
		center.add(submit, BorderLayout.EAST);
		submit.setEnabled(false);
		submit.addActionListener(new submitListener());
		
		add(center, BorderLayout.CENTER);
	}
	
	/**
	 * Build panel for editing active client's previous appointments
	 */
	public void buildExtendedEditPanel() { // Build Extended Edit Panel (South)
		Border southBorder = BorderFactory.createTitledBorder("Edit Previous Appointment");
		south.setBorder(southBorder);
		
		JScrollPane choiceList = new JScrollPane(selectAppt);
		choiceList.setPreferredSize(new Dimension(256,128));
		selectAppt.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		south.add(choiceList, BorderLayout.CENTER);
		JPanel southButtonPanel = new JPanel(new GridLayout(1,2,5,5));
		southButtonPanel.add(edit, BorderLayout.NORTH);
		southButtonPanel.add(delete, BorderLayout.SOUTH);
		south.add(southButtonPanel, BorderLayout.SOUTH);
		edit.setEnabled(false);
		delete.setEnabled(false);
		edit.addActionListener(new EditApptListener());
		delete.addActionListener(new DeleteApptListener());
		
		add(south, BorderLayout.SOUTH);
	}
	
	/**
	 * Update information after EditPreviousAppointment closed
	 * 
	 * @author Scott Hoelsema
	 */
	private class epaWindowListener implements WindowListener
	{
		public void windowClosed(WindowEvent arg0) 
		{
			if(!epa.getForcedClose()) {
				updateForClient(activeClient);
				selectAppt.setSelectedIndex(0);
			}
		}

		
		public void windowActivated(WindowEvent arg0) {}
		public void windowClosing(WindowEvent arg0) {}
		public void windowDeactivated(WindowEvent arg0) {}
		public void windowDeiconified(WindowEvent arg0) {}
		public void windowIconified(WindowEvent arg0) {}
		public void windowOpened(WindowEvent arg0) {}		
	}
	
	public void updateForClient(Client c) {
		// A necessary check to prevent extra work.
		// Too many events fire because each tab tries to update every other tab on selecting a different client in the list.
		// The root problem is that SearchPanel has three different instances - one on each of three different tabs.
		// We cannot add the same SearchPanel object to each different tab - that doesn't work.
		// This could perhaps be resolved by changing the GUI so that SearchPanel is separate, but then it would have to appear above every tab,
		// eating up space in tabs that don't need the search panel.
		if (activeClient == c) return;
		
		if(c == null) { // If no client is selected (there were no search matches)...
			// Nullify active client
			activeClient = null;
			
			// Disable elements
			date.setEnabled(false);
			time.setEnabled(false);
			submit.setEnabled(false);
			selectAppt.setEnabled(false);
			edit.setEnabled(false);
			delete.setEnabled(false);
		} else {
			// Set as active client
			activeClient = c;
			
			// Make sure client is selected in this panel's search panel
			// The updates may be coming from selecting a client in another panel's search panel
			sp.setSelected(c);
			
			// Enable elements
			date.setEnabled(true);
			time.setEnabled(true);
			submit.setEnabled(true);
			selectAppt.setEnabled(true);
			edit.setEnabled(true);
			delete.setEnabled(true);
			
			// Fill in next appointment section
			Appointment nextAppt = Queries.getNextAppointment(c.getClientID());
			if(nextAppt != null) { // Connection error or database problem if is null
				date.setToTimestamp(nextAppt.getDate());
				time.setToTimestamp(nextAppt.getDate());
				
				// Fill in previous appointments section
				ArrayList<Appointment> prevAppts = Queries.getCompletedApptsForClient(c.getClientID()); // Lookup completed appointments
				if(prevAppts != null) { // There was not a database or connection problem
					selectAppt.setListData(prevAppts.toArray(new Appointment[prevAppts.size()]));
				}
				if(selectAppt.getModel().getSize() > 0) { // Lead the eye by selecting the first item in selectAppt
					selectAppt.setSelectedIndex(0);	
				}
			}
		}
	}
	
	/**
	 * When a pickup is performed in MainPanel, the display is updated. The same
	 * client is active in this class (EditAppointmentPanel), and therefore
	 * needs to be updated to show the appointment just completed. Also used by
	 * DeleteApptListener.
	 */
	public void refresh() {
		updateForClient(activeClient);
	}
	
	public SearchPanel getSp() {
		return sp;
	}
	
	/**
	 * On click of "Submit" update the next appointment to the given date and time.
	 * 
	 * @author Scott Hoelsema
	 */
	private class submitListener implements ActionListener {
		public void actionPerformed(ActionEvent ae) {
			if(!performFormatChecks()) {
				return;
			}
					
			// Get client's next appointment
			Appointment nextAppt = Queries.getNextAppointment(activeClient.getClientID());
			if(nextAppt != null) { // There was not a database or connection problem looking up the next appointment
				nextAppt.setDate(time.getTimestamp(date));
				boolean successfulUpdateNextAppt = Updates.updateAppointment(nextAppt);
				if(successfulUpdateNextAppt) {
					JOptionPane.showMessageDialog(null, "Successful update.", "Success", JOptionPane.INFORMATION_MESSAGE);
				}
			}
		}
		
		public boolean performFormatChecks() {
			boolean noFormatProblems = true;
			noFormatProblems = date.checkFormat();
			noFormatProblems = time.checkFormat();
			return noFormatProblems;
		}
	}

	/**
	 * Open window to edit a previous appointment
	 * 
	 * @author Scott Hoelsema
	 */
	private class EditApptListener implements ActionListener {
		public void actionPerformed(ActionEvent ae) {
			if(selectAppt.getSelectedIndex() != -1) { // Ascertain something is actually selected
				epa = new EditPreviousAppointment(selectAppt.getSelectedValue());
				epa.addWindowListener(new epaWindowListener());
			}
		}
	}
	
	/**
	 * Delete selected appointment
	 * 
	 * @author Scott Hoelsema
	 */
	private class DeleteApptListener implements ActionListener {
		public void actionPerformed(ActionEvent ae) {
			if(selectAppt.getSelectedIndex() != -1) { // Ascertain something is actually selected
				int selectedOption = JOptionPane.showConfirmDialog(null, "Are you sure you want to delete this appointment?", "Confirm", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
				if(selectedOption == 0) {
					boolean success = Updates.deleteAppointment(selectAppt.getSelectedValue());
					if(success) {
						refresh();
						JOptionPane.showMessageDialog(null, "Appointment successfully deleted.", "Success", JOptionPane.INFORMATION_MESSAGE);
					}
				}
			}
		}
	}
}