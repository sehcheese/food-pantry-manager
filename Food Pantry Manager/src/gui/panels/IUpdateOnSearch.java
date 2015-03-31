// Copyright (C) 2015 Scott Hoelsema
// Licensed under GPL v3.0; see LICENSE for full text

package gui.panels;

import database.Client;

public interface IUpdateOnSearch {
	public void updateForClient(Client c);
}