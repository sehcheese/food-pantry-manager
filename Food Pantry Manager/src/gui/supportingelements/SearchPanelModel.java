// Copyright (C) 2015 Scott Hoelsema
// Licensed under GPL v3.0; see LICENSE for full text

package gui.supportingelements;

import java.util.ArrayList;

import javax.swing.DefaultListModel;

import database.Client;

/**
 * This is the abstracted data model for the JList of SearchPanel. By
 * abstracting this, we can show the same search results across all instances of
 * SearchPanel.
 * 
 * @author Scott Hoelsema
 */
public class SearchPanelModel {
	private ArrayList<Client> modelData = new ArrayList<Client>();
	private DefaultListModel<Client> listModel = new DefaultListModel<Client>();
	
	public void setData(ArrayList<Client> searchResults) {		
		modelData = searchResults;
	}
	
	public DefaultListModel<Client> getListModel() {
		listModel.clear();
		for(int i = 0; i < modelData.size(); i++) {
			listModel.add(i, modelData.get(i));
		}
		return listModel;
	}
}