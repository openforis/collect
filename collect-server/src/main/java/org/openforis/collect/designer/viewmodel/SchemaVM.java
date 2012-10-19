/**
 * 
 */
package org.openforis.collect.designer.viewmodel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.openforis.collect.designer.component.SchemaTreeModel;
import org.openforis.collect.designer.form.EntityDefinitionFormObject;
import org.openforis.collect.designer.model.AttributeType;
import org.openforis.collect.designer.model.NodeType;
import org.openforis.collect.designer.util.MessageUtil;
import org.openforis.collect.designer.util.Resources;
import org.openforis.collect.metamodel.ui.UIOptions;
import org.openforis.collect.metamodel.ui.UITab;
import org.openforis.collect.metamodel.ui.UITabSet;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.idm.metamodel.AttributeDefinition;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.metamodel.NodeLabel.Type;
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
public class SchemaVM extends SurveyBaseVM {

	private static final String TAB_NAME_PREFIX = "tab_";
	private static final String ROOT_TABSET_NAME_PREFIX = "tabset_";

	private static final String SCHEMA_CHANGED_GLOBAL_COMMAND = "schemaChanged";
	
	private static final String CONFIRM_REMOVE_NODE_MESSAGE_KEY = "survey.schema.confirm_remove_node";
	private static final String CONFIRM_REMOVE_NON_EMPTY_ENTITY_MESSAGE_KEY = "survey.schema.confirm_remove_non_empty_entity";
	private static final String CONFIRM_REMOVE_NODE_TITLE_KEY = "survey.schema.confirm_remove_node_title";
	
	private SchemaTreeModel treeModel;
	private EntityDefinition selectedNode;
	private EntityDefinition editedNode;
	private EntityDefinition parentEntity;
	private AttributeDefinition selectedAttribute;
	private Form tempFormObject;
	private EntityDefinitionFormObject<EntityDefinition> formObject;
	private boolean newItem;
	private AttributeDefinition editedAttribute;
	private boolean editingNewAttribute;

	@Wire
	private Tree nodesTree;
	@Wire
	private Include nodeFormInclude;
	
	//popups
	private Window attributePopUp;
	private Window unitsPopUp;
	private Window codeListsPopUp;
	private Window versioningPopUp;

	@AfterCompose
	public void afterCompose(@ContextParam(ContextType.VIEW) Component view){
		 Selectors.wireComponents(view, this, false);
		 Selectors.wireEventListeners(view, this);
	}
	
	@Command
	@NotifyChange({"nodes","selectedNode","editedNode"})
	public void nodeSelected(@BindingParam("node") Treeitem node) {
		if ( node != null ) {
			TreeNode<EntityDefinition> treeNode = node.getValue();
			selectedNode = treeNode.getData();
			editedNode = selectedNode;
			selectedAttribute = null;
		} else {
			selectedNode = null;
			editedNode = null;
			selectedAttribute = null;
		}
		newItem = false;
		initFormObject(selectedNode);
		refreshNodeForm();
	}
	
	@Command
	public void addRootEntity(@ContextParam(ContextType.BINDER) Binder binder) {
		if ( checkCurrentFormValid() ) {
			newItem = true;
			parentEntity = null;
			initFormObject();
			EntityDefinition newNode = createRootEntityDefinition();
			onAfterNodeCreated(binder, newNode);
		}
	}

	@Command
	public void addChildEntity(@ContextParam(ContextType.BINDER) Binder binder) {
		if ( checkCurrentFormValid() ) {
			newItem = true;
			parentEntity = selectedNode;
			initFormObject();
			EntityDefinition newNode = createEntityDefinition();
			onAfterNodeCreated(binder, newNode);
		}
	}
	
	@Command
	public void addAttribute(@ContextParam(ContextType.BINDER) Binder binder, @BindingParam("attributeType") String attributeType) throws Exception {
		if ( checkCurrentFormValid() ) {
			if ( selectedNode != null && selectedNode instanceof EntityDefinition ) {
				newItem = true;
				parentEntity = (EntityDefinition) selectedNode;
				initFormObject();
				
				AttributeType attributeTypeEnum = attributeType != null ? AttributeType.valueOf(attributeType): null;
				
				editedAttribute = (AttributeDefinition) NodeType.createNodeDefinition(survey, NodeType.ATTRIBUTE, attributeTypeEnum );
				editingNewAttribute = true;
				openAttributeEditPopUp();
//				( (EntityDefinition) selectedNode).addChildDefinition(newNode);
			} else {
				MessageUtil.showWarning("survey.schema.add_node.error.parent_entity_not_selected");
			}
		}
	}

