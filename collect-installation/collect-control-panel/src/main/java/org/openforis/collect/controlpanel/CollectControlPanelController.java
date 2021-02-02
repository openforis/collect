package org.openforis.collect.controlpanel;

import java.awt.Color;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
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
import org.openforis.utils.Browser;
import org.openforis.utils.Files;
import org.openforis.web.server.ApplicationServer;

public class CollectControlPanelController {

	private static final Logger LOG = LogManager.getLogger(CollectControlPanelController.class);

	private static final String COLLECT_USER_HOME_LOCATION = Files.getLocation(Files.getUserHomeLocation(), "OpenForis",
			"Collect");
	private static final String COLLECT_DATA_FOLDER_NAME = "data";
	private static final String SETTINGS_FILENAME = "collect.properties";
	private static final String SETTINGS_FILE_LOCATION = Files.getLocation(COLLECT_USER_HOME_LOCATION,
			SETTINGS_FILENAME);
	private static final String SETTINGS_FILE_LOCATION_DEV = Files.getLocation(Files.getCurrentLocation(),
			SETTINGS_FILENAME);
	private static final String DEFAULT_WEBAPPS_FOLDER_NAME = "webapps";
	private static final String DEFAULT_WEBAPPS_LOCATION = Files.getLocation(Files.getCurrentLocation(),
			DEFAULT_WEBAPPS_FOLDER_NAME);
	private static final String CATALINA_BASE = "catalina.base";

	private static final String ERROR_MSG_FORMAT = "An error has occurred: %s\nOpen Log for more details";
	private static final String ERROR_SHUTTING_DOWN_MSG_FORMAT = "Error shutting down Collect: %s";

	public enum Status {
		INITIALIZING, STARTING, RUNNING, STOPPING, ERROR, IDLE;
	}

	private ControlPanel controlPanel;
	private ApplicationServer server;
	private ScheduledExecutorService executorService;

	private String webappsLocation;
	private Status status = Status.INITIALIZING;
	private String errorMessage;

	public CollectControlPanelController(ControlPanel controlPanel) {
		this.controlPanel = controlPanel;
	}
	
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

			/*
			 * if running on a Jetty server, set catalina.base system property to current
			 * location to prevent Saiku from storing log files in a wrong location
			 */
			if (System.getProperty(CATALINA_BASE) == null) {
				System.setProperty(CATALINA_BASE, Files.getCurrentLocation());
			}

			server = new CollectJettyServer(collectProperties.getHttpPort(), webappsFolder,
					collectProperties.getCollectDataSourceConfiguration());
			server.initialize();

			controlPanel.getUrlHyperlink().setUrl(server.getUrl());
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
		Browser.openPage(server.getUrl());
	}

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

	private void updateUI() {
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

	private static void createFolder(File folder) {
		if (!folder.mkdirs()) {
			throw new RuntimeException(String.format("Cannot create folder: %s", folder.getAbsolutePath()));
		}
	}

}
