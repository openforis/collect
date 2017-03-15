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

	private static final String DEFAULT_WEBAPPS_LOCATION = System.getProperty("user.dir") + File.separator + "webapps";

	private static final String LOG_FILENAME = "collect.log";
	private static final String LOGS_FOLDER_NAME = "logs";
	private static final String SETTINGS_FILENAME = "collect.properties";
	private static final int LOG_OPENED_WINDOW_HEIGHT = 550;
	private static final int LOG_CLOSED_WINDOW_HEIGHT = 200;
	
	private enum Status {
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
	private boolean logOpened = false;
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		try {
			CollectProperties collectProperties = new CollectPropertiesParser().parse(loadProperties());
			String webappsLocation = collectProperties.getWebappsLocation();
			if (webappsLocation == null) {
				webappsLocation = DEFAULT_WEBAPPS_LOCATION;
			}
			File webappsFolder = new File(webappsLocation);
			server = new CollectJettyServer(collectProperties.getHttpPort(),
					webappsFolder,
					collectProperties.getCollectDataSourceConfiguration());
			
			urlHyperlink.setText(server.getUrl());
			
			executorService = Executors.newScheduledThreadPool(5);
			
			initializeLogger();
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
		File logFile = new File(System.getProperty("user.dir") + File.separator + LOGS_FOLDER_NAME + File.separator + LOG_FILENAME);
		
		FileLinesProcessor logFileLinesProcessor = new FileLinesProcessor(logFile, (String text) -> {
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
	
	public void startServer() throws Exception {
		changeStatus(Status.STARTING);
		
		server.start();
		
		//enable stop button after 5 seconds
		runAfter(() -> {
			changeStatus(Status.RUNNING);
		}, 5000);
	}

	private void changeStatus(Status status) {
		this.status = status;
		updateUI();
	}

	public void stopServer() throws Exception {
		changeStatus(Status.STOPPING);

		server.stop();
		
		// enable start button once server is down
		waitThenRun(() -> {
			changeStatus(Status.IDLE);
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
	
	@FXML
	void openBrowserFromLink(MouseEvent event) {
		openBrowser(0);
	}
	
	void openBrowser(final long delay) {
		final HostServicesDelegate hostServices = HostServicesFactory.getInstance(app);
		
		executorService.submit(() -> {
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
		setLogVisible(! logOpened);
	}
	
	public void closeLog() {
		setLogVisible(false);
	}

	private void setLogVisible(boolean value) {
		logOpened = value;
		updateUI();
	}
	
	public void updateUI() {
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
			infoMessage = "Error occurred: see log";
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
	
	public void setApp(CollectControlPanel app) {
		this.app = app;
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
