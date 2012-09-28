/**
 * 
 */
package org.openforis.collect.designer.viewmodel;

import java.util.ArrayList;
import java.util.List;

import org.openforis.collect.designer.component.SchemaTreeModel;
import org.openforis.collect.designer.component.SchemaTreeModel.NodeDefinitionTreeNode;
import org.openforis.collect.designer.form.AttributeDefinitionFormObject;
import org.openforis.collect.designer.form.NodeDefinitionFormObject;
import org.openforis.collect.designer.form.NumericAttributeDefinitionFormObject;
import org.openforis.collect.designer.model.AttributeType;
import org.openforis.collect.designer.model.NodeType;
import org.openforis.collect.designer.util.MessageUtil;
import org.openforis.collect.designer.util.Resources;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.ui.UIConfiguration;
import org.openforis.collect.model.ui.UITab;
import org.openforis.collect.model.ui.UITabDefinition;
import org.openforis.idm.metamodel.AttributeDefault;
import org.openforis.idm.metamodel.AttributeDefinition;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.metamodel.Precision;
import org.openforis.idm.metamodel.Schema;
import org.zkoss.bind.BindUtils;
import org.zkoss.bind.Form;
import org.zkoss.bind.SimpleForm;
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
import org.zkoss.zk.ui.select.annotation.VariableResolver;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.DefaultTreeModel;
import org.zkoss.zul.Tree;
import org.zkoss.zul.TreeNode;
import org.zkoss.zul.Treeitem;

/**
 * 
 * @author S. Ricci
 *
 */
@VariableResolver(org.zkoss.zkplus.spring.DelegatingVariableResolver.class)
public class SurveySchemaEditVM extends SurveyEditBaseVM {

	private static final String SCHEMA_CHANGED_GLOBAL_COMMAND = "schemaChanged";
	
	private static final String ATTRIBUTE_DEFAULTS_FIELD = "attributeDefaults";
	private static final String NUMBER_ATTRIBUTE_PRECISIONS_FIELD = "precisions";
	
	private static final String CONFIRM_REMOVE_NODE_MESSAGE_KEY = "survey.schema.confirm_remove_node";
	private static final String CONFIRM_REMOVE_NON_EMPTY_ENTITY_MESSAGE_KEY = "survey.schema.confirm_remove_non_empty_entity";
	
	private SchemaTreeModel treeModel;
	private NodeDefinition selectedNode;
	private Form tempFormObject;
	private NodeDefinitionFormObject<NodeDefinition> formObject;
	private String nodeType;
	private String attributeType;
	private boolean editingNode;
	private List<AttributeDefault> attributeDefaults;
	private List<Precision> numericAttributePrecisions;
	
	@Wire
	private Tree nodesTree;
	
	@AfterCompose
	public void afterCompose(@ContextParam(ContextType.VIEW) Component view){
		 Selectors.wireComponents(view, this, false);
		 Selectors.wireEventListeners(view, this);
	}
	
	@Listen("onDrop = tree#nodesTree")
	public void nodesTreeDropHandler(DropEvent evt) {
		Component dragged = evt.getDragged();
		if ( dragged instanceof Treeitem ) {
			Treeitem treeItem = (Treeitem) dragged;
			if ( treeItem.getTree() == nodesTree ) {
				NodeDefinitionTreeNode treeNode = ((Treeitem) dragged).getValue();
				NodeDefinition nodeDefn = treeNode.getData();
				Component target = evt.getTarget();
				if ( target == nodesTree ) {
					
				} else if ( target instanceof Treeitem ) {
					
				}
			}
		}
	}

	@Command
	@NotifyChange({"nodes","editingNode","nodeType","attributeType",
		"tempFormObject","formObject","attributeDefaults","numericAttributePrecisions"})
	public void nodeSelected(@BindingParam("node") Treeitem node) {
		if ( node != null ) {
			TreeNode<NodeDefinition> treeNode = node.getValue();
			selectedNode = treeNode.getData();
			editingNode = true;
		} else {
			selectedNode = null;
			editingNode = false;
		}
		initFormObject(selectedNode);
	}
	
	@Command
	@NotifyChange({"nodes","editingNode","nodeType","attributeType",
		"tempFormObject","formObject","attributeDefaults","numericAttributePrecisions"})
	public void addRootEntity() {
		if ( checkCurrentFormValid() ) {
			editingNode = true;
			nodeType = NodeType.ENTITY.name();
			initFormObject();
			
			NodeDefinition newNode = createRootEntityNode();
			
			treeModel.appendNodeToSelected(newNode);
			
			selectedNode = newNode;
			
			postSchemaChangedCommand();
		}
	}

