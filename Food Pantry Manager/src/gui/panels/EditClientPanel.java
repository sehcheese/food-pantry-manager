// Copyright (C) 2015 Scott Hoelsema
// Licensed under GPL v3.0; see LICENSE for full text

package gui.panels;

import gui.FoodPantryManager;
import gui.listeners.entryFieldFocusListener;
import gui.supportingelements.DatePanel;
import gui.supportingelements.SearchPanel;
import gui.supportingelements.SearchPanelModel;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.Border;

import database.Client;
import database.Household;
import database.Inserts;
import database.Queries;
import database.Updates;

public class EditClientPanel extends JPanel implements IUpdateOnSearch {
	private Client activeClient;
	
	private FoodPantryManager fpm;
	private SearchPanel sp; // Search Subpanel	
	
	private JPanel east = new JPanel(new BorderLayout(5,5)); // Household Member Subpanel
	private JPanel newHouseholdMember = new JPanel(new BorderLayout(5,5));
	private JPanel eastTop = new JPanel(new GridLayout(3,2,5,5));
	private JTextField newHouseholdMemberName = new JTextField(10);
	private JComboBox<String> eastSex = new JComboBox<String>();
	private JComboBox<String> relationship = new JComboBox<String>();
	private JPanel eastHighCenter = new JPanel(new GridLayout(1,2,5,5));
	private JPanel eastLowCenter = new JPanel(new GridLayout(1,2,5,5));
	private DatePanel eastDate = new DatePanel(false, true);
	private JPanel eastBottom = new JPanel(new GridLayout(2,2,5,5));
	private JButton submitNewHouseholdMember = new JButton("Submit");
	private JComboBox<Household> deleteHouseholdMember = new JComboBox<Household>();
	private JButton submitDeleteHouseholdMember = new JButton("Delete Member");
	
	private JPanel west = new JPanel(new BorderLayout(5,5)); // Edit Client Information Subpanel
	private JPanel westCenter = new JPanel(new GridLayout(1,2,5,5));
	private JPanel westTop = new JPanel(new GridLayout(8,2,5,5));
	private JTextField editFirstName = new JTextField(10);
	private JTextField editLastName = new JTextField(10);
	private JTextField editSSN = new JTextField("0, 4, or 9 Digits");
	private JTextField address = new JTextField(7);
	private JTextField telephone = new JTextField("XxxYyyZzzz", 10);
	private JComboBox<String> westSex = new JComboBox<String>();
	private JComboBox<String> cityList; // If entries here change must be updated in the selectClientListListener
	private JTextField otherLocation = new JTextField("Other City", 10);
	private JPanel westBottom = new JPanel(new GridLayout(1,2,5,5));
	private JButton deleteClient = new JButton("Delete Client");
	private JButton updateClient = new JButton("Update Client");
	private DatePanel westDate = new DatePanel(false, true);
	
	public EditClientPanel(FoodPantryManager fpm, SearchPanelModel spm)
	{
		// Set variables
		this.fpm = fpm;
		sp = new SearchPanel(this, spm);
		
		add(sp, BorderLayout.NORTH);
		
		buildEditClient();
	}
	
	public void buildEditClient() { // GUI Building Headquarters
		buildClientEditPanel();
		buildHouseholdPanel();
	}
		
	public void buildClientEditPanel() { // Build Edit Client Panel (West)
		Border westBorder = BorderFactory.createTitledBorder("Edit Client");
		west.setBorder(westBorder);
		
		westTop.add(new JLabel("First Name:"));
		westTop.add(editFirstName);
		editFirstName.addFocusListener(new entryFieldFocusListener(editFirstName));
		westTop.add(new JLabel("Last Name:"));
		westTop.add(editLastName);
		editLastName.addFocusListener(new entryFieldFocusListener(editLastName));
		westTop.add(new JLabel("Social Security Number:"));
		westTop.add(editSSN);
		editSSN.addFocusListener(new entryFieldFocusListener(editSSN));
		westTop.add(new JLabel("Address:"));
		westTop.add(address);
		address.addFocusListener(new entryFieldFocusListener(address));
		westTop.add(new JLabel("City:"));
		ArrayList<String> cities = Queries.getCities();
		cities.add("Other");
		cityList = new JComboBox<String>(cities.toArray(new String[cities.size()]));
		cityList.addItemListener(new cityListListener());
		westTop.add(cityList);
		westTop.add(new JLabel());
		westTop.add(otherLocation);
		// cityListListener will not trigger if only 1 item in list (which would be "Other") because the state never changes; 
		// therefore if there is only one item in the list we need to set the original state of otherLocation to be enabled
		if(cities.size() < 2) 
			otherLocation.setEnabled(true);
		else
			otherLocation.setEnabled(false);
		otherLocation.addFocusListener(new entryFieldFocusListener(otherLocation));
		westTop.add(new JLabel("Telephone:"));
		westTop.add(telephone);
		telephone.addFocusListener(new entryFieldFocusListener(telephone));
		westTop.add(new JLabel("Sex:"));
		westSex.addItem("Male");
		westSex.addItem("Female");
		westTop.add(westSex);
		west.add(westTop, BorderLayout.NORTH);
		
		westCenter.add(new JLabel("Birthday:"));
		westCenter.add(westDate);
		west.add(westCenter, BorderLayout.CENTER);
		
		westBottom.add(deleteClient);
		deleteClient.setEnabled(false);
		deleteClient.addActionListener(new deleteClientListener());
		westBottom.add(updateClient);
		updateClient.setEnabled(false);
		updateClient.addActionListener(new updateClientListener());
		west.add(westBottom, BorderLayout.SOUTH);
		
		add(west, BorderLayout.WEST);
	}
	
