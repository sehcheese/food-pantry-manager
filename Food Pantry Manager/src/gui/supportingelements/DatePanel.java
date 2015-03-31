// Copyright (C) 2015 Scott Hoelsema
// Licensed under GPL v3.0; see LICENSE for full text

package gui.supportingelements;

import gui.listeners.entryFieldFocusListener;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.Calendar;

import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class DatePanel extends JPanel {
	private JComboBox<String> month;
	private JComboBox<String> day;
	private JTextField year;
	private boolean includeAllOption; // Determines whether month and day have an option for "All"
	private boolean isOptional; // Determines whether the contents of this DatePanel must be supplied
	
	public DatePanel(boolean includeAllOption, boolean isOptional)
	{
		// Set variables
		month = new JComboBox<String>();
		day = new JComboBox<String>();
		year = new JTextField();
		year.addFocusListener(new entryFieldFocusListener(year));
		year.setColumns(3);
		this.includeAllOption = includeAllOption;
		this.isOptional = isOptional;
		
		addMonths();
		addDays();
		add(year);
		
		clear(); // Set to default values
	}
	
	private void addMonths() {
		if(includeAllOption) {
			month.addItem("All Year");
		}
		month.addItem("January");
		month.addItem("February");
		month.addItem("March");
		month.addItem("April");
		month.addItem("May");
		month.addItem("June");
		month.addItem("July");
		month.addItem("August");
		month.addItem("September");
		month.addItem("October");
		month.addItem("November");
		month.addItem("December");
		
		month.addActionListener(new buildDaysBasedOnMonthListener());
		
		this.add(month);
	}
	
	private void addDays() {
		if(includeAllOption) {
			day.addItem("All Month");
		}
		day.addItem("1");
		day.addItem("2");
		day.addItem("3");
		day.addItem("4");
		day.addItem("5");
		day.addItem("6");
		day.addItem("7");
		day.addItem("8");
		day.addItem("9");
		day.addItem("10");
		day.addItem("11");
		day.addItem("12");
		day.addItem("13");
		day.addItem("14");
		day.addItem("15");
		day.addItem("16");
		day.addItem("17");
		day.addItem("18");
		day.addItem("19");
		day.addItem("20");
		day.addItem("21");
		day.addItem("22");
		day.addItem("23");
		day.addItem("24");
		day.addItem("25");
		day.addItem("26");
		day.addItem("27");
		day.addItem("28");
		day.addItem("29");
		
		add(day);
	}
	
	/**
	 * This returns the number of the month that is selected (not its index in the combobox)
	 * 
	 * @return The number of the month that is selected (January = 1, June = 6, etc.)
	 */
	public int getMonthSelected() {
		if(includeAllOption) {
			return month.getSelectedIndex();
		} else {
			return month.getSelectedIndex() + 1;
		}
	}
	
	/**
	 * This returns the number of the day that is selected (not its index in the combobox)
	 * 
	 * @return The number of the day that is selected
	 */
	public int getDaySelected() {
		if(includeAllOption) {
			return day.getSelectedIndex();
		} else {
			return day.getSelectedIndex() + 1;
		}
	}
	
	public String getYearEnteredString() {
		return year.getText();		
	}
	
	public int getYearEnteredInt() {
		return Integer.parseInt(year.getText());
	}

	public Date getSQLDate() {
		if(getYearEnteredString().equals("xxxx") || getYearEnteredString().equals("")) {
			return null;
		} else {
			if(includeAllOption && (month.getSelectedIndex() == 0 || day.getSelectedIndex() == 0)) { // Not meaningful to get SQL Date for "All Year" or "All Month"
				return null;
			}
			Calendar convert = Calendar.getInstance();
			convert.set(getYearEnteredInt(), getMonthSelected() - 1, getDaySelected());
			Date date = new Date(convert.getTimeInMillis());
			return date;
		}
	}
	
	/**
	 * Get timestamp for current values, if includeAllOption == true, this may include wildcards
	 * 
	 * @return String representing the current selections; it is a String because there may be wildcards
	 */
	public String getTimestamp() {
		if(includeAllOption) {
			if(month.getSelectedIndex() == 0) { // "All Year" is selected
				return getYearEnteredString() + "-%";
			} else if(day.getSelectedIndex() == 0) { // "All Month" is selected
				String monthString; // Because month needs to be two digits
				if(getMonthSelected() < 10) {
					monthString = "0" + Integer.toString(getMonthSelected());
				} else {
					monthString = Integer.toString(getMonthSelected());
				}
				return getYearEnteredString() + "-" + monthString + "-%";
			} else {
				return getSQLDate().toString() + "%";
			}
		} else {
			return getSQLDate().toString() + "%";
		}
	}
	
	public boolean checkFormat() {
		if(isOptional && (year.getText().equals("") || year.getText().equals("xxxx"))) {
			return true;
		} else {
			if(year.getText().length() == 4) { // First check that there are four characters in the year
				try {
					int yearInt = Integer.parseInt(year.getText()); // Now make sure it's a number
					
					// Check that February 29 is not selected in a non leap year
					if(yearInt%4!=0 && getMonthSelected()==2 && getDaySelected()==29) {
						JOptionPane.showMessageDialog(null, "Year not a leap year- you cannot select February 29.", "Error", JOptionPane.ERROR_MESSAGE);
						return false;
					}
					return true;
				} catch(NumberFormatException e) {
					JOptionPane.showMessageDialog(null, "Invalid year entry.", "Error", JOptionPane.ERROR_MESSAGE);
					return false; // Couldn't successfully parse year into a number
				}
			} else {
				JOptionPane.showMessageDialog(null, "Invalid year entry.", "Error", JOptionPane.ERROR_MESSAGE);
				return false; // Too many or too few characters in the year
			}
		}
	}
	
	public void setToSQLDate(Date date) {
		setToTimestamp(new Timestamp(date.getTime()));
	}
	
	public void setToTimestamp(Timestamp ts)
	{
		String date = ts.toString();
		
		if(date.substring(5,7).equals("01")) {
			month.setSelectedItem("January");
		} else if(date.substring(5,7).equals("02")) {
			month.setSelectedItem("February");
		} else if(date.substring(5,7).equals("03")) {
			month.setSelectedItem("March");
		} else if(date.substring(5,7).equals("04")) {
			month.setSelectedItem("April");
		} else if(date.substring(5,7).equals("05")) {
			month.setSelectedItem("May");
		} else if(date.substring(5,7).equals("06")) {
			month.setSelectedItem("June");
		} else if(date.substring(5,7).equals("07")) {
			month.setSelectedItem("July");
		} else if(date.substring(5,7).equals("08")) {
			month.setSelectedItem("August");
		} else if(date.substring(5,7).equals("09")) {
			month.setSelectedItem("September");
		} else if(date.substring(5,7).equals("10")) {
			month.setSelectedItem("October");
		} else if(date.substring(5,7).equals("11")) {
			month.setSelectedItem("November");
		} else if(date.substring(5,7).equals("12")) {
			month.setSelectedItem("December");
		}
		
		if(includeAllOption) {
			day.setSelectedIndex(Integer.parseInt(date.substring(8,10)));
		} else {
			day.setSelectedIndex(Integer.parseInt(date.substring(8,10)) - 1);
		}
		
		year.setText(date.substring(0,4));
	}
	
	public void setToCurrent() {
		Date currentDatetime = new Date(System.currentTimeMillis()); 
		Timestamp current = new Timestamp(currentDatetime.getTime());
		String date = current.toString();
		
		if(date.substring(5,7).equals("01")) {
			month.setSelectedItem("January");
		}
		else if(date.substring(5,7).equals("02")) {
			month.setSelectedItem("February");
		}
		else if(date.substring(5,7).equals("03")) {
			month.setSelectedItem("March");
		}
		else if(date.substring(5,7).equals("04")) {
			month.setSelectedItem("April");
		}
		else if(date.substring(5,7).equals("05")) {
			month.setSelectedItem("May");
		}
		else if(date.substring(5,7).equals("06")) {
			month.setSelectedItem("June");
		}
		else if(date.substring(5,7).equals("07")) {
			month.setSelectedItem("July");
		}
		else if(date.substring(5,7).equals("08")) {
			month.setSelectedItem("August");
		}
		else if(date.substring(5,7).equals("09")) {
			month.setSelectedItem("September");
		}
		else if(date.substring(5,7).equals("10")) {
			month.setSelectedItem("October");
		}
		else if(date.substring(5,7).equals("11")) {
			month.setSelectedItem("November");
		}
		else if(date.substring(5,7).equals("12")) {
			month.setSelectedItem("December");
		}
		
		if(includeAllOption) {
			day.setSelectedIndex(Integer.parseInt(date.substring(8,10)));
		} else {
			day.setSelectedIndex(Integer.parseInt(date.substring(8,10)) - 1);
		}
		
		year.setText(date.substring(0,4));
	}
	
	public void setAheadFromCurrent(int numWeeks) {
		long current = System.currentTimeMillis();
		long ahead = numWeeks * 604800000L;
		Timestamp plusFiveWeeks = new Timestamp(current + ahead);
		String date = plusFiveWeeks.toString();
		
		if(date.substring(5,7).equals("01")) {
			month.setSelectedItem("January");
		}
		else if(date.substring(5,7).equals("02")) {
			month.setSelectedItem("February");
		}
		else if(date.substring(5,7).equals("03")) {
			month.setSelectedItem("March");
		}
		else if(date.substring(5,7).equals("04")) {
			month.setSelectedItem("April");
		}
		else if(date.substring(5,7).equals("05")) {
			month.setSelectedItem("May");
		}
		else if(date.substring(5,7).equals("06")) {
			month.setSelectedItem("June");
		}
		else if(date.substring(5,7).equals("07")) {
			month.setSelectedItem("July");
		}
		else if(date.substring(5,7).equals("08")) {
			month.setSelectedItem("August");
		}
		else if(date.substring(5,7).equals("09")) {
			month.setSelectedItem("September");
		}
		else if(date.substring(5,7).equals("10")) {
			month.setSelectedItem("October");
		}
		else if(date.substring(5,7).equals("11")) {
			month.setSelectedItem("November");
		}
		else if(date.substring(5,7).equals("12")) {
			month.setSelectedItem("December");
		}
		
		if(includeAllOption) {
			day.setSelectedIndex(Integer.parseInt(date.substring(8,10)));
		} else {
			day.setSelectedIndex(Integer.parseInt(date.substring(8,10)) - 1);
		}
		
		year.setText(date.substring(0,4));
	}
	
	@Override
	public String toString() {
		if(includeAllOption) {
			if(month.getSelectedIndex() == 0) {
				return getYearEnteredString();
			} else if(day.getSelectedIndex() == 0) {
				return (String)month.getSelectedItem() + " " + getYearEnteredString();
			} else {
				return (String)month.getSelectedItem() + " " + getDaySelected() + ", " + getYearEnteredString();
			}
		} else {
			return (String)month.getSelectedItem() + " " + getDaySelected() + ", " + getYearEnteredString();
		}
	}
	
	/**
	 * Set fields to their default values
	 */
	public void clear() {
		month.setSelectedIndex(0);
		day.setSelectedIndex(0);
		year.setText("xxxx");
	}
	
	@Override
	public void setEnabled(boolean enabled) {
		month.setEnabled(enabled);
		day.setEnabled(enabled);
		year.setEnabled(enabled);
	}
	
	private class buildDaysBasedOnMonthListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			if(month.getSelectedItem().equals("All Year")) {
				day.setEnabled(false);
			} else if(month.getSelectedItem().equals("January") || month.getSelectedItem().equals("March") || month.getSelectedItem().equals("May") || month.getSelectedItem().equals("July") || month.getSelectedItem().equals("August") || month.getSelectedItem().equals("October") || month.getSelectedItem().equals("December")) {
				day.setEnabled(true);
				build31Days();
			} else if(month.getSelectedItem().equals("April") || month.getSelectedItem().equals("June") || month.getSelectedItem().equals("September") || month.getSelectedItem().equals("November")) {
				day.setEnabled(true);
				build30Days();
			} else { // month = February
				day.setEnabled(true);
				build29Days();
			}			
		}
		
		public void build31Days()
		{
			if(includeAllOption) {
				if(day.getModel().getSize() == 30) {
					day.addItem("30");
					day.addItem("31");
				} else if(day.getModel().getSize() == 31) {
					day.addItem("31");
				}
			} else {
				if(day.getModel().getSize() == 29) {
					day.addItem("30");
					day.addItem("31");
				} else if(day.getModel().getSize() == 30) {
					day.addItem("31");
				}
			}
		}
		
		public void build30Days() {
			if(includeAllOption) {
				if(day.getModel().getSize() == 30) {
					day.addItem("30");
				} else if(day.getModel().getSize() == 32) {
					day.removeItemAt(31);
				}
			} else {
				if(day.getModel().getSize() == 29) {
					day.addItem("30");
				} else if(day.getModel().getSize() == 31) {
					day.removeItemAt(30);
				}
			}
		}
		
		public void build29Days()
		{
			if(includeAllOption) {
				if(day.getModel().getSize() == 31) {
					day.removeItemAt(30);
				} else if(day.getModel().getSize() == 32) {
					day.removeItemAt(31);
					day.removeItemAt(30);
				}
			} else {
				if(day.getModel().getSize() == 30) {
					day.removeItemAt(29);
				} else if(day.getModel().getSize() == 31) {
					day.removeItemAt(30);
					day.removeItemAt(29);
				}
			}
		}
	}
}