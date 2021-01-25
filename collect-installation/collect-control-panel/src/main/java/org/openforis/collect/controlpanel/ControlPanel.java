package org.openforis.collect.controlpanel;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JProgressBar;

import org.openforis.collect.controlpanel.component.HyperlinkLabel;

public interface ControlPanel {
	
	HyperlinkLabel getUrlHyperlink();

	JComponent getRunningAtUrlBox();

	JButton getShutdownBtn();

	JLabel getErrorMessageTxt();

	JLabel getStatusTxt();

	JProgressBar getProgressBar();

}
