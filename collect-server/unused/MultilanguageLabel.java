package org.openforis.collect.designer.component;

import org.zkoss.zk.ui.HtmlMacroComponent;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.Label;

/**
 * 
 * @author S. Ricci
 *
 */
public class MultilanguageLabel extends HtmlMacroComponent {

	private static final long serialVersionUID = 1L;
	
	private String value;
	private String languageCode;
	
	@Wire
	private Label mainLabel;
	@Wire
	private Label languageCodeLabel;
	
	public MultilanguageLabel() {
		compose();
	}
	
	public void updateView() {
		mainLabel.setValue(value);
		languageCodeLabel.setValue(languageCode);
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
		updateView();
	}

	public String getLanguageCode() {
		return languageCode;
	}

	public void setLanguageCode(String languageCode) {
		this.languageCode = languageCode;
		updateView();
	}
	
}
