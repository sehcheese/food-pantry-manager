// Copyright (C) 2015 Scott Hoelsema
// Licensed under GPL v3.0; see LICENSE for full text

package utils;

import java.awt.GridLayout;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.ProgressMonitor;

import org.jasypt.util.text.BasicTextEncryptor;

import database.Appointment;
import database.Client;
import database.DatabaseConnection;
import database.Household;
import database.Queries;

public class Backup extends Thread {
	private ProgressMonitor pm;
	private String encryptionPassword;
	private JPanel encryptionPasswordInputPanel;
	private JPasswordField encryptionPasswordOne;
	private JPasswordField encryptionPasswordTwo;
	
	public Backup(ProgressMonitor pm)
	{
		this.pm = pm;
		setupEncryptionPasswordPromptJPanel();
	}
	
	public void setupEncryptionPasswordPromptJPanel() {
		// Set up JPanel to garner encryption password
		encryptionPasswordInputPanel = new JPanel();
		GridLayout encryptionPasswordInputPanelLayout = new GridLayout(2, 2);
		encryptionPasswordInputPanel.setLayout(encryptionPasswordInputPanelLayout);
		encryptionPasswordOne = new JPasswordField();
		encryptionPasswordTwo = new JPasswordField();
	    encryptionPasswordInputPanel.add(new JLabel("Enter encryption password:"));
	    encryptionPasswordInputPanel.add(encryptionPasswordOne);
	    encryptionPasswordInputPanel.add(new JLabel("Re-enter encryption password:"));
	    encryptionPasswordInputPanel.add(encryptionPasswordTwo);
	}
	
	@Override
	public void run() {
		// Prompt for encryption password
		if(!promptForEncryptionPassword()) { // If a password was not procured, return, do not do any encryption
			return;
		}
		
		if(backupFoodPantryManager()) {
			// Set completion message
			String completionMessage = "";
			if(Configuration.isDropBoxBackup && Configuration.isLocalOrNetworkBackup)
				completionMessage = "Local and remote backup complete!";
			else if(Configuration.isLocalOrNetworkBackup)
				completionMessage = "Local backup complete!";
			else if(Configuration.isDropBoxBackup)
				completionMessage = "Remote backup complete!";
			
			JOptionPane.showMessageDialog(null, completionMessage, "Success", JOptionPane.INFORMATION_MESSAGE);
		}
		
		// Close database connection
		DatabaseConnection.logOut();
		
		// Not sure why does not exit automatically.
		System.exit(NORM_PRIORITY);
	}
	
	/**
	 * Attempt to garner an encryption password from the user.
	 * Since this happens every time encryption happens, the password could
	 * be different each time if the user so chose.
	 * This method sets the global variable encryptionPassword and returns
	 * a boolean indicating whether an encryption password was successfully procured.
	 * @return boolean flag indicating whether or not an encryption password was successfully procured
	 */
	private boolean promptForEncryptionPassword() {
	    int result = JOptionPane.showConfirmDialog(null, encryptionPasswordInputPanel, 
	               "Encryption Password for Data Backup", JOptionPane.OK_CANCEL_OPTION);
	    if (result == JOptionPane.OK_OPTION) { // OK button selected
	    	if(new String(encryptionPasswordOne.getPassword()).equals(new String(encryptionPasswordTwo.getPassword()))) { // Typed passwords match
	    	   encryptionPassword = new String(encryptionPasswordOne.getPassword());
	    	   return true;
	    	} else { // Typed passwords do not match
	    	   JOptionPane.showMessageDialog(null, "Password not confirmed; fields do not match.", "Error", JOptionPane.ERROR_MESSAGE);
	    	   
	    	   // Enter recursive method that prompts for passwords anew until matching passwords obtained
	    	   return promptForEncryptionPassword();
	       }
	    } else { // Cancel button selected
	    	return false;
	    }
	}
	