	@Command
	public void editAttribute(@BindingParam("attribute") AttributeDefinition attribute) {
		editedAttribute = attribute;
		editingNewAttribute = false;
		openAttributeEditPopUp();
	}
	
	@GlobalCommand
	public void applyChangesToEditedAttribute() {
		if ( checkCurrentFormValid() ) {
			performCommitChangesOnEditedAttribute();
		}
	}

	protected void performCommitChangesOnEditedAttribute() {
		Binder binder = (Binder) attributePopUp.getAttribute("binder");
		AttributeVM viewModel = (AttributeVM) binder.getViewModel();
		viewModel.commitChanges();
		closePopUp(attributePopUp);
		notifyChange("childAttributes");
	}
	
	@GlobalCommand
	public void cancelChangesToEditedAttribute() {
		//TODO confirm if there are not committed changes 
		if ( editingNewAttribute ) {
			Schema schema = editedAttribute.getSchema();
			schema.detach(editedAttribute);
		}
		closePopUp(attributePopUp);
	}
	
	protected void openAttributeEditPopUp() {
		Map<String, Object> args = new HashMap<String, Object>();
		args.put("newItem", editingNewAttribute);
		args.put("item", editedAttribute);
		attributePopUp = openPopUp(Resources.Component.ATTRIBUTE_POPUP.getLocation(), true, args);
	}
	
	protected void onAfterNodeCreated(Binder binder, EntityDefinition newNode) {
		editedNode = newNode;
		selectedNode = null;
		selectedAttribute = null;
		treeModel.select(null);
		
		refreshNodeForm();
		
		notifyChange("nodes","selectedNode","editedNode");

		validateForm(binder);
	}
	
	protected void refreshNodeForm() {
		nodeFormInclude.setSrc(null);
		nodeFormInclude.setSrc(Resources.Component.ENTITY.getLocation());
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
		Component formComponent = Path.getComponent(currentIdSpace, "nodeFormInclude/nodeFormContainer");
		Binder formComponentBinder = (Binder) formComponent.getAttribute("binder");
		formComponentBinder.postCommand("applyChanges", null);
	}

	@Command
	public void removeNode() {
		removeNode(selectedNode);
	}

	public void removeNode(final NodeDefinition nodeDefn) {
		if ( nodeDefn != null ) {
			String confirmMessageKey;
			if (nodeDefn instanceof EntityDefinition && !((EntityDefinition) nodeDefn).getChildDefinitions().isEmpty() ) {
				confirmMessageKey = CONFIRM_REMOVE_NON_EMPTY_ENTITY_MESSAGE_KEY;
			} else {
				confirmMessageKey = CONFIRM_REMOVE_NODE_MESSAGE_KEY;
			}
			NodeType type = NodeType.typeOf(nodeDefn);
			String typeLabel = type.getLabel().toLowerCase();
			Object[] messageArgs = new String[] {typeLabel, nodeDefn.getName()};
			Object[] titleArgs = new String[] {typeLabel};
			MessageUtil.showConfirm(new MessageUtil.ConfirmHandler() {
				@Override
				public void onOk() {
					performRemoveNode(nodeDefn);
				}
			}, confirmMessageKey, messageArgs, CONFIRM_REMOVE_NODE_TITLE_KEY, titleArgs);
		}
	}
	
	@Command
	public void removeAttribute() {
		removeNode(selectedAttribute);
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
		List<EntityDefinition> siblingEntities = getSibilingEntities(selectedNode);
		int oldIndexInTree = siblingEntities.indexOf(selectedNode);
		int newIndexInTree = up ? oldIndexInTree - 1: oldIndexInTree + 1;
		EntityDefinition firstSiblingEntity = siblingEntities.get(newIndexInTree);
		int newIndexInSchema = getIndex(firstSiblingEntity);
		moveNode(newIndexInSchema);
		treeModel.moveSelectedNode(newIndexInTree);
		dispatchSchemaChangedCommand();
	}
	
	protected void moveNode(int newIndex) {
		EntityDefinition parentDefn = (EntityDefinition) selectedNode.getParentDefinition();
		if ( parentDefn != null ) {
			parentDefn.moveChildDefinition(selectedNode, newIndex);
		} else {
			EntityDefinition rootEntity = selectedNode.getRootEntity();
			Schema schema = rootEntity.getSchema();
			schema.moveRootEntityDefinition(rootEntity, newIndex);
		}
	}
	
