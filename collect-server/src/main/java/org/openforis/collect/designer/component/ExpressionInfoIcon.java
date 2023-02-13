/**
 * 
 */
package org.openforis.collect.designer.component;

import org.openforis.collect.designer.util.ComponentUtil;
import org.openforis.collect.designer.util.PopUpUtil;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zul.Button;
import org.zkoss.zul.Window;

/**
 * @author S. Ricci
 *
 */
public class ExpressionInfoIcon extends Button {

	private static final long serialVersionUID = 1L;

	private static final String SCLASS = "expression-info";
	private static final String IMAGE_SRC = "/assets/images/fx-16x16.png";
	
	private static Window popup;
	
	public ExpressionInfoIcon() {
		super();
		setImage(IMAGE_SRC);
		setTooltiptext(Labels.getLabel("global.expression_info_tooltip"));
		ComponentUtil.addClass(this, SCLASS);
		addEventListener("onClick", new EventListener<Event>() {
			@Override
			public void onEvent(Event event) throws Exception {
				onClick();
			}
		});
	}

	private void onClick() {
		if (popup != null) {
			PopUpUtil.closePopUp(popup);
		}
		popup = PopUpUtil.openPopUp("survey_edit/schema/idm_expression_language_popup.zul", false);
	}

}
