package org.openforis.collect.controlpanel;

import java.awt.Color;
import java.awt.Desktop;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openforis.collect.controlpanel.server.CollectJettyServer;
import org.openforis.utils.Files;
import org.openforis.web.server.ApplicationServer;

public class CollectControlPanelController {

	private static final Logger LOG = LogManager.getLogger(CollectControlPanelController.class);

	private static final String COLLECT_USER_HOME_LOCATION = Files.getLocation(Files.getUserHomeLocation(), "OpenForis",
			"Collect");
	private static final String COLLECT_DATA_FOLDER_NAME = "data";
//	private static final String LOGS_LOCATION = Files.getLocation(Files.getCurrentLocation(), "logs");
//	private static final String SERVER_LOG_FILE_LOCATION = Files.getLocation(LOGS_LOCATION, "collect_server.log");
//	private static final String COLLECT_LOG_FILE_LOCATION = Files.getLocation(LOGS_LOCATION, "collect.log");
//	private static final String SAIKU_LOG_FILE_LOCATION = Files.getLocation(LOGS_LOCATION, "saiku.log");
	private static final String SETTINGS_FILENAME = "collect.properties";
	private static final String SETTINGS_FILE_LOCATION = Files.getLocation(COLLECT_USER_HOME_LOCATION,
			SETTINGS_FILENAME);
	private static final String SETTINGS_FILE_LOCATION_DEV = Files.getLocation(Files.getCurrentLocation(),
			SETTINGS_FILENAME);
	private static final String DEFAULT_WEBAPPS_FOLDER_NAME = "webapps";
	private static final String DEFAULT_WEBAPPS_LOCATION = Files.getLocation(Files.getCurrentLocation(),
			DEFAULT_WEBAPPS_FOLDER_NAME);
//	private static final int LOG_OPENED_WINDOW_HEIGHT = 580;
//	private static final int LOG_CLOSED_WINDOW_HEIGHT = 260;
//	private static final int LOG_TEXT_MAX_LENGTH = 20000;
	private static final String CATALINA_BASE = "catalina.base";
	private static final URI ONLINE_MANUAL_URI;
	private static final URI CHANGELOG_URI;
	static {
		try {
			ONLINE_MANUAL_URI = new URI("http://www.openforis.org/tools/collect.html");
			CHANGELOG_URI = new URI("https://github.com/openforis/collect/blob/master/CHANGELOG.md");
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	private static final String ERROR_MSG_FORMAT = "An error has occurred: %s\nOpen Log for more details";
	private static final String ERROR_SHUTTING_DOWN_MSG_FORMAT = "Error shutting down Collect: %s";

	public enum Status {
		INITIALIZING, STARTING, RUNNING, STOPPING, ERROR, IDLE;
	}

	// ui elements
	private ControlPanel controlPanel;
	private ApplicationServer server;
	private ScheduledExecutorService executorService;

	private String webappsLocation;
	private Status status = Status.INITIALIZING;
	private String errorMessage;
//	private boolean logOpened = false;
//	private ConsoleLogFileReader serverLogFileReader;
//	private ConsoleLogFileReader collectLogFileReader;
//	private ConsoleLogFileReader saikuLogFileReader;

	public void init() {
		LOG.info("initializing control panel");
		try {
			executorService = Executors.newScheduledThreadPool(5);

			File collectHomeFolder = new File(COLLECT_USER_HOME_LOCATION);
			if (!collectHomeFolder.exists()) {
				initializeCollectHomeFolder();
				CollectProperties collectProperties = new CollectProperties();
				new CollectPropertiesHandler().write(collectProperties, new File(collectHomeFolder, SETTINGS_FILENAME));
			}
			CollectProperties collectProperties = loadProperties();
			webappsLocation = collectProperties.getWebappsLocation();
			if (webappsLocation == null || webappsLocation.isEmpty()) {
				webappsLocation = DEFAULT_WEBAPPS_LOCATION;
			}
			File webappsFolder = new File(webappsLocation);

			deleteBrokenTemporaryFiles();

			// if running on a Jetty server, set catalina.base system property to current
			// location
			// to prevent Saiku from storing log files in a wrong location
			if (System.getProperty(CATALINA_BASE) == null) {
				System.setProperty(CATALINA_BASE, Files.getCurrentLocation());
			}

			server = new CollectJettyServer(collectProperties.getHttpPort(), webappsFolder,
					collectProperties.getCollectDataSourceConfiguration());
			server.initialize();

//			initLogFileReaders();

			controlPanel.getUrlHyperlink().setUri(new URI(server.getUrl()));
		} catch (Exception e) {
			LOG.error("error initializing Collect: " + e.getMessage(), e);
			throw new RuntimeException(e);
		}
	}

	private void initializeCollectHomeFolder() {
		File collectHomeFolder = new File(COLLECT_USER_HOME_LOCATION);
		createFolder(collectHomeFolder);
		createFolder(new File(collectHomeFolder, COLLECT_DATA_FOLDER_NAME));
	}

	/**
	 * Use a rolling file output to log information
	 * 
	 * @throws IOException
	 */
//	private void initLogFileReaders() throws IOException {
//		this.serverLogFileReader = new ConsoleLogFileReader(new File(SERVER_LOG_FILE_LOCATION), serverConsole);
//		this.collectLogFileReader = new ConsoleLogFileReader(new File(COLLECT_LOG_FILE_LOCATION), collectConsole);
//		this.saikuLogFileReader = new ConsoleLogFileReader(new File(SAIKU_LOG_FILE_LOCATION), saikuConsole);
//
//		// write logging info to console
//		executorService.scheduleWithFixedDelay(() -> {
//			Platform.runLater(() -> {
//				serverLogFileReader.readFile();
//				collectLogFileReader.readFile();
//				saikuLogFileReader.readFile();
//			});
//		}, 3, 3, TimeUnit.SECONDS);
//	}

	public void startServer() {
		startServer((Runnable) null);
	}

	public void startServer(Runnable onComplete) {
		executorService.schedule(() -> {
			changeStatus(Status.STARTING);

			try {
				server.start();

				changeStatus(Status.RUNNING);

				if (onComplete != null) {
					onComplete.run();
				}
			} catch (Exception e) {
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

				waitUntil(() -> changeStatus(Status.IDLE), (Void) -> server.isRunning(), 1000);
			} catch (Exception e) {
				handleException(e);
			}
		}
	}

	public void stop() throws Exception {
		stopServer();
		executorService.shutdownNow();
	}

	public void shutdown() {
		try {
			stop();
			exit();
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, String.format(ERROR_SHUTTING_DOWN_MSG_FORMAT, e.getMessage()));
			LOG.error(e);
		}
	}

	private void exit() {
		System.exit(0);
	}

	public void openCollectInBrowser() {
		String url = server.getUrl();
		try {
			openWebpage(new URI(url));
		} catch (Exception e) {
			// ignore it
		}
	}

//	@FXML
//	public void toggleLog(MouseEvent event) {
//		setLogVisible(!logOpened);
//	}

	public void handleShowOnlineManual() {
		openWebpage(ONLINE_MANUAL_URI);
	}

	public void handleShowChangelog() {
		openWebpage(CHANGELOG_URI);
	}

//	public void handleAboutAction(ActionEvent event) throws IOException {
//		FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("about_dialog.fxml"));
//		Parent parent = fxmlLoader.load();
//		AboutController aboutController = fxmlLoader.getController();
//		aboutController.setHostServices(app.getHostServices());
//
//		Scene scene = new Scene(parent, 300, 200);
//		Stage dialogStage = new Stage();
//		dialogStage.setTitle("About");
//		dialogStage.initModality(Modality.APPLICATION_MODAL);
//		dialogStage.setScene(scene);
//		dialogStage.showAndWait();
//	}

	public void handleExitAction() {
		switch (status) {
		case INITIALIZING:
		case STARTING:
			break;
		default:
			int confirmResult = JOptionPane.showConfirmDialog(null, "Shutdown Collect?", "Confirm",
					JOptionPane.YES_NO_OPTION);
			if (confirmResult == JOptionPane.YES_OPTION) {
				if (status == Status.RUNNING || status == Status.ERROR) {
					shutdown();
				} else {
					exit();
				}
			}
		}
	}

//	public void closeLog() {
//		setLogVisible(false);
//	}

//	private void setLogVisible(boolean visible) {
//		logOpened = visible;
//		logBtn.setText(visible ? "Hide Log" : "Show Log");
//		updateUI();
//	}

	private void updateUI() {
//		int windowHeight = this.logOpened ? LOG_OPENED_WINDOW_HEIGHT : LOG_CLOSED_WINDOW_HEIGHT;
//		Window window = applicationPane.getScene().getWindow();
//		window.setHeight(windowHeight);

		boolean runningAtUrlVisible = false;
		boolean errorMessageVisible = false;
		boolean shutdownBtnVisible = false;
		boolean progressBarVisible = false;
		String detailedErrorMessage = null;
		String statusMessage = null;
		Color statusMessageColor = Color.ORANGE;

		switch (status) {
		case INITIALIZING:
			statusMessage = "Initializing...";
			progressBarVisible = true;
			break;
		case STARTING:
			statusMessage = "Starting up...";
			progressBarVisible = true;
			break;
		case RUNNING:
			statusMessage = "Running!";
			runningAtUrlVisible = true;
			shutdownBtnVisible = true;
			statusMessageColor = Color.BLUE;
			break;
		case STOPPING:
			statusMessage = "Shutting down...";
			break;
		case IDLE:
			break;
		case ERROR:
			statusMessage = "Error";
			detailedErrorMessage = String.format(ERROR_MSG_FORMAT, errorMessage);
			errorMessageVisible = true;
			statusMessageColor = Color.RED;
			break;
		default:
			break;
		}
		controlPanel.getRunningAtUrlBox().setVisible(runningAtUrlVisible);
		controlPanel.getShutdownBtn().setVisible(shutdownBtnVisible);
		controlPanel.getErrorMessageTxt().setText(detailedErrorMessage);
		controlPanel.getErrorMessageTxt().setVisible(errorMessageVisible);
		controlPanel.getStatusTxt().setText(statusMessage);
		controlPanel.getStatusTxt().setForeground(statusMessageColor);
		controlPanel.getProgressBar().setVisible(progressBarVisible);
//		serverConsole.setVisible(logOpened);
	}

	private void handleException(Exception e) {
		e.printStackTrace();
		errorMessage = e.getMessage();
		changeStatus(Status.ERROR);
	}

	private void waitUntil(Runnable runnable, Predicate<Void> sleepConditionVerifier, int sleepInterval) {
		while (sleepConditionVerifier.test(null)) {
			try {
				Thread.sleep(sleepInterval);
			} catch (InterruptedException e) {
			}
		}
		SwingUtilities.invokeLater(runnable);
	}

	private CollectProperties loadProperties() throws IOException {
		Properties properties = new Properties();
		String[] possibleLocations = new String[] { SETTINGS_FILE_LOCATION_DEV, SETTINGS_FILE_LOCATION };
		File propertiesFile = null;
		for (String location : possibleLocations) {
			propertiesFile = new File(location);
			if (propertiesFile.exists()) {
				break;
			}
		}
		if (!propertiesFile.exists()) {
			throw new IllegalStateException(String.format("Cannot find %s file", SETTINGS_FILENAME));
		}
		FileInputStream is = new FileInputStream(propertiesFile);
		properties.load(is);
		return new CollectPropertiesHandler().parse(properties);
	}

	private void deleteBrokenTemporaryFiles() throws IOException {
		File webappsFolder = new File(webappsLocation);
		File collectWebappFolder = new File(webappsFolder, CollectJettyServer.WEBAPP_NAME);
		if (collectWebappFolder.exists() && collectWebappFolder.isDirectory()) {
			String[] folderContent = collectWebappFolder.list();
			if (folderContent.length == 0 || !Arrays.<String>asList(folderContent).contains("index.html")) {
				LOG.info("deleting empty Collect webapps folder");
				try {
					FileUtils.forceDelete(collectWebappFolder);
					LOG.info("Collect webapps folder deleted successfully");
				} catch (IOException e) {
					String message = String.format(
							"Error deleting folder %s: %s. Please delete it manually and start Collect again",
							collectWebappFolder.getAbsolutePath(), e.getMessage());
					throw new IOException(message, e);
				}
			}
		}

	}

	public Status getStatus() {
		return status;
	}

	public void setControlPanel(ControlPanel controlPanel) {
		this.controlPanel = controlPanel;
	}

	static void closeQuietly(Closeable closeable) {
		if (closeable != null) {
			try {
				closeable.close();
			} catch (IOException e) {
			}
		}
	}

	private static void createFolder(File folder) {
		if (!folder.mkdirs()) {
			throw new RuntimeException(String.format("Cannot create folder: %s", folder.getAbsolutePath()));
		}
	}

//	private static class ConsoleLogFileReader {
//		private File file;
//		private TextArea textArea;
//
//		public ConsoleLogFileReader(File file, TextArea textArea) {
//			this.file = file;
//			this.textArea = textArea;
//		}
//
//		public void readFile() {
//			String content = Files.tail(file, LOG_TEXT_MAX_LENGTH);
//			String oldContent = textArea.getText();
//			if (!content.equals(oldContent)) {
//				textArea.setText(content);
//				textArea.setScrollTop(Double.MAX_VALUE);
//			}
//		}
//	}

	public static void openWebpage(URI uri) {
		try {
			Desktop.getDesktop().browse(uri);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
