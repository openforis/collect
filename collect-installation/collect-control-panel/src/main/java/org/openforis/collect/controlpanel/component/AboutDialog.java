package org.openforis.collect.controlpanel.component;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Font;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SpringLayout;

import org.openforis.collect.Collect;
import org.openforis.collect.Environment;
import org.openforis.utils.SpringLayoutUtilities;

public class AboutDialog extends JDialog {

	private static final long serialVersionUID = 1L;

	private static final int WIDTH = 350;
	private static final int HEIGHT = 200;

	public AboutDialog(JFrame parent) {
		super(parent, "About", true);
		setLocationRelativeTo(null);
		setResizable(false);
		setSize(WIDTH, HEIGHT);

		Container pane = getContentPane();

		Box center = Box.createVerticalBox();
		center.add(Box.createGlue());

		// title
		JLabel titleLabel = new JLabel("Open Foris Collect");
		titleLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 15));
		titleLabel.setAlignmentX(CENTER_ALIGNMENT);
		center.add(titleLabel);

		center.add(Box.createGlue());

		JPanel form = new JPanel(new SpringLayout());

		addFormItem(form, "Created by", new JHyperlinkLabel("http://www.openforis.org", "Open Foris"));
		addFormItem(form, "Version", new JLabel(Collect.VERSION.toString()));
		addFormItem(form, "Java version", new JLabel(Environment.getJREVersion()));

		// Lay out the panel.
		SpringLayoutUtilities.makeCompactGrid(form, 3, 2, // rows, cols
				6, 6, // initX, initY
				6, 6); // xPad, yPad
		center.add(form);

		center.add(Box.createGlue());

		pane.add(center, BorderLayout.CENTER);

		JPanel south = new JPanel();
		JButton ok = new JButton("Ok");
		ok.addActionListener(evt -> setVisible(false));
		south.add(ok);

		pane.add(south, BorderLayout.SOUTH);

		setVisible(true);
	}

	private void addFormItem(JPanel form, String label, Component component) {
		JLabel jLabel = new JLabel(label + ":");
		jLabel.setSize(120, 30);
		form.add(jLabel);
		form.add(component);
	}

}