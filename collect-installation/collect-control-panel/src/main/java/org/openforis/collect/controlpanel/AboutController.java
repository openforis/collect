package org.openforis.collect.controlpanel;

import java.net.URL;
import java.util.ResourceBundle;

import org.openforis.collect.Collect;

import javafx.application.HostServices;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;

public class AboutController implements Initializable {

	private static final String OPENFORIS_URI = "http://www.openforis.org";

	@FXML
	private Label versionLabel;

	private HostServices hostServices;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		versionLabel.setText(Collect.VERSION.toString());
	}

	@FXML
	public void openOpenForisLink(ActionEvent event) {
		hostServices.showDocument(OPENFORIS_URI);
	}

	public void setHostServices(HostServices hostServices) {
		this.hostServices = hostServices;
	}

}
