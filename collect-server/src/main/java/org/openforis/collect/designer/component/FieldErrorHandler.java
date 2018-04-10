/**
 * 
 */
package org.openforis.collect.designer.component;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.openforis.collect.designer.util.ComponentUtil;
import org.zkoss.zk.ui.Component;
import org.zkoss.zul.Hlayout;
import org.zkoss.zul.impl.XulElement;

/**
 * @author S. Ricci
 *
 */
public class FieldErrorHandler extends Hlayout {

	private static final long serialVersionUID = 1L;

	private static final String SCLASS = "fielderrorhandler";
	private static final String ERROR_SCLASS = "error";

	private FieldErrorTooltip tooltip;
	private String message;

	public FieldErrorHandler() {
		super();
		ComponentUtil.addClass(this, SCLASS);
	}

	@Override
	public void setSclass(String sclass) {
		super.setSclass(sclass);
		ComponentUtil.addClass(this, SCLASS);
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
		List<Component> children = getChildren();
		for (Component child : children) {
			if ( child instanceof XulElement ) {
				setTooltipOnField((XulElement) child);
				updateFieldStyle((XulElement) child);
			}
		}
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

	protected void setTooltipOnField(XulElement field) {
		field.setTooltip(this.tooltip);
	}
	
	protected void updateFieldStyle(XulElement field) {
		boolean hasError = StringUtils.isNotBlank(message);
		ComponentUtil.toggleClass(field, ERROR_SCLASS, hasError);
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
	
}