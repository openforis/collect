package org.openforis.collect.controlpanel.component;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URI;

import javax.swing.JLabel;
import javax.swing.JOptionPane;

public class HyperlinkLabel extends JLabel {

	private static final long serialVersionUID = 1L;

	private URI uri;
	private String _text;

	public HyperlinkLabel() {
		this(null, null);
	}

	public HyperlinkLabel(URI uri, String text) {
		super(text);
		this.uri = uri;
		this._text = text;

		this.setForeground(Color.BLUE.darker());
		this.setCursor(new Cursor(Cursor.HAND_CURSOR));

		this.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseClicked(MouseEvent e) {
				try {
					Desktop.getDesktop().browse(HyperlinkLabel.this.getUri());
				} catch (IOException ex) {
					JOptionPane.showMessageDialog(null, "Error opening browser: " + ex.getMessage());
				}
			}

			@Override
			public void mouseExited(MouseEvent e) {
				HyperlinkLabel.this.setText(HyperlinkLabel.this._text);
			}

			@Override
			public void mouseEntered(MouseEvent e) {
				HyperlinkLabel.this.setText("<html><a href=''>" + HyperlinkLabel.this._text + "</a></html>");
			}

		});
	}

	public URI getUri() {
		return uri;
	}

	public void setUri(URI uri) {
		this.uri = uri;
		this._text = uri == null ? null : uri.toString();
		this.setText(_text);
	}

}
