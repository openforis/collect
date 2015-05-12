package org.openforis.collect.designer.component;

import org.zkoss.zul.Html;
import org.zkoss.zul.Popup;

/**
 * 
 * @author S. Ricci
 *
 */
public class FieldErrorTooltip extends Popup {

	private static final long serialVersionUID = 1L;

	private static final String ERROR_SCLASS = "error";

	private Html htmlContent;

	public FieldErrorTooltip() {
		this(null);
	}
	
	public FieldErrorTooltip(String message) {
		setSclass(ERROR_SCLASS);
		htmlContent = new Html();
		appendChild(htmlContent);
		setMessage(message);
	}
	
	public String getMessage() {
		return htmlContent.getContent();
	}
	
	public void setMessage(String message) {
		this.htmlContent.setContent(message);
	}
	
}
