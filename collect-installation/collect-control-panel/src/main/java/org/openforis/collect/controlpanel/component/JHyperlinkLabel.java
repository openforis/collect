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

public class JHyperlinkLabel extends JLabel {

	private static final long serialVersionUID = 1L;

	private URI uri;
	private String _text;
	private boolean mouseEntered;

	public JHyperlinkLabel() {
		this(null, null);
	}

	public JHyperlinkLabel(URI uri, String text) {
		super(text);
		this.uri = uri;
		this._text = text;

		this.setForeground(Color.BLUE.darker());
		this.setCursor(new Cursor(Cursor.HAND_CURSOR));

		this.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseClicked(MouseEvent e) {
				try {
					Desktop.getDesktop().browse(JHyperlinkLabel.this.getUri());
				} catch (IOException ex) {
					JOptionPane.showMessageDialog(null, "Error opening browser: " + ex.getMessage());
				}
			}

			@Override
			public void mouseExited(MouseEvent e) {
				JHyperlinkLabel.this.mouseEntered = false;
				JHyperlinkLabel.this.updateText();
			}

			@Override
			public void mouseEntered(MouseEvent e) {
				JHyperlinkLabel.this.mouseEntered = true;
				JHyperlinkLabel.this.updateText();
			}

		});
	}

	private void updateText() {
		this.setText(mouseEntered ? "<html><a href=''>" + _text + "</a></html>" : _text);
	}
	
	public URI getUri() {
		return uri;
	}

	public void setUri(URI uri) {
		this.uri = uri;
		this._text = uri == null ? null : uri.toString();
		this.updateText();
	}

}
