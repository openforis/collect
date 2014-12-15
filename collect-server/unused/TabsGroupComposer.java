/**
 * 
 */
package org.openforis.collect.designer.composer;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import org.apache.commons.lang3.StringUtils;
import org.openforis.collect.model.ui.UITab;
import org.openforis.collect.model.ui.UITabsGroup;
import org.zkoss.bind.BindComposer;
import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.DependsOn;
import org.zkoss.bind.annotation.ExecutionArgParam;
import org.zkoss.bind.annotation.Init;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.zk.ui.Component;

/**
 * @author S. Ricci
 *
 */
public class TabsGroupComposer extends BindComposer<Component> {

	private static final long serialVersionUID = 1L;
	
	public static UITab FAKE_ADD_TAB;
	
	static {
		FAKE_ADD_TAB = new UITab();
		FAKE_ADD_TAB.setLabel("+");
	}
	
	private UITabsGroup tabsGroup;
	
	@Init
	public void init(@ExecutionArgParam("tabsGroup") UITabsGroup tabsGroup) {
		this.tabsGroup = tabsGroup;
	}
	
	@Command
	@NotifyChange({"tabs"})
	public void addTab() {
		UITab tab = new UITab();
		String tabName = generateNewTabName(tabsGroup);
		tab.setName(tabName);
		tabsGroup.addTab(tab);
	}
	
	@Command
	@NotifyChange({"tabs"})
	public void removeTab(@BindingParam("tab") UITab tab) {
		UITabsGroup parent = tab.getParent();
		parent.removeTab(tab);
	}
	
	private String generateNewTabName(UITabsGroup parentGroup) {
		String prefix = "tab_";
		Stack<Integer> parts = new Stack<Integer>();
		UITabsGroup currentGroup = parentGroup;
		do {
			int position = currentGroup.getTabs().size() + 1;
			parts.push(position);
			currentGroup = currentGroup.getParent();
		} while ( currentGroup != null );
		String suffix = StringUtils.join(parts.toArray(), "_");
		String tabName = prefix + suffix;
		return tabName;
	}

//	@GlobalCommand
//	public void tabDefinitionChanged(@BindingParam("tabDefinition") UITabDefinition tabDefinition) {
//		group = tabDefinition;
//	}
	
	public UITabsGroup getTabsGroup() {
		return tabsGroup;
	}
	
	public List<UITab> getTabs() {
		return tabsGroup != null ? tabsGroup.getTabs(): null;
	}
	
	@DependsOn("tabs")
	public List<UITab> getTabsPlusAddButton() {
		List<UITab> tabs = new ArrayList<UITab>();
		if ( tabsGroup != null ) {
			tabs.addAll(tabsGroup.getTabs());
		}
		tabs.add(FAKE_ADD_TAB);
		return tabs;
	}

	public UITab getFakeAddTab() {
		return FAKE_ADD_TAB;
	}

}