	@Command
	@NotifyChange({"nodes","editingNode","nodeType","attributeType","tempFormObject","formObject",
		"attributeDefaults","numericAttributePrecisions"})
	public void addNode(@BindingParam("nodeType") String nodeType, @BindingParam("attributeType") String attributeType) throws Exception {
		if ( checkCurrentFormValid() ) {
			if ( selectedNode != null && selectedNode instanceof EntityDefinition ) {
				editingNode = true;
				this.nodeType = nodeType;
				this.attributeType = attributeType;
				initFormObject();
				
				NodeType nodeTypeEnum = nodeType != null ? NodeType.valueOf(nodeType): null;
				AttributeType attributeTypeEnum = attributeType != null ? AttributeType.valueOf(attributeType): null;
				
				NodeDefinition newNode = NodeType.createNodeDefinition(survey, nodeTypeEnum, attributeTypeEnum );
				( (EntityDefinition) selectedNode).addChildDefinition(newNode);
				treeModel.appendNodeToSelected(newNode);
				
				selectedNode = newNode;
				postSchemaChangedCommand();
			} else {
				MessageUtil.showWarning("survey.schema.add_node.error.parent_entity_not_selected");
			}
		}
	}
	
	@Command
	public void removeNode() {
		if ( selectedNode != null ) {
			String confirmMessageKey;
			if (selectedNode instanceof EntityDefinition && !((EntityDefinition) selectedNode).getChildDefinitions().isEmpty() ) {
				confirmMessageKey = CONFIRM_REMOVE_NON_EMPTY_ENTITY_MESSAGE_KEY;
			} else {
				confirmMessageKey = CONFIRM_REMOVE_NODE_MESSAGE_KEY;
			}
			MessageUtil.showConfirm(new MessageUtil.ConfirmHandler() {
				@Override
				public void onOk() {
					performRemoveSelectedNode();
				}
			}, confirmMessageKey);
		}
	}

	protected void performRemoveSelectedNode() {
		EntityDefinition parentDefn = (EntityDefinition) selectedNode.getParentDefinition();
		if ( parentDefn != null ) {
			parentDefn.removeChildDefinition(selectedNode);
		} else {
			Schema schema = selectedNode.getSchema();
			String nodeName = selectedNode.getName();
			schema.removeRootEntityDefinition(nodeName);
		}
		treeModel.removeSelectedNode();
		editingNode = false;
		selectedNode = null;
		tempFormObject = null;
		formObject = null;
		BindUtils.postNotifyChange(null, null, this, "nodes");
		BindUtils.postNotifyChange(null, null, this, "editingNode");
		BindUtils.postNotifyChange(null, null, this, "tempFormObject");
		BindUtils.postNotifyChange(null, null, this, "formObject");
	}

	@Override
	@GlobalCommand
	@NotifyChange("currentFormValid")
	public void currentFormValidated(@BindingParam("valid") boolean valid) {
		super.currentFormValidated(valid);
		nodesTree.setNonselectableTags(valid ? "": "*");
	}
	
	@Command
	@NotifyChange({"nodes","selectedNode","tempFormObject","formObject","newNode","rootEntityCreation"})
	public void applyChanges() {
		formObject.saveTo(selectedNode, currentLanguageCode);
		postSchemaChangedCommand();
	}

	@Command
	public void openVersioningManagerPopUp() {
		if ( checkCurrentFormValid() ) {
			openPopUp(Resources.Component.VERSIONING_POPUP.getLocation(), true);
		}
	}

	@Command
	public void openCodeListsManagerPopUp() {
		if ( checkCurrentFormValid() ) {
			openPopUp(Resources.Component.CODE_LISTS_POPUP.getLocation(), true);
		}
	}

	@Command
	public void openSRSManagerPopUp() {
		if ( checkCurrentFormValid() ) {
			openPopUp(Resources.Component.SRS_MANAGER_POP_UP.getLocation(), true);
		}
	}

	@Command
	@NotifyChange("attributeDefaults")
	public void addAttributeDefault() {
		if ( attributeDefaults == null ) {
			initAttributeDefaultsList();
		}
		AttributeDefault attributeDefault = new AttributeDefault();
		attributeDefaults.add(attributeDefault);
	}
	
	@Command
	@NotifyChange("attributeDefaults")
	public void deleteAttributeDefault(@BindingParam("attributeDefault") AttributeDefault attributeDefault) {
		attributeDefaults.remove(attributeDefault);
	}
	
	@Command
	@NotifyChange("numericAttributePrecisions")
	public void addNumericAttributePrecision() {
		if ( numericAttributePrecisions == null ) {
			initNumericAttributePrecisionsList();
		}
		Precision precision = new Precision();
		numericAttributePrecisions.add(precision);
	}
	
	@Command
	@NotifyChange("numericAttributePrecisions")
	public void deleteNumericAttributePrecision(@BindingParam("precision") Precision precision) {
		numericAttributePrecisions.remove(precision);
	}
	
	protected EntityDefinition createRootEntityNode() {
		EntityDefinition newNode = (EntityDefinition) NodeType.createNodeDefinition(survey, NodeType.ENTITY, null);
		Schema schema = survey.getSchema();
		schema.addRootEntityDefinition((EntityDefinition) newNode);
		UITabDefinition tabDefn = createRootTabDefinition(newNode);
		createFirstTab(newNode, tabDefn);
		return newNode;
	}

