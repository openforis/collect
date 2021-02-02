package org.openforis.collect.controlpanel.component;

import java.awt.BorderLayout;
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
import org.openforis.utils.SpringLayoutUtilities;

public class AboutDialog extends JDialog {

	private static final long serialVersionUID = 1L;

	private static final int WIDTH = 350;
	private static final int HEIGHT = 150;

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
		{
			// created by
			JLabel label = new JLabel("Created By:");
			label.setSize(100, 30);
			form.add(label);
			form.add(new JHyperlinkLabel("http://www.openforis.org", "Open Foris"));
		}
		{
			// version
			JLabel label = new JLabel("Version:");
			label.setSize(100, 30);
			form.add(label);
			form.add(new JLabel(Collect.VERSION.toString()));
		}
		//Lay out the panel.
		SpringLayoutUtilities.makeCompactGrid(form,
		                                2, 2,  //rows, cols
		                                6, 6,  //initX, initY
		                                6, 6); //xPad, yPad
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
}