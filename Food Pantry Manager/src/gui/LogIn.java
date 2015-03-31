// Copyright (C) 2015 Scott Hoelsema
// Licensed under GPL v3.0; see LICENSE for full text

package gui;

import java.awt.GridLayout;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPasswordField;

import utils.Utilities;
import database.DatabaseConnection;
import database.Inserts;
import database.Queries;
import database.Updates;

/**
 * GUI to log in to Food Pantry Manager with the database credentials
 * 
 * @author Scott Hoelsema
 */
public class LogIn extends JFrame {
	private TextField usernameField;
	private JPasswordField passwordField;
	private JButton submitLogInData;
	private boolean invokeFPM; // Identifies whether or not to build a new instance of Food Pantry Manager (log in prompt may have been because of a timeout)

	public LogIn(boolean invokeFPM) {
		this.invokeFPM = invokeFPM;
		
		setUpWindow();
		createGUIElements();
		addListeners();
		
		setVisible(true);
	}
	
	/**
	 * Set up the window (icon, size, title, etc.)
	 */
	private void setUpWindow() {
		Utilities.setIcon(this);
		setTitle("Log In");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		GridLayout logInLayout = new GridLayout(5, 1);
		setLayout(logInLayout);
		logInLayout.setVgap(3);
		setSize(250, 200);
		Utilities.centerOnScreen(this);
	}
	
	/**
	 * Make and add Swing elements to the GUI
	 */
	private void createGUIElements() {
		// Create GUI elements
		JLabel usernameFieldLabel = new JLabel("Enter Database Username:");
		usernameField = new TextField(15);
		JLabel passwordFieldLabel = new JLabel("Enter Database Password:");
		passwordField = new JPasswordField(15);
		submitLogInData = new JButton("Submit Log In Data");

		// Add GUI elements
		add(usernameFieldLabel);
		add(usernameField);
		add(passwordFieldLabel);
		add(passwordField);
		add(submitLogInData);
	}
	
	/**
	 * Add listeners to password field and submit button that will call attemptLogIn
	 */
	private void addListeners() {
		passwordField.addKeyListener(new KeyListener() {
			@Override
			public void keyTyped(KeyEvent e) {}

			@Override
			public void keyPressed(KeyEvent e) {				
				if( e.getKeyCode() == KeyEvent.VK_ENTER ) {
					attemptLogIn();
				}
			}

			@Override
			public void keyReleased(KeyEvent e) {}
			
		});
		
		submitLogInData.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				attemptLogIn();	
			}
			
		});
	}
	
	/**
	 * Called from listeners; grabs credentials and attempts to log in
	 */
	private void attemptLogIn() {
		// Get username
		String username = usernameField.getText();
		
		// Get password
		StringBuilder sb = new StringBuilder();
		char pw[] = passwordField.getPassword();
		for(int i = 0; i<pw.length; i++)
		{
			sb.append(pw[i]);
		}
		String password = sb.toString();
		
		// Attempt log in
		boolean successfulLogIn = DatabaseConnection.logIn(username, password);
		
		// If log in successful, dispose of log in frame and build Food Pantry Manager; otherwise reset the connection - the login was because of a killed connection (Food Pantry Manager is already open)
		if(successfulLogIn) {
			setVisible(false);
			dispose();
			if(invokeFPM) {
				new FoodPantryManager();
			} else {
				Inserts.resetConnection();
				Queries.resetConnection();
				Queries.unblockQuerying();
				Updates.resetConnection();
			}
		}	
	}
}