	public void buildHouseholdPanel() { // Build Household Panel (East)
		Border eastTopBorder = BorderFactory.createTitledBorder("New Household Member");
		newHouseholdMember.setBorder(eastTopBorder);
		
		eastTop.add(new JLabel("Name:"));
		eastTop.add(newHouseholdMemberName);
		newHouseholdMemberName.addFocusListener(new entryFieldFocusListener(newHouseholdMemberName));
		eastTop.add(new JLabel("Sex:"));
		eastSex.addItem("Male");
		eastSex.addItem("Female");
		eastTop.add(eastSex);
		eastTop.add(new JLabel("Relationship:"));
		relationship.addItem("Spouse");
		relationship.addItem("Child");
		relationship.addItem("Grandchild");
		relationship.addItem("Parent");
		relationship.addItem("Grandparent");
		relationship.addItem("Brother");
		relationship.addItem("Sister");
		relationship.addItem("Other Family Member");
		relationship.addItem("Boyfriend");
		relationship.addItem("Girlfriend");
		relationship.addItem("Friend");
		relationship.addItem("Fiance");
		relationship.addItem("Other");
		eastTop.add(relationship);
		newHouseholdMember.add(eastTop, BorderLayout.NORTH);
		
		eastHighCenter.add(new JLabel("Birthday:"));
		eastHighCenter.add(eastDate);
		newHouseholdMember.add(eastHighCenter, BorderLayout.CENTER);
		
		eastLowCenter.add(new JLabel(""));
		eastLowCenter.add(submitNewHouseholdMember);
		submitNewHouseholdMember.setEnabled(false);
		submitNewHouseholdMember.addActionListener(new submitNewHouseholdMemberListener());
		newHouseholdMember.add(eastLowCenter, BorderLayout.SOUTH);
		
		east.add(newHouseholdMember, BorderLayout.NORTH);
		
		Border eastBottomBorder = BorderFactory.createTitledBorder("Delete Household Member");
		eastBottom.setBorder(eastBottomBorder);
		
		eastBottom.add(new JLabel("Delete Member:"));
		eastBottom.add(deleteHouseholdMember);
		deleteHouseholdMember.setPreferredSize(new Dimension(195,25));
		eastBottom.add(new JLabel(""));
		eastBottom.add(submitDeleteHouseholdMember);
		submitDeleteHouseholdMember.setEnabled(false);
		submitDeleteHouseholdMember.addActionListener(new submitDeleteHouseholdMemberListener());
		east.add(eastBottom, BorderLayout.SOUTH);
		
		add(east, BorderLayout.EAST);
	}
	