	protected int getIndex(EntityDefinition entityDefn) {
		int index;
		EntityDefinition parentDefn = (EntityDefinition) entityDefn.getParentDefinition();
		if ( parentDefn != null ) {
			index = parentDefn.getChildIndex(entityDefn);
		} else {
			EntityDefinition rootEntity = selectedNode.getRootEntity();
			Schema schema = rootEntity.getSchema();
			index = schema.getRootEntityIndex(rootEntity);
		}
		return index;
	}
	
	protected int getIndexInTree(EntityDefinition entityDefn) {
		List<EntityDefinition> siblingEntities = getSibilingEntities(entityDefn);
		int index = siblingEntities.indexOf(entityDefn);
		return index;
	}
	
	@Command
	@NotifyChange({"childAttributes","moveAttributeUpDisabled","moveAttributeDownDisabled"})
	public void moveAttributeUp() {
		moveAttribute(true);
	}
	
	@Command
	@NotifyChange({"childAttributes","moveAttributeUpDisabled","moveAttributeDownDisabled"})
	public void moveAttributeDown() {
		moveAttribute(false);
	}
	
	protected void moveAttribute(boolean up) {
		if ( editedNode != null && selectedAttribute != null ) {
			List<AttributeDefinition> siblings = getChildAttributes();
			int oldIndexInList = siblings.indexOf(selectedAttribute);
			int newIndexInList = up ? oldIndexInList - 1: oldIndexInList + 1;
			AttributeDefinition firstSibling = siblings.get(newIndexInList);
			int newIndex = editedNode.getChildIndex(firstSibling);
			editedNode.moveChildDefinition(selectedAttribute, newIndex);
		}
		dispatchSchemaChangedCommand();
	}
	
	protected void performRemoveNode(NodeDefinition nodeDefn) {
		EntityDefinition parentDefn = (EntityDefinition) nodeDefn.getParentDefinition();
		if ( parentDefn != null ) {
			parentDefn.removeChildDefinition(nodeDefn);
		} else {
			UIOptions uiOpts = survey.getUIOptions();
			UITabSet tabSet = uiOpts.getTabSet((EntityDefinition) nodeDefn);
			uiOpts.removeTabSet(tabSet);
			Schema schema = nodeDefn.getSchema();
			String nodeName = nodeDefn.getName();
			schema.removeRootEntityDefinition(nodeName);
		}
		if ( nodeDefn instanceof EntityDefinition ) {
			treeModel.removeSelectedNode();
			selectedNode = null;
			editedNode = null;
			tempFormObject = null;
			formObject = null;
			notifyChange("nodes","editingNode","tempFormObject","formObject",
					"moveNodeUpDisabled","moveNodeDownDisabled");
			dispatchCurrentFormValidatedCommand(true);
			updateTreeSelectionActivation();
		} else {
			selectedAttribute = null;
			notifyChange("childAttributes","selectedAttribute");
		}
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
		if ( newItem ) {
			if ( parentEntity == null ) {
				Schema schema = editedNode.getSchema();
				schema.addRootEntityDefinition((EntityDefinition) editedNode);
			} else {
				parentEntity.addChildDefinition(editedNode);
			}
			treeModel.select(parentEntity);
			treeModel.appendNodeToSelected(editedNode);
			selectedNode = editedNode;
			newItem = false;
		}
		dispatchSchemaChangedCommand();
	}

	@GlobalCommand
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
	
	@GlobalCommand
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
	
	@GlobalCommand
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
	
	protected EntityDefinition createRootEntityDefinition() {
		EntityDefinition newNode = createEntityDefinition();
		UITabSet tabSet = createRootTabSet(newNode);
		newNode.setAnnotation(UIOptions.Annotation.TAB_SET.getQName(), tabSet.getName());
		return newNode;
	}

