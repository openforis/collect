package org.openforis.collect.controlpanel;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.openforis.collect.controlpanel.CollectServer.WebAppConfiguration;

import com.sun.deploy.uitoolkit.impl.fx.HostServicesFactory;
import com.sun.javafx.application.HostServicesDelegate;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.stage.Window;

public class CollectControlPanelController implements Initializable {

	private static final String SETTINGS_PROPERTIES = "collect.properties";
	
	//ui elements
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
	private ScheduledExecutorService executorService;
	private Integer linesRead;
	
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
			
			executorService = Executors.newScheduledThreadPool( 5 );
			
			// logger thread
			this.linesRead = 0;
			//executorService.scheduleWithFixedDelay( new Logging( this ), 1, 1, TimeUnit.SECONDS);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void startServer() throws Exception {
		startBtn.setDisable(true);
		stopBtn.setDisable(true);
		
		server.start();
		
		//enable stop button after 5 seconds
		runLater(5000, () -> {
			stopBtn.setDisable(false);
		});
	}

	public void stopServer() throws Exception {
		stopBtn.setDisable(true);
		server.stop();
		
		// enable start button once server is down
		int interval = 1000;
		Runnable runnable = () -> {
			startBtn.setDisable( false );
		};
		Verifier verifier = () -> {
			return server.isRunning();
		};
		sleepThenRun(runnable, verifier, interval);
	}

	private void sleepThenRun(Runnable runnable, Verifier sleepConditionVerifier, int sleepInterval) {
		executorService.schedule(() -> {
			while (sleepConditionVerifier.verify()) {
				try {
					Thread.sleep(sleepInterval);
				} catch (InterruptedException e) {
				}
			}
			Platform.runLater(runnable);
		}, 0, TimeUnit.SECONDS);
	}
	
	void shutdown() throws Exception {
		stopServer();
		executorService.shutdownNow();
	}
	
	void openBrowser(Application application, final long delay) {
		final HostServicesDelegate hostServices = HostServicesFactory.getInstance( application );
		
		executorService.submit( () -> {
			try {
				Thread.sleep(delay);
				String url = server.getUrl();
				hostServices.showDocument( url );
			} catch ( Exception e ) {
				e.printStackTrace();
			}
		});
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
	
	private Properties loadProperties() throws IOException {
		Properties properties = new Properties();
		InputStream propertiesIs = getClass().getClassLoader().getResourceAsStream(SETTINGS_PROPERTIES);
		properties.load(propertiesIs);
		return properties;
	}
	
	private void runLater(int delay, Runnable runnable) {
		executorService.schedule( new Runnable() {
			public void run() {
				try {
					Thread.sleep( delay );
				} catch ( InterruptedException e ) {
				}
				Platform.runLater(runnable);
				
			}
		}, 0, TimeUnit.SECONDS );
	}
	
	private interface Verifier {
		boolean verify();
	}
	
}
