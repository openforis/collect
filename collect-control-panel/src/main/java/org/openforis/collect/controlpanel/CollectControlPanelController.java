package org.openforis.collect.controlpanel;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;
import java.util.ResourceBundle;

import org.openforis.collect.controlpanel.CollectServer.WebAppConfiguration;

import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.stage.Window;

public class CollectControlPanelController implements Initializable {

	// ui elements
	@FXML
	private Button startBtn;
	@FXML
	private Button stopBtn;
	@FXML
	private Button logBtn;
	@FXML
	public TextArea console;
	@FXML
	private Pane applicationPane;
		
	private CollectServer server;

	private boolean logOpened = false;
	private double windowHeight;
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		try {
			Properties properties = loadProperties();
			String portStr = properties.getProperty("http_port");
			String collectWarFileLocation = properties.getProperty("collect_war_file_location");
			String collectContext = properties.getProperty("collect_context");
			String saikuWarFileLocation = properties.getProperty("saiku_war_file_location");
			String saikuContext = properties.getProperty("saiku_context");
			
			int port = Integer.parseInt(portStr);
			
			server = new CollectServer(port, collectContext, 
					new WebAppConfiguration(collectWarFileLocation, collectContext), 
					new WebAppConfiguration(saikuWarFileLocation, saikuContext));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private Properties loadProperties() throws IOException {
		Properties properties = new Properties();
		InputStream propertiesIs = getClass().getClassLoader().getResourceAsStream("collect.properties");
		properties.load(propertiesIs);
		return properties;
	}
	
	public void startServer() throws Exception {
		server.start();
	}
	
	public void stopServer() throws Exception {
		server.stop();
	}
	
	void shutdown() throws Exception {
		stopServer();
//		executorService.shutdownNow();
	}
	
	void openBrowser( Application application , final long delay ) {
		
//		final HostServicesDelegate hostServices = HostServicesFactory.getInstance( application );
	}
	
	@FXML
	public void toggleLog( MouseEvent event ) {
		Window window = applicationPane.getScene().getWindow();
		
		if( this.logOpened ){
			window.setHeight( 150 );
			this.logOpened = false;
		} else {
			window.setHeight( windowHeight );
			this.logOpened = true;
		}
	}

}
