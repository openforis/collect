package org.openforis.collect.designer.component;

import org.openforis.collect.designer.util.ComponentUtil;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zul.Caption;
import org.zkoss.zul.Div;
import org.zkoss.zul.Groupbox;
import org.zkoss.zul.Label;

public class CollapsiblePanel extends Groupbox {
	
	private static final long serialVersionUID = 1L;
	private static final String SCLASS = "collapsible";

	public CollapsiblePanel() {
		ComponentUtil.addClass(this, SCLASS);
		setMold("3d");
		
		createAndAddCaption();
		
		addEventListener("onOpen",new EventListener<Event>() {
			public void onEvent(Event event) throws Exception {
				getCaption().setSclass("open-" + isOpen());
			}
		});
	}

	public void setCaptionLabel(String captionText) {
		Label label = (Label) getCaption().getFirstChild().getFirstChild();
		label.setValue(captionText);
	}
	
	@Override
	public void setOpen(boolean open) {
		super.setOpen(open);
		getCaption().setSclass("open-" + isOpen());
	}
	
	private void createAndAddCaption() {
		Caption caption = new Caption();
		Div labelWrapper = new Div();
		labelWrapper.setStyle("text-align: left;");
		labelWrapper.getChildren().add(new Label());
		caption.getChildren().add(labelWrapper);
		getChildren().add(caption);
	}
}
