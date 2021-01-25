package org.openforis.collect.controlpanel;

import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JProgressBar;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;

import org.openforis.collect.controlpanel.component.HyperlinkLabel;

public class CollectControlPanel extends JFrame implements ControlPanel {

	private static final long serialVersionUID = 1L;

//	private static final Logger LOG = LogManager.getLogger(CollectControlPanel.class);

	private static final String TITLE = "Open Foris Collect - Control Panel";
	private static final String LOGO_PATH = "of-collect-logo.png";
	private static final String ERROR_DIALOG_TITLE = "Open Foris Collect - Error";

	private CollectControlPanelController controller;

	private HyperlinkLabel urlHyperlink;
	private JComponent runningAtUrlBox;
	private JButton shutdownBtn;
	private JLabel errorMessageTxt;
	private JLabel statusTxt;
	private JProgressBar progressBar;

	public static void main(String[] args) {
		EventQueue.invokeLater(() -> {
			CollectControlPanel panel = new CollectControlPanel();
			panel.setVisible(true);
		});
	}

	public CollectControlPanel() {
		initUI();
	}

	private void initUI() {

		setLocationRelativeTo(null);
		setSize(500, 250);
		setResizable(false);
		setTitle();
		setLogo();

		// on close -> shutdown (with confirm)
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		this.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				controller.onExit();
			}
		});

		createMenuBar();

		Box box = new Box(BoxLayout.Y_AXIS);

		statusTxt = new JLabel();
		statusTxt.setAlignmentX(CENTER_ALIGNMENT);
		addMargin(statusTxt);
		box.add(statusTxt);

		runningAtUrlBox = new Box(BoxLayout.X_AXIS);
		runningAtUrlBox.setAlignmentX(LEFT_ALIGNMENT);
		JLabel runningAtLabel = new JLabel("Running at URL: ");
		runningAtUrlBox.add(runningAtLabel);
		urlHyperlink = new HyperlinkLabel();
		runningAtUrlBox.add(urlHyperlink);
		addMargin(runningAtUrlBox);
		box.add(runningAtUrlBox);

		shutdownBtn = new JButton("Shutdown");
		shutdownBtn.setAlignmentX(CENTER_ALIGNMENT);
		shutdownBtn.addActionListener(e -> controller.onExit());
		addMargin(shutdownBtn);
		box.add(shutdownBtn);

		errorMessageTxt = new JLabel();
		addMargin(errorMessageTxt);
		box.add(errorMessageTxt);

		progressBar = new JProgressBar();
		progressBar.setIndeterminate(true);
		progressBar.setSize(new Dimension(300, 80));
		progressBar.setAlignmentX(CENTER_ALIGNMENT);
		box.add(progressBar);

		this.add(box);

		controller = new CollectControlPanelController();

		controller.setControlPanel(this);
		controller.init();
		controller.startServer(() -> {
			controller.openBrowser();
		});
	}

	private void addMargin(JComponent comp) {
		Border border = comp.getBorder();
		Border margin = new EmptyBorder(10, 10, 10, 10);
		comp.setBorder(new CompoundBorder(border, margin));
	}

	private void setTitle() {
		setTitle(TITLE);
		try {
			Toolkit xToolkit = Toolkit.getDefaultToolkit();
			java.lang.reflect.Field awtAppClassNameField = xToolkit.getClass().getDeclaredField("awtAppClassName");
			awtAppClassNameField.setAccessible(true);
			awtAppClassNameField.set(xToolkit, TITLE);
		} catch (Exception e) {
			// ignore it
		}
	}

	private void createMenuBar() {
		JMenuBar menuBar = new JMenuBar();
		JMenu fileMenu = new JMenu("File");
		fileMenu.setMnemonic(KeyEvent.VK_F);

		JMenuItem eMenuItem = new JMenuItem("Exit");
		eMenuItem.setMnemonic(KeyEvent.VK_E);
		eMenuItem.addActionListener((event) -> controller.onExit());

		fileMenu.add(eMenuItem);
		menuBar.add(fileMenu);

		setJMenuBar(menuBar);
	}

	@Override
	public HyperlinkLabel getUrlHyperlink() {
		return urlHyperlink;
	}

	@Override
	public JComponent getRunningAtUrlBox() {
		return runningAtUrlBox;
	}

	@Override
	public JButton getShutdownBtn() {
		return shutdownBtn;
	}

	@Override
	public JLabel getErrorMessageTxt() {
		return errorMessageTxt;
	}

	@Override
	public JLabel getStatusTxt() {
		return statusTxt;
	}

	@Override
	public JProgressBar getProgressBar() {
		return progressBar;
	}

	private void setLogo() {
		Image image = Toolkit.getDefaultToolkit().getImage(getClass().getResource(LOGO_PATH));
		this.setIconImage(image);
	}

//	@Override
//	public void stop() throws Exception {
//		super.stop();
//		if (controller != null) {
//			controller.stop();
//		}
//	}
//
//	private static void showErrorDialog(Throwable e) {
//		Stage dialog = new Stage();
//		dialog.setTitle(ERROR_DIALOG_TITLE);
//		dialog.initModality(Modality.APPLICATION_MODAL);
//		dialog.setResizable(false);
//		FXMLLoader loader = new FXMLLoader();
//		try {
//			Parent root = loader.load(CollectControlPanel.class.getResourceAsStream("error_dialog.fxml"));
//			ErrorController errorController = loader.getController();
//			errorController.setMainText("Error initializing Collect");
//			StringWriter errorDetailsSW = new StringWriter();
//			e.printStackTrace(new PrintWriter(errorDetailsSW));
//			errorController.setErrorText(errorDetailsSW.toString());
//			dialog.setScene(new Scene(root, 400, 300));
//			dialog.show();
//		} catch (IOException exc) {
//			LOG.error(exc);
//		}
//	}

//	private class OnCloseHandler implements EventHandler<WindowEvent> {
//		public void handle(WindowEvent event) {
//			event.consume();
//			switch (controller.getStatus()) {
//			case INITIALIZING:
//			case STARTING:
//				break;
//			default:
//				try {
//					controller.shutdown(null);
//				} catch (Exception e) {
//					LOG.error(e);
//				}
//			}
//		}
//	}

}