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
import org.openforis.collect.metamodel.ui.UITabSet;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.idm.metamodel.AttributeDefinition;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.ModelVersion;
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
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.IdSpace;
import org.zkoss.zk.ui.Path;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.VariableResolver;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.Include;

/**
 * 
 * @author S. Ricci
 *
 */
@VariableResolver(org.zkoss.zkplus.spring.DelegatingVariableResolver.class)
public class SchemaVM extends SurveyBaseVM {

	private static final String ROOT_ENTITY_NOT_SELECTED = "survey.schema.edit_root_entity.not_selected";

	private static final String SCHEMA_CHANGED_GLOBAL_COMMAND = "schemaChanged";
	
	private static final String CONFIRM_REMOVE_NODE_MESSAGE_KEY = "survey.schema.confirm_remove_node";
	private static final String CONFIRM_REMOVE_NON_EMPTY_ENTITY_MESSAGE_KEY = "survey.schema.confirm_remove_non_empty_entity";
	private static final String CONFIRM_REMOVE_NODE_TITLE_KEY = "survey.schema.confirm_remove_node_title";

	private static final String VALIDATE_COMMAND = "validate";
	
	private NodeDefinition selectedNode;
	private NodeDefinition editedNode;
	private boolean newItem;

	private EntityDefinition selectedRootEntity;
	private ModelVersion selectedVersion;
	
	@Wire
	private Include nodeFormInclude;
	
	private SchemaTreeModel treeModel;

	private EntityDefinition editedNodeParentEntity;

	@AfterCompose
	public void afterCompose(@ContextParam(ContextType.VIEW) Component view){
		 Selectors.wireComponents(view, this, false);
		 Selectors.wireEventListeners(view, this);
	}
	
	@Command
	@NotifyChange({"selectedNode","editedNode"})
	public void nodeSelected(@ContextParam(ContextType.BINDER) final Binder binder, @BindingParam("node") final NodeDefinition node) {
		if ( node != null ) {
			checkCanLeaveForm(new MessageUtil.CompleteConfirmHandler() {
				@Override
				public void onOk() {
					performSelectNode(binder, node);
				}
				@Override
				public void onCancel() {
					treeModel.select(selectedNode);
				}
			});
		} else {
			resetEditingStatus();
		}
	}

	@Command
	public void rootEntitySelected(@BindingParam("rootEntity") final EntityDefinition rootEntity) {
		nodesTreeFilterChanged(rootEntity, selectedVersion);
	}
	
	@Command
	@NotifyChange({"selectedNode","nodes","selectedVersion"})
	public void versionSelected(@BindingParam("version") ModelVersion version) {
		nodesTreeFilterChanged(selectedRootEntity, version);
	}
	
	protected void nodesTreeFilterChanged(final EntityDefinition rootEntity, final ModelVersion version) {
		if(checkCanLeaveForm(new MessageUtil.CompleteConfirmHandler() {
			@Override
			public void onOk() {
				selectedRootEntity = rootEntity;
				selectedVersion = version;
				resetEditingStatus();
				updateTreeModel();
				dispatchCurrentFormValidatedCommand(true, isCurrentFormBlocking());
				notifyChange("selectedNode","selectedRootEntity","selectedVersion");
			}
			@Override
			public void onCancel() {
				notifyChange("selectedRootEntity","selectedVersion");
			}
		}));
	}

	protected void performSelectNode(Binder binder, NodeDefinition node) {
		selectedNode = node;
		editNode(binder, false, selectedNode == null ? null : (EntityDefinition) selectedNode.getParentDefinition(), node);
	}

	@Command
	public void addRootEntity(@ContextParam(ContextType.BINDER) final Binder binder) {
		checkCanLeaveForm(new MessageUtil.ConfirmHandler() {
			@Override
			public void onOk() {
				resetNodeSelection();
				selectedRootEntity = null;
				updateTreeModel();
				EntityDefinition newNode = createRootEntityDefinition();
				editNode(binder, true, null, newNode);
			}
		});
	}

	@Command
	public void addEntity(@ContextParam(ContextType.BINDER) final Binder binder) {
		resetNodeSelection();
		addChildEntity(binder);
	}

	@Command
	public void addChildEntity(@ContextParam(ContextType.BINDER) final Binder binder) {
		checkCanLeaveForm(new MessageUtil.ConfirmHandler() {
			@Override
			public void onOk() {
				EntityDefinition newNode = createEntityDefinition();
				EntityDefinition parentEntity = (EntityDefinition) (selectedNode != null ? selectedNode: selectedRootEntity);
				editNode(binder, true, parentEntity, newNode);
			}
		});
	}
	
