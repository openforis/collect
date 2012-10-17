/**
 * 
 */
package org.openforis.collect.designer.viewmodel;

import java.util.ArrayList;
import java.util.List;

import org.openforis.collect.designer.component.SchemaTreeModel;
import org.openforis.collect.designer.form.AttributeDefinitionFormObject;
import org.openforis.collect.designer.form.NodeDefinitionFormObject;
import org.openforis.collect.designer.form.NumericAttributeDefinitionFormObject;
import org.openforis.collect.designer.model.AttributeType;
import org.openforis.collect.designer.model.NodeType;
import org.openforis.collect.designer.util.MessageUtil;
import org.openforis.collect.designer.util.Resources;
import org.openforis.collect.metamodel.ui.UIOptions;
import org.openforis.collect.metamodel.ui.UITab;
import org.openforis.collect.metamodel.ui.UITabSet;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.idm.metamodel.AttributeDefault;
import org.openforis.idm.metamodel.AttributeDefinition;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.metamodel.Precision;
import org.openforis.idm.metamodel.Schema;
import org.zkoss.bind.BindUtils;
import org.zkoss.bind.Binder;
import org.zkoss.bind.Form;
import org.zkoss.bind.SimpleForm;
import org.zkoss.bind.annotation.AfterCompose;
import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.DependsOn;
import org.zkoss.bind.annotation.GlobalCommand;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.IdSpace;
import org.zkoss.zk.ui.Path;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.VariableResolver;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.Include;
import org.zkoss.zul.Tree;
import org.zkoss.zul.TreeNode;
import org.zkoss.zul.Treeitem;
import org.zkoss.zul.Window;

/**
 * 
 * @author S. Ricci
 *
 */
@VariableResolver(org.zkoss.zkplus.spring.DelegatingVariableResolver.class)
public class SurveySchemaEditVM extends SurveyBaseVM {

	private static final String TAB_NAME_PREFIX = "tab_";

	private static final String ROOT_TABSET_NAME_PREFIX = "tabset_";

	private static final String SCHEMA_CHANGED_GLOBAL_COMMAND = "schemaChanged";
	
	private static final String ATTRIBUTE_DEFAULTS_FIELD = "attributeDefaults";
	private static final String NUMBER_ATTRIBUTE_PRECISIONS_FIELD = "precisions";
	
	private static final String CONFIRM_REMOVE_NODE_MESSAGE_KEY = "survey.schema.confirm_remove_node";
	private static final String CONFIRM_REMOVE_NON_EMPTY_ENTITY_MESSAGE_KEY = "survey.schema.confirm_remove_non_empty_entity";
	
	private SchemaTreeModel treeModel;
	private NodeDefinition selectedNode;
	private NodeDefinition editedNode;
	private EntityDefinition parentEntity;
	private Form tempFormObject;
	private NodeDefinitionFormObject<NodeDefinition> formObject;
	private String nodeType;
	private String attributeType;
	private boolean newNode;
	private List<AttributeDefault> attributeDefaults;
	private List<Precision> numericAttributePrecisions;
	
	@Wire
	private Tree nodesTree;
	@Wire
	private Include nodeFormInclude;
	
	//popups
	private Window unitsPopUp;
	private Window codeListsPopUp;
	private Window versioningPopUp;
	
	@AfterCompose
	public void afterCompose(@ContextParam(ContextType.VIEW) Component view){
		 Selectors.wireComponents(view, this, false);
		 Selectors.wireEventListeners(view, this);
	}
	
	@Command
	@NotifyChange({"nodes","editingNode","nodeType","attributeType",
		"moveNodeUpDisabled","moveNodeDownDisabled",
		"tempFormObject","formObject","attributeDefaults","numericAttributePrecisions"})
	public void nodeSelected(@BindingParam("node") Treeitem node) {
		if ( node != null ) {
			TreeNode<NodeDefinition> treeNode = node.getValue();
			selectedNode = treeNode.getData();
			editedNode = selectedNode;
		} else {
			selectedNode = null;
			editedNode = null;
		}
		newNode = false;
		initFormObject(selectedNode);
		refreshNodeForm();
	}
	
