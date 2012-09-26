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
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.DropEvent;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.VariableResolver;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.DefaultTreeModel;
import org.zkoss.zul.Listitem;
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
	private static final String VERSIONING_POPUP_URL = "versioning_popup.zul";
	private static final String ATTRIBUTE_DEFAULTS_FIELD = "attributeDefaults";
	private static final String NUMBER_ATTRIBUTE_PRECISIONS_FIELD = "precisions";
	private static final String CODE_LISTS_POPUP_URL = "code_lists_popup.zul";
	private static final String SRS_POPUP_URL = "srs_popup.zul";
	
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

	protected EntityDefinition createRootEntityNode() {
		EntityDefinition newNode = (EntityDefinition) NodeType.createNodeDefinition(survey, NodeType.ENTITY, null);
		Schema schema = survey.getSchema();
		schema.addRootEntityDefinition((EntityDefinition) newNode);
		UITabDefinition tabDefn = createRootTabDefinition(newNode);
		createFirstTab(newNode, tabDefn);
		return newNode;
	}

	private void createFirstTab(EntityDefinition newNode,
			UITabDefinition tabDefn) {
		UITab tab = new UITab();
		int tabPosition = 1;
		String tabName = "tab_" + tabPosition;
		tab.setName(tabName);
		tabDefn.addTab(tab);
		newNode.setAnnotation(UIConfiguration.TAB_NAME_ANNOTATION, tabName);
	}

	private UITabDefinition createRootTabDefinition(EntityDefinition newNode) {
		UIConfiguration uiConf = survey.getUIConfiguration();
		UITabDefinition tabDefn = new UITabDefinition();
		int tabDefnPosition = uiConf.getTabDefinitions().size() + 1;
		String tabDefnName = "tabdefn_" + tabDefnPosition;
		tabDefn.setName(tabDefnName);
		uiConf.addTabDefinition(tabDefn);
		newNode.setAnnotation(UIConfiguration.TAB_DEFINITION_ANNOTATION, tabDefnName);
		return tabDefn;
	}

	private void postSchemaChangedCommand() {
		BindUtils.postGlobalCommand(null, null, SCHEMA_CHANGED_GLOBAL_COMMAND, null);
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
	@NotifyChange({"nodes","selectedNode","tempFormObject","formObject","newNode","rootEntityCreation"})
	public void applyChanges() {
		formObject.saveTo(selectedNode, currentLanguageCode);
		postSchemaChangedCommand();
	}

	@Command
	public void openVersioningManagerPopUp() {
		if ( checkCurrentFormValid() ) {
			openPopUp(VERSIONING_POPUP_URL, true);
		}
	}

	@Command
	public void openCodeListsManagerPopUp() {
		if ( checkCurrentFormValid() ) {
			openPopUp(CODE_LISTS_POPUP_URL, true);
		}
	}

	@Command
	public void openSRSManagerPopUp() {
		if ( checkCurrentFormValid() ) {
			openPopUp(SRS_POPUP_URL, true);
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
