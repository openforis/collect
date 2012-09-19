package org.openforis.collect.designer.viewmodel;

import org.openforis.collect.designer.component.SchemaTreeModel;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.ui.UIConfiguration;
import org.openforis.collect.model.ui.UITab;
import org.openforis.collect.model.ui.UITabDefinition;
import org.openforis.collect.model.ui.UITabsGroup;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.NodeDefinition;
import org.zkoss.bind.BindUtils;
import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.GlobalCommand;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.zul.DefaultTreeModel;
import org.zkoss.zul.TreeNode;
import org.zkoss.zul.Treeitem;

/**
 * 
 * @author S. Ricci
 *
 */
public class SurveyLayoutEditVM extends SurveyEditBaseVM {

	private UITabDefinition tabDefinition;
	private SchemaTreeModel treeModel;
	private NodeDefinition selectedNode;
	
	@GlobalCommand
	@NotifyChange({"nodes"})
	public void schemaChanged() {
		initTreeModel();
	}
	
	@Command
	@NotifyChange({"tabDefinition","selectedNode"})
	public void nodeSelected(@BindingParam("node") Treeitem node) {
		if ( node != null ) {
			TreeNode<NodeDefinition> treeNode = node.getValue();
			selectedNode = treeNode.getData();
			UIConfiguration uiConfiguration = survey.getUIConfiguration();
			EntityDefinition rootEntity = selectedNode.getRootEntity();
			tabDefinition = uiConfiguration.getTabDefinition(rootEntity);
		} else {
			selectedNode = null;
			tabDefinition = null;
		}
	}
	
	@GlobalCommand
	public void addTab(@BindingParam("group") UITabsGroup group) {
		UITab tab = new UITab();
		int tabPosition = group.getTabs().size() + 1;
		tab.setName("tab_" + tabPosition);
		group.addTab(tab);
		BindUtils.postNotifyChange(null, null, group, "tabs");
	}

	@Command
	@NotifyChange({"tabDefinition"})
	public void removeTab(@BindingParam("group") UITabsGroup group, @BindingParam("tab") UITab tab) {
		group.removeTab(tab);
	}
	
	public UITabDefinition getTabDefinition() {
		return tabDefinition;
	}
	
	public DefaultTreeModel<NodeDefinition> getNodes() {
		if ( treeModel == null ) {
			initTreeModel();
		}
		return treeModel;
    }

	protected void initTreeModel() {
		CollectSurvey survey = getSurvey();
		treeModel = SchemaTreeModel.createInstance(survey);
	}

	public NodeDefinition getSelectedNode() {
		return selectedNode;
	}
	

}
