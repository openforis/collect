/**
 * 
 */
package org.openforis.collect.designer.viewmodel;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.openforis.collect.designer.component.SchemaTreeModel;
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
	
	private NodeDefinition selectedNode;
	private NodeDefinition editedNode;
	private boolean newItem;

	@Wire
	private Include nodeFormInclude;
	@Wire
	private Tree nodesTree;
	
	private SchemaTreeModel treeModel;

	//popups
	private Window versioningPopUp;

	@AfterCompose
	public void afterCompose(@ContextParam(ContextType.VIEW) Component view){
		 Selectors.wireComponents(view, this, false);
		 Selectors.wireEventListeners(view, this);
	}
	
	@Command
	@NotifyChange({"selectedNode","editedNode"})
	public void nodeSelected(@BindingParam("node") Treeitem node) {
		if ( node != null ) {
			TreeNode<NodeDefinition> treeNode = node.getValue();
			selectedNode = treeNode.getData();
			editedNode = selectedNode;
		} else {
			selectedNode = null;
			editedNode = null;
		}
		newItem = false;
		EntityDefinition parentDefinition = selectedNode == null ? null : (EntityDefinition) selectedNode.getParentDefinition();
		refreshNodeForm(parentDefinition);
	}
	
	@Command
	public void addRootEntity(@ContextParam(ContextType.BINDER) Binder binder) {
		if ( checkCurrentFormValid() ) {
			EntityDefinition newNode = createRootEntityDefinition();
			onAfterNodeCreated(binder, null, newNode);
		}
	}

	@Command
	public void addChildEntity(@ContextParam(ContextType.BINDER) Binder binder) {
		if ( checkCurrentFormValid() ) {
			EntityDefinition newNode = createEntityDefinition();
			onAfterNodeCreated(binder, (EntityDefinition) selectedNode, newNode);
		}
	}
	
	@Command
	public void addAttribute(@ContextParam(ContextType.BINDER) Binder binder, @BindingParam("attributeType") String attributeType) throws Exception {
		if ( checkCurrentFormValid() ) {
			if ( selectedNode != null && selectedNode instanceof EntityDefinition ) {
				AttributeType attributeTypeEnum = AttributeType.valueOf(attributeType);
				AttributeDefinition newNode = (AttributeDefinition) NodeType.createNodeDefinition(survey, NodeType.ATTRIBUTE, attributeTypeEnum);
				onAfterNodeCreated(binder, (EntityDefinition) selectedNode, newNode);
			} else {
				MessageUtil.showWarning("survey.schema.add_node.error.parent_entity_not_selected");
			}
		}
	}

	protected void onAfterNodeCreated(Binder binder, EntityDefinition parentEntity, NodeDefinition newNode) {
		newItem = true;
		editedNode = newNode;
		selectedNode = null;
		treeModel.select(null);
		
		refreshNodeForm(parentEntity);
		
		notifyChange("nodes","selectedNode","editedNode");

		validateForm(binder);
	}
	
	protected void refreshNodeForm(EntityDefinition parentEntity) {
		nodeFormInclude.setSrc(null);
		if ( editedNode != null ) {
			nodeFormInclude.setDynamicProperty("parentEntity", parentEntity);
			nodeFormInclude.setDynamicProperty("item", editedNode);
			nodeFormInclude.setDynamicProperty("newItem", newItem);
			String location;
			if ( editedNode instanceof EntityDefinition ) {
				location = Resources.Component.ENTITY.getLocation();
			} else {
				AttributeType attributeType = AttributeType.valueOf((AttributeDefinition) editedNode);
				location = Resources.Component.ATTRIBUTE.getLocation();
				String attributeTypeShort = attributeType.name().toLowerCase();
				location = MessageFormat.format(location, attributeTypeShort);
			}
			nodeFormInclude.setSrc(location);
		}
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
		List<NodeDefinition> siblings = getSiblings(selectedNode);
		int oldIndex = siblings.indexOf(selectedNode);
		int newIndex = up ? oldIndex - 1: oldIndex + 1;
		moveNode(newIndex);
		treeModel.moveSelectedNode(newIndex);
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
	
	protected int getIndex(NodeDefinition nodeDefn) {
		int index;
		EntityDefinition parentDefn = (EntityDefinition) nodeDefn.getParentDefinition();
		if ( parentDefn != null ) {
			index = parentDefn.getChildIndex(nodeDefn);
		} else {
			EntityDefinition rootEntity = selectedNode.getRootEntity();
			Schema schema = rootEntity.getSchema();
			index = schema.getRootEntityIndex(rootEntity);
		}
		return index;
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
		treeModel.removeSelectedNode();
		selectedNode = null;
		editedNode = null;
		notifyChange("nodes","moveNodeUpDisabled","moveNodeDownDisabled");
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
	
	@GlobalCommand
	@NotifyChange({"nodes","selectedNode","newItem"})
	public void editedNodeChanged(@BindingParam("parentEntity") EntityDefinition parentEntity) {
		if ( newItem ) {
			treeModel.select(parentEntity);
			treeModel.appendNodeToSelected(editedNode);
			selectedNode = editedNode;
			newItem = false;
		}
		dispatchSchemaChangedCommand();
	}
	
	@GlobalCommand
	public void openVersioningManagerPopUp() {
		if ( versioningPopUp == null ) {
			dispatchCurrentFormValidatedCommand(true);
			versioningPopUp = openPopUp(Resources.Component.VERSIONING_POPUP.getLocation(), true);
		}
	}

	@GlobalCommand
	public void closeVersioningManagerPopUp() {
		if ( versioningPopUp != null && checkCurrentFormValid() ) {
			closePopUp(versioningPopUp);
			versioningPopUp = null;
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
		UITab tab = tabSet.createTab();
		int tabPosition = 1;
		String tabName = TAB_NAME_PREFIX + tabPosition;
		tab.setName(tabName);
		tabSet.addTab(tab);
	}

	protected UITabSet createRootTabSet(EntityDefinition rootEntity) {
		UIOptions uiOpts = survey.getUIOptions();
		UITabSet tabSet = uiOpts.createTabSet();
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
	
	public SchemaTreeModel getNodes() {
		if ( treeModel == null ) {
			CollectSurvey survey = getSurvey();
			treeModel = SchemaTreeModel.createInstance(survey, true);
		}
		return treeModel;
    }
	
	public boolean isEntity(NodeDefinition nodeDefn) {
		return nodeDefn instanceof EntityDefinition;
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
			int index = getIndex(selectedNode);
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
			List<NodeDefinition> siblings = getSiblings(selectedNode);
			int index = siblings.indexOf(selectedNode);
			return isMoveItemDisabled(siblings, index, up);
		} else {
			return true;
		}
	}

	protected boolean isMoveItemDisabled(List<?> siblings, int index, boolean up) {
		return up ? index <= 0: index < 0 || index >= siblings.size() - 1;
	}
	
	public NodeDefinition getSelectedNode() {
		return selectedNode;
	}
	
	public NodeDefinition getEditedNode() {
		return editedNode;
	}
	
	@DependsOn("editedNode")
	public boolean isEditingNode() {
		return editedNode != null;
	}

	public boolean isNewNode() {
		return newItem;
	}

}
