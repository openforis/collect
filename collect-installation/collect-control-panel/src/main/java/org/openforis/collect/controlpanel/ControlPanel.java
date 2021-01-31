package org.openforis.collect.controlpanel;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JProgressBar;

import org.openforis.collect.controlpanel.component.JHyperlinkLabel;
import org.openforis.collect.controlpanel.component.JMultilineLabel;

public interface ControlPanel {
	
	JHyperlinkLabel getUrlHyperlink();

	JComponent getRunningAtUrlBox();

	JButton getShutdownBtn();

	JMultilineLabel getErrorMessageTxt();

	JLabel getStatusTxt();

	JProgressBar getProgressBar();

}
