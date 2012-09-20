/**
 * 
 */
package org.openforis.collect.designer.component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openforis.collect.designer.event.TabsGroupEvent;
import org.openforis.collect.model.ui.UITab;
import org.openforis.collect.model.ui.UITabsGroup;
import org.zkoss.bind.BindUtils;
import org.zkoss.composite.Composite;
import org.zkoss.zk.ui.Components;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.IdSpace;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.Div;
import org.zkoss.zul.Tab;
import org.zkoss.zul.Tabbox;
import org.zkoss.zul.Tabpanel;
import org.zkoss.zul.Tabpanels;
import org.zkoss.zul.Tabs;

/**
 * 
 * @author S. Ricci
 *
 */
@Composite
public class TabsGroupContainer extends Div implements IdSpace {

	private static final long serialVersionUID = 1L;
	
	private static final String TABSGROUP_URL = "/view/designer/component/schema_layout/tabsgroup.zul";
	
    @Wire
    private Tabbox tabbox;
    
    private UITabsGroup tabsGroup;

	private Tab newChildTab;
 
    public TabsGroupContainer() {
        Executions.createComponents(TABSGROUP_URL, this, null);
        Selectors.wireComponents(this, this, false);
        Selectors.wireEventListeners(this, this);
    }

	private void build() {
		cleanTabs();
		
		if ( tabsGroup != null ) {
			List<UITab> uiTabs = tabsGroup.getTabs();
			for (UITab uiTab : uiTabs) {
				addTab(uiTab);
			}
			addNewChildTab();
		}
	}

	private void addNewChildTab() {
		newChildTab = new Tab();
		newChildTab.setLabel("+");
		newChildTab.addEventListener("onClick", new EventListener<Event>() {
			@Override
			public void onEvent(Event event) throws Exception {
				postAddTabCommand();
			}
		});
		Tabs tabs = tabbox.getTabs();
		tabs.appendChild(newChildTab);
	}

	private void postAddTabCommand() {
		Map<String, Object> args = new HashMap<String, Object>();
		args.put("group", tabsGroup);
		BindUtils.postGlobalCommand(null, null, "addTab", args);
//		Event newEvent = new TabsGroupEvent(TabsGroupEvent.ADD_TAB, tabsGroup);
//		Events.sendEvent(this, newEvent);
	}
	
//	private void addNewTab() {
//		Tabs tabs = tabbox.getTabs();
//		tabs.removeChild(newChildTab);
//		UITab uiTab = new UITab();
//		int position = tabsGroup.getTabs().size() + 1;
//		String tabName = "tab_" + position;
//		uiTab.setName(tabName);
//		addTab(uiTab);
//		addNewChildTab();
//	}

	private void cleanTabs() {
		Tabpanels tabpanels = tabbox.getTabpanels();
		Tabs tabs = tabbox.getTabs();
		Components.removeAllChildren(tabpanels);
		Components.removeAllChildren(tabs);
	}

//	private void addNewTab() {
//		Tabs tabs = tabbox.getTabs();
//		tabs.removeChild(newChildTab);
//		UITab uiTab = new UITab();
//		int position = tabsGroup.getTabs().size() + 1;
//		String tabName = "tab_" + position;
//		uiTab.setName(tabName);
//		addTab(uiTab);
//		addNewChildTab();
//	}

	private void addTab(UITab uiTab) {
		Tabs tabs = tabbox.getTabs();
		Tab tab = new Tab();
		tab.setLabel(uiTab.getLabel());
		tabs.appendChild(tab);
		Tabpanels tabpanels = tabbox.getTabpanels();
		Tabpanel tabpanel = new Tabpanel();
		tabpanels.appendChild(tabpanel);
	}
	
	public UITabsGroup getTabsGroup() {
		return tabsGroup;
	}

	public void setTabsGroup(UITabsGroup tabsGroup) {
		this.tabsGroup = tabsGroup;
//		TabsGroupEvent event = new TabsGroupEvent(TabsGroupEvent.GROUP_CHANGE, tabsGroup);
//		Events.sendEvent(this, event);
//		build();
	}

}
