package org.openforis.collect.controlpanel.component;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

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

		Box b = Box.createVerticalBox();
		b.add(Box.createGlue());
		JLabel titleLabel = new JLabel("Open Foris Collect");
		titleLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 15));
		titleLabel.setAlignmentX(CENTER_ALIGNMENT);
		b.add(titleLabel, "Center");
		
		JPanel form = new JPanel(new SpringLayout());
		{
			JLabel label = new JLabel("Created By", JLabel.TRAILING);
			label.setSize(100, 30);
			form.add(label);
			form.add(new JHyperlinkLabel("http://www.openforis.org", "Open Foris"));
		}
		{
			JLabel label = new JLabel("Version");
			label.setSize(100, 30);
			form.add(label);
			form.add(new JLabel(Collect.VERSION.toString()));
		}
		//Lay out the panel.
		SpringLayoutUtilities.makeCompactGrid(form,
		                                2, 2, //rows, cols
		                                6, 6,        //initX, initY
		                                6, 6);       //xPad, yPad
		b.add(form);

		b.add(Box.createGlue());

		getContentPane().add(b, "Center");

		JPanel p2 = new JPanel();
		JButton ok = new JButton("Ok");
		p2.add(ok);
		getContentPane().add(p2, "South");

		ok.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				setVisible(false);
			}
		});

		setVisible(true);
	}
}