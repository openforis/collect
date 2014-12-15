package org.openforis.collect.designer.viewmodel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openforis.collect.designer.component.SchemaTreeModel;
import org.openforis.collect.designer.component.SchemaTreeModel.SchemaNodeData;
import org.openforis.collect.designer.session.SessionStatus;
import org.openforis.collect.designer.util.Resources;
import org.openforis.collect.metamodel.ui.UIOptions;
import org.openforis.collect.metamodel.ui.UITab;
import org.openforis.collect.metamodel.ui.UITabSet;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.ModelVersion;
import org.openforis.idm.metamodel.NodeDefinition;
import org.zkoss.bind.BindUtils;
import org.zkoss.bind.annotation.AfterCompose;
import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.GlobalCommand;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.DropEvent;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zkplus.databind.BindingListModelList;
import org.zkoss.zul.DefaultTreeModel;
import org.zkoss.zul.Include;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.TreeNode;
import org.zkoss.zul.Treeitem;

/**
 * 
 * @author S. Ricci
 *
 */
public class SchemaLayoutVM extends SurveyBaseVM {

	private static final String NODES_PER_TAB_CHANGED_GLOABAL_COMMAND = "nodesPerTabChanged";
	
	private UITabSet rootTabSet;
	private SchemaTreeModel treeModel;
	
//	@Wire
//	private Tree nodesTree;
	@Wire
	private Include tabsGroupContainerInclude;
	
	@AfterCompose
	public void afterCompose(@ContextParam(ContextType.VIEW) Component view){
		 Selectors.wireComponents(view, this, false);
		 Selectors.wireEventListeners(view, this);
	}
	
	@Override
	@GlobalCommand
	@NotifyChange("formVersions")
	public void versionsUpdated() {}
	
	@GlobalCommand
	@NotifyChange({"nodes"})
	public void schemaChanged() {
		initTreeModel();
		nodeSelected(null);
	}
	
	@Command
	@NotifyChange({"rootTabSet"})
	public void nodeSelected(@BindingParam("node") Treeitem node) {
		List<ModelVersion> versions = survey.getVersions();
		setFormVersion(versions.isEmpty() ? null: versions.get(0));
		UITabSet tabSet = getRootTabSet(node);
		refreshTabSetLayoutPanel(tabSet, false);
		this.rootTabSet = tabSet;
		dispatchTabSetChangedCommand();
	}

	@Command
	@NotifyChange({"nodes"})
	public void formVersionChanged(@BindingParam("version") ModelVersion version) {
		setFormVersion(version);
		refreshTabSetLayoutPanel(this.rootTabSet, true);
		initTreeModel();
		Map<String, Object> args = new HashMap<String, Object>();
		args.put("version", version);
		BindUtils.postGlobalCommand(null, null, "layoutFormVersionChanged", args);
	}
	
	protected void dispatchTabSetChangedCommand() {
		Map<String, Object> args = new HashMap<String, Object>();
		args.put("tabSet", rootTabSet);
		BindUtils.postGlobalCommand(null, null, "tabSetChanged", args);
	}

	protected void refreshTabSetLayoutPanel(UITabSet tabSet, boolean forceRefresh) {
		if ( tabSet == null ) {
			tabsGroupContainerInclude.setSrc(null);
		} else if ( forceRefresh || this.rootTabSet != tabSet) {
			tabsGroupContainerInclude.setSrc(null); //workaround: include is not refreshed otherwise
			tabsGroupContainerInclude.setDynamicProperty("tabSet", tabSet);
			tabsGroupContainerInclude.setSrc(Resources.Component.TABSGROUP.getLocation());
		}
	}
	
	protected UITabSet getRootTabSet(Treeitem treeItem) {
		if ( treeItem != null ) {
			TreeNode<NodeDefinition> treeNode = treeItem.getValue();
			NodeDefinition nodeDefn = treeNode.getData();
			UIOptions uiOptions = survey.getUIOptions();
			EntityDefinition rootEntity = nodeDefn.getRootEntity();
			return uiOptions.getAssignedRootTabSet(rootEntity);
		} else {
			return null;
		}
	}
	
	@Listen("onDrop = tree#nodesTree")
	public void listOfNodesDropHandler(DropEvent evt) {
		Component dragged = evt.getDragged();
		if ( dragged instanceof Listitem ) {
			NodeDefinition node = ((Listitem) dragged).getValue();
			CollectSurvey survey = getSurvey();
			UIOptions uiOpts = survey.getUIOptions();
			UITab oldTab = uiOpts.getAssignedTab(node, false);
			uiOpts.removeTabAssociation(node);
			if ( oldTab != null ) {
				postNodePerTabChangedCommand(oldTab);
			}
		}
	}

	protected void postNodePerTabChangedCommand(UITab tab) {
		Map<String, Object> args = new HashMap<String, Object>();
		args.put("tab", tab);
		BindUtils.postGlobalCommand(null, null, NODES_PER_TAB_CHANGED_GLOABAL_COMMAND, args);
	}
	
	public UITabSet getRootTabSet() {
		return rootTabSet;
	}
	
	public DefaultTreeModel<SchemaNodeData> getNodes() {
		if ( treeModel == null ) {
			initTreeModel();
		}
		return treeModel;
    }
	
	public boolean isAssociatedToTab(NodeDefinition nodeDefn) {
		UIOptions uiOptions = survey.getUIOptions();
		boolean result = uiOptions.isAssociatedToTab(nodeDefn);
		return result;
	}
	
	public List<ModelVersion> getFormVersions() {
		CollectSurvey survey = getSurvey();
		List<ModelVersion> result = new ArrayList<ModelVersion>(survey.getVersions());
		return new BindingListModelList<ModelVersion>(result, false);
	}

	protected void initTreeModel() {
		CollectSurvey survey = getSurvey();
		ModelVersion formVersion = getFormVersion();
		treeModel = SchemaTreeModel.createInstance(survey, formVersion, true);
	}

	public ModelVersion getFormVersion() {
		SessionStatus sessionStatus = getSessionStatus();
		return sessionStatus.getLayoutFormVersion();
	}

	public void setFormVersion(ModelVersion formVersion) {
		SessionStatus sessionStatus = getSessionStatus();
		sessionStatus.setLayoutFormVersion(formVersion);
	}

}
