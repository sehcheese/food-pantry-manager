// Copyright (C) 2015 Scott Hoelsema
// Licensed under GPL v3.0; see LICENSE for full text

package gui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.text.MessageFormat;

import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;

import utils.Utilities;

/**
 * This class prompts the user to select a printer; it will then perform a print
 * of the text in the passed in JTextArea and the page will have the given table
 * 
 * @author Scott Hoelsema
 */
public class SelectPrinter extends JFrame
{
	private FoodPantryManager fpm;
	private JComboBox<PrintService> printerChoices;
	private JButton select;
	private JTextArea display;
	private String title;
	
	public SelectPrinter(JTextArea display, String title, FoodPantryManager fpm)
	{
		// Set variables
		this.display = display;
		this.title = title;
		this.fpm = fpm;
		
		// Window Settings
		Utilities.setIcon(this);
		setTitle("Select Printer");
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setResizable(false);
		Utilities.centerOnScreen(this);
		
		buildGUI();
		
		// Set visible
		setVisible(true);
	}
	
	private void buildGUI() {
		// Create elements
		printerChoices = new JComboBox<PrintService>();
		select = new JButton("Select");
		
		// Set layout
		setLayout(new BorderLayout());
		
		// Look up printers and add to combo box
		PrintService[] printers = lookupPrinters();
		for(PrintService p : printers) {
			printerChoices.addItem(p);
		}
		
		// Add listener to perform print
		select.addActionListener(new SubmitPrintJob());
		
		// Add elements to JFrame
		add(printerChoices, BorderLayout.CENTER);
		add(select, BorderLayout.SOUTH);
		
		// Pack
		pack();
	}
	
	private PrintService[] lookupPrinters() {
		// Lookup print services
		PrintService pservices[] = PrintServiceLookup.lookupPrintServices(null, null);
		
		// Move default printer to first position
		PrintService defaultPrinter = PrintServiceLookup.lookupDefaultPrintService();
		for(int i = 0; i < pservices.length; i++) {
			if(pservices[i].equals(defaultPrinter)) {
				PrintService temp = pservices[0];
				pservices[0] = defaultPrinter;
				pservices[i] = temp;
			}
		}
		
		return pservices;
	}
	
	private class SubmitPrintJob implements ActionListener {
		public void actionPerformed(ActionEvent ae) {
			PrinterJob job = PrinterJob.getPrinterJob();
			
			try {
				job.setPrintService((PrintService) printerChoices.getSelectedItem());
				Printable printable = display.getPrintable(new MessageFormat(title), new MessageFormat("Page - {0}"));
				
				job.setPrintable(printable);
				job.print();
			} catch (PrinterException e){
				JOptionPane.showMessageDialog(null, "Printer Error", "Error", JOptionPane.ERROR_MESSAGE);
				e.printStackTrace();
			}			
			
			fpm.toFront();
			dispose();
		}
	}
}