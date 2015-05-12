/**
 * 
 */
package org.openforis.collect.designer.component;

import org.openforis.collect.metamodel.ui.UITab;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.IdSpace;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.Div;
import org.zkoss.zul.Listbox;

/**
 * @author S. Ricci
 *
 */
@Deprecated
public class TabsGroupPanel extends Div implements IdSpace {

	private static final long serialVersionUID = 1L;

	private static final String COMPONENT_URL = "/view/designer/component/schema_layout/tabsgrouppanel.zul";

	@Wire
	private TabsGroupContainer tabsGroupContainer;
	@Wire
	private Listbox nodesListBox;
	
	private UITab tab;

	public TabsGroupPanel() {
        Executions.createComponents(COMPONENT_URL, this, null);
        Selectors.wireComponents(this, this, false);
        Selectors.wireEventListeners(this, this);
    }
	
	protected void build() {
		tabsGroupContainer.setTabsGroup(tab);
//		nodesListBox.setModel(model);
	}

	public UITab getTab() {
		return tab;
	}

	public void setTab(UITab tab) {
		this.tab = tab;
		build();
	}

}
