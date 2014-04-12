/**
 * 
 */
package org.openforis.collect.designer.component;

import org.apache.commons.lang3.StringUtils;
import org.openforis.collect.designer.util.ComponentUtil;
import org.zkoss.zk.ui.Component;
import org.zkoss.zul.Div;
import org.zkoss.zul.impl.XulElement;

/**
 * @author S. Ricci
 *
 */
public class FieldErrorHandler extends Div {

	private static final long serialVersionUID = 1L;

	private static final String FIELD_ERROR_SCLASS = "error";

	private FieldErrorTooltip tooltip;
	private String message;

	private XulElement field;
	
	public FieldErrorHandler() {
		super();
	}

	@Override
	public boolean insertBefore(Component newChild, Component refChild) {
		boolean result = super.insertBefore(newChild, refChild);
		if ( newChild instanceof XulElement ) {
			field = (XulElement) newChild;
			updateFieldStyle();
		}
		return result;
	}
	
	
	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
		updateErrorFeedback();
	}
	
	protected void updateErrorFeedback() {
		updateTooltip();
		updateFieldStyle();
	}

	protected void updateTooltip() {
		if ( StringUtils.isBlank(message) ) {
			removeTooltip();
		} else {
			if ( this.tooltip == null ) {
				initTooltip();
			} else {
				this.tooltip.setMessage(message);
			}
		}
	}

	protected void updateFieldStyle() {
		if ( field != null ) {
			field.setTooltip(tooltip);
			boolean hasError = StringUtils.isNotBlank(message);
			ComponentUtil.toggleClass(field, FIELD_ERROR_SCLASS, hasError);
		}
	}

	protected void initTooltip() {
		this.tooltip = new FieldErrorTooltip(message);
		super.insertBefore(tooltip, null);
	}
	
	protected void removeTooltip() {
		if ( this.tooltip != null ) {
			this.removeChild(tooltip);
			this.tooltip = null;
		}
	}
	
	public XulElement getField() {
		return field;
	}

	public void setField(XulElement field) {
		this.field = field;
	}
	
}
