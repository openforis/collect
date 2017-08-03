package org.openforis.collect.controlpanel;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.Scanner;
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
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Window;

public class CollectControlPanelController implements Initializable {

	private static final String COLLECT_USER_HOME_LOCATION = Files.getLocation(Files.getUserHomeLocation(), "OpenForis", "Collect");
	private static final String LOGS_LOCATION = Files.getLocation(Files.getCurrentLocation(), "logs");
	private static final String SERVER_LOG_FILE_LOCATION = Files.getLocation(LOGS_LOCATION, "collect_server.log");
	private static final String COLLECT_LOG_FILE_LOCATION = Files.getLocation(LOGS_LOCATION, "collect.log");
	private static final String SAIKU_LOG_FILE_LOCATION = Files.getLocation(LOGS_LOCATION, "saiku.log");
	private static final String SETTINGS_FILENAME = "collect.properties";
	private static final String SETTINGS_FILE_LOCATION = Files.getLocation(COLLECT_USER_HOME_LOCATION, SETTINGS_FILENAME);
	private static final String SETTINGS_FILE_LOCATION_DEV = Files.getLocation(Files.getCurrentLocation(), SETTINGS_FILENAME);
	private static final String DEFAULT_WEBAPPS_FOLDER_NAME = "webapps";
	private static final String DEFAULT_WEBAPPS_LOCATION = Files.getLocation(Files.getCurrentLocation(), DEFAULT_WEBAPPS_FOLDER_NAME);
	private static final int LOG_OPENED_WINDOW_HEIGHT = 550;
	private static final int LOG_CLOSED_WINDOW_HEIGHT = 210;
	private static final int LOG_TEXT_MAX_LENGTH = 5000;
	private static final String CATALINA_BASE = "catalina.base";
	
	public enum Status {
		INITIALIZING, STARTING, RUNNING, STOPPING, ERROR, IDLE;
	}
	
	//ui elements
	@FXML
	private Pane applicationPane;
	@FXML
	private Button logBtn;
	@FXML
	private Button shutdownBtn;
	@FXML
	public TextArea serverConsole;
	@FXML
	public TextArea collectConsole;
	@FXML
	public TextArea saikuConsole;
	@FXML
	public Hyperlink urlHyperlink;
	@FXML
	public Text statusTxt;
	@FXML
	public Text errorMessageTxt;
	@FXML
	private VBox runningAtUrlBox;
		
	private CollectControlPanel app;
	private ApplicationServer server;
	private ScheduledExecutorService executorService;
	
