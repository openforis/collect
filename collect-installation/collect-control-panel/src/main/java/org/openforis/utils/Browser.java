package org.openforis.utils;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;

import javax.swing.JOptionPane;

public abstract class Browser {

	public static void openPage(URI uri) {
		try {
			Desktop.getDesktop().browse(uri);
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null, "Error opening browser: " + e.getMessage());
		}
	}

	public static void openPage(String url) {
		try {
			openPage(new URI(url));
		} catch (Exception e) {
			// ignore it
		}
	}

}
