package org.openforis.collect.controlpanel;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.URL;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.TimeZone;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.eclipse.jetty.util.RolloverFileOutputStream;
import org.eclipse.jetty.util.log.Log;
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

@SuppressWarnings("restriction")
public class CollectControlPanelController implements Initializable {

	private static final String LOG_FILENAME = "collect.log";
	private static final String LOGS_FOLDER_NAME = "logs";
	private static final String SETTINGS_FILENAME = "collect.properties";
	private static final int WINDOW_TOTAL_HEIGHT = 550;
	
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
	private int logFileReadLine = 0;
	private double windowHeight = WINDOW_TOTAL_HEIGHT;
	private ScheduledExecutorService executorService;
	
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
			
			server = new CollectServer(port,
					new WebAppConfiguration(collectWarFileLocation, collectContext), 
					new WebAppConfiguration(saikuWarFileLocation, saikuContext));
			
			executorService = Executors.newScheduledThreadPool( 5 );
			
			initializeLogger();
			
			Log.getRootLogger().info("Test");
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Use a rolling file output to log information
	 * 
	 * @throws IOException
	 */
	private void initializeLogger() throws IOException {
		String relativePathLogFilename = "./" + LOGS_FOLDER_NAME +"/" + LOG_FILENAME;
		RolloverFileOutputStream os = new RolloverFileOutputStream(relativePathLogFilename, false, 90,
				TimeZone.getTimeZone("GMT"), "yyyy_MM_dd", "yyyy_MM_dd_hhmm");
		
		PrintStream logStream = new PrintStream(os);

		System.setOut(logStream);
		System.setErr(logStream);
		File logFile = new File(System.getProperty("user.dir") + "/" + LOGS_FOLDER_NAME +"/" + LOG_FILENAME);
		
		//write logging info to console
		executorService.scheduleWithFixedDelay(new LogFileReader(logFile, (String text) -> {
			console.appendText(text);
			console.appendText("\n");
		}), 1, 1, TimeUnit.SECONDS);
	}
	
	public void startServer() throws Exception {
		startBtn.setDisable(true);
		stopBtn.setDisable(true);
		
		server.start();
		
		//enable stop button after 5 seconds
		runAfter(() -> {
			stopBtn.setDisable(false);
		}, 5000);
	}

	public void stopServer() throws Exception {
		stopBtn.setDisable(true);
		server.stop();
		
		// enable start button once server is down
		waitThenRun(() -> {
			startBtn.setDisable(false);
		}, () -> {
			return server.isRunning();
		}, 1000);
	}

	private void waitThenRun(Runnable runnable, Verifier sleepConditionVerifier, int sleepInterval) {
		executorService.schedule(() -> {
			while (sleepConditionVerifier.verify()) {
				try {
					Thread.sleep(sleepInterval);
				} catch (InterruptedException e) {}
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
				hostServices.showDocument(url);
			} catch ( Exception e ) {
				e.printStackTrace();
			}
		});
	}
	
	@FXML
	public void toggleLog( MouseEvent event ) {
		Window window = applicationPane.getScene().getWindow();
		
		if (this.logOpened) {
			window.setHeight(150);
			this.logOpened = false;
		} else {
			window.setHeight(windowHeight);
			this.logOpened = true;
		}
	}
	
	private Properties loadProperties() throws IOException {
		Properties properties = new Properties();
		InputStream propertiesIs = getClass().getClassLoader().getResourceAsStream(SETTINGS_FILENAME);
		properties.load(propertiesIs);
		return properties;
	}
	
	private void runAfter(Runnable runnable, int delay) {
		executorService.schedule( new Runnable() {
			public void run() {
				try {
					Thread.sleep(delay);
				} catch (InterruptedException e) {
				}
				Platform.runLater(runnable);

			}
		}, 0, TimeUnit.SECONDS );
	}
	
	/**
	 * Reads a file and writes it's content into a TextWriter
	 * 
	 * @author S. Ricci
	 *
	 */
	private class LogFileReader implements Runnable {

		private File file;
		private TextProcessor lineProcessor;

		public LogFileReader(File file, TextProcessor lineProcessor) {
			this.file = file;
			this.lineProcessor = lineProcessor;
		}

		@Override
		public void run() {
			Platform.runLater(() -> {
				try {
					FileInputStream inputStream = new FileInputStream(file);
					@SuppressWarnings("resource")
					BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
					for (int i = 0; i < logFileReadLine; i++) {
						br.readLine();
					}

					String line = null;

					while ((line = br.readLine()) != null) {
						lineProcessor.process(line);
						logFileReadLine ++;
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			});

		}
	}
	
	private interface TextProcessor {
		void process(String text);
	}
	
	private interface Verifier {
		boolean verify();
	}
	
}
