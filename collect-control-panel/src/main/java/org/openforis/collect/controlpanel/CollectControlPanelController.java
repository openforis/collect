package org.openforis.collect.controlpanel;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.openforis.utils.Files;
import org.openforis.web.server.ApplicationServer;

import com.sun.deploy.uitoolkit.impl.fx.HostServicesFactory;
import com.sun.javafx.application.HostServicesDelegate;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.TextArea;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import javafx.stage.Window;

@SuppressWarnings("restriction")
public class CollectControlPanelController implements Initializable {

	private static final String SETTINGS_FILENAME = "collect.properties";
	private static final String LOG_FILE_LOCATION = Files.getLocation(Files.getUserHomeLocation(), "OpenForis", "Collect", "logs", "collect.log");
	private static final String DEFAULT_WEBAPPS_LOCATION = Files.getLocation(Files.getCurrentLocation(), "webapps");
	private static final int LOG_OPENED_WINDOW_HEIGHT = 550;
	private static final int LOG_CLOSED_WINDOW_HEIGHT = 200;
	public enum Status {
		INITIALIZING, STARTING, RUNNING, STOPPING, ERROR, IDLE;
	}
	
	//ui elements
	@FXML
	private Pane applicationPane;
	@FXML
	private Button startBtn;
	@FXML
	private Button stopBtn;
	@FXML
	private Button logBtn;
	@FXML
	public TextArea console;
	@FXML
	public Hyperlink urlHyperlink;
	@FXML
	public Text infoTxt;
	@FXML
	private HBox runningAtUrlBox;
		
	private CollectControlPanel app;
	private ApplicationServer server;
	private ScheduledExecutorService executorService;
	
