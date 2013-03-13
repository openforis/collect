/**
 * 
 */
package org.openforis.collect.designer.component;

import org.apache.commons.lang3.StringUtils;
import org.openforis.collect.designer.util.ComponentUtil;
import org.zkoss.zk.ui.Component;
import org.zkoss.zul.Div;
import org.zkoss.zul.Html;
import org.zkoss.zul.Popup;
import org.zkoss.zul.impl.XulElement;

/**
 * @author S. Ricci
 *
 */
public class FieldErrorHandler extends Div {

	private static final long serialVersionUID = 1L;

	private static final String ERROR_SCLASS = "error";
	private static final String FIELD_ERROR_SCLASS = "error";

	private Popup tooltip;
	private String message;
	private Html htmlContent;

	private XulElement field;
	
	public FieldErrorHandler() {
		super();
	}

	@Override
	public boolean insertBefore(Component newChild, Component refChild) {
		boolean result = super.insertBefore(newChild, refChild);
		if ( newChild instanceof XulElement ) {
			field = (XulElement) newChild;
			updateField();
		}
		return result;
	}
	
	
	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
		updateView();
	}
	
	protected void updateView() {
		updateTooltip();
		updateField();
	}

	protected void updateTooltip() {
		if ( StringUtils.isBlank(message) ) {
			removeTooltip();
		} else {
			initTooltip();
			htmlContent.setContent(message);
		}
	}

	protected void updateField() {
		if ( field != null ) {
			field.setTooltip(tooltip);
			toggleErrorClassToField(StringUtils.isNotBlank(message));
		}
	}

	protected void initTooltip() {
		tooltip = new Popup();
		tooltip.setSclass(ERROR_SCLASS);
		htmlContent = new Html();
		tooltip.appendChild(htmlContent);
		super.insertBefore(tooltip, null);
		//tooltip.setParent(this);
	}
	
	protected void removeTooltip() {
		if ( tooltip != null ) {
			this.removeChild(tooltip);
		}
		htmlContent = null;
		tooltip = null;
	}
	
	protected void toggleErrorClassToField(boolean present) {
		ComponentUtil.toggleClass(field, FIELD_ERROR_SCLASS, present);
	}
	
	public XulElement getField() {
		return field;
	}

	public void setField(XulElement field) {
		this.field = field;
	}
	
}
