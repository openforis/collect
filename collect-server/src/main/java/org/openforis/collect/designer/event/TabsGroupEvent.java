/**
 * 
 */
package org.openforis.collect.designer.event;

import org.openforis.collect.model.ui.UITabsGroup;
import org.zkoss.zk.ui.event.Event;

/**
 * @author S. Ricci
 *
 */
public class TabsGroupEvent extends Event {

	private static final long serialVersionUID = 1L;

	public static final String ADD_TAB = "onAddTab";
	public static final String GROUP_CHANGE = "onGroupChange";
	
	private UITabsGroup tabsGroup;

	public TabsGroupEvent(String name, UITabsGroup tabsGroup) {
		super(name);
		this.tabsGroup = tabsGroup;
	}

	public UITabsGroup getTabsGroup() {
		return tabsGroup;
	}
	
}