	public void updateForClient(Client c) {
		if(c == null) { // If no client is selected (there were no search matches)...
			// Nullify active client
			activeClient = null;
			
			editFirstName.setText("");
			editLastName.setText("");
			editSSN.setText("0, 4, or 9 Digits");
			address.setText("");
			telephone.setText("XxxYyyZzzz");
			westSex.setSelectedIndex(0);
			cityList.setSelectedIndex(0);
			otherLocation.setText("Other City");
			westDate.clear();
			
			updateClient.setEnabled(false);
			deleteClient.setEnabled(false);
			deleteHouseholdMember.setEnabled(false);
			submitDeleteHouseholdMember.setEnabled(false);
			submitNewHouseholdMember.setEnabled(false);
		} else {
			// Enable buttons/boxes
			updateClient.setEnabled(true);
			deleteClient.setEnabled(true);
			deleteHouseholdMember.setEnabled(true);
			submitDeleteHouseholdMember.setEnabled(true);
			submitNewHouseholdMember.setEnabled(true);
			
			// Set as active client
			activeClient = c;
			
			// Notify other tabs with SearchPanels what is selected
			fpm.getMainPanel().getSp().setSelected(c);
			fpm.getEditAppointmentsPanel().getSp().setSelected(c);
			
			// Populate client information
			editFirstName.setText(c.getFirstName());
			editLastName.setText(c.getLastName());
			if(!c.getSsn().equals("Withheld")) {
				editSSN.setText(c.getSsn());						
			} else {
				editSSN.setText("0, 4, or 9 Digits");
			}
			address.setText(c.getAddress());
			if(c.getTelephone()!=null) {
				String tempTelephone = c.getTelephone();
				tempTelephone = tempTelephone.substring(1,4) + tempTelephone.substring(6,9) + tempTelephone.substring(10); // Because format checking requires only numbers, so we remove dashes and parentheses
				telephone.setText(tempTelephone);
			} else {
				telephone.setText("XxxYyyZzzz");
			}
			String gender = c.getGender();
			if(gender.equals("Male")) {
				westSex.setSelectedIndex(0);
			} else {
				westSex.setSelectedIndex(1);
			}
			cityList.setSelectedItem(c.getCity());
			if(c.getBirthday() != null) {
				westDate.setToSQLDate(c.getBirthday());
			} else {
				westDate.clear();
			}
			
			// Populate delete household member combobox
			ArrayList<Household> householdMembers = Queries.getHouseholdForClient(c.getClientID());
			if(householdMembers == null) { // There was a database or connection problem
				return;
			}
			
			if(householdMembers.size() == 0) {
				deleteHouseholdMember.setEnabled(false);
				deleteHouseholdMember.removeAllItems();
				submitDeleteHouseholdMember.setEnabled(false);
				setRelationshipsChoices(false); // Ascertain "Spouse" is an option
			} else {
				deleteHouseholdMember.setEnabled(true);
				deleteHouseholdMember.removeAllItems();
				submitDeleteHouseholdMember.setEnabled(true);
				boolean hasSpouse = false;
				for(Household hm : householdMembers) {
					deleteHouseholdMember.addItem(hm);
					if(hm.getRelationship().equals("Spouse")) {
						hasSpouse = true;
					}
				}
				setRelationshipsChoices(hasSpouse);
			}
		}
	}
	
	private void setRelationshipsChoices(boolean withoutSpouseOption) {
		if(withoutSpouseOption) {
			relationship.removeAllItems();
			relationship.addItem("Child");
			relationship.addItem("Grandchild");
			relationship.addItem("Parent");
			relationship.addItem("Grandparent");
			relationship.addItem("Brother");
			relationship.addItem("Sister");
			relationship.addItem("Other Family Member");
			relationship.addItem("Boyfriend");
			relationship.addItem("Girlfriend");
			relationship.addItem("Friend");
			relationship.addItem("Fiance");
			relationship.addItem("Other");
		} else {
			relationship.removeAllItems();
			relationship.addItem("Spouse");
			relationship.addItem("Child");
			relationship.addItem("Grandchild");
			relationship.addItem("Parent");
			relationship.addItem("Grandparent");
			relationship.addItem("Brother");
			relationship.addItem("Sister");
			relationship.addItem("Other Family Member");
			relationship.addItem("Boyfriend");
			relationship.addItem("Girlfriend");
			relationship.addItem("Friend");
			relationship.addItem("Fiance");
			relationship.addItem("Other");
		}
	}
	
	public SearchPanel getSp() {
		return sp;
	}
	
