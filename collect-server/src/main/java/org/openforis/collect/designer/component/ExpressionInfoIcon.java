/**
 * 
 */
package org.openforis.collect.designer.component;

import org.openforis.collect.designer.CollectDesignerOptions;
import org.openforis.collect.designer.util.ComponentUtil;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zul.Image;

/**
 * @author S. Ricci
 *
 */
public class ExpressionInfoIcon extends Image {

	private static final long serialVersionUID = 1L;

	private static final String SCLASS = "expression-info";
	private static final String IMAGE_SRC = "/assets/images/expression-small.png";
	private static final String INFO_URL = CollectDesignerOptions.getIdmExpressionLanguageWikiUrl();
	
	public ExpressionInfoIcon() {
		super();
		setSrc(IMAGE_SRC);
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
		Executions.getCurrent().sendRedirect(INFO_URL, "_blank");
	}

}
