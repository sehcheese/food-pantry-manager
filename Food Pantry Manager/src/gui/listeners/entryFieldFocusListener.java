// Copyright (C) 2015 Scott Hoelsema
// Licensed under GPL v3.0; see LICENSE for full text

package gui.listeners;

import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.JTextField;

/**
 * Highlights text of field passed in when it gains focus
 * 
 * @author Scott Hoelsema
 */
public class entryFieldFocusListener implements FocusListener {
	JTextField active;
	
	public entryFieldFocusListener(JTextField in) {
		active = in;
	}
	
	public void focusGained(FocusEvent e) {
		active.selectAll();
	}
	
	public void focusLost(FocusEvent e) {}
}
