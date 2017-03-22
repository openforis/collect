package org.openforis.collect.controlpanel;

import java.io.InputStream;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public class CollectControlPanel extends Application {

	private static final int BROWSER_OPEN_DELAY = 3000;
	private static final String CONTROL_PANEL_FXML = "collect_control_panel.fxml";
	private static final String TITLE = "Open Foris Collect - Control Panel";

	private CollectControlPanelController controller;

	public static void main(String[] args) {
		launch();
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		FXMLLoader fxmlLoader = new FXMLLoader();
		Pane pane = (Pane) fxmlLoader.load(getClass().getResourceAsStream(CONTROL_PANEL_FXML));
		Scene scene = new Scene(pane);
		primaryStage.setScene(scene);
		primaryStage.setTitle(TITLE);
		primaryStage.setResizable(false);

		setLogo(primaryStage);

		//initialize controller
		controller = fxmlLoader.getController();
		controller.setApp(this);
		controller.closeLog();
		
		controller.startServer(() -> {
			controller.openBrowser(BROWSER_OPEN_DELAY);
		});
		
		primaryStage.show();
	}

	private void setLogo(Stage primaryStage) {
		InputStream logoIs = this.getClass().getClassLoader().getResourceAsStream("org/openforis/collect/controlpanel/of-collect-logo.png");
		Image logo = new Image(logoIs);
		primaryStage.getIcons().add(logo);
	}
	
	@Override
	public void stop() throws Exception {
		super.stop();
		controller.stop();
	}

}