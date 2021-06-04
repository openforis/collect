package org.openforis.collect.controlpanel;

import java.awt.BorderLayout;
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
import javax.swing.ImageIcon;
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

import org.openforis.collect.Collect;
import org.openforis.collect.controlpanel.component.AboutDialog;
import org.openforis.collect.controlpanel.component.JHyperlinkLabel;
import org.openforis.collect.controlpanel.component.JMultilineLabel;
import org.openforis.utils.Browser;

import com.formdev.flatlaf.FlatLightLaf;

public class CollectControlPanel extends JFrame implements ControlPanel {

	private static final long serialVersionUID = 1L;

	private static final int WIDTH = 500;
	private static final int HEIGHT = 200;

	private static final String TITLE = "Open Foris Collect - v" + Collect.VERSION.toString();
	private static final String LOGO_PATH = "of-collect-logo.png";
	private static final Image LOGO_IMAGE = Toolkit.getDefaultToolkit()
			.getImage(CollectControlPanel.class.getResource(LOGO_PATH));
	private static final String ONLINE_MANUAL_URL = "http://www.openforis.org/tools/collect.html";
	private static final String CHANGELOG_URL = "https://github.com/openforis/collect/blob/master/CHANGELOG.md";

	private static final Font ERROR_MSG_FONT = new Font(Font.SANS_SERIF, Font.PLAIN, 15);
	private static final Font STATUS_MSG_FONT = new Font(Font.SANS_SERIF, Font.BOLD, 20);
	private static final Font SHUTDOWN_BTN_FONT = new Font(Font.SANS_SERIF, Font.BOLD, 15);

	private CollectControlPanelController controller;

	private JHyperlinkLabel urlHyperlink;
	private JComponent runningAtUrlBox;
	private JButton shutdownBtn;
	private JMultilineLabel errorMessageTxt;
	private JLabel statusTxt;
	private JProgressBar progressBar;

	public static void main(String[] args) {
		FlatLightLaf.setup();

		EventQueue.invokeLater(() -> {
			CollectControlPanel panel = new CollectControlPanel();
			panel.setVisible(true);
		});
	}

	public CollectControlPanel() {
		setLocationRelativeTo(null);
		setSize(WIDTH, HEIGHT);
		setResizable(false);
		setTitle();
		setIconImage(LOGO_IMAGE);

		// on close -> shutdown (with confirm)
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				controller.handleExitAction();
			}
		});

		createMenuBar();

		JComponent pane = (JComponent) this.getContentPane();

		pane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		Box center = Box.createHorizontalBox();

		// icon
		JLabel iconLabel = new JLabel();
		iconLabel.setIcon(new ImageIcon(LOGO_IMAGE));
		center.add(iconLabel);

		Box centerInnerBox = Box.createVerticalBox();
		centerInnerBox.setAlignmentX(CENTER_ALIGNMENT);

		// status message
		statusTxt = new JLabel();
		statusTxt.setFont(STATUS_MSG_FONT);
		centerInnerBox.add(statusTxt);

		// progress bar
		progressBar = new JProgressBar();
		progressBar.setIndeterminate(true);
		centerInnerBox.add(progressBar);

		// running at box
		runningAtUrlBox = Box.createHorizontalBox();
		runningAtUrlBox.add(new JLabel("Running at: "));
		urlHyperlink = new JHyperlinkLabel();
		runningAtUrlBox.add(urlHyperlink);
		addMargin(runningAtUrlBox);
		centerInnerBox.add(runningAtUrlBox);

		// error message
		errorMessageTxt = new JMultilineLabel();
		errorMessageTxt.setFont(ERROR_MSG_FONT);
		errorMessageTxt.setForeground(Color.RED);
		errorMessageTxt.setMaximumSize(new Dimension(400, 100));
		addMargin(errorMessageTxt);
		centerInnerBox.add(errorMessageTxt);

		center.add(centerInnerBox);

		pane.add(center, BorderLayout.CENTER);

		Box south = Box.createVerticalBox();

		// shutdown button
		shutdownBtn = new JButton("Shutdown");
		shutdownBtn.setFont(SHUTDOWN_BTN_FONT);
		shutdownBtn.setAlignmentX(CENTER_ALIGNMENT);
		shutdownBtn.addActionListener(e -> controller.handleExitAction());
		addMargin(shutdownBtn);
		south.add(shutdownBtn);

		pane.add(south, BorderLayout.SOUTH);

		// init and start controller
		controller = new CollectControlPanelController(this);
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

		// File
		JMenu fileMenu = new JMenu("File");
		fileMenu.setMnemonic(KeyEvent.VK_F);
		{
			// Exit
			JMenuItem item = new JMenuItem("Exit");
			item.setMnemonic(KeyEvent.VK_E);
			item.addActionListener((event) -> controller.handleExitAction());
			fileMenu.add(item);
		}
		menuBar.add(fileMenu);

		// Help
		JMenu helpMenu = new JMenu("Help");
		helpMenu.setMnemonic(KeyEvent.VK_H);
		{
			// Online manual
			JMenuItem item = new JMenuItem("Online manual");
			item.setMnemonic(KeyEvent.VK_M);
			item.addActionListener((event) -> Browser.openPage(ONLINE_MANUAL_URL));
			helpMenu.add(item);
		}
		{
			// Changelog
			JMenuItem item = new JMenuItem("Changelog");
			item.setMnemonic(KeyEvent.VK_C);
			item.addActionListener((event) -> Browser.openPage(CHANGELOG_URL));
			helpMenu.add(item);
		}
		{
			// About
			JMenuItem item = new JMenuItem("About");
			item.setMnemonic(KeyEvent.VK_A);
			item.addActionListener((event) -> new AboutDialog(this));
			helpMenu.add(item);
		}
		menuBar.add(helpMenu);

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

}