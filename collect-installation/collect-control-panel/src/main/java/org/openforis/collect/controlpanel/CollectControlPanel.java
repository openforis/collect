package org.openforis.collect.controlpanel;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.BorderFactory;
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

import org.openforis.collect.controlpanel.component.JHyperlinkLabel;
import org.openforis.collect.controlpanel.component.JMultilineLabel;

import com.formdev.flatlaf.FlatLightLaf;

public class CollectControlPanel extends JFrame implements ControlPanel {

	private static final long serialVersionUID = 1L;

	private static final int WIDTH = 500;
	private static final int HEIGHT = 200;

	// private static final Logger LOG =
	// LogManager.getLogger(CollectControlPanel.class);

	private static final String TITLE = "Open Foris Collect - Control Panel";
	private static final String LOGO_PATH = "of-collect-logo.png";
//	private static final String ERROR_DIALOG_TITLE = "Open Foris Collect - Error";

	private static final Font ERROR_MSG_FONT = new Font(Font.SERIF, Font.PLAIN, 15);
	private static final Font STATUS_MSG_FONT = new Font(Font.SERIF, Font.BOLD, 20);

	private CollectControlPanelController controller;

	private JHyperlinkLabel urlHyperlink;
	private JComponent runningAtUrlBox;
	private JButton shutdownBtn;
	private JMultilineLabel errorMessageTxt;
	private JLabel statusTxt;
	private JProgressBar progressBar;

	public static void main(String[] args) {
		FlatLightLaf.install();

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
		setSize(WIDTH, HEIGHT);
		setResizable(false);
		setTitle();
		setLogo();

		// on close -> shutdown (with confirm)
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		this.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				controller.handleExitAction();
			}
		});

		createMenuBar();

		Box box = new Box(BoxLayout.PAGE_AXIS);
		int boxWidth = WIDTH - 20;
		box.setSize(boxWidth, HEIGHT);
		box.setAlignmentX(CENTER_ALIGNMENT);
		box.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

		statusTxt = new JLabel();
		statusTxt.setAlignmentX(CENTER_ALIGNMENT);
		statusTxt.setFont(STATUS_MSG_FONT);
		addMargin(statusTxt);
		box.add(statusTxt);

		runningAtUrlBox = new Box(BoxLayout.X_AXIS);
		runningAtUrlBox.setSize(boxWidth, 30);
		JLabel runningAtLabel = new JLabel("Running at URL: ");
		runningAtUrlBox.add(runningAtLabel);
		urlHyperlink = new JHyperlinkLabel();
		urlHyperlink.setMaximumSize(new Dimension(boxWidth, 30));
		runningAtUrlBox.add(urlHyperlink);
		addMargin(runningAtUrlBox);
		box.add(runningAtUrlBox);

		shutdownBtn = new JButton("Shutdown");
		shutdownBtn.setAlignmentX(CENTER_ALIGNMENT);
		shutdownBtn.addActionListener(e -> controller.handleExitAction());
		addMargin(shutdownBtn);
		box.add(shutdownBtn);

		errorMessageTxt = new JMultilineLabel();
		errorMessageTxt.setFont(ERROR_MSG_FONT);
		errorMessageTxt.setForeground(Color.RED);
		addMargin(errorMessageTxt);
		box.add(errorMessageTxt);

		progressBar = new JProgressBar();
		progressBar.setIndeterminate(true);
		progressBar.setMaximumSize(new Dimension(WIDTH, 30));
		progressBar.setAlignmentX(CENTER_ALIGNMENT);
		box.add(progressBar);

		this.add(box);

		controller = new CollectControlPanelController();

		controller.setControlPanel(this);
		controller.init();
		controller.startServer(() -> {
			controller.openCollectInBrowser();
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
		eMenuItem.addActionListener((event) -> controller.handleExitAction());

		fileMenu.add(eMenuItem);
		menuBar.add(fileMenu);

		setJMenuBar(menuBar);
	}

	@Override
	public JHyperlinkLabel getUrlHyperlink() {
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
	public JMultilineLabel getErrorMessageTxt() {
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

}