	/**
	 * Update a client based on the Update Client section on the left/west side
	 * 
	 * @author Scott Hoelsema
	 */
	private class updateClientListener implements ActionListener
	{
		public void actionPerformed(ActionEvent e)
		{
			// Format checks
			if(!performWestFormatChecks()) {
				return;
			}
			
			// Check for duplicate address
			if(cityList.getSelectedIndex() == cityList.getModel().getSize() - 1) { // If "Other" is selected in cityList
				int result = Queries.checkForDuplicateAddresses(address.getText(), otherLocation.getText(), activeClient);
				
				if(result == -1) { // There was a database or connection error
					return;
				} else if(result == 1) { // A duplicate address was found
					JOptionPane.showMessageDialog(null, "Duplicate addresses are not allowed.", "Error", JOptionPane.ERROR_MESSAGE);
					return;
				}
			} else { // A listed city is selected
				int result = Queries.checkForDuplicateAddresses(address.getText(), (String)cityList.getSelectedItem(), activeClient);
				
				if(result == -1) { // There was a database or connection error
					return;
				} else if(result == 1) { // A duplicate address was found
					JOptionPane.showMessageDialog(null, "Duplicate addresses are not allowed.", "Error", JOptionPane.ERROR_MESSAGE);
					return;
				}
			}
			
			// Update Client object with entered information
			activeClient.setFirstName(editFirstName.getText()); // Set firstName
			activeClient.setLastName(editLastName.getText()); // Set lastName
			if(editSSN.getText().length() == 4 || editSSN.getText().length() == 9) { // Set SSN
				activeClient.setSsn(editSSN.getText());
			} else {
				activeClient.setSsn("Withheld");
			}
			if(westDate.getSQLDate() != null) { // Set birthday
				activeClient.setBirthday(westDate.getSQLDate());
			} else {
				activeClient.setBirthday(null);
			}
			activeClient.setAddress(address.getText()); // Set address
			if(cityList.getSelectedIndex() == cityList.getModel().getSize() - 1) { // Set city
				activeClient.setCity(otherLocation.getText()); // "Other" was selected, so get the city name manually entered
			} else {
				activeClient.setCity((String)cityList.getSelectedItem());
			}
			if(!telephone.getText().equals("") && !telephone.getText().equals("XxxYyyZzzz")) { // Set telephone; put in pretty format
				String telephoneString = telephone.getText();
				telephoneString = "(" + telephoneString.substring(0,3) + ") " + telephoneString.substring(3,6) + "-" + telephoneString.substring(6);
				activeClient.setTelephone(telephoneString);
			} else {
				activeClient.setTelephone(null);
			}
			activeClient.setGender((String)westSex.getSelectedItem()); // Set gender
			/*** If notes for client added, do c.setNotes(String notes) here ***/
			
			boolean success = Updates.updateClient(activeClient);
			if(success) {
				// Update city lists if a new city was added
				if(otherLocation.isEnabled()) {
					fpm.updateCityLists();
					otherLocation.setEnabled(false);
				}
				
				JOptionPane.showMessageDialog(null, "Successful Update", "Success", JOptionPane.INFORMATION_MESSAGE);
			}
			
			// Update SearchPanel JList so it displays critical information changes
			sp.refreshForClient(activeClient);
		}
		
		public boolean performWestFormatChecks() {
			boolean noWestFormatProblems = true;
			
			if(editFirstName.getText().equals("") || editLastName.getText().equals("") || address.getText().equals("")) {
				JOptionPane.showMessageDialog(null, "No empty fields allowed", "Error", JOptionPane.ERROR_MESSAGE);
				noWestFormatProblems = false;
			} else {
				if(editSSN.getText().length()!=9 && editSSN.getText().length()!=4 && editSSN.getText().length() != 0 && !editSSN.getText().equals("0, 4, or 9 Digits"))
				{
					JOptionPane.showMessageDialog(null, "Invalid Social Security Number Entry", "Error", JOptionPane.ERROR_MESSAGE);
					editSSN.setText("0, 4, or 9 Digits");
					noWestFormatProblems = false;
				} else {
					try {
						if(editSSN.getText().length()==9 || editSSN.getText().length()==4) {
							Integer.parseInt(editSSN.getText());
						}
					}
					catch(NumberFormatException e1) {
						JOptionPane.showMessageDialog(null, "Invalid Social Security Number Entry", "Error", JOptionPane.ERROR_MESSAGE);
						editSSN.setText("0, 4, or 9 Digits");
						noWestFormatProblems = false;
					}
				}
				
				if(telephone.getText().length()!=10 && !telephone.getText().equals("XxxYyyZzzz") && !telephone.getText().equals("")) {
					JOptionPane.showMessageDialog(null, "Invalid Telephone Number Entry", "Error", JOptionPane.ERROR_MESSAGE);
					telephone.setText("XxxYyyZzzz");
					noWestFormatProblems = false;
				}
				else {
					if(telephone.getText().equals("") || telephone.getText().equals("XxxYyyZzzz")) {
						
					} else {
						try {
							Long.parseLong(telephone.getText());
						} catch(NumberFormatException e1) {
							JOptionPane.showMessageDialog(null, "Invalid Telephone Number Entry", "Error", JOptionPane.ERROR_MESSAGE);
							telephone.setText("XxxYyyZzzz");
							noWestFormatProblems = false;
						}
					}
				}
				noWestFormatProblems = westDate.checkFormat();
			}
			return noWestFormatProblems;
		}
	}
	