	protected EntityDefinition createEntityDefinition() {
		Schema schema = survey.getSchema();
		EntityDefinition newNode = schema.createEntityDefinition();
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
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void initFormObject() {
		formObject = new EntityDefinitionFormObject();
		tempFormObject = new SimpleForm();
	}

	protected void initFormObject(EntityDefinition node) {
		initFormObject();
		formObject.loadFrom(node, currentLanguageCode);
	}
	
	public SchemaTreeModel getNodes() {
		if ( treeModel == null ) {
			CollectSurvey survey = getSurvey();
			treeModel = SchemaTreeModel.createInstance(survey, false);
		}
		return treeModel;
    }
	
	@DependsOn("editedNode")
	public List<AttributeDefinition> getChildAttributes() {
		List<AttributeDefinition> result = new ArrayList<AttributeDefinition>();
		if ( editedNode instanceof EntityDefinition ) {
			List<NodeDefinition> childDefns = ((EntityDefinition) editedNode).getChildDefinitions();
			for (NodeDefinition nodeDefn : childDefns) {
				if ( nodeDefn instanceof AttributeDefinition ) {
					result.add((AttributeDefinition) nodeDefn);
				}
			}
		}
		return result;
	}

	public boolean isAttributeRequired(AttributeDefinition attributeDefn) {
		return attributeDefn.getMinCount() != null && attributeDefn.getMinCount() > 0;
	}
	
	public List<String> getAttributeTypeValues() {
		List<String> result = new ArrayList<String>();
		AttributeType[] values = AttributeType.values();
		for (AttributeType type : values) {
			result.add(type.name());
		}
		return result;
	}
	
	public String getAttributeTypeLabel(String typeValue) {
		if ( StringUtils.isNotBlank(typeValue) ) {
			AttributeType type = AttributeType.valueOf(typeValue);
			return type.getLabel();
		} else {
			return null;
		}
	}
	
	public String getAttributeTypeLabelFromDefinition(AttributeDefinition attrDefn) {
		if ( attrDefn != null ) {
			AttributeType type = AttributeType.valueOf(attrDefn);
			return type.getLabel();
		} else {
			return null;
		}
	}
	
	public String getAttributeInstanceLabel(AttributeDefinition attrDefn) {
		return attrDefn.getLabel(Type.INSTANCE, currentLanguageCode);
	}
	
	public List<NodeDefinition> getSiblings(NodeDefinition nodeDefinition) {
		List<NodeDefinition> siblings = new ArrayList<NodeDefinition>();
		EntityDefinition parentDefn = (EntityDefinition) nodeDefinition.getParentDefinition();
		if ( parentDefn != null ) {
			siblings.addAll(parentDefn.getChildDefinitions());
		} else {
			EntityDefinition rootEntity = nodeDefinition.getRootEntity();
			Schema schema = rootEntity.getSchema();
			siblings.addAll(schema.getRootEntityDefinitions());
		}
		return siblings;
	}
	
	protected List<EntityDefinition> getSibilingEntities(EntityDefinition entityDefn) {
		List<EntityDefinition> result = new ArrayList<EntityDefinition>();
		List<NodeDefinition> siblings = getSiblings(entityDefn);
		for (NodeDefinition sibling : siblings) {
			if ( sibling instanceof EntityDefinition ) {
				result.add((EntityDefinition) sibling);
			}
		}
		return result;
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

	@DependsOn("selectedNode")
	public boolean isMoveNodeUpDisabled() {
		if ( selectedNode != null ) {
			int index = getIndexInTree(selectedNode);
			return index <= 0;
		} else {
			return true;
		}
	}
	
	@DependsOn("selectedNode")
	public boolean isMoveNodeDownDisabled() {
		return isMoveNodeDisabled(false);
	}
	
	protected boolean isMoveNodeDisabled(boolean up) {
		if ( selectedNode != null ) {
			List<EntityDefinition> siblings = getSibilingEntities(selectedNode);
			int index = siblings.indexOf(selectedNode);
			return isMoveItemDisabled(siblings, index, up);
		} else {
			return true;
		}
	}

	protected boolean isMoveItemDisabled(List<?> siblings, int index, boolean up) {
		return up ? index <= 0: index < 0 || index >= siblings.size() - 1;
	}
	
	@DependsOn("selectedAttribute")
	public boolean isMoveAttributeUpDisabled() {
		return isMoveAttributeDisabled(true);
	}
	
	@DependsOn("selectedAttribute")
	public boolean isMoveAttributeDownDisabled() {
		return isMoveAttributeDisabled(false);
	}
	
	protected boolean isMoveAttributeDisabled(boolean up) {
		if ( selectedAttribute != null && editedNode != null ) {
			List<AttributeDefinition> siblings = getChildAttributes();
			int index = siblings.indexOf(selectedAttribute);
			return isMoveItemDisabled(siblings, index, up);
		} else {
			return true;
		}
	}
	
	public NodeDefinition getSelectedNode() {
		return selectedNode;
	}
	
	@DependsOn("editedNode")
	public EntityDefinitionFormObject<EntityDefinition> getFormObject() {
		return formObject;
	}

	public NodeDefinition getEditedNode() {
		return editedNode;
	}
	
	@DependsOn("editedNode")
	public boolean isEditingNode() {
		return editedNode != null;
	}

	@DependsOn("editedNode")
	public Form getTempFormObject() {
		return tempFormObject;
	}

	public boolean isNewNode() {
		return newItem;
	}

	public AttributeDefinition getSelectedAttribute() {
		return selectedAttribute;
	}

	public void setSelectedAttribute(AttributeDefinition selectedAttribute) {
		this.selectedAttribute = selectedAttribute;
	}
	
}
