package org.openforis.collect.controlpanel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

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
	private static final int HEIGHT = 300;

	private static final String TITLE = "Open Foris Collect - v" + Collect.VERSION.toString();
	private static final String LOGO_PATH = "of-collect-logo.png";
	private static final Image LOGO_IMAGE = Toolkit.getDefaultToolkit()
			.getImage(CollectControlPanel.class.getResource(LOGO_PATH));
	private static final String ONLINE_MANUAL_URL = "http://www.openforis.org/tools/collect.html";
	private static final String CHANGELOG_URL = "https://github.com/openforis/collect/blob/master/CHANGELOG.md";

	private static final Font ERROR_MSG_FONT = new Font(Font.SANS_SERIF, Font.PLAIN, 15);
	private static final Font STATUS_MSG_FONT = new Font(Font.SANS_SERIF, Font.BOLD, 20);

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

		Container pane = this.getContentPane();

		int boxContentWidth = WIDTH - 20;
		int boxContentItemMaxWidth = boxContentWidth - 30;

		// icon & status message
		statusTxt = new JLabel();
		statusTxt.setIcon(new ImageIcon(LOGO_IMAGE));
		statusTxt.setFont(STATUS_MSG_FONT);
		pane.add(statusTxt, BorderLayout.NORTH);

		// running at box
		runningAtUrlBox = Box.createHorizontalBox();
		JLabel runningAtLabel = new JLabel("Running at: ");
		runningAtUrlBox.add(runningAtLabel);
		urlHyperlink = new JHyperlinkLabel();
		runningAtUrlBox.add(urlHyperlink);
		addMargin(runningAtUrlBox);
		pane.add(runningAtUrlBox, BorderLayout.CENTER);

		Box south = Box.createVerticalBox();
		
		// shutdown button
		shutdownBtn = new JButton("Shutdown");
		shutdownBtn.setAlignmentX(CENTER_ALIGNMENT);
		shutdownBtn.addActionListener(e -> controller.handleExitAction());
		addMargin(shutdownBtn);
		south.add(shutdownBtn);

		// error message
		errorMessageTxt = new JMultilineLabel();
		errorMessageTxt.setFont(ERROR_MSG_FONT);
		errorMessageTxt.setForeground(Color.RED);
		errorMessageTxt.setPreferredSize(new Dimension(boxContentItemMaxWidth, 100));
		addMargin(errorMessageTxt);
		south.add(errorMessageTxt);

		// progress bar
		progressBar = new JProgressBar();
		progressBar.setIndeterminate(true);
		progressBar.setPreferredSize(new Dimension(boxContentItemMaxWidth, 20));
		south.add(progressBar);

		pane.add(south, BorderLayout.SOUTH);

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