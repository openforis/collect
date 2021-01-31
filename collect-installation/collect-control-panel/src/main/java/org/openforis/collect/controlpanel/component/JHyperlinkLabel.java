package org.openforis.collect.controlpanel.component;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JLabel;

import org.openforis.utils.Browser;

public class JHyperlinkLabel extends JLabel {

	private static final long serialVersionUID = 1L;

	private String url;
	private String _text;
	private boolean mouseEntered;

	public JHyperlinkLabel() {
		this(null, null);
	}

	public JHyperlinkLabel(String url, String text) {
		super(text);
		this.url = url;
		this._text = text;

		this.setForeground(Color.BLUE.darker());
		this.setCursor(new Cursor(Cursor.HAND_CURSOR));

		this.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseClicked(MouseEvent e) {
				Browser.openPage(JHyperlinkLabel.this.getUrl());
			}

			@Override
			public void mouseExited(MouseEvent e) {
				setMouseEntered(false);
			}

			@Override
			public void mouseEntered(MouseEvent e) {
				setMouseEntered(true);
			}
		});
	}

	private void setMouseEntered(boolean mouseEntered) {
		this.mouseEntered = mouseEntered;
		updateText();
	}

	private void updateText() {
//		this.setText(mouseEntered ? "<html><a href=''>" + _text + "</a></html>" : _text);
		this.setText(_text);
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
		this._text = url == null ? null : url;
		this.updateText();
	}

}