	/*
	 * Back up the entire database
	 */
	public boolean backupFoodPantryManager() {
		// If not making local backup, change backup directory to current workspace where files will be created, uploaded, and then destroyed
		if(!Configuration.isLocalOrNetworkBackup) {
			File currentWorkspace = new File("./");
			if(currentWorkspace.canWrite())
				Configuration.localOrNetworkBackupDirectory = "./";
			else {
				JOptionPane.showMessageDialog(null, "DropBox backup only - cannot write to same directory as jar for temporary files. Try moving jar or widening permissions on the folder that contains it.", "Error", JOptionPane.ERROR_MESSAGE);
				return false;
			}
		} else {
			// Ascertain that the backup directory exists
			File backupDirectory = new File(Configuration.localOrNetworkBackupDirectory);
			if(!backupDirectory.exists()) {
				// Attempt to create it
				boolean backupDirectoryCreated = backupDirectory.mkdir();
				
				if(!backupDirectoryCreated) { // If backup directory could not be created
					JOptionPane.showMessageDialog(null, "Local backup directory does not exist and cannot be created. Check your configurations.", "Error", JOptionPane.ERROR_MESSAGE);
					
					// If local directory does not exist continue with backup if set to upload to Dropbox
					Configuration.isLocalOrNetworkBackup = false; // Set flag to false to indicate that local backup should not happen because the directory doesn't exist
					if(!Configuration.isDropBoxBackup) // Local backup directory does not exist and no DropBox backup, therefore return
						return false;
					else { // Local backup directory does not exist but still do DropBox backup
						Configuration.localOrNetworkBackupDirectory = "./"; // Change to current workspace so files can created for DropBox upload
						File currentWorkspace = new File(Configuration.localOrNetworkBackupDirectory);
						if(!currentWorkspace.canWrite()) {
							JOptionPane.showMessageDialog(null, "You do not have permission to write to the working directory. Obtain permissions or move the jar file to a directory where you have write permission.");
							return false;
						}
					}
				}
				
				
			} else { // Backup directory exists, check if we have permission to write to it
				if(!backupDirectory.canWrite()) {
					JOptionPane.showMessageDialog(null, "You do not have permission to write to the backup directory. Obtain permissions or change the backup directory.");
					return false;
				}
			}
		}
		
		if(backupClients() && !pm.isCanceled()) {
			if(backupAppointments() && !pm.isCanceled()) {
				if(backupHouseholds() && !pm.isCanceled()) {
					return true;
				}
			}
		}
		
		return false;
	}
	
