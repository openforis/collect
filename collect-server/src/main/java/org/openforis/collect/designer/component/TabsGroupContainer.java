/**
 * 
 */
package org.openforis.collect.designer.component;

import java.util.List;

import org.openforis.collect.model.ui.UITab;
import org.openforis.collect.model.ui.UITabsGroup;
import org.zkoss.composite.Composite;
import org.zkoss.zk.ui.Components;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.IdSpace;
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
	
	private static final String TABSGROUP_URL = "/view/designer/macros/tabsgroup.zul";
	
    @Wire
    private Tabbox tabbox;
    
    private UITabsGroup tabGroup;
 
    public TabsGroupContainer() {
        Executions.createComponents(TABSGROUP_URL, this, null);
        Selectors.wireComponents(this, this, false);
        Selectors.wireEventListeners(this, this);
    }

	public UITabsGroup getTabGroup() {
		return tabGroup;
	}
	
	public void setTabGroup(UITabsGroup tabGroup) {
		this.tabGroup = tabGroup;
		
		Tabpanels tabpanels = tabbox.getTabpanels();
		Tabs tabs = tabbox.getTabs();
		
		if ( tabGroup != null ) {
			List<UITab> uiTabs = tabGroup.getTabs();
			for (UITab uiTab : uiTabs) {
				Tab tab = new Tab();
				tab.setLabel(uiTab.getLabel());
				tabs.appendChild(tab);
				Tabpanel tabpanel = new Tabpanel();
				tabpanels.appendChild(tabpanel);
			}
		} else {
			Components.removeAllChildren(tabpanels);
			Components.removeAllChildren(tabs);
		}
	}
	
}
