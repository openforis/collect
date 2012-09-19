/**
 * 
 */
package org.openforis.collect.designer.composer;

import org.openforis.collect.designer.component.TabsGroupContainer;
import org.openforis.collect.designer.event.TabsGroupEvent;
import org.openforis.collect.model.ui.UITabsGroup;
import org.zkoss.bind.BindComposer;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.Listen;

/**
 * @author S. Ricci
 *
 */
public class TabsGroupComposer extends BindComposer<TabsGroupContainer> {

	private static final long serialVersionUID = 1L;
	
	private UITabsGroup group;
	
//	@Listen("onGroupChange")
//	public void onGroupChange(Event event) {
//		group = comp.getTabsGroup();
//	}
//	
	@Override
	public void doAfterCompose(TabsGroupContainer comp) throws Exception {
		super.doAfterCompose(comp);
		Selectors.wireEventListeners(comp, this);
	}

	@Listen("onGroupChange")
	@NotifyChange({"group"})
	public void onGroupChange(TabsGroupEvent event) {
		group = event.getTabsGroup();
	}
	
	public UITabsGroup getGroup() {
		return group;
	}

}