	private Status status = Status.INITIALIZING;
	private String errorMessage;
	private boolean logOpened = false;
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		try {
			executorService = Executors.newScheduledThreadPool(5);

			CollectProperties collectProperties = new CollectPropertiesParser().parse(loadProperties());
			String webappsLocation = collectProperties.getWebappsLocation();
			if (webappsLocation == null || webappsLocation.isEmpty()) {
				webappsLocation = DEFAULT_WEBAPPS_LOCATION;
			}
			File webappsFolder = new File(webappsLocation);
			File logFile = new File(LOG_FILE_LOCATION);
			
			server = new CollectJettyServer(collectProperties.getHttpPort(),
					webappsFolder, logFile,
					collectProperties.getCollectDataSourceConfiguration());
			server.initialize();
			
			initializeLogFileReader();
			
			urlHyperlink.setText(server.getUrl());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Use a rolling file output to log information
	 * 
	 * @throws IOException
	 */
	private void initializeLogFileReader() throws IOException {
		FileLinesProcessor logFileLinesProcessor = new FileLinesProcessor(server.getLogFile(), (String text) -> {
			console.appendText(text);
			console.appendText("\n");
		});
		//write logging info to console
		executorService.scheduleWithFixedDelay(() -> {
			Platform.runLater(() -> {
				logFileLinesProcessor.processNextLines();
			});
		}, 1, 1, TimeUnit.SECONDS);
	}
	
	public void startServer(MouseEvent event) throws Exception {
		startServer((Runnable) null);
	}
	
	public void startServer(Runnable onComplete) throws Exception {
		executorService.schedule(() -> {
			changeStatus(Status.STARTING);
			
			try {
				server.start();
				
				changeStatus(Status.RUNNING);
				
				if (onComplete != null) {
					onComplete.run();
				}
			} catch(Exception e) {
				handleException(e);
			}
		}, 0, TimeUnit.SECONDS);
	}

	private void changeStatus(Status status) {
		this.status = status;
		updateUI();
	}

	public void stopServer() throws Exception {
		changeStatus(Status.STOPPING);

		try {
			server.stop();
		
			waitUntilConditionIsVerifiedThenRun(() -> {
				changeStatus(Status.IDLE);
			}, () -> {
				return server.isRunning();
			}, 1000);
		} catch(Exception e) {
			handleException(e);
		}
	}

	void shutdown() throws Exception {
		stopServer();
		executorService.shutdownNow();
	}
	
	@FXML
	void openBrowserFromLink(MouseEvent event) {
		openBrowser(0);
	}
	
	void openBrowser(final long delay) {
		executorService.submit(() -> {
			try {
				HostServicesDelegate hostServices = HostServicesFactory.getInstance(app);
				Thread.sleep(delay);
				String url = server.getUrl();
				hostServices.showDocument(url);
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
	}
	
	@FXML
	public void toggleLog( MouseEvent event ) {
		setLogVisible(! logOpened);
	}
	
	public void closeLog() {
		setLogVisible(false);
	}

	private void setLogVisible(boolean value) {
		logOpened = value;
		updateUI();
	}
	
	private void updateUI() {
		int windowHeight = this.logOpened ? LOG_OPENED_WINDOW_HEIGHT : LOG_CLOSED_WINDOW_HEIGHT;
		Window window = applicationPane.getScene().getWindow();
		window.setHeight(windowHeight);
		
		boolean runningAtUrlVisible = false;
		boolean infoMessageVisible = true;
		boolean startBtnDisabled = true;
		boolean stopBtnDisabled = true;
		String infoMessage = null;
		switch(status) {
		case INITIALIZING:
			infoMessage = "Initializing...";
			break;
		case STARTING:
			infoMessage = "Starting up...";
			break;
		case RUNNING:
			runningAtUrlVisible = true;
			infoMessage = null;
			infoMessageVisible = false;
			stopBtnDisabled = false;
			break;
		case STOPPING:
			infoMessage = "Shutting down...";
			break;
		case IDLE:
			infoMessage = null;
			infoMessageVisible = false;
			startBtnDisabled = false;
		case ERROR:
			infoMessage = String.format("An error occurred: %s\n"
					+ "Open Log for more detals", errorMessage);
		default:
			break;
		}
		runningAtUrlBox.setVisible(runningAtUrlVisible);
		infoTxt.setText(infoMessage);
		infoTxt.setVisible(infoMessageVisible);
		startBtn.setDisable(startBtnDisabled);
		stopBtn.setDisable(stopBtnDisabled);
		console.setVisible(logOpened);
	}
	
	private void handleException(Exception e) {
		e.printStackTrace();
		errorMessage = e.getMessage();
		changeStatus(Status.ERROR);
	}

	private void waitUntilConditionIsVerifiedThenRun(Runnable runnable, Verifier sleepConditionVerifier, int sleepInterval) {
		executorService.schedule(() -> {
			while (sleepConditionVerifier.verify()) {
				try {
					Thread.sleep(sleepInterval);
				} catch (InterruptedException e) {}
			}
			Platform.runLater(runnable);
		}, 0, TimeUnit.SECONDS);
	}
	
	private Properties loadProperties() throws IOException {
		Properties properties = new Properties();
		String currentLocation = Files.getCurrentLocation();
		File propertiesFile = new File(currentLocation, SETTINGS_FILENAME);
		FileInputStream is = new FileInputStream(propertiesFile);
		properties.load(is);
		return properties;
	}
	
	public void setApp(CollectControlPanel app) {
		this.app = app;
	}
	
	public Status getStatus() {
		return status;
	}

	
	/**
	 * Reads a file and writes it's content into a TextWriter
	 * 
	 * @author S. Ricci
	 *
	 */
	private static class FileLinesProcessor {

		private File file;
		private TextProcessor lineProcessor;
		private int readLines;

		public FileLinesProcessor(File file, TextProcessor lineProcessor) {
			this.file = file;
			this.lineProcessor = lineProcessor;
		}

		public void processNextLines() {
			BufferedReader br = null;
			try {
				FileInputStream inputStream = new FileInputStream(file);
				br = new BufferedReader(new InputStreamReader(inputStream));
				for (int i = 0; i < readLines; i++) {
					br.readLine();
				}
				
				//process only new lines
				String line = null;
				while ((line = br.readLine()) != null) {
					lineProcessor.process(line);
					readLines ++;
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (br != null) {
					try {
						br.close();
					} catch (IOException e) {}
				}
			}
		}
	}
	
	private interface TextProcessor {
		void process(String text);
	}
	
	private interface Verifier {
		boolean verify();
	}
	
}
