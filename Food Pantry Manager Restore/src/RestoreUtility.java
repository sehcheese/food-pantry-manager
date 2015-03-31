// Copyright (C) 2015 Scott Hoelsema
// Licensed under GPL v3.0; see LICENSE for full text

import java.awt.GridLayout;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.jasypt.exceptions.EncryptionOperationNotPossibleException;
import org.jasypt.util.text.BasicTextEncryptor;

public class RestoreUtility {	
	private static void decrypt(File inputFile) {		
		if(inputFile != null) {
			// Prompt for decryption password
			String decryptionPassword = promptForDecryptionPassword();
			if(decryptionPassword == null) { // No password provided
				return;
			}
			
			try {
				ArrayList<String> lines = new ArrayList<String>();
				BufferedReader br = new BufferedReader(new FileReader(inputFile));
				BasicTextEncryptor bte = new BasicTextEncryptor();
				bte.setPassword(decryptionPassword);
				
				// Read in and decrypt lines
				String line = bte.decrypt(br.readLine());
				while(line != null) {
					lines.add(line);
					line = bte.decrypt(br.readLine());
				}
				br.close();
					
					saveOutput(lines);
			} catch (FileNotFoundException e2) {
				JOptionPane.showMessageDialog(null, "I/O Error", "Error", JOptionPane.ERROR_MESSAGE);
				e2.printStackTrace();
			} catch (IOException e1) {
				JOptionPane.showMessageDialog(null, "I/O Error", "Error", JOptionPane.ERROR_MESSAGE);
				e1.printStackTrace();
			} catch (EncryptionOperationNotPossibleException e) {
				JOptionPane.showMessageDialog(null, "Decryption not possible - the decryption password is likely invalid.", "Error", JOptionPane.ERROR_MESSAGE);
				decrypt(inputFile);
			}
		}
	}
	
	private static void selectInputFile() {
		JFileChooser open = new JFileChooser();
		open.setDialogTitle("Open File to Decrypt...");
		FileNameExtensionFilter openFilter = new FileNameExtensionFilter("SQL File (*.sql)", "sql");
		open.addChoosableFileFilter(openFilter);
		int retVal = open.showOpenDialog(null);
		File inputFile = open.getSelectedFile();
		
		if(retVal == JFileChooser.APPROVE_OPTION && open.getSelectedFile() != null) {
			decrypt(inputFile);
		}
	}
	
	private static String promptForDecryptionPassword() {
		// Set up custom JPanel with masked password field
		JPanel decryptionPasswordInputPanel = new JPanel(new GridLayout(1,2));
		JPasswordField decryptionPasswordPrompt = new JPasswordField();
		decryptionPasswordInputPanel.add(new JLabel("Enter decryption password:"));
		decryptionPasswordInputPanel.add(decryptionPasswordPrompt);
		
		// Prompt for decryption password
		int result = JOptionPane.showConfirmDialog(null, decryptionPasswordInputPanel, "Decryption Password", JOptionPane.OK_CANCEL_OPTION);
		if(result == JOptionPane.OK_OPTION) { // OK selected
			return new String(decryptionPasswordPrompt.getPassword());
		} else { // Dialog cancelled
			return null;
		}
	}
	
	private static void saveOutput(ArrayList<String> lines) {
		// Save decrypted text to file
		JFileChooser save = new JFileChooser();
		save.setDialogTitle("Save Decrypted File As...");
		FileNameExtensionFilter saveFilter = new FileNameExtensionFilter("SQL File (*.sql)", "sql");
		save.addChoosableFileFilter(saveFilter);
		save.setFileFilter(saveFilter);
		int saveRetVal = save.showSaveDialog(null);
		File saveAs = save.getSelectedFile();
		
		// Ascertain being saved as a .sql file
		int enteredNameLengh = saveAs.getName().length();
		if(enteredNameLengh < 5 || !saveAs.getName().substring(enteredNameLengh - 4, enteredNameLengh).equals(".sql")) {
			saveAs = new File(saveAs.getAbsolutePath() + ".sql");
		}
		
		if(saveRetVal == JFileChooser.APPROVE_OPTION) {
			try {
				FileOutputStream fout = new FileOutputStream(saveAs);
				DataOutputStream dout = new DataOutputStream(fout);
				
				if(save.getSelectedFile()!=null) {
					for(String s : lines) {
						dout.writeBytes(s);
					}
				}
				System.out.flush();
				dout.close();
				fout.close();
				JOptionPane.showMessageDialog(null, "Restore commands generated.", "Success", JOptionPane.INFORMATION_MESSAGE);
			} catch (FileNotFoundException e2) 	{
				JOptionPane.showMessageDialog(null, "I/O Exception", "Error", JOptionPane.ERROR_MESSAGE);
				e2.printStackTrace();
			} catch (IOException e1) {
				JOptionPane.showMessageDialog(null, "I/O Exception", "Error", JOptionPane.ERROR_MESSAGE);
				e1.printStackTrace();
			}
		} else {
			JOptionPane.showMessageDialog(null, "No output file specified!", "Error", JOptionPane.ERROR_MESSAGE);
		}
	}
	
	public static void main(String[] args) {
		selectInputFile();
	}
}