	@Command
	public void addRootEntity(@ContextParam(ContextType.BINDER) Binder binder) {
		if ( checkCurrentFormValid() ) {
			newNode = true;
			parentEntity = null;
			nodeType = NodeType.ENTITY.name();
			initFormObject();
			
			NodeDefinition newNode = createRootEntityNode();
			
			onAfterNodeCreated(binder, newNode);
		}
	}

	@Command
	public void addNode(@ContextParam(ContextType.BINDER) Binder binder, @BindingParam("nodeType") String nodeType, @BindingParam("attributeType") String attributeType) throws Exception {
		if ( checkCurrentFormValid() ) {
			if ( selectedNode != null && selectedNode instanceof EntityDefinition ) {
				newNode = true;
				parentEntity = (EntityDefinition) selectedNode;
				this.nodeType = nodeType;
				this.attributeType = attributeType;
				initFormObject();
				
				NodeType nodeTypeEnum = nodeType != null ? NodeType.valueOf(nodeType): null;
				AttributeType attributeTypeEnum = attributeType != null ? AttributeType.valueOf(attributeType): null;
				
				NodeDefinition newNode = NodeType.createNodeDefinition(survey, nodeTypeEnum, attributeTypeEnum );
				editedNode = newNode;
//				( (EntityDefinition) selectedNode).addChildDefinition(newNode);
				onAfterNodeCreated(binder, newNode);
			} else {
				MessageUtil.showWarning("survey.schema.add_node.error.parent_entity_not_selected");
			}
		}
	}
	
	protected void onAfterNodeCreated(Binder binder, NodeDefinition newNode) {
//		treeModel.appendNodeToSelected(newNode);
//		
//		selectedNode = newNode;
		
		editedNode = newNode;
		selectedNode = null;
		
		treeModel.select(null);
		
		refreshNodeForm();
		
		notifyChange("nodes","selectedNode","editedNode","nodeType","attributeType",
				"tempFormObject","formObject", "attributeDefaults","numericAttributePrecisions");

		validateForm(binder);
	}
	
	protected void refreshNodeForm() {
		nodeFormInclude.setSrc(null);
		nodeFormInclude.setSrc(Resources.Component.NODE.getLocation());
		NodeType nodeTypeEnum = NodeType.valueOf(nodeType);
		String nodeDetailFormIncludeSrc = null;
		switch(nodeTypeEnum) {
		case ENTITY:
			nodeDetailFormIncludeSrc = Resources.COMPONENTS_BASE_PATH + "survey_edit/schema/entity.zul";
			break;
		case ATTRIBUTE:
			if ( attributeType != null ) {
				nodeDetailFormIncludeSrc = Resources.COMPONENTS_BASE_PATH + "survey_edit/schema/attribute_" + attributeType.toLowerCase() + ".zul";
			}
			break;
		}
		IdSpace nodeFormIncludeIdSpace = nodeFormInclude.getSpaceOwner();
		Include nodeDetailFormInclude = (Include) Path.getComponent(nodeFormIncludeIdSpace, "nodeDetailFormInclude");
		nodeDetailFormInclude.setSrc(nodeDetailFormIncludeSrc);
	}
	
	protected void validateForm() {
		if ( editedNode != null ) {
			Binder binder = (Binder) nodeFormInclude.getAttribute("$BINDER$");
			validateForm(binder);
		}
	}
		
