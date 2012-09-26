package org.openforis.collect.designer.viewmodel;

import java.util.HashMap;
import java.util.Map;

import org.openforis.collect.designer.component.SchemaTreeModel;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.ui.UIConfiguration;
import org.openforis.collect.model.ui.UITab;
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
import org.zkoss.zk.ui.event.DropEvent;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.DefaultTreeModel;
import org.zkoss.zul.Include;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.Tree;
import org.zkoss.zul.TreeNode;
import org.zkoss.zul.Treeitem;

/**
 * 
 * @author S. Ricci
 *
 */
public class SurveyLayoutEditVM extends SurveyEditBaseVM {

	private static final String NODES_PER_TAB_CHANGED_GLOABAL_COMMAND = "nodesPerTabChanged";
	private static final String TABSGROUP_URL = "/view/designer/component/schema_layout/tabsgroup.zul";
	private UITabDefinition tabsDefinition;
	private SchemaTreeModel treeModel;
	
	@Wire
	private Tree nodesTree;
	@Wire
	private Include tabsGroupContainerInclude;
	
	@AfterCompose
	public void afterCompose(@ContextParam(ContextType.VIEW) Component view){
		 Selectors.wireComponents(view, this, false);
		 Selectors.wireEventListeners(view, this);
	}
	
	@GlobalCommand
	@NotifyChange({"nodes"})
	public void schemaChanged() {
		initTreeModel();
	}
	
	@Command
	@NotifyChange({"tabDefinition"})
	public void nodeSelected(@BindingParam("node") Treeitem node) {
		UITabDefinition tabDefinition = extractTabDefinition(node);
		refreshTabDefinitionLayoutPanel(tabDefinition);
		this.tabsDefinition = tabDefinition;
		postTabDefinitionChangedCommand();
	}

	protected void postTabDefinitionChangedCommand() {
		Map<String, Object> args = new HashMap<String, Object>();
		args.put("tabDefinition", tabsDefinition);
		BindUtils.postGlobalCommand(null, null, "tabDefinitionChanged", args);
	}

	protected void refreshTabDefinitionLayoutPanel(UITabDefinition tabDefinition) {
		if ( tabDefinition == null ) {
			tabsGroupContainerInclude.setSrc(null);
		} else if ( this.tabsDefinition != tabDefinition) {
			tabsGroupContainerInclude.setDynamicProperty("tabsGroup", tabDefinition);
			tabsGroupContainerInclude.setSrc(TABSGROUP_URL);
		}
	}
	
	protected UITabDefinition extractTabDefinition(Treeitem treeItem) {
		if ( treeItem != null ) {
			TreeNode<NodeDefinition> treeNode = treeItem.getValue();
			NodeDefinition nodeDefn = treeNode.getData();
			UIConfiguration uiConfiguration = survey.getUIConfiguration();
			EntityDefinition rootEntity = nodeDefn.getRootEntity();
			return uiConfiguration.getTabDefinition(rootEntity);
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
			UIConfiguration uiConf = survey.getUIConfiguration();
			UITab oldTab = uiConf.getTab(node, false);
			uiConf.removeTabAssociation(node);
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

}
