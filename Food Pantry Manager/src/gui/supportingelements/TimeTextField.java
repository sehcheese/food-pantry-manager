// Copyright (C) 2015 Scott Hoelsema
// Licensed under GPL v3.0; see LICENSE for full text

package gui.supportingelements;

import gui.listeners.entryFieldFocusListener;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.Calendar;

import javax.swing.JOptionPane;
import javax.swing.JTextField;

public class TimeTextField extends JTextField {
	public TimeTextField() 
	{
		setColumns(4);
		clear(); // Set to default values
		addFocusListener(new entryFieldFocusListener(this));
	}
	
	/**
	 * Returns an SQL Timestamp - requires a DatePanel
	 * 
	 * @param dp A DatePanel so we know the year, month, and day
	 * @return A SQL Timestamp for the entered date and time
	 */
	public Timestamp getTimestamp(DatePanel dp) {
		if(checkFormat()) {
			Calendar convert = Calendar.getInstance();
			String hour;
			int northPlusPM = 0; 
			if(Integer.parseInt(getText().substring(0,2)) < 8) // Assume PM if less than 08:00
			{
				northPlusPM = 12;
			}
			hour = getText().substring(0,2);
			convert.set(dp.getYearEnteredInt(), dp.getMonthSelected() - 1, dp.getDaySelected(), Integer.parseInt(hour) + northPlusPM, Integer.parseInt(getText().substring(3,5)), 0);
			long date = convert.getTimeInMillis();
			return new Timestamp(date);
		} else {
			// Don't need to do anything else, appropriate errors have already been displayed
			return null;
		}
	}
	
	public boolean checkFormat() {
		if(getText().length() != 5) { // Too many or too few characters
			JOptionPane.showMessageDialog(null, "Invalid Pick Up Time", "Error", JOptionPane.ERROR_MESSAGE);
			setText("xx:xx");
			return false;
		} else {
			try {
				int testHour = Integer.parseInt(getText().substring(0,2));
				int testMinute = Integer.parseInt(getText().substring(3,5));
				if(testHour < 1 || testHour > 12 || testMinute < 0 || testMinute > 59) { // Invalid numbers
					JOptionPane.showMessageDialog(null, "Invalid Pick Up Time", "Error", JOptionPane.ERROR_MESSAGE);
					setText("xx:xx");
					return false;
				} else {
					if(getText().charAt(2)!=':') { // Something other than a colon is in the middle
						return false;
					} else {
						return true;
					}
				}
			} catch(NumberFormatException e) { // Not a number
				JOptionPane.showMessageDialog(null, "Invalid Pick Up Time", "Error", JOptionPane.ERROR_MESSAGE);
				setText("xx:xx");
				return false;
			}
		}
	}
	
	public void setToTimestamp(Timestamp ts) {
		String date = ts.toString();
		
		int hour = Integer.parseInt(date.substring(11,13));
		if(hour>12)
		{
			hour = hour - 12;
			String modHour = "";
			if(hour<10)
			{
				modHour = "0" + hour;
			}
			else
			{
				modHour = "" + hour;
			}
			setText(modHour + date.substring(13,16));
		}
		else
		{
			if(hour==0)
			{
				setText("12:" + date.substring(14,16));
			}
			else
			{
				setText(date.substring(11,16));
			}
		}
	}
	
	public void setToCurrent() // Set default selection date for this appointment date to current datetime
	{
		Date currentDatetime = new Date(System.currentTimeMillis()); 
		Timestamp current = new Timestamp(currentDatetime.getTime());
		String date = current.toString();
		
		int hour = Integer.parseInt(date.substring(11,13));
		if(hour>12)
		{
			hour = hour - 12;
			String modHour = "";
			if(hour<10)
			{
				modHour = "0" + hour;
			}
			else
			{
				modHour = "" + hour;
			}
			setText(modHour + date.substring(13,16));
		}
		else
		{
			if(hour==0)
			{
				setText("12:" + date.substring(14,16));
			}
			else
			{
				setText(date.substring(11,16));
			}
		}
	}
	
	/**
	 * Sets the TimeTextField to the nearest half hour based on first setting
	 * the field to the current time.
	 */
	public void setToNearestHalfHour() {
		setToCurrent();
		int minutes = Integer.parseInt(getText().substring(3));
		if(minutes < 15) { // Round down to the beginning of this hour
			setText(getText().substring(0,3) + "00");
		} else if(minutes < 45) { // Round to the half hour of this hour
			setText(getText().substring(0,3) + "30");
		} else { // Round to the beginning of next hour
			int hour = Integer.parseInt(getText().substring(0,2));
			if(hour < 9) { // Current hour between 1 and 8
				setText("0" + (hour + 1) + ":00");
			} else if(hour < 12) { // Current hour between 9 and 11
				setText((hour + 1) + ":00");
			} else { // Current hour is 12
				setText("01:00");
			}
		}
	}
	
	/**
	 * Set the TimeTextField to its default value
	 */
	public void clear() {
		setText("xx:xx");
	}
}