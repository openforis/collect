/**
 * 
 */
package org.openforis.collect.designer.viewmodel.layout;

import java.util.ArrayList;
import java.util.List;

import org.openforis.collect.designer.session.SessionStatus;
import org.openforis.collect.designer.util.Resources;
import org.openforis.collect.designer.viewmodel.BaseVM;
import org.openforis.collect.metamodel.ui.UIOptions;
import org.openforis.collect.metamodel.ui.UITab;
import org.openforis.collect.metamodel.ui.UITabSet;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.idm.metamodel.ModelVersion;
import org.openforis.idm.metamodel.NodeDefinition;
import org.zkoss.bind.annotation.AfterCompose;
import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.ExecutionArgParam;
import org.zkoss.bind.annotation.GlobalCommand;
import org.zkoss.bind.annotation.Init;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.Include;

/**
 * @author S. Ricci
 *
 */
public class TabsGroupPanelVM extends BaseVM {

	@Wire
	private Include listOfNodesInclude;

	private UITab tab;
	
	@Init
	public void init(@ExecutionArgParam("tab") UITab tab) {
		this.tab = tab;
	}
	
	@AfterCompose
	public void afterCompose(@ContextParam(ContextType.VIEW) Component view){
		 Selectors.wireComponents(view, this, false);
		 Selectors.wireEventListeners(view, this);
	}
	
	@GlobalCommand
	public void nodeAssignedToTab(@BindingParam("oldTab") UITab oldTab, @BindingParam("newTab") UITab newTab) {
		if ( oldTab != null && oldTab.equals(tab) || newTab != null && newTab.equals(tab) ) {
//			BindUtils.postNotifyChange(null, null, this, "nodesPerTab");
			refreshListOfNodes();
		}
	}
	
	@GlobalCommand
	public void nodesPerTabChanged(@BindingParam("tab") UITabSet tab) {
		if ( tab.equals(this.tab) ) {
			//BindUtils.postNotifyChange(null, null, this, "nodesPerTab");
			refreshListOfNodes();
		}
	}
	
	protected void refreshListOfNodes() {
		listOfNodesInclude.setSrc(null);
		List<NodeDefinition> nodesPerTab = getNodesPerTab();
		listOfNodesInclude.setDynamicProperty("tab", tab);
		listOfNodesInclude.setDynamicProperty("nodes", nodesPerTab);
		listOfNodesInclude.setSrc(Resources.Component.TABSGROUP_LIST_OF_NODES.getLocation());
	}
	
	public List<NodeDefinition> getNodesPerTab() {
		UIOptions uiOpts = getUIOptions();
		List<NodeDefinition> result = new ArrayList<NodeDefinition>();
		List<NodeDefinition> nodesPerTab = uiOpts.getNodesPerTab(tab, false);
		ModelVersion version = getFormVersion();
		for (NodeDefinition nodeDefn : nodesPerTab) {
			if ( version == null || version.isApplicable(nodeDefn) ) {
				result.add(nodeDefn);
			}
		}
		return result;
	}
	
	protected CollectSurvey getSurvey() {
		SessionStatus sessionStatus = super.getSessionStatus();
		CollectSurvey survey = sessionStatus.getSurvey();
		return survey;
	}
	
	public ModelVersion getFormVersion() {
		SessionStatus sessionStatus = super.getSessionStatus();
		ModelVersion version = sessionStatus.getLayoutFormVersion();
		return version;
	}
	
	protected UIOptions getUIOptions() {
		CollectSurvey survey = getSurvey();
		UIOptions uiConf = survey.getUIOptions();
		return uiConf;
	}
	
	public UITab getTab() {
		return tab;
	}

}