	@Command
	public void addAttribute(@ContextParam(ContextType.BINDER) final Binder binder, 
			@BindingParam("attributeType") final String attributeType) throws Exception {
		resetNodeSelection();
		addChildAttribute(binder, attributeType);
	}
	
	@Command
	public void addChildAttribute(@ContextParam(ContextType.BINDER) final Binder binder, 
			@BindingParam("attributeType") final String attributeType) throws Exception {
		if ( selectedNode != null && selectedNode instanceof EntityDefinition ) {
			checkCanLeaveForm(new MessageUtil.ConfirmHandler() {
				@Override
				public void onOk() {
					AttributeType attributeTypeEnum = AttributeType.valueOf(attributeType);
					AttributeDefinition newNode = (AttributeDefinition) NodeType.createNodeDefinition(survey, NodeType.ATTRIBUTE, attributeTypeEnum);
					editNode(binder, true, (EntityDefinition) selectedNode, newNode);
				}
			});
		} else {
			MessageUtil.showWarning("survey.schema.add_node.error.parent_entity_not_selected");
		}
	}

	@Override
	@GlobalCommand
	public void undoLastChanges() {
		if ( ! isCurrentFormValid() && editedNode != null ) {
			resetEditingStatus();
			notifyChange("selectedNode","editedNode");
		}
	}

	protected void resetEditingStatus() {
		resetNodeSelection();
		editedNode = null;
		notifyChange("selectedNode","editedNode");
	}
	
	protected void resetNodeSelection() {
		selectedNode = null;
		notifyChange("selectedNode");
		resetTreeSelection();
	}
	
	protected void resetTreeSelection() {
		if ( treeModel != null ) {
			treeModel.deselect();
		}
	}
	
	@Override
	@GlobalCommand
	public void versionsUpdated() {
		super.versionsUpdated();
		if ( selectedVersion != null && ! survey.getVersions().contains(selectedVersion) ) {
			selectedVersion = null;
			buildTreeModel();
			notifyChange("selectedVersion");
		}
	}
	
	@Command
	public void editRootEntity(@ContextParam(ContextType.BINDER) Binder binder) {
		if ( selectedRootEntity == null ) {
			MessageUtil.showWarning(ROOT_ENTITY_NOT_SELECTED);
		} else {
			resetNodeSelection();
			editNode(binder, false, null, selectedRootEntity);
		}
	}
	
	
	
