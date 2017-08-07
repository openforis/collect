package org.openforis.collect.controlpanel;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;

public class ErrorController {
	@FXML
    private Label mainText;
    @FXML
    private TextArea errorMessage;
    
	public void setMainText(String text) {
		mainText.setText(text);
	}
	
    public void setErrorText(String text) {
        errorMessage.setText(text);
    }

    @FXML
    private void close() {
        errorMessage.getScene().getWindow().hide();
    }

}