	private boolean backupClients() {
		pm.setProgress(0);
		
		// Create client backup file
		Timestamp nowTM = new Timestamp(System.currentTimeMillis());
		String now = nowTM.toString().replace(':', '.');
		now = now.replace(' ', '_');
		File clientsSQL = new File(Configuration.localOrNetworkBackupDirectory + now + "_Clients.sql");
		
		try {
			clientsSQL.createNewFile();
						
			// Set up encryption
			BasicTextEncryptor bte = new BasicTextEncryptor();
			bte.setPassword(encryptionPassword);
			
			// Get all clients
			pm.setNote("Retrieving and encrypting client data...");
			ArrayList<Client> allClients = Queries.getAllClients();
			
			// One by one...
			StringBuilder sb = new StringBuilder();
			for(Client c : allClients) {
				// Get insert statement
				String insertStatement = c.asInsertStatement();
				
				// Encrypt and add to StringBuilder
				sb.append(bte.encrypt(insertStatement));
				sb.append("\n");
				
			}
			pm.setProgress(7);
			
			// Write to file
			if(Configuration.isLocalOrNetworkBackup)
				pm.setNote("Writing client data to local backup file...");
				
			FileOutputStream fout = new FileOutputStream(clientsSQL);
			DataOutputStream dout = new DataOutputStream(fout);
			dout.writeBytes(sb.toString());
			
			// Close outputs
			dout.close();
			fout.close();
			
			// Update progress
			if(Configuration.isDropBoxBackup)
				pm.setProgress(15);
			else
				pm.setProgress(40);
			
			// Upload file to dropbox
			if(Configuration.isDropBoxBackup) {
				pm.setNote("Uploading client data to DropBox...");
				Utilities.uploadToDropbox(clientsSQL);
				pm.setProgress(40);
				
				// If no local backup, delete created file
				if(!Configuration.isLocalOrNetworkBackup) {
					clientsSQL.delete();
				}
			}
			
			return true;
		} catch (FileNotFoundException e) {
			Logger.logThrowable(e);
			JOptionPane.showMessageDialog(null, "File Not Found Exception", "Error", JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			Logger.logThrowable(e);
			JOptionPane.showMessageDialog(null, "I/O Exception", "Error", JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
			return false;
		}	
	}
	
	public boolean backupAppointments() {
		// Set up output file and stream
		Timestamp nowTM = new Timestamp(System.currentTimeMillis());
		String now = nowTM.toString().replace(':', '.');
		now = now.replace(' ', '_');
		File appointmentsSQL = new File(Configuration.localOrNetworkBackupDirectory + now + "_Appointments.sql");
		try {
			appointmentsSQL.createNewFile();
						
			// Set up encryption
			BasicTextEncryptor bte = new BasicTextEncryptor();
			bte.setPassword(encryptionPassword);
			
			// Get all clients
			pm.setNote("Retrieving and encrypting appointment data...");
			ArrayList<Appointment> allAppts = Queries.getAllAppointments();
						
			// One by one...
			StringBuilder sb = new StringBuilder();
			for(Appointment a : allAppts) {
				// Get insert statement
				String insertStatement = a.asInsertStatement();
				
				// Encrypt and add to StringBuilder
				sb.append(bte.encrypt(insertStatement));
				sb.append("\n");
			}
			pm.setProgress(52);
			
			// Write to file
			if(Configuration.isLocalOrNetworkBackup)
				pm.setNote("Writing appointment data to local backup file...");
				
			FileOutputStream fout = new FileOutputStream(appointmentsSQL);
			DataOutputStream dout = new DataOutputStream(fout);
			dout.writeBytes(sb.toString());
			
			// Close outputs
			dout.close();
			fout.close();
			
			// Update progress
			if(Configuration.isDropBoxBackup)
				pm.setProgress(66);
			else
				pm.setProgress(120);
			
			// Upload file to dropbox
			if(Configuration.isDropBoxBackup) {
				pm.setNote("Uploading appointment data to DropBox...");
				Utilities.uploadToDropbox(appointmentsSQL);
				pm.setProgress(120);
				
				// If no local backup, delete created file
				if(!Configuration.isLocalOrNetworkBackup) {
					appointmentsSQL.delete();
				}
			}
			
			return true;
		} catch (FileNotFoundException e) {
			Logger.logThrowable(e);
			JOptionPane.showMessageDialog(null, "File Not Found Exception", "Error", JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			Logger.logThrowable(e);
			JOptionPane.showMessageDialog(null, "I/O Exception", "Error", JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
			return false;
		}
	}
	
	public boolean backupHouseholds() {
		// Set up output file and stream
		Timestamp nowTM = new Timestamp(System.currentTimeMillis());
		String now = nowTM.toString().replace(':', '.');
		now = now.replace(' ', '_');
		File householdsSQL = new File(Configuration.localOrNetworkBackupDirectory + now + "_Households.sql");
		
		try {
			householdsSQL.createNewFile();
			
			// Set up encryption
			BasicTextEncryptor bte = new BasicTextEncryptor();
			bte.setPassword(encryptionPassword);
			
			// Get all clients
			pm.setNote("Retrieving and encrypting household data...");
			ArrayList<Household> allHouseholds = Queries.getAllHouseholds();
			
			// One by one...
			StringBuilder sb = new StringBuilder();
			for(Household h : allHouseholds) {
				// Get insert statement
				String insertStatement = h.asInsertStatement();
				
				// Encrypt and add to StringBuilder
				sb.append(bte.encrypt(insertStatement));
				sb.append("\n");
			}
			pm.setProgress(129);
			
			// Write to file
			if(Configuration.isLocalOrNetworkBackup)
				pm.setNote("Writing household data to local backup file...");
				
			FileOutputStream fout = new FileOutputStream(householdsSQL);
			DataOutputStream dout = new DataOutputStream(fout);
			dout.writeBytes(sb.toString());
			
			// Close outputs
			dout.close();
			fout.close();
			
			// Update progress
			if(Configuration.isDropBoxBackup)
				pm.setProgress(140);
			else
				pm.setProgress(170);
			
			// Upload file to dropbox
			if(Configuration.isDropBoxBackup) {
				pm.setNote("Uploading household data to DropBox...");
				Utilities.uploadToDropbox(householdsSQL);
				pm.setProgress(170);
				
				// If no local backup, delete created file
				if(!Configuration.isLocalOrNetworkBackup) {
					householdsSQL.delete();
				}
			}
			
			return true;
		} catch (FileNotFoundException e) {
			Logger.logThrowable(e);
			JOptionPane.showMessageDialog(null, "File Not Found Exception", "Error", JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			Logger.logThrowable(e);
			JOptionPane.showMessageDialog(null, "I/O Exception", "Error", JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
			return false;
		}
	}
}
