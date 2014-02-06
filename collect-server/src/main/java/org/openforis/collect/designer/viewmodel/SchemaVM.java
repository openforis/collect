/**
 * 
 */
package org.openforis.collect.designer.viewmodel;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.openforis.collect.designer.component.SchemaTreeModel;
import org.openforis.collect.designer.component.SchemaTreeModel.SchemaNodeData;
import org.openforis.collect.designer.composer.SurveySchemaEditComposer;
import org.openforis.collect.designer.model.AttributeType;
import org.openforis.collect.designer.model.NodeType;
import org.openforis.collect.designer.util.ComponentUtil;
import org.openforis.collect.designer.util.MessageUtil;
import org.openforis.collect.designer.util.Resources;
import org.openforis.collect.metamodel.ui.UIOptions;
import org.openforis.collect.metamodel.ui.UIOptions.Layout;
import org.openforis.collect.metamodel.ui.UITab;
import org.openforis.collect.metamodel.ui.UITabSet;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.idm.metamodel.AttributeDefinition;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.ModelVersion;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.metamodel.NodeLabel.Type;
import org.openforis.idm.metamodel.Schema;
import org.openforis.idm.metamodel.SurveyObject;
import org.zkoss.bind.BindUtils;
import org.zkoss.bind.Binder;
import org.zkoss.bind.annotation.AfterCompose;
import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.DependsOn;
import org.zkoss.bind.annotation.GlobalCommand;
import org.zkoss.bind.annotation.Init;
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

	private static final String DEFAULT_ROOT_ENTITY_NAME = "change_it_to_your_record_type";

	private static final boolean FILTER_BY_ROOT_ENTITY = true;
	private static final String NODE_TYPES_IMAGES_PATH = "/assets/images/node_types/";

	private static final String SCHEMA_CHANGED_GLOBAL_COMMAND = "schemaChanged";
	private static final String VALIDATE_COMMAND = "validate";
	private static final String APPLY_CHANGES_COMMAND = "applyChanges";
	
	private static final String CONFIRM_REMOVE_NODE_MESSAGE_KEY = "survey.schema.confirm_remove_node";
	private static final String CONFIRM_REMOVE_NON_EMPTY_ENTITY_MESSAGE_KEY = "survey.schema.confirm_remove_non_empty_entity";
	private static final String CONFIRM_REMOVE_NODE_TITLE_KEY = "survey.schema.confirm_remove_node_title";

	private SchemaNodeData selectedTreeNode;
	private SurveyObject editedNode;
	private boolean newNode;

	private EntityDefinition selectedRootEntity;
	private UITabSet rootTabSet;
	private ModelVersion selectedVersion;
	
	@Wire
	private Include nodeFormInclude;
	
	private SchemaTreeModel treeModel;

	private EntityDefinition editedNodeParentEntity;

	@Override
	@Init(superclass=false)
	public void init() {
		super.init();
	}
	
	@AfterCompose
	public void doAfterCompose(@ContextParam(ContextType.VIEW) Component view){
		Selectors.wireComponents(view, this, false);
		Selectors.wireEventListeners(view, this);
		 
		//if one root entity is defined, select it
		List<EntityDefinition> rootEntities = getRootEntities();
		if (rootEntities.size() == 1) {
			EntityDefinition mainRootEntity = rootEntities.get(0);
			performNodeTreeFilterChange(mainRootEntity, null);
		}
	}
	
	@Command
	public void nodeSelected(@ContextParam(ContextType.BINDER) final Binder binder, 
			@ContextParam(ContextType.VIEW) final Component view,
			@BindingParam("data") final SchemaNodeData data) {
		if ( data != null ) {
			checkCanLeaveForm(new CanLeaveFormCompleteConfirmHandler() {
				@Override
				public void onOk(boolean confirmed) {
					if ( confirmed ) {
						undoLastChanges(view);
					}
					performSelectNode(binder, data);
				}
				@Override
				public void onCancel() {
					treeModel.select(selectedTreeNode);
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
	public void versionSelected(@BindingParam("version") ModelVersion version) {
		nodesTreeFilterChanged(selectedRootEntity, version);
	}
	
	protected void nodesTreeFilterChanged(final EntityDefinition rootEntity, final ModelVersion version) {
		checkCanLeaveForm(new CanLeaveFormCompleteConfirmHandler() {
			@Override
			public void onOk(boolean confirmed) {
				performNodeTreeFilterChange(rootEntity, version);
			}
			@Override
			public void onCancel() {
				notifyChange("selectedRootEntity","selectedVersion");
			}
		});
	}

	private void performNodeTreeFilterChange(EntityDefinition rootEntity, ModelVersion version) {
		selectedRootEntity = rootEntity;
		rootTabSet = survey.getUIOptions().getAssignedRootTabSet(rootEntity);
		selectedVersion = version;
		resetEditingStatus();
		updateTreeModel();
		dispatchCurrentFormValidatedCommand(true, isCurrentFormBlocking());
		notifyChange("selectedTreeNode","selectedRootEntity","selectedVersion","treeModel");
	}
	
	protected void performSelectNode(Binder binder, SchemaNodeData data) {
		selectedTreeNode = data;
		treeModel.select(data);
		SurveyObject surveyObject = data.getSurveyObject();
		EntityDefinition parentDefn = treeModel.getNearestEntityDefinition(surveyObject);
		editNode(binder, false, parentDefn, surveyObject);
	}

	@Command
	public void addRootEntity(@ContextParam(ContextType.BINDER) final Binder binder) {
		checkCanLeaveForm(new CanLeaveFormConfirmHandler() {
			@Override
			public void onOk(boolean confirmed) {
				resetNodeSelection();
				EntityDefinition rootEntity = createRootEntityDefinition();
				selectedRootEntity = rootEntity;
				selectedVersion = null;
//				editNode(binder, true, null, rootEntity);
				updateTreeModel();
//				selectTreeNode(rootEntity);
//				treeModel.markSelectedNodeAsDetached();
				notifyChange("selectedRootEntity","selectedVersion");
			}
		});
	}

	@Command
	public void addEntity(@ContextParam(ContextType.BINDER) final Binder binder, 
			@BindingParam("multiple") boolean multiple, 
			@BindingParam("layout") String layout) {
		resetNodeSelection();
		addChildEntity(binder, multiple, layout);
	}

	@Command
	public void addChildEntity(@ContextParam(ContextType.BINDER) final Binder binder,
			@BindingParam("multiple") final boolean multiple, 
			@BindingParam("layout") final String layout) {
		checkCanLeaveForm(new CanLeaveFormConfirmHandler() {
			@Override
			public void onOk(boolean confirmed) {
				EntityDefinition parentEntity = getSelectedNodeParentEntity();
				EntityDefinition newNode = createEntityDefinition();
				newNode.setMultiple(multiple);
				UIOptions uiOpts = survey.getUIOptions();
				Layout layoutEnum = Layout.valueOf(layout);
//				if ( uiOpts.isLayoutSupported(parentEntity, newNode.getId(), (UITab) null, multiple, layoutEnum) ) {
					uiOpts.setLayout(newNode, layoutEnum);
					SurveyObject surveyObject = selectedTreeNode.getSurveyObject();
					if ( surveyObject instanceof UITab ) {
						UIOptions uiOptions = survey.getUIOptions();
						uiOptions.assignToTab(newNode, (UITab) surveyObject);
					}
					editNode(binder, true, parentEntity, newNode);
					afterNewNodeCreated(newNode, true);
//				} else {
//					MessageUtil.showWarning(LabelKeys.LAYOUT_NOT_SUPPORTED_MESSAGE_KEY);
//				}
			}

		});
	}
	
	private EntityDefinition getSelectedNodeParentEntity() {
		if ( selectedTreeNode == null ) {
			return selectedRootEntity;
		} else {
			SurveyObject surveyObject = selectedTreeNode.getSurveyObject();
			if ( surveyObject instanceof NodeDefinition ) {
				return (EntityDefinition) surveyObject;
			} else {
				EntityDefinition parentEntity = treeModel.getNearestEntityDefinition(surveyObject);
				return parentEntity;
			}
		}
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
		checkCanLeaveForm(new CanLeaveFormConfirmHandler() {
			@Override
			public void onOk(boolean confirmed) {
				AttributeType attributeTypeEnum = AttributeType.valueOf(attributeType);
				AttributeDefinition newNode = (AttributeDefinition) NodeType.createNodeDefinition(survey, NodeType.ATTRIBUTE, attributeTypeEnum);
				EntityDefinition parentEntity = getSelectedNodeParentEntity();
				SurveyObject surveyObject = selectedTreeNode.getSurveyObject();
				if ( surveyObject instanceof UITab ) {
					UIOptions uiOptions = survey.getUIOptions();
					uiOptions.assignToTab(newNode, (UITab) surveyObject);
				}
				editNode(binder, true, parentEntity, newNode);
				afterNewNodeCreated(newNode, true);
			}
		});
	}

	private void afterNewNodeCreated(SurveyObject surveyObject, boolean detached) {
		treeModel.appendNodeToSelected(surveyObject, detached);
		selectTreeNode(surveyObject);
		//workaround: tree nodes not refreshed when adding child to "leaf" nodes (i.e. empty entity)
		notifyChange("treeModel");
	}

	@Override
	@GlobalCommand
	public void undoLastChanges(@ContextParam(ContextType.VIEW) Component view) {
		if ( editedNode != null ) {
			if ( newNode ) {
				if ( editedNode instanceof NodeDefinition ) {
					treeModel.select((NodeDefinition) editedNode);
				} else {
					treeModel.select((UITab) editedNode);
				}
				treeModel.removeSelectedNode();
			} else {
				String nodeLabel = editedNode instanceof NodeDefinition ? ((NodeDefinition) editedNode).getName(): 
					((UITab) editedNode).getLabel(currentLanguageCode);
				modfiyNodeLabel(view, editedNode, nodeLabel);
			}
			resetEditingStatus(false);
		}
	}
	
	@GlobalCommand
	public void editedNodeNameChanging(@ContextParam(ContextType.BINDER) Binder binder, 
			@ContextParam(ContextType.VIEW) Component view, @BindingParam("name") String name) {
		applyChangesToForm(binder);
		modfiyNodeLabel(view, editedNode, name);
	}

	private void modfiyNodeLabel(Component view, SurveyObject item, String label) {
		SurveySchemaEditComposer composer = ComponentUtil.getComposer(view);
		composer.updateNodeLabel(item, label);
	}

	protected void resetEditingStatus() {
		resetEditingStatus(true);
	}
	
	protected void resetEditingStatus(boolean notifyChange) {
		resetNodeSelection();
		editedNode = null;
		editedNodeParentEntity = null;
		refreshNodeForm();
		if ( notifyChange ) {
			notifyChange("editedNodeParentEntity","editedNode");
		}
	}
	
	protected void resetNodeSelection() {
		selectedTreeNode = null;
		notifyChange("selectedTreeNode");
		resetTreeSelection();
	}
	
	protected void resetTreeSelection() {
		if ( treeModel != null ) {
			treeModel.deselect();
		}
	}
	
	protected void selectTreeNode(SurveyObject surveyObject) {
		treeModel.select(surveyObject);
		selectedTreeNode = treeModel.getNodeData(surveyObject);
		BindUtils.postNotifyChange(null, null, selectedTreeNode, "*");
	}

	@Override
	@GlobalCommand
	public void versionsUpdated() {
		super.versionsUpdated();
		if ( selectedVersion != null && ! survey.getVersions().contains(selectedVersion) ) {
			resetEditingStatus();
			selectedVersion = null;
			updateTreeModel();
			notifyChange("selectedVersion");
		}
	}
	
	protected void editNode(Binder binder, boolean newNode, EntityDefinition parentEntity, SurveyObject node) {
		this.newNode = newNode;
		editedNodeParentEntity = parentEntity;
		editedNode = node;
		if ( ! newNode ) {
			selectedTreeNode = treeModel.getNodeData(node);
		}
		refreshNodeForm();
		validateForm(binder);
		notifyChange("selectedTreeNode","editedNode");
	}

	protected void refreshNodeForm() {
		nodeFormInclude.setSrc(null);
		if ( editedNode != null ) {
			nodeFormInclude.setDynamicProperty("parentEntity", editedNodeParentEntity);
			nodeFormInclude.setDynamicProperty("item", editedNode);
			nodeFormInclude.setDynamicProperty("newItem", newNode);
			String location;
			if ( editedNode instanceof UITab ) {
				location = Resources.Component.TAB.getLocation();
			} else if ( editedNode instanceof EntityDefinition ) {
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
		Binder formComponentBinder = getNodeFormBinder(binder);
		formComponentBinder.postCommand(VALIDATE_COMMAND, null);
	}

	protected Binder getNodeFormBinder(Binder binder) {
		Component view = binder.getView();
		IdSpace currentIdSpace = view.getSpaceOwner();
		Component formComponent = Path.getComponent(currentIdSpace, "nodeFormInclude/nodeFormContainer");
		Binder formComponentBinder = (Binder) formComponent.getAttribute("binder");
		return formComponentBinder;
	}

	protected void applyChangesToForm(@ContextParam(ContextType.BINDER) Binder binder) {
		Binder formComponentBinder = getNodeFormBinder(binder);
		formComponentBinder.postCommand(APPLY_CHANGES_COMMAND, null);
	}
	
	@Command
	public void removeNode() {
		removeNode(selectedTreeNode);
	}

	public void removeNode(final SchemaNodeData data) {
		if ( data.isDetached() ) {
			performRemoveDetachedNode();
		} else {
			SurveyObject surveyObject = data.getSurveyObject();
			if ( surveyObject instanceof NodeDefinition ) {
				final NodeDefinition nodeDefn = (NodeDefinition) surveyObject;
				String confirmMessageKey;
				if (nodeDefn instanceof EntityDefinition && !((EntityDefinition) nodeDefn).getChildDefinitions().isEmpty() ) {
					confirmMessageKey = CONFIRM_REMOVE_NON_EMPTY_ENTITY_MESSAGE_KEY;
				} else {
					confirmMessageKey = CONFIRM_REMOVE_NODE_MESSAGE_KEY;
				}
				NodeType type = NodeType.valueOf(nodeDefn);
				String typeLabel = type.getLabel().toLowerCase();
				boolean isRootEntity = nodeDefn.getParentDefinition() == null;
				if ( isRootEntity ) {
					typeLabel = Labels.getLabel("survey.schema.root_entity");
				}
				Object[] messageArgs = new String[] {typeLabel, nodeDefn.getName()};
				Object[] titleArgs = new String[] {typeLabel};
				MessageUtil.showConfirm(new MessageUtil.ConfirmHandler() {
					@Override
					public void onOk() {
						performRemoveNode(nodeDefn);
					}
				}, confirmMessageKey, messageArgs, CONFIRM_REMOVE_NODE_TITLE_KEY, titleArgs, "global.remove_item", "global.cancel");
			} else {
				//TODO
			}
		}
	}
	
	@Command
	public void moveNodeUp() {
		moveNode(true);
	}
	
	@Command
	public void moveNodeDown() {
		moveNode(false);
	}
	
	protected void moveNode(boolean up) {
		SurveyObject surveyObject = selectedTreeNode.getSurveyObject();
		if ( surveyObject instanceof NodeDefinition ) {
			NodeDefinition selectedNodeDefn = (NodeDefinition) surveyObject;
			List<NodeDefinition> siblings = getSiblingsInTree(selectedTreeNode);
			int oldIndex = siblings.indexOf(selectedNodeDefn);
			int newIndexInTree = up ? oldIndex - 1: oldIndex + 1;
			moveNode(newIndexInTree);
		} else {
			//TODO
		}
	}
	
	protected void moveNode(int newIndexInTree) {
		SurveyObject surveyObject = selectedTreeNode.getSurveyObject();
		if ( surveyObject instanceof NodeDefinition ) {
			NodeDefinition selectedNodeDefn = (NodeDefinition) surveyObject;
			List<NodeDefinition> siblings = getSiblingsInTree(selectedTreeNode);
			EntityDefinition parentDefn = (EntityDefinition) selectedNodeDefn.getParentDefinition();
			NodeDefinition treeNodeToMoveInto = siblings.get(newIndexInTree);
			int toIndex;
			if ( treeNodeToMoveInto != null ) {
				toIndex = parentDefn.getChildDefinitionIndex(treeNodeToMoveInto);
			} else {
				toIndex = newIndexInTree;
			}
			if ( parentDefn != null ) {
				parentDefn.moveChildDefinition(selectedNodeDefn, toIndex);
			} else {
				EntityDefinition rootEntity = selectedNodeDefn.getRootEntity();
				Schema schema = rootEntity.getSchema();
				schema.moveRootEntityDefinition(rootEntity, toIndex);
			}
			treeModel.moveSelectedNode(newIndexInTree);
			notifyChange("treeModel","moveNodeUpDisabled","moveNodeDownDisabled");
			dispatchSchemaChangedCommand();
		} else {
			//TODO
		}
	}
	
	protected void performRemoveDetachedNode() {
		treeModel.removeSelectedNode();
		notifyChange("treeModel");
		resetEditingStatus();
		dispatchCurrentFormValidatedCommand(true);
	}
	
	protected void performRemoveNode(NodeDefinition nodeDefn) {
		EntityDefinition parentDefn = (EntityDefinition) nodeDefn.getParentDefinition();
		if ( parentDefn != null ) {
			if ( treeModel != null ) {
				treeModel.removeSelectedNode();
				notifyChange("treeModel");
			}
			parentDefn.removeChildDefinition(nodeDefn);
		} else {
			UIOptions uiOpts = survey.getUIOptions();
			UITabSet tabSet = uiOpts.getAssignedRootTabSet((EntityDefinition) nodeDefn);
			uiOpts.removeTabSet(tabSet);
			Schema schema = nodeDefn.getSchema();
			String nodeName = nodeDefn.getName();
			schema.removeRootEntityDefinition(nodeName);
			selectedRootEntity = null;
			rootTabSet = null;
			notifyChange("selectedRootEntity");
			updateTreeModel();
		}
		resetEditingStatus();
		dispatchCurrentFormValidatedCommand(true);
		dispatchSchemaChangedCommand();
	}

	@GlobalCommand
	public void editedNodeChanged(@ContextParam(ContextType.VIEW) Component view, 
			@BindingParam("parentEntity") EntityDefinition parentEntity) {
		if ( newNode ) {
			if ( editedNode instanceof NodeDefinition && parentEntity == null ) {
				selectedRootEntity = (EntityDefinition) editedNode;
				notifyChange("selectedRootEntity","selectedVersion");
				updateTreeModel();
			} else {
				//editing tab or nested node definition
				selectedTreeNode.setDetached(false);
				BindUtils.postNotifyChange(null, null, selectedTreeNode, "detached");
			}
			selectedTreeNode = treeModel.getNodeData(editedNode);
			treeModel.select(selectedTreeNode);
			newNode = false;
			notifyChange("selectedTreeNode", "newNode");
		}
		dispatchSchemaChangedCommand();
		//to be called when not notifying changes on treeModel
		refreshSelectedTreeNode(view);
	}

	protected void refreshSelectedTreeNode(Component view) {
		SurveySchemaEditComposer composer = ComponentUtil.getComposer(view);
		composer.refreshSelectedTreeNodeContextMenu();
	}
	
	protected EntityDefinition createRootEntityDefinition() {
		EntityDefinition rootEntity = createEntityDefinition();
		rootEntity.setName(DEFAULT_ROOT_ENTITY_NAME);
		survey.getSchema().addRootEntityDefinition(rootEntity);
		survey.getUIOptions().createRootTabSet((EntityDefinition) rootEntity);
		return rootEntity;
	}

	protected EntityDefinition createEntityDefinition() {
		Schema schema = survey.getSchema();
		EntityDefinition newNode = schema.createEntityDefinition();
		return newNode;
	}
	
	protected void dispatchSchemaChangedCommand() {
		BindUtils.postGlobalCommand(null, null, SCHEMA_CHANGED_GLOBAL_COMMAND, null);
	}
	
	public SchemaTreeModel getTreeModel() {
		if ( treeModel == null ) {
			buildTreeModel();
		}
		return treeModel;
    }

	protected void buildTreeModel() {
		CollectSurvey survey = getSurvey();
		if ( survey == null ) {
			//TODO session expired...?
		} else {
			treeModel = SchemaTreeModel.createInstance(selectedRootEntity, selectedVersion, true, currentLanguageCode);
		}
	}

	protected boolean isVersionSelected() {
		return survey.getVersions().isEmpty() || selectedVersion != null;
	}
	
	protected void updateTreeModel() {
		buildTreeModel();
		notifyChange("treeModel");
	}
	
	public boolean isTab(SchemaNodeData data) {
		return data.getSurveyObject() instanceof UITab;
	}
	
	public boolean isMainTab(SchemaNodeData data) {
		SurveyObject surveyObject = data.getSurveyObject();
		if ( surveyObject instanceof UITab ) {
			UIOptions uiOptions = survey.getUIOptions();
			return uiOptions.isMainTab((UITab) surveyObject);
		} else {
			return false;
		}
	}
	
	public boolean isEntity(SchemaNodeData data) {
		return data.getSurveyObject() instanceof EntityDefinition;
	}
	
	public boolean isSingleEntity(SchemaNodeData data) {
		return data.getSurveyObject() instanceof EntityDefinition && ! ((NodeDefinition) data.getSurveyObject()).isMultiple();
	}
	
	public boolean isTableEntity(SchemaNodeData data) {
		if ( isEntity(data) ) {
			UIOptions uiOptions = survey.getUIOptions();
			EntityDefinition entityDefn = (EntityDefinition) data.getSurveyObject();
			Layout layout = uiOptions.getLayout(entityDefn);
			return layout == Layout.TABLE;
		} else {
			return false;
		}
	}
	
	protected List<NodeDefinition> getSiblingsInTree(SchemaNodeData data) {
		List<NodeDefinition> result = new ArrayList<NodeDefinition>();
		NodeDefinition nodeDefn = (NodeDefinition) data.getSurveyObject();
		EntityDefinition parentDefn = (EntityDefinition) nodeDefn.getParentDefinition();
		List<? extends NodeDefinition> allSiblings;
		if ( parentDefn == null ) {
			if ( FILTER_BY_ROOT_ENTITY ) {
				allSiblings = Arrays.asList(selectedRootEntity);
			} else {
				Schema schema = selectedRootEntity.getSchema();
				allSiblings = schema.getRootEntityDefinitions();
			}
		} else {
			allSiblings = parentDefn.getChildDefinitions();
		}
		//filter siblings
		for (NodeDefinition sibling : allSiblings) {
			if ( selectedVersion == null || selectedVersion.isApplicable(sibling) ) {
				result.add(sibling);
			}
		}
		return result;
	}
	
	@DependsOn("selectedTreeNode")
	public boolean isMoveNodeUpDisabled() {
		return isMoveNodeDisabled(true);
	}
	
	@DependsOn("selectedTreeNode")
	public boolean isMoveNodeDownDisabled() {
		return isMoveNodeDisabled(false);
	}
	
	protected boolean isMoveNodeDisabled(boolean up) {
		if ( ! newNode && selectedTreeNode != null && selectedTreeNode.getSurveyObject() instanceof NodeDefinition ) {
			NodeDefinition selectedNodeDefn = (NodeDefinition) selectedTreeNode.getSurveyObject();
			List<NodeDefinition> siblings = getSiblingsInTree(selectedTreeNode);
			int index = siblings.indexOf(selectedNodeDefn);
			return isMoveItemDisabled(siblings, index, up);
		} else {
			return true;
		}
	}

	protected boolean isMoveItemDisabled(List<?> siblings, int index, boolean up) {
		return up ? index <= 0: index < 0 || index >= siblings.size() - 1;
	}
	
	@DependsOn({"newNode","editedNode"})
	public String getNodeTypeHeaderLabel() {
		if ( editedNode != null ) {
			if ( editedNode instanceof NodeDefinition ) {
				return NodeType.getHeaderLabel((NodeDefinition) editedNode, editedNodeParentEntity == null, newNode);
			} else {
				//TODO
				return null;
			}
		} else {
			return null;
		}
	}
	
	@DependsOn("editedNode")
	public String getNodeType() {
		if ( editedNode != null && editedNode instanceof NodeDefinition ) {
			NodeType type = NodeType.valueOf((NodeDefinition) editedNode);
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
	
	public static String getIcon(SchemaNodeData data) {
		SurveyObject surveyObject = data.getSurveyObject();
		if ( surveyObject instanceof UITab ) {
			return "/assets/images/tab-small.png";
		} else {
			if ( surveyObject instanceof EntityDefinition ) {
				return getEntityIcon((EntityDefinition) surveyObject);
			} else {
				AttributeType attributeType = AttributeType.valueOf((AttributeDefinition) surveyObject);
				return getAttributeIcon(attributeType.name());
			}
		}
	}
	
	protected static String getEntityIcon(EntityDefinition entityDefn) {
		CollectSurvey survey = (CollectSurvey) entityDefn.getSurvey();
		UIOptions uiOptions = survey.getUIOptions();
		Layout layout = uiOptions.getLayout(entityDefn);
		String icon;
		if ( entityDefn.isMultiple() ) {
			switch ( layout ) {
			case TABLE:
				icon = "table-small.png";
				break;
			case FORM:
			default:
				icon = "form-small.png";
			}
		} else {
			icon = "entity-small.png";
		}
		return NODE_TYPES_IMAGES_PATH + icon;
	}

	public static String getAttributeIcon(String type) {
		AttributeType attributeType = AttributeType.valueOf(type);
		String result = NODE_TYPES_IMAGES_PATH + attributeType.name().toLowerCase() + "-small.png";
		return result;
	}
	
	@Command
	@NotifyChange({"treeModel","selectedTab"})
	public void addTab(@ContextParam(ContextType.BINDER) Binder binder) {
		treeModel.deselect();
		addTabInternal(binder, rootTabSet);
	}
	
	@Command
	@NotifyChange({"treeModel","selectedTab"})
	public void addChildTab(@ContextParam(ContextType.BINDER) Binder binder) {
		SurveyObject surveyObject = selectedTreeNode.getSurveyObject();
		addTabInternal(binder, surveyObject);
	}
	
	protected void addTabInternal(final Binder binder, final SurveyObject parent) {
		if ( rootTabSet != null ) {
			checkCanLeaveForm(new CanLeaveFormConfirmHandler() {
				@Override
				public void onOk(boolean confirmed) {
					CollectSurvey survey = getSurvey();
					UIOptions uiOptions = survey.getUIOptions();
					UITab tab = uiOptions.createTab();
					String label = Labels.getLabel("survey.schema.node.layout.default_tab_label");
					tab.setLabel(currentLanguageCode, label);
					UITabSet parentTab;
					if ( parent instanceof UITabSet ) {
						parentTab = (UITabSet) parent;
					} else {
						parentTab = uiOptions.getAssignedTab((NodeDefinition) parent);
					}
					parentTab.addTab(tab);
		
					editNode(binder, true, null, tab);
					afterNewNodeCreated(tab, false);

					//dispatchTabSetChangedCommand();
				}
			});
		}
	}
	
	@Command
	public void removeTab() {
		String confirmMessageKey = null;
		UITab tab = (UITab) selectedTreeNode.getSurveyObject();
		if ( tab.getTabs().isEmpty() ) {
			CollectSurvey survey = getSurvey();
			UIOptions uiOpts = survey.getUIOptions();
			List<NodeDefinition> nodesPerTab = uiOpts.getNodesPerTab(tab, false);
			if ( ! nodesPerTab.isEmpty() ) {
				confirmMessageKey = "survey.layout.tab.remove.confirm.associated_nodes_present";
			}
		} else {
			confirmMessageKey = "survey.layout.tab.remove.confirm.nested_tabs_present";
		}
		if ( confirmMessageKey != null ) {
			MessageUtil.ConfirmParams params = new MessageUtil.ConfirmParams(new MessageUtil.ConfirmHandler() {
				@Override
				public void onOk() {
					performRemoveSelectedTab();
				}
			}, confirmMessageKey);
			params.setOkLabelKey("global.delete_item");
			MessageUtil.showConfirm(params);
		} else {
			performRemoveSelectedTab();
		}
	}
	
	protected void performRemoveSelectedTab() {
		UITab tab = (UITab) selectedTreeNode.getSurveyObject();
		UITabSet parent = tab.getParent();
		parent.removeTab(tab);
		treeModel.removeSelectedNode();
		notifyChange("treeModel", "selectedTab");
//		dispatchTabSetChangedCommand();
	}
	
	@Command
	public void updateTabLabel(@BindingParam("tab") UITab tab, @BindingParam("label") String label) {
		if ( validateTabLabel(label) ) {
			tab.setLabel(currentLanguageCode, label.trim());
//			dispatchTabChangedCommand(tab);
		}
	}
	
	protected boolean validateTabLabel(String label) {
		if ( StringUtils.isBlank(label) ) {
			MessageUtil.showWarning("survey.layout.tab.label.error.required");
			return false;
		} else {
			return true;
		}
	}
	
	public SchemaNodeData getSelectedTreeNode() {
		return selectedTreeNode;
	}
	
	public SurveyObject getEditedNode() {
		return editedNode;
	}
	
	@DependsOn("editedNode")
	public boolean isEditingNode() {
		return editedNode != null;
	}

	public boolean isNewNode() {
		return newNode;
	}

	public EntityDefinition getSelectedRootEntity() {
		return selectedRootEntity;
	}

	public ModelVersion getSelectedVersion() {
		return selectedVersion;
	}

}