	private String webappsLocation;
	private Status status = Status.INITIALIZING;
	private String errorMessage;
	private boolean logOpened = false;
	private FileLinesProcessor serverLogFileLinesProcessor;
	private FileLinesProcessor collectLogFileLinesProcessor;
	private FileLinesProcessor saikuLogFileLinesProcessor;
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		try {
			executorService = Executors.newScheduledThreadPool(5);

			CollectProperties collectProperties = new CollectPropertiesParser().parse(loadProperties());
			webappsLocation = collectProperties.getWebappsLocation();
			if (webappsLocation == null || webappsLocation.isEmpty()) {
				webappsLocation = DEFAULT_WEBAPPS_LOCATION;
			}
			File webappsFolder = new File(webappsLocation);
			File serverLogFile = new File(SERVER_LOG_FILE_LOCATION);

			//if running on a Jetty server, set catalina.base system property to current location
			//to prevent Saiku from storing log files in a wrong location
			if (System.getProperty(CATALINA_BASE) == null) {
				System.setProperty(CATALINA_BASE, Files.getCurrentLocation());
			}
			
			server = new CollectJettyServer(collectProperties.getHttpPort(),
					webappsFolder, serverLogFile,
					collectProperties.getCollectDataSourceConfiguration());
			server.initialize();
			
			initLogFileReaders();
			
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
	private void initLogFileReaders() throws IOException {
		this.serverLogFileLinesProcessor = new ConsoleLogFileProcessor(
				server.getLogFile(), serverConsole);
		this.collectLogFileLinesProcessor = new ConsoleLogFileProcessor(
				new File(COLLECT_LOG_FILE_LOCATION), collectConsole);
		this.saikuLogFileLinesProcessor = new ConsoleLogFileProcessor(
				new File(SAIKU_LOG_FILE_LOCATION), saikuConsole);
		
		//write logging info to console
		executorService.scheduleWithFixedDelay(() -> {
			Platform.runLater(() -> {
				serverLogFileLinesProcessor.processNextLines();
				collectLogFileLinesProcessor.processNextLines();
				saikuLogFileLinesProcessor.processNextLines();
			});
		}, 3, 3, TimeUnit.SECONDS);
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
		if (server == null) {
			changeStatus(Status.ERROR);
		} else {
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
	}

	void stop() throws Exception {
		stopServer();
		executorService.shutdownNow();
		closeQuietly(serverLogFileLinesProcessor);
		closeQuietly(collectLogFileLinesProcessor);
		closeQuietly(saikuLogFileLinesProcessor);
	}
	
	void closeQuietly(Closeable closeable) {
		if (closeable != null) {
			try {
				closeable.close();
			} catch (IOException e) {
			}
		}
	}
	
	@FXML
	void openBrowserFromLink(MouseEvent event) {
		openBrowser();
	}

	@FXML
	void shutdown(MouseEvent event) throws Exception {
		stop();
		Platform.exit();
	}
	
	void openBrowser() {
		HostServicesDelegate hostServices = HostServicesFactory.getInstance(app);
		String url = server.getUrl();
		hostServices.showDocument(url);
	}
	
	@FXML
	public void toggleLog( MouseEvent event ) {
		setLogVisible(! logOpened);
	}
	
	public void closeLog() {
		setLogVisible(false);
	}

	private void setLogVisible(boolean visible) {
		logOpened = visible;
		logBtn.setText(visible ? "Hide Log" : "Show Log");
		updateUI();
	}
	
	private void updateUI() {
		int windowHeight = this.logOpened ? LOG_OPENED_WINDOW_HEIGHT : LOG_CLOSED_WINDOW_HEIGHT;
		Window window = applicationPane.getScene().getWindow();
		window.setHeight(windowHeight);
		
		boolean runningAtUrlVisible = false;
		boolean errorMessageVisible = false;
		boolean shutdownBtnVisible = false;
		String detailedErrorMessage = null;
		String statusMessage = null;
		String statusMessageClassName = "info";
		switch(status) {
		case INITIALIZING:
			statusMessage = "Initializing...";
			break;
		case STARTING:
			statusMessage = "Starting up...";
			break;
		case RUNNING:
			statusMessage = "Running!";
			runningAtUrlVisible = true;
			shutdownBtnVisible = true;
			break;
		case STOPPING:
			statusMessage = "Shutting down...";
			break;
		case IDLE:
			break;
		case ERROR:
			statusMessage = "Error";
			detailedErrorMessage = String.format("An error has occurred: %s\n"
					+ "Open Log for more detals", errorMessage);
			errorMessageVisible = true;
			break;
		default:
			break;
		}
		statusMessageClassName = status.name().toLowerCase();
		runningAtUrlBox.setVisible(runningAtUrlVisible);
		shutdownBtn.setVisible(shutdownBtnVisible);
		errorMessageTxt.setText(detailedErrorMessage);
		errorMessageTxt.setVisible(errorMessageVisible);
		statusTxt.setText(statusMessage);
		statusTxt.getStyleClass().clear();
		statusTxt.getStyleClass().add(statusMessageClassName);
		serverConsole.setVisible(logOpened);
	}
	
	private void handleException(Exception e) {
		e.printStackTrace();
		errorMessage = e.getMessage();
		changeStatus(Status.ERROR);
	}

	private void waitUntilConditionIsVerifiedThenRun(Runnable runnable, Verifier sleepConditionVerifier, int sleepInterval) {
		while (sleepConditionVerifier.verify()) {
			try {
				Thread.sleep(sleepInterval);
			} catch (InterruptedException e) {}
		}
		Platform.runLater(runnable);
	}
	
	private Properties loadProperties() throws IOException {
		Properties properties = new Properties();
		String[] possibleLocations = new String[]{
			SETTINGS_FILE_LOCATION, 
			SETTINGS_FILE_LOCATION_DEV
		};
		File propertiesFile = null;
		for (String location : possibleLocations) {
			propertiesFile = new File(location);
			if (propertiesFile.exists()) {
				break;
			}
		}
		if (! propertiesFile.exists()) {
			throw new IllegalStateException(String.format("Cannot find %s file", SETTINGS_FILENAME));
		}
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
	 * Reads a file and process it's content with a TextProcessor
	 * 
	 * @author S. Ricci
	 *
	 */
	private static class FileLinesProcessor implements Closeable {

		private File file;
		private TextProcessor lineProcessor;
		private int readLines;
		private Scanner scanner;

		public FileLinesProcessor(File file, TextProcessor lineProcessor) {
			this.file = file;
			this.lineProcessor = lineProcessor;
		}

		public void processNextLines() {
			try {
				if (scanner == null) {
					initScanner();
				}
				int count = 0;
				while (scanner.hasNextLine() && count < readLines) {
					scanner.nextLine();
					count ++;
				}
				//process only new lines
				while (scanner.hasNextLine()) {
					String line = scanner.nextLine();
					lineProcessor.process(line);
					readLines ++;
				}
			} catch (FileNotFoundException e) {
				//ignore it, file not ready
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		private void initScanner() throws FileNotFoundException {
			this.scanner = new Scanner(this.file);
		}

		@Override
		public void close() throws IOException {
			if (scanner != null) {
				scanner.close();
			}
		}
	}
	
	private static class ConsoleLogFileProcessor extends FileLinesProcessor {

		public ConsoleLogFileProcessor(File file, TextArea textArea) {
			super(file, text -> {
				textArea.appendText(text);
				textArea.appendText("\n");
				int extraCharacters = textArea.getLength() - LOG_TEXT_MAX_LENGTH;
				if (extraCharacters > 0) {
					textArea.deleteText(0, extraCharacters);
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