	protected void validateForm(@ContextParam(ContextType.BINDER) Binder binder) {
		Component view = binder.getView();
		IdSpace currentIdSpace = view.getSpaceOwner();
		Component formComponent = Path.getComponent(currentIdSpace, "nodeFormInclude/nodeDetailFormInclude/nodeFormContainer");
		Binder formComponentBinder = (Binder) formComponent.getAttribute("binder");
		formComponentBinder.postCommand("applyChanges", null);
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

	@Command
	@NotifyChange({"nodes","moveNodeUpDisabled","moveNodeDownDisabled"})
	public void moveNodeUp() {
		moveNode(true);
	}
	
	@Command
	@NotifyChange({"nodes","moveNodeUpDisabled","moveNodeDownDisabled"})
	public void moveNodeDown() {
		moveNode(false);
	}
	
	protected void moveNode(boolean up) {
		int newIndex;
		EntityDefinition parentDefn = (EntityDefinition) selectedNode.getParentDefinition();
		if ( parentDefn != null ) {
			int oldIndex = parentDefn.getChildIndex(selectedNode);
			newIndex = up ? oldIndex - 1: oldIndex + 1;
			parentDefn.moveChildDefinition(selectedNode, newIndex);
		} else {
			EntityDefinition rootEntity = selectedNode.getRootEntity();
			Schema schema = rootEntity.getSchema();
			int oldIndex = schema.getRootEntityIndex(rootEntity);
			newIndex = up ? oldIndex - 1: oldIndex + 1;
			schema.moveRootEntityDefinition(rootEntity, newIndex);
		}
		treeModel.moveSelectedNode(newIndex);
		dispatchSchemaChangedCommand();
	}
	
	protected void performRemoveSelectedNode() {
		EntityDefinition parentDefn = (EntityDefinition) selectedNode.getParentDefinition();
		if ( parentDefn != null ) {
			parentDefn.removeChildDefinition(selectedNode);
		} else {
			UIOptions uiOpts = survey.getUIOptions();
			UITabSet tabSet = uiOpts.getTabSet((EntityDefinition) selectedNode);
			uiOpts.removeTabSet(tabSet);
			Schema schema = selectedNode.getSchema();
			String nodeName = selectedNode.getName();
			schema.removeRootEntityDefinition(nodeName);
		}
		treeModel.removeSelectedNode();
		selectedNode = null;
		editedNode = null;
		tempFormObject = null;
		formObject = null;
		notifyChange("nodes","editingNode","tempFormObject","formObject",
				"moveNodeUpDisabled","moveNodeDownDisabled");
		dispatchCurrentFormValidatedCommand(true);
		updateTreeSelectionActivation();
	}

	@Override
	@GlobalCommand
	@NotifyChange("currentFormValid")
	public void currentFormValidated(@BindingParam("valid") boolean valid) {
		super.currentFormValidated(valid);
		updateTreeSelectionActivation();
	}

	protected void updateTreeSelectionActivation() {
		nodesTree.setNonselectableTags(isCurrentFormValid() ? "": "*");
	}
	
	@Override
	@GlobalCommand
	public void currentLanguageChanged() {
		super.currentLanguageChanged();
		if ( isEditingNode() ) {
			initFormObject(editedNode);
//			refreshNodeForm();
			notifyChange("tempFormObject","formObject","attributeDefaults","numericAttributePrecisions");
		}
	}
	
	@Command
	@NotifyChange({"nodes","selectedNode","tempFormObject","formObject","newNode","rootEntityCreation"})
	public void applyChanges() {
		formObject.saveTo(editedNode, currentLanguageCode);
		if ( newNode ) {
			if ( parentEntity == null ) {
				Schema schema = editedNode.getSchema();
				schema.addRootEntityDefinition((EntityDefinition) editedNode);
			} else {
				parentEntity.addChildDefinition(editedNode);
			}
			treeModel.select(parentEntity);
			treeModel.appendNodeToSelected(editedNode);
			selectedNode = editedNode;
			newNode = false;
		}
		dispatchSchemaChangedCommand();
	}

	@Command
	public void openVersioningManagerPopUp() {
		dispatchCurrentFormValidatedCommand(true);
		versioningPopUp = openPopUp(Resources.Component.VERSIONING_POPUP.getLocation(), true);
	}

	@GlobalCommand
	public void closeVersioningManagerPopUp() {
		if ( checkCurrentFormValid() ) {
			closePopUp(versioningPopUp);
			validateForm();
		}
	}
	
	@Command
	public void openCodeListsManagerPopUp() {
		dispatchCurrentFormValidatedCommand(true);
		codeListsPopUp = openPopUp(Resources.Component.CODE_LISTS_POPUP.getLocation(), true);
	}

	@GlobalCommand
	public void closeCodeListsManagerPopUp() {
		if ( checkCurrentFormValid() ) {
			closePopUp(codeListsPopUp);
			validateForm();
		}
	}
	
	@Command
	public void openUnitsManagerPopUp() {
		dispatchCurrentFormValidatedCommand(true);
		unitsPopUp = openPopUp(Resources.Component.UNITS_MANAGER_POP_UP.getLocation(), true);
	}
	
	@GlobalCommand
	public void closeUnitsManagerPopUp() {
		if ( checkCurrentFormValid() ) {
			closePopUp(unitsPopUp);
			validateForm();
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
		UITabSet tabSet = createRootTabSet(newNode);
		newNode.setAnnotation(UIOptions.Annotation.TAB_SET.getQName(), tabSet.getName());
		return newNode;
	}

	protected void addFirstTab(EntityDefinition newNode,
			UITabSet tabSet) {
		UITab tab = new UITab();
		int tabPosition = 1;
		String tabName = TAB_NAME_PREFIX + tabPosition;
		tab.setName(tabName);
		tabSet.addTab(tab);
	}

	protected UITabSet createRootTabSet(EntityDefinition rootEntity) {
		UIOptions uiOpts = survey.getUIOptions();
		UITabSet tabSet = new UITabSet();
		int tabSetPosition = uiOpts.getTabSets().size() + 1;
		String tabSetName = ROOT_TABSET_NAME_PREFIX + tabSetPosition;
		tabSet.setName(tabSetName);
		addFirstTab(rootEntity, tabSet);
		uiOpts.addTabSet(tabSet);
		return tabSet;
	}

	protected void dispatchSchemaChangedCommand() {
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
	
	public SchemaTreeModel getNodes() {
		if ( treeModel == null ) {
			CollectSurvey survey = getSurvey();
			treeModel = SchemaTreeModel.createInstance(survey);
		}
		return treeModel;
    }

	public List<NodeDefinition> getSiblings(NodeDefinition nodeDefinition) {
		List<NodeDefinition> siblings = new ArrayList<NodeDefinition>();
		EntityDefinition parentDefn = (EntityDefinition) selectedNode.getParentDefinition();
		if ( parentDefn != null ) {
			siblings.addAll(parentDefn.getChildDefinitions());
		} else {
			EntityDefinition rootEntity = selectedNode.getRootEntity();
			Schema schema = rootEntity.getSchema();
			siblings.addAll(schema.getRootEntityDefinitions());
		}
		siblings.remove(nodeDefinition);
		return siblings;
	}
	
	public int getSelectedNodeIndex() {
		int index;
		if ( selectedNode != null ) {
			EntityDefinition parentDefn = (EntityDefinition) selectedNode.getParentDefinition();
			if ( parentDefn != null ) {
				index = parentDefn.getChildIndex(selectedNode);
			} else {
				EntityDefinition rootEntity = selectedNode.getRootEntity();
				Schema schema = rootEntity.getSchema();
				index = schema.getRootEntityIndex(rootEntity);
			}
		} else {
			index = -1;
		}
		return index;
	}

	public boolean isMoveNodeUpDisabled() {
		int index = getSelectedNodeIndex();
		return index <= 0;
	}
	
	public boolean isMoveNodeDownDisabled() {
		if ( selectedNode != null ) {
			List<NodeDefinition> siblings = getSiblings(selectedNode);
			int index = getSelectedNodeIndex();
			return index < 0 || index >= siblings.size();
		} else {
			return true;
		}
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

	public NodeDefinition getEditedNode() {
		return editedNode;
	}
	
	@DependsOn("editedNode")
	public boolean isEditingNode() {
		return editedNode != null;
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

	public boolean isNewNode() {
		return newNode;
	}
	
}