	protected void createFirstTab(EntityDefinition newNode,
			UITabDefinition tabDefn) {
		UITab tab = new UITab();
		int tabPosition = 1;
		String tabName = "tab_" + tabPosition;
		tab.setName(tabName);
		tabDefn.addTab(tab);
		newNode.setAnnotation(UIConfiguration.TAB_NAME_ANNOTATION, tabName);
	}

	protected UITabDefinition createRootTabDefinition(EntityDefinition newNode) {
		UIConfiguration uiConf = survey.getUIConfiguration();
		UITabDefinition tabDefn = new UITabDefinition();
		int tabDefnPosition = uiConf.getTabDefinitions().size() + 1;
		String tabDefnName = "tabdefn_" + tabDefnPosition;
		tabDefn.setName(tabDefnName);
		uiConf.addTabDefinition(tabDefn);
		newNode.setAnnotation(UIConfiguration.TAB_DEFINITION_ANNOTATION, tabDefnName);
		return tabDefn;
	}

	protected void postSchemaChangedCommand() {
		BindUtils.postGlobalCommand(null, null, SCHEMA_CHANGED_GLOBAL_COMMAND, null);
	}
	
	protected void initAttributeDefaultsList() {
		if ( attributeDefaults == null ) {
			attributeDefaults = new ArrayList<AttributeDefault>();
			tempFormObject.setField(ATTRIBUTE_DEFAULTS_FIELD, attributeDefaults);
			((AttributeDefinitionFormObject<?>) formObject).setAttributeDefaults(attributeDefaults);
		}
	}
	
	protected void initNumericAttributePrecisionsList() {
		if ( numericAttributePrecisions == null ) {
			numericAttributePrecisions = new ArrayList<Precision>();
			tempFormObject.setField(NUMBER_ATTRIBUTE_PRECISIONS_FIELD, numericAttributePrecisions);
			((NumericAttributeDefinitionFormObject<?>) formObject).setPrecisions(numericAttributePrecisions);
		}
	}
	
	private void initFormObject() {
		NodeType nodeTypeEnum = null;
		AttributeType attributeTypeEnum = null;
		if ( nodeType != null ) {
			nodeTypeEnum = NodeType.valueOf(nodeType);
		}
		if ( attributeType != null ) {
			attributeTypeEnum = AttributeType.valueOf(attributeType);
		}
		formObject = NodeDefinitionFormObject.newInstance(nodeTypeEnum, attributeTypeEnum);
		tempFormObject = new SimpleForm();
		attributeDefaults = null;
		numericAttributePrecisions = null;
	}

	protected void initFormObject(NodeDefinition node) {
		calculateNodeType(node);
		initFormObject();
		formObject.loadFrom(node, currentLanguageCode);
		if ( formObject instanceof AttributeDefinitionFormObject ) {
			attributeDefaults = ((AttributeDefinitionFormObject<?>) formObject).getAttributeDefaults();
			tempFormObject.setField(ATTRIBUTE_DEFAULTS_FIELD, attributeDefaults);
			
			if ( formObject instanceof NumericAttributeDefinitionFormObject ) {
				numericAttributePrecisions = ((NumericAttributeDefinitionFormObject<?>) formObject).getPrecisions();
				tempFormObject.setField(NUMBER_ATTRIBUTE_PRECISIONS_FIELD, numericAttributePrecisions);
			}
		}
	}
	
	protected void calculateNodeType(NodeDefinition node) {
		NodeType nodeTypeEnum = NodeType.typeOf(node);
		nodeType = nodeTypeEnum.name();
		if ( nodeTypeEnum == NodeType.ATTRIBUTE) {
			AttributeType attributeTypeEnum = AttributeType.typeOf((AttributeDefinition) node);
			attributeType = attributeTypeEnum.name();
		} else {
			attributeType = null;
		}
	}
	
	public DefaultTreeModel<NodeDefinition> getNodes() {
		if ( treeModel == null ) {
			CollectSurvey survey = getSurvey();
			treeModel = SchemaTreeModel.createInstance(survey);
		}
		return treeModel;
    }

	public String getNodeType() {
		return nodeType;
	}

	public String getAttributeType() {
		return attributeType;
	}
	
	public NodeDefinition getSelectedNode() {
		return selectedNode;
	}
	
	public NodeDefinitionFormObject<NodeDefinition> getFormObject() {
		return formObject;
	}

	public boolean isEditingNode() {
		return editingNode;
	}

	public Form getTempFormObject() {
		return tempFormObject;
	}

	public List<AttributeDefault> getAttributeDefaults() {
		return attributeDefaults;
	}
	
	public List<Precision> getNumericAttributePrecisions() {
		return numericAttributePrecisions;
	}
	
}
