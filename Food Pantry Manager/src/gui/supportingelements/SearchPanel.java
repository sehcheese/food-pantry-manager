// Copyright (C) 2015 Scott Hoelsema
// Licensed under GPL v3.0; see LICENSE for full text

package gui.supportingelements;

import gui.FoodPantryManager;
import gui.listeners.entryFieldFocusListener;
import gui.panels.IUpdateOnSearch;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.border.Border;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import database.Client;
import database.Queries;

/**
 * The search panel that serves MainPanel, EditAppointmentPanel, and EditClientPanel
 * 
 * @author Scott Hoelsema
 */
public class SearchPanel extends JPanel {
	private FoodPantryManager fpm; // The panel that gets updated
	private SearchPanelModel spm;
	private JPanel lookUpPanel; // Holds labels, text fields, and search button
	private JTextField firstName;
	private JTextField lastName;
	private JTextField ssn;
	private JButton search;
	private JScrollPane choiceList;
	private JList<Client> selectClient; // Lists results of search
	
	public SearchPanel(FoodPantryManager fpm, SearchPanelModel spm)
	{		
		// Create elements
		this.fpm = fpm;
		this.spm = spm;
		lookUpPanel = new JPanel(new GridLayout(3,3,10,10));
		
		buildLookUp();
		buildResultsList();
		addListeners();
		
		selectClient.setModel(spm.getListModel());
		
		// Add elements to searchPanel
		Border northBorder = BorderFactory.createTitledBorder("Select a Client");
		setBorder(northBorder);
		add(lookUpPanel, BorderLayout.WEST);
		add(choiceList, BorderLayout.EAST);
	}
	
	/**
	 * Create and add elements to lookUpPanel
	 */
	private void buildLookUp() {
		// Create elements
		firstName = new JTextField(10);
		lastName = new JTextField(10);
		ssn = new JTextField(10);
		search = new JButton("Search");
		JLabel firstNameLabel = new JLabel("First Name:");
		JLabel lastNameLabel = new JLabel("Last Name:");
		JLabel ssnLabel = new JLabel("SSN:");
		
		// Add elements to lookUp
		lookUpPanel.add(firstNameLabel);
		lookUpPanel.add(firstName);
		lookUpPanel.add(new JLabel()); // Filler
		lookUpPanel.add(lastNameLabel);
		lookUpPanel.add(lastName);
		lookUpPanel.add(search);
		lookUpPanel.add(ssnLabel);
		lookUpPanel.add(ssn);
		lookUpPanel.add(new JLabel()); // Filler
	}
	
	/**
	 * Create selectClient, the search results list
	 */
	private void buildResultsList() {
		// Build selectClient (the search results list)
		selectClient = new JList<Client>();
		selectClient.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		
		// Put in JScrollPane
		choiceList = new JScrollPane(selectClient);
		choiceList.setPreferredSize(new Dimension(256,128));
	}
	
	/**
	 * Add listeners for various elements of the search panel
	 */
	private void addListeners() {
		// Add focus listeners to text fields to highlight text when focus gained
		firstName.addFocusListener(new entryFieldFocusListener(firstName));
		lastName.addFocusListener(new entryFieldFocusListener(lastName));
		ssn.addFocusListener(new entryFieldFocusListener(ssn));
		
		// Add enter listeners to text fields to search on push of enter key
		firstName.addKeyListener(new KeyListener() {
			@Override
			public void keyTyped(KeyEvent e) {}

			@Override
			public void keyPressed(KeyEvent e) {
				if( e.getKeyCode() == KeyEvent.VK_ENTER ) {
					performSearch();
				}
			}

			@Override
			public void keyReleased(KeyEvent e) {}
			
		});
		lastName.addKeyListener(new KeyListener() {
			@Override
			public void keyTyped(KeyEvent e) {}

			@Override
			public void keyPressed(KeyEvent e) {
				if( e.getKeyCode() == KeyEvent.VK_ENTER ) {
					performSearch();
				}
			}

			@Override
			public void keyReleased(KeyEvent e) {}
			
		});
		ssn.addKeyListener(new KeyListener() {
			@Override
			public void keyTyped(KeyEvent e) {}

			@Override
			public void keyPressed(KeyEvent e) {
				if( e.getKeyCode() == KeyEvent.VK_ENTER ) {
					performSearch();
				}
			}

			@Override
			public void keyReleased(KeyEvent e) {}
			
		});
		search.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				performSearch();
			}
		});
		
		selectClient.addListSelectionListener(new selectClientListListener());
	}
	
	private void performSearch() {
		// Query the database
		ArrayList<Client> searchResults = Queries.searchClients(firstName.getText(), lastName.getText(), ssn.getText());
		
		// Put results from the model in the JList
		if(searchResults != null) { // There was not a database or connection problem
			spm.setData(searchResults);
			selectClient.setModel(spm.getListModel());
		}
		
		// Select first item in results- this will trigger an event that will update panels based on active client
		if(selectClient.getModel().getSize() > 0) {
			selectClient.setSelectedIndex(0);
		} else {
			/* No matches, so list won't change and event won't trigger. We
			 * still want to do stuff though, so we'll pass a null
			 * client to the same method called by the
			 * selectClientListListener. */
			updatePanelWithActiveClient(null);
		}
	}
	
	/**
	 * Update the panels that display client information based on the active
	 * client. This is abstracted because it is called from performSearch (if no
	 * results) and selectClientListListener.
	 * 
	 * @param c
	 *            The active client
	 */
	private void updatePanelWithActiveClient(Client c) {
		fpm.updatePanelsOnSearchSelectionChange(c);
	}
	
	/**
	 * Set the given client as the selected one. Used so that tabs all have the
	 * same client selected; this is a gateway from the outside to control which
	 * client is selected.
	 * 
	 * @param c
	 *            The client to set selected
	 */
	public void setSelected(Client c) {
		if(c == null) {
			selectClient.clearSelection();
		}
		selectClient.setSelectedValue(c, true);
	}
	
	/**
	 * Updates selected client in the list with the passed in client;
	 * information such as their name may have changed
	 * 
	 * @param c
	 *            The client containing the updated information
	 */
	public void refreshForClient(Client c) {
		int selectedIndex = selectClient.getSelectedIndex();
		spm.getListModel().setElementAt(c, selectedIndex);
		setSelected(c);
	}
	
	public void removeClient(Client c) {
		spm.getListModel().removeElement(c);
	}
	
	/**
	 * Activated on selection of client from the search results; calls
	 * updatePanelAfterSearch to fill in corresponding information.
	 * NOTE: Because of data being set in the model, there are a lot of
	 * events fired to this that mean and do nothing, and hopefully eat
	 * up little resources. These extraneous events end up calling the
	 * a part of updateForClient. We can't just check for null, because
	 * if there actually aren't any search results we need to be able to
	 * update so that the user can't make any changes.
	 * 
	 * @author Scott Hoelsema
	 */
	private class selectClientListListener implements ListSelectionListener {
		public void valueChanged(ListSelectionEvent arg0) {
			if (! arg0.getValueIsAdjusting()) {
				updatePanelWithActiveClient(selectClient.getSelectedValue());				
			}
		}
	}
}