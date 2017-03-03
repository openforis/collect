package org.openforis.collect.controlpanel;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.stage.Window;

@SuppressWarnings("restriction")
public class CollectControlPanel extends Application {

	private CollectControlPanelController controller;

	private String title = "Open Foris Collect - Control Panel";
	
	public static void main(String[] args) {
		launch();
	}

	@Override
	public void start(Stage stage) throws Exception {
		stage.setTitle( title );
		stage.setResizable( false );
		
		FXMLLoader fxmlLoader = new FXMLLoader();
		Pane pane = (Pane) fxmlLoader.load( getClass().getResource( "calc_control_panel.fxml" ).openStream() );
		
		controller = fxmlLoader.getController();
		controller.startServer();
			
		Scene scene = new Scene( pane );
		stage.setScene( scene );
		Window window = scene.getWindow();
		window.setHeight( 150 );
		stage.show();
		
		controller.openBrowser( this , 3000 );
	}
	
	@Override
	public void stop() throws Exception {
		controller.shutdown();
		
		super.stop();
	}
}
