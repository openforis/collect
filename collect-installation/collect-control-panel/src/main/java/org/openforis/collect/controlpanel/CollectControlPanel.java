package org.openforis.collect.controlpanel;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class CollectControlPanel extends Application {

	private static final String TITLE = "Open Foris Collect - Control Panel";
	private static final String CONTROL_PANEL_FXML = "collect_control_panel.fxml";
	private static final String LOGO_PATH = "of-collect-logo.png";
	private static final String ERROR_DIALOG_TITLE = "Open Foris Collect - Error";

	private CollectControlPanelController controller;

	public static void main(String[] args) {
		launch();
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		try {
			FXMLLoader fxmlLoader = new FXMLLoader();
			Pane pane = fxmlLoader.load(getClass().getResourceAsStream(CONTROL_PANEL_FXML));
			Scene scene = new Scene(pane);
			primaryStage.setScene(scene);
			primaryStage.setTitle(TITLE);
			primaryStage.setResizable(false);
	
			//prevent window close during initialization
			primaryStage.setOnCloseRequest(event -> {
				switch(controller.getStatus()) {
				case INITIALIZING:
				case STARTING:
					event.consume();
					break;
				default:
				}
			});
			
			setLogo(primaryStage);
	
			//initialize controller
			controller = fxmlLoader.getController();
			controller.setApp(this);
			controller.setStage(primaryStage);
			
			controller.closeLog();
			
			controller.startServer(() -> {
				controller.openBrowser();
			});
			
			primaryStage.show();
		} catch(Exception e) {
			showErrorDialog(e.getCause());
		}
	}

	private void setLogo(Stage primaryStage) {
		InputStream logoIs = getClass().getResourceAsStream(LOGO_PATH);
		Image logo = new Image(logoIs);
		primaryStage.getIcons().add(logo);
	}
	
	@Override
	public void stop() throws Exception {
		super.stop();
		if (controller != null) {
			controller.stop();
		}
	}
	
	private static void showErrorDialog(Throwable e) {
        Stage dialog = new Stage();
        dialog.setTitle(ERROR_DIALOG_TITLE);
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setResizable(false);
        FXMLLoader loader = new FXMLLoader();
        try {
        	Parent root = loader.load(CollectControlPanel.class.getResourceAsStream("error_dialog.fxml"));
            ErrorController errorController = loader.getController();
            errorController.setMainText("Error initializing Collect");
            StringWriter errorDetailsSW = new StringWriter();
            e.printStackTrace(new PrintWriter(errorDetailsSW));
            errorController.setErrorText(errorDetailsSW.toString());
            dialog.setScene(new Scene(root, 400, 300));
            dialog.show();
        } catch (IOException exc) {
            exc.printStackTrace();
        }
    }

}