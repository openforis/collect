package org.openforis.collect.controlpanel;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.stage.Window;

public class CollectControlPanel extends Application {

	private static final String CONTROL_PANEL_FXML = "collect_control_panel.fxml";
	private static final String TITLE = "Open Foris Collect - Control Panel";

	private CollectControlPanelController controller;


	public static void main(String[] args) {
		launch();
	}

	@Override
	public void start(Stage stage) throws Exception {
		stage.setTitle(TITLE);
		stage.setResizable(false);

		FXMLLoader fxmlLoader = new FXMLLoader();
		Pane pane = (Pane) fxmlLoader.load(getClass().getResourceAsStream(CONTROL_PANEL_FXML));

		controller = fxmlLoader.getController();
		controller.startServer();

		Scene scene = new Scene(pane);
		stage.setScene(scene);
		Window window = scene.getWindow();
		window.setHeight(150);
//		window.setWidth(400);
		stage.show();

		controller.openBrowser(this, 3000);
	}

	@Override
	public void stop() throws Exception {
		controller.shutdown();

		super.stop();
	}

}