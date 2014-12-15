/**
 * 
 */
package org.openforis.collect.designer.viewmodel.layout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.apache.commons.lang3.StringUtils;
import org.openforis.collect.designer.model.NamedObject;
import org.openforis.collect.designer.session.SessionStatus;
import org.openforis.collect.designer.util.MessageUtil;
import org.openforis.collect.designer.util.Resources;
import org.openforis.collect.designer.viewmodel.BaseVM;
import org.openforis.collect.metamodel.ui.UIOptions;
import org.openforis.collect.metamodel.ui.UITab;
import org.openforis.collect.metamodel.ui.UITabSet;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.idm.metamodel.NodeDefinition;
import org.zkoss.bind.BindUtils;
import org.zkoss.bind.annotation.AfterCompose;
import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.DependsOn;
import org.zkoss.bind.annotation.ExecutionArgParam;
import org.zkoss.bind.annotation.GlobalCommand;
import org.zkoss.bind.annotation.Init;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.Tabbox;
import org.zkoss.zul.Window;

/**
 * @author S. Ricci
 *
 */
public class TabsGroupVM extends BaseVM {

	private static final String TAB_NAME_PREFIX = "tab_";
	private static final String TAB_NAME_SEPARATOR = "_";
	public static final String TAB_CHANGED_GLOBAL_COMMAND = "tabChanged";
	
	public static NamedObject FAKE_ADD_TAB;
	
	static {
		FAKE_ADD_TAB = new NamedObject("survey.layout.tab.add_short");
	}
	
	@Wire
	private Tabbox tabbox;
	
	private UITabSet tabSet;
	private Window tabLabelPopUp;
	
	@Init
	public void init(@ExecutionArgParam("tabSet") UITabSet tabSet) {
		this.tabSet = tabSet;
	}
	
	@AfterCompose
	public void afterCompose(@ContextParam(ContextType.VIEW) Component view){
		 Selectors.wireComponents(view, this, false);
	}
	
	@Command
	@NotifyChange({"tabs"})
	public void addTab() {
		UIOptions uiOptions = tabSet.getUIOptions();
		UITab tab = uiOptions.createTab();
		String tabName = generateNewTabName(tabSet);
		tab.setName(tabName);
		tabSet.addTab(tab);
		postTabChangedCommand(tabSet);
		openTabLabelEditPopUp(tab);
	}

	@Command
	public void editTabLabel(@BindingParam("tab") UITab tab) {
		openTabLabelEditPopUp(tab);
	}

	@Command
	@NotifyChange({"tabs"})
	public void removeTab(@BindingParam("tab") UITab tab) {
		if ( tab.getTabs().isEmpty() ) {
			SessionStatus sessionStatus = getSessionStatus();
			CollectSurvey survey = sessionStatus.getSurvey();
			UIOptions uiOpts = survey.getUIOptions();
			List<NodeDefinition> nodesPerTab = uiOpts.getNodesPerTab(tab, false);
			if ( nodesPerTab.isEmpty() ) {
				UITabSet parent = tab.getParent();
				parent.removeTab(tab);
				postTabChangedCommand(parent);
			} else {
				MessageUtil.showWarning("survey.layout.tab.remove.error.associated_nodes_present");
			}
		} else {
			MessageUtil.showWarning("survey.layout.tab.remove.error.nested_tabs_present");
		}
	}

	protected void openTabLabelEditPopUp(final UITab tab) {
		Map<String, Object> args = new HashMap<String, Object>();
		args.put("tab", tab);
		tabLabelPopUp = openPopUp(Resources.Component.TAB_LABEL_POPUP.getLocation(), true, args);
	}
	
	@GlobalCommand
	public void applyChangesToTabLabel(@BindingParam("tab") UITab tab, @BindingParam("label") String label) {
		UITabSet parent = tab.getParent();
		if ( parent.equals(tabSet) && validateTabLabel(label) ) {
			performUpdateTabLabel(tab, label);
		}
	}

	protected void performUpdateTabLabel(UITab tab, String label) {
		List<UITab> tabs = tabSet.getTabs();
		int index = tabs.indexOf(tab);
		tabbox.setSelectedIndex(index);
		tab.setLabel(getCurrentLanguageCode(), label.trim());
		closePopUp(tabLabelPopUp);
		BindUtils.postNotifyChange(null, null, tab, "*");
	}
	
	protected boolean validateTabLabel(String label) {
		if ( StringUtils.isBlank(label) ) {
			MessageUtil.showWarning("survey.layout.tab.label.error.required");
			return false;
		} else {
			return true;
		}
	}
	
	private void postTabChangedCommand(UITabSet parent) {
		Map<String, Object> args = new HashMap<String, Object>();
		args.put("tab", parent);
		BindUtils.postGlobalCommand(null, null, TAB_CHANGED_GLOBAL_COMMAND, args);
	}
	
	private String generateNewTabName(UITabSet parentGroup) {
		String prefix = TAB_NAME_PREFIX;
		Stack<Integer> parts = new Stack<Integer>();
		UITabSet currentGroup = parentGroup;
		do {
			int position = currentGroup.getTabs().size() + 1;
			parts.push(position);
			currentGroup = currentGroup.getParent();
		} while ( currentGroup != null );
		String suffix = StringUtils.join(parts.toArray(), TAB_NAME_SEPARATOR);
		String tabName = prefix + suffix;
		return tabName;
	}

//	@GlobalCommand
//	public void rootTabSetChanged(@BindingParam("tabSet") UITabSet tabSet) {
//		tabsGroup = tabSet;
//	}
	
	public UITabSet getTabsGroup() {
		return tabSet;
	}
	
	public List<UITab> getTabs() {
		return tabSet != null ? tabSet.getTabs(): null;
	}
	
	@DependsOn("tabs")
	public List<Object> getTabsPlusAddButton() {
		List<Object> tabs = new ArrayList<Object>();
		if ( tabSet != null ) {
			tabs.addAll(tabSet.getTabs());
		}
		tabs.add(FAKE_ADD_TAB);
		return tabs;
	}

	public NamedObject getFakeAddTab() {
		return FAKE_ADD_TAB;
	}


}