	protected void editNode(Binder binder, boolean newNode, EntityDefinition parentEntity, NodeDefinition node) {
		newItem = newNode;
		editedNodeParentEntity = parentEntity;
		editedNode = node;
		if ( newNode ) {
			resetNodeSelection();
		} else {
			selectedNode = node;
		}
		refreshNodeForm(parentEntity);
		validateForm(binder);
		notifyChange("selectedNode","editedNode");
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
		} else {
			dispatchCurrentFormValidatedCommand(true);
		}
	}
		
	protected void validateForm(@ContextParam(ContextType.BINDER) Binder binder) {
		Component view = binder.getView();
		IdSpace currentIdSpace = view.getSpaceOwner();
		Component formComponent = Path.getComponent(currentIdSpace, "nodeFormInclude/nodeFormContainer");
		Binder formComponentBinder = (Binder) formComponent.getAttribute("binder");
		formComponentBinder.postCommand(VALIDATE_COMMAND, null);
	}

	@Command
	public void removeRootEntity() {
		removeNode(selectedRootEntity);
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
			NodeType type = NodeType.valueOf(nodeDefn);
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
	@NotifyChange({"moveNodeUpDisabled","moveNodeDownDisabled"})
	public void moveNodeUp() {
		moveNode(true);
	}
	
	@Command
	@NotifyChange({"moveNodeUpDisabled","moveNodeDownDisabled"})
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
			UITabSet tabSet = uiOpts.getAssignedRootTabSet((EntityDefinition) nodeDefn);
			uiOpts.removeTabSet(tabSet);
			Schema schema = nodeDefn.getSchema();
			String nodeName = nodeDefn.getName();
			schema.removeRootEntityDefinition(nodeName);
		}
		if ( treeModel != null ) {
			treeModel.removeSelectedNode();
		}
		selectedNode = null;
		editedNode = null;
		notifyChange("editedNode","selectedNode","nodes");
		dispatchCurrentFormValidatedCommand(true);
	}

	@GlobalCommand
	@NotifyChange({"selectedNode","newItem"})
	public void editedNodeChanged(@BindingParam("parentEntity") EntityDefinition parentEntity) {
		if ( newItem ) {
			if ( parentEntity != null ) {
				if ( treeModel != null ) {
					if ( parentEntity.getParentDefinition() != null ) {
						//is not root entity, the nodes tree will contain it
						treeModel.select(parentEntity);
					}
					treeModel.appendNodeToSelected(editedNode);
				}
				selectedNode = editedNode;
			} else {
				selectedRootEntity = (EntityDefinition) editedNode;
				updateTreeModel();
			}
			newItem = false;
		}
		dispatchSchemaChangedCommand();
	}
	
	protected EntityDefinition createRootEntityDefinition() {
		EntityDefinition newNode = createEntityDefinition();
		CollectSurvey survey = getSurvey();
		UIOptions uiOptions = survey.getUIOptions();
		uiOptions.createRootTabSet(newNode);
		return newNode;
	}

	protected EntityDefinition createEntityDefinition() {
		Schema schema = survey.getSchema();
		EntityDefinition newNode = schema.createEntityDefinition();
		return newNode;
	}
	
	protected void dispatchSchemaChangedCommand() {
		BindUtils.postGlobalCommand(null, null, SCHEMA_CHANGED_GLOBAL_COMMAND, null);
	}
	
	public SchemaTreeModel getNodes() {
		if ( treeModel == null ) {
			buildTreeModel();
		}
		return treeModel;
    }

	protected void buildTreeModel() {
		CollectSurvey survey = getSurvey();
		if ( survey == null ) {
			//TODO session expired...?
		} else if ( survey.getVersions().size() == 0 || selectedVersion != null ) {
			treeModel = SchemaTreeModel.createInstance(selectedRootEntity, selectedVersion, false, true);
		} else {
			treeModel = null;
		}
	}
	
	protected void updateTreeModel() {
		buildTreeModel();
		notifyChange("nodes");
	}
	
	public boolean isEntity(NodeDefinition nodeDefn) {
		return nodeDefn instanceof EntityDefinition;
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
	
	@DependsOn("editedNode")
	public String getNodeTypeHeaderLabel() {
		String result = null;
		String nodeTypeStr = getNodeType();
		String messageKey;
		if ( nodeTypeStr != null ) {
			NodeType nodeType = NodeType.valueOf(nodeTypeStr);
			switch (nodeType) {
			case ENTITY:
				if ( newItem ) {
					if ( editedNodeParentEntity == null ) {
						messageKey = "survey.schema.node_detail_title.new_root_entity";
					} else {
						messageKey = "survey.schema.node_detail_title.new_entity";
					}
				} else if ( editedNodeParentEntity == null ) {
					messageKey = "survey.schema.node_detail_title.root_entity";
				} else {
					messageKey = "survey.schema.node_detail_title.entity";
				}
				result = Labels.getLabel(messageKey);
				break;
			case ATTRIBUTE:
				if ( newItem ) {
					messageKey = "survey.schema.node_detail_title.new_attribute";
				} else {
					messageKey = "survey.schema.node_detail_title.attribute";
				}
				Object[] args = new String[]{getAttributeTypeLabel()};
				result = Labels.getLabel(messageKey, args);
				break;
			}
		}
		return result;
	}
	
	@DependsOn("editedNode")
	public String getNodeType() {
		if ( editedNode != null ) {
			NodeType type = NodeType.valueOf(editedNode);
			return type.name();
		} else {
			return null;
		}
	}

	@DependsOn("editedNode")
	public String getAttributeType() {
		if ( editedNode != null && editedNode instanceof AttributeDefinition ) {
			AttributeType type = AttributeType.valueOf((AttributeDefinition) editedNode);
			return type.name();
		} else {
			return null;
		}
	}
	
	@DependsOn("editedNode")
	public String getAttributeTypeLabel() {
		String type = getAttributeType();
		return getAttributeTypeLabel(type);
	}

	public String getAttributeTypeLabel(String typeValue) {
		if ( StringUtils.isNotBlank(typeValue) ) {
			AttributeType type = AttributeType.valueOf(typeValue);
			return type.getLabel();
		} else {
			return null;
		}
	}
	
	public List<String> getAttributeTypeValues() {
		List<String> result = new ArrayList<String>();
		AttributeType[] values = AttributeType.values();
		for (AttributeType type : values) {
			result.add(type.name());
		}
		return result;
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

	public EntityDefinition getSelectedRootEntity() {
		return selectedRootEntity;
	}

	public ModelVersion getSelectedVersion() {
		return selectedVersion;
	}

}
