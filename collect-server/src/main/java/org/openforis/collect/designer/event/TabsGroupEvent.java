/**
 * 
 */
package org.openforis.collect.designer.event;

import org.openforis.collect.metamodel.ui.UITabSet;
import org.zkoss.zk.ui.event.Event;

/**
 * @author S. Ricci
 *
 */
public class TabsGroupEvent extends Event {

	private static final long serialVersionUID = 1L;

	public static final String ADD_TAB = "onAddTab";
	public static final String GROUP_CHANGE = "onGroupChange";
	
	private UITabSet tabsGroup;

	public TabsGroupEvent(String name, UITabSet tabsGroup) {
		super(name);
		this.tabsGroup = tabsGroup;
	}

	public UITabSet getTabsGroup() {
		return tabsGroup;
	}
	
}
