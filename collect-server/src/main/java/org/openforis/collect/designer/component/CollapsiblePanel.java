package org.openforis.collect.designer.component;

import org.openforis.collect.designer.util.ComponentUtil;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zul.Caption;
import org.zkoss.zul.Groupbox;

public class CollapsiblePanel extends Groupbox {
	
	private static final long serialVersionUID = 1L;
	private static final String SCLASS = "collapsible";

	public CollapsiblePanel() {
		ComponentUtil.addClass(this, SCLASS);
		setMold("3d");
		
		createAndAddCaption();
		
		addEventListener("onOpen",new EventListener<Event>() {
			public void onEvent(Event event) throws Exception {
				handleOpenChange();
			}
		});
	}

	protected void handleOpenChange() {
		getCaption().setIconSclass("z-icon-caret-" + (isOpen() ? "up": "down"));
	}

	public void setCaptionLabel(String captionText) {
		getCaption().setLabel(captionText);
	}
	
	@Override
	public void setOpen(boolean open) {
		super.setOpen(open);
		handleOpenChange();
	}
	
	private void createAndAddCaption() {
		Caption caption = new Caption();
		getChildren().add(caption);
	}
}
