package org.openforis.collect.designer.viewmodel;

import java.util.HashMap;
import java.util.Map;

import org.openforis.collect.designer.component.SchemaTreeModel;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.ui.UIConfiguration;
import org.openforis.collect.model.ui.UITabDefinition;
import org.openforis.idm.metamodel.EntityDefinition;
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
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.DefaultTreeModel;
import org.zkoss.zul.Include;
import org.zkoss.zul.TreeNode;
import org.zkoss.zul.Treeitem;

/**
 * 
 * @author S. Ricci
 *
 */
public class SurveyLayoutEditVM extends SurveyEditBaseVM {

	private static final String TABSGROUP_URL = "/view/designer/component/schema_layout/tabsgroup.zul";
	private UITabDefinition tabsDefinition;
	private SchemaTreeModel treeModel;
	private NodeDefinition selectedNode;
	
	@Wire
	private Include tabsGroupContainerInclude;
	
	@AfterCompose
	public void afterCompose(@ContextParam(ContextType.VIEW) Component view){
		 Selectors.wireComponents(view, this, false);
	}
	
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
			tabsDefinition = uiConfiguration.getTabDefinition(rootEntity);
		} else {
			selectedNode = null;
			tabsDefinition = null;
		}
		Map<String, Object> args = new HashMap<String, Object>();
		args.put("tabDefinition", tabsDefinition);
		BindUtils.postGlobalCommand(null, null, "tabDefinitionChanged", args);
		
		tabsGroupContainerInclude.setSrc(null);
		if ( tabsDefinition != null ) {
			tabsGroupContainerInclude.setDynamicProperty("tabsGroup", tabsDefinition);
			tabsGroupContainerInclude.setSrc(TABSGROUP_URL);
		}
	}
	
	public UITabDefinition getTabDefinition() {
		return tabsDefinition;
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