	/**
	 * Delete the active client; appointment information will still be saved
	 * (for poundage reports) but not tied to any client
	 * 
	 * @author Scott Hoelsema
	 */
	private class deleteClientListener implements ActionListener
	{
		public void actionPerformed(ActionEvent e)
		{
			// Get name for later
			String name = activeClient.toString();
			
			// Confirm delete
			int response = JOptionPane.showConfirmDialog(null, "Delete " + name + "?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
			if(response==0)
			{
				// Delete activeClient
				boolean success = Updates.deleteClient(activeClient);
				
				if(success) {
					// Alert of success
					JOptionPane.showMessageDialog(null, name + " has been deleted.", "Success", JOptionPane.INFORMATION_MESSAGE);
					
					// Update search results
					sp.removeClient(activeClient);
				}				
			}
		}
	}
	
	/**
	 * Add new household member for the active client
	 * 
	 * @author Scott Hoelsema
	 */
	private class submitNewHouseholdMemberListener implements ActionListener {
		public void actionPerformed(ActionEvent ae) {			
			if(!performEastFormatChecks()) {
				return;
			}
			
			// Make new Household object to represent new household member
			Household hm = new Household();
			hm.setClientID(activeClient.getClientID());
			hm.setName(newHouseholdMemberName.getText());
			hm.setBirthday(eastDate.getSQLDate());
			hm.setGender((String)eastSex.getSelectedItem());
			hm.setRelationship((String)relationship.getSelectedItem());
			
			// Insert new household member
			boolean success = Inserts.addHouseholdMember(hm);
			
			if(success) {
				// Alert of success
				JOptionPane.showMessageDialog(null, "Successful addition of household member.", "Success", JOptionPane.INFORMATION_MESSAGE);
				
				// Clear household member section
				newHouseholdMemberName.setText("");
				eastSex.setSelectedIndex(0);
				eastDate.clear();
				relationship.setSelectedIndex(0);
				
				// Add to combobox that allows deleting of household membbers
				if(deleteHouseholdMember.getModel().getSize() == 0) { // If this will be the first household member, set these things enabled
					deleteHouseholdMember.setEnabled(true);
					submitDeleteHouseholdMember.setEnabled(true);
				}
				
				deleteHouseholdMember.addItem(hm); // Add item
				
				boolean hasSpouse = false;
				if(hm.getRelationship().equals("Spouse")) { // Remove "Spouse" if necessary
					hasSpouse = true;
				}
				setRelationshipsChoices(hasSpouse);
			}
		}
		
		public boolean performEastFormatChecks()
		{
			boolean noEastFormatProblems = true;
			
			if(newHouseholdMemberName.getText().equals(""))
			{
				JOptionPane.showMessageDialog(null, "The household memeber must have a name.", "Error", JOptionPane.ERROR_MESSAGE);
				noEastFormatProblems = false;
			}
		
			noEastFormatProblems = eastDate.checkFormat();
			
			return noEastFormatProblems;
		}
	}

	/**
	 * Delete selected household member
	 * 
	 * @author Scott Hoelsema
	 */
	private class submitDeleteHouseholdMemberListener implements ActionListener {
		public void actionPerformed(ActionEvent ae) {
			Household hm = (Household)deleteHouseholdMember.getSelectedItem();
			int response = JOptionPane.showConfirmDialog(null, "Delete " + hm.getName() + "?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
			if(response==0) {
				boolean success = Updates.deleteHouseholdMember(hm);
				if(success) {
					boolean hasSpouse = false;
					if(hm.getRelationship().equals("Spouse")) { // Add back in "Spouse" if necessary
						hasSpouse = false; // Set to false because the spouse is about to be removed
					}
					setRelationshipsChoices(hasSpouse);
					
					deleteHouseholdMember.removeItem(hm); // Add item
					
					if(deleteHouseholdMember.getModel().getSize() == 0) { // If this was the only household member, set these things disabled
						deleteHouseholdMember.setEnabled(false);
						submitDeleteHouseholdMember.setEnabled(false);
					}
				}
			}
		}
	}
	
	public void updateCityList(String[] cities) {
		cityList.setModel(new DefaultComboBoxModel<String>(cities));
	}
	
	/**
	 * Set otherLocation enabled/disabled based on if "Other" is selected in cityList
	 * 
	 * @author Scott Hoelsema
	 */
	private class cityListListener implements ItemListener {
		public void itemStateChanged(ItemEvent ie) {
			if(cityList.getSelectedIndex() == cityList.getModel().getSize() - 1) {
				otherLocation.setEnabled(true);
			} else {
				otherLocation.setEnabled(false);
			}
		}
	}
}