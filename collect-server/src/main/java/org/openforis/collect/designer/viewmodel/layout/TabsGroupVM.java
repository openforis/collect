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
import org.openforis.collect.designer.session.SessionStatus;
import org.openforis.collect.designer.util.MessageUtil;
import org.openforis.collect.designer.util.Resources;
import org.openforis.collect.designer.viewmodel.BaseVM;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.ui.UIConfiguration;
import org.openforis.collect.model.ui.UITab;
import org.openforis.collect.model.ui.UITabDefinition;
import org.openforis.collect.model.ui.UITabsGroup;
import org.openforis.idm.metamodel.NodeDefinition;
import org.zkoss.bind.BindUtils;
import org.zkoss.bind.annotation.AfterCompose;
import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.DependsOn;
import org.zkoss.bind.annotation.ExecutionArgParam;
import org.zkoss.bind.annotation.Init;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.Button;
import org.zkoss.zul.Tabbox;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Window;

/**
 * @author S. Ricci
 *
 */
public class TabsGroupVM extends BaseVM {

	private static final String TAB_CHANGED_GLOBAL_COMMAND = "tabChanged";
	
	public static UITab FAKE_ADD_TAB;
	
	static {
		FAKE_ADD_TAB = new UITab();
		FAKE_ADD_TAB.setLabel("+");
	}
	
	@Wire
	private Tabbox tabbox;
	
	@AfterCompose
	public void afterCompose(@ContextParam(ContextType.VIEW) Component view){
		 Selectors.wireComponents(view, this, false);
	}
	
	private UITabsGroup tabsGroup;

	private Window tabLabelPopUp;
	
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
		postTabChangedCommand(tabsGroup);
		openTabLabelEditPopUp(tab);
	}

	@Command
	public void editTabLabel(@BindingParam("tab") UITab tab) {
		if ( canEditTabLabel(tab) ) {
			openTabLabelEditPopUp(tab);
		}
	}

	@Command
	@NotifyChange({"tabs"})
	public void removeTab(@BindingParam("tab") UITab tab) {
		if ( tab.getTabs().isEmpty() ) {
			SessionStatus sessionStatus = getSessionStatus();
			CollectSurvey survey = sessionStatus.getSurvey();
			UIConfiguration uiConfiguration = survey.getUIConfiguration();
			List<NodeDefinition> nodesPerTab = uiConfiguration.getNodesPerTab(tab, false);
			if ( nodesPerTab.isEmpty() ) {
				UITabsGroup parent = tab.getParent();
				parent.removeTab(tab);
				postTabChangedCommand(parent);
			} else {
				MessageUtil.showWarning("survey.layout.tab.remove.error.associated_nodes_present");
			}
		} else {
			MessageUtil.showWarning("survey.layout.tab.remove.error.nested_tabs_present");
		}
	}

	protected boolean canEditTabLabel(UITab tab) {
		UITabsGroup parent = tab.getParent();
		if ( parent instanceof UITabDefinition ) {
			int index = parent.getTabs().indexOf(tab);
			if ( index != 0 ) {
				return true;
			} else {
				return false;
			}
		} else {
			return true;
		}
	}
	
	protected void openTabLabelEditPopUp(final UITab tab) {
		tabLabelPopUp = openPopUp(Resources.Component.TAB_LABEL_POPUP.getLocation(), true);
		Button okButton = (Button) tabLabelPopUp.query("#okBtn");
		final Textbox textbox = (Textbox) tabLabelPopUp.query("#textbox");
		textbox.setText(tab.getLabel());
		okButton.addEventListener("onClick", new EventListener<Event>() {
			@Override
			public void onEvent(Event event) throws Exception {
				String label = textbox.getText();
				label.trim();
				if ( validateTabLabel(label) ) {
					List<UITab> tabs = tabsGroup.getTabs();
					int index = tabs.indexOf(tab);
					tabbox.setSelectedIndex(index);
					tab.setLabel(label);
					closePopUp(tabLabelPopUp);
					BindUtils.postNotifyChange(null, null, tab, "label");
				}
			}
			
			boolean validateTabLabel(String label) {
				if ( StringUtils.isBlank(label) ) {
					MessageUtil.showWarning("survey.layout.tab.label.error.required");
					return false;
				} else {
					return true;
				}
			}
		});
	}
	
	private void postTabChangedCommand(UITabsGroup parent) {
		Map<String, Object> args = new HashMap<String, Object>();
		args.put("tab", parent);
		BindUtils.postGlobalCommand(null, null, TAB_CHANGED_GLOBAL_COMMAND, args);
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
