/**
 * 
 */
package org.openforis.collect.designer.viewmodel;

import static org.openforis.collect.designer.metamodel.AttributeType.BOOLEAN;
import static org.openforis.collect.designer.metamodel.AttributeType.CODE;
import static org.openforis.collect.designer.metamodel.AttributeType.DATE;
import static org.openforis.collect.designer.metamodel.AttributeType.NUMBER;
import static org.openforis.collect.designer.metamodel.AttributeType.TEXT;
import static org.openforis.collect.designer.metamodel.AttributeType.TIME;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.openforis.collect.designer.component.SchemaTreeModel;
import org.openforis.collect.designer.component.SchemaTreeModel.SchemaNodeData;
import org.openforis.collect.designer.component.SchemaTreeModel.SchemaTreeNode;
import org.openforis.collect.designer.component.SchemaTreeModelCreator;
import org.openforis.collect.designer.component.SurveyObjectTreeModelCreator;
import org.openforis.collect.designer.component.UITreeModelCreator;
import org.openforis.collect.designer.form.FormObject;
import org.openforis.collect.designer.metamodel.AttributeType;
import org.openforis.collect.designer.metamodel.NodeType;
import org.openforis.collect.designer.model.LabelKeys;
import org.openforis.collect.designer.util.ComponentUtil;
import org.openforis.collect.designer.util.MessageUtil;
import org.openforis.collect.designer.util.Predicate;
import org.openforis.collect.designer.util.Resources;
import org.openforis.collect.designer.viewmodel.SchemaTreePopUpVM.NodeSelectedEvent;
import org.openforis.collect.manager.SurveyManager;
import org.openforis.collect.manager.validation.CollectEarthSurveyValidator;
import org.openforis.collect.metamodel.ui.UIOptions;
import org.openforis.collect.metamodel.ui.UIOptions.Layout;
import org.openforis.collect.metamodel.ui.UITab;
import org.openforis.collect.metamodel.ui.UITabSet;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.idm.metamodel.AttributeDefinition;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.KeyAttributeDefinition;
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
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zul.Include;
import org.zkoss.zul.Menupopup;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Tree;
import org.zkoss.zul.TreeNode;
import org.zkoss.zul.Treeitem;
import org.zkoss.zul.Window;

import com.google.common.collect.Sets;

/**
 * 
 * @author S. Ricci
 *
 */
public class SchemaVM extends SurveyBaseVM {

	public static final String DEFAULT_ROOT_ENTITY_NAME = "change_it_to_your_sampling_unit";
	public static final String DEFAULT_MAIN_TAB_LABEL = "Change it to your main tab label";
	private static final String PATH_NULL_VALUES_REPLACE = "...";

	private static final String TAB_NAME_LABEL_PATH = "labelTextbox";
	private static final String ENTITY_NAME_TEXTBOX_PATH = "nodeCommonInclude/nodeNameTextbox";
	private static final String ATTRIBUTE_NAME_TEXTBOX_PATH = "attributeCommonInclude/nodeCommonInclude/nodeNameTextbox";

	private static final String NODE_TYPES_IMAGES_PATH = "/assets/images/node_types/";

	private static final String VALIDATE_COMMAND = "validate";
	private static final String APPLY_CHANGES_COMMAND = "applyChanges";

	private static final String CONFIRM_REMOVE_NODE_MESSAGE_KEY = "survey.schema.confirm_remove_node";
	private static final String CONFIRM_REMOVE_NON_EMPTY_ENTITY_MESSAGE_KEY = "survey.schema.confirm_remove_non_empty_entity";
	private static final String CONFIRM_REMOVE_NODE_WITH_DEPENDENCIES_MESSAGE_KEY = "survey.schema.confirm_remove_node_with_dependencies";
	private static final String CONFIRM_REMOVE_REFERENCED_ATTRIBUTE_MESSAGE_KEY = "survey.schema.attribute.confirm_remove_referenced_attribute";
	private static final String CONFIRM_REMOVE_NODE_TITLE_KEY = "survey.schema.confirm_remove_node_title";

	private static final Set<AttributeType> SUPPORTED_COLLECT_EARTH_ATTRIBUTE_TYPES = Sets.immutableEnumSet(BOOLEAN,
			CODE, DATE, NUMBER, TEXT, TIME);

	private static final Pattern CLONED_NAME_PATTERN = Pattern.compile("(.*)(\\d+)");

	private SchemaNodeData selectedTreeNode;
	private SurveyObject editedNode;
	private boolean newNode;

	private EntityDefinition selectedRootEntity;
	private UITabSet rootTabSet;
	private ModelVersion selectedVersion;
	private String selectedTreeViewType;
	private EntityDefinition editedNodeParentEntity;

	@Wire
	private Include nodeFormInclude;
	@Wire
	private Tree nodesTree;
	@Wire
	private Menupopup mainTabPopup;
	@Wire
	private Menupopup tabPopup;
	@Wire
	private Menupopup singleEntityPopup;
	@Wire
	private Menupopup tableEntityPopup;
	@Wire
	private Menupopup formEntityPopup;
	@Wire
	private Menupopup attributePopup;
	@Wire
	private Menupopup detachedNodePopup;

	@WireVariable
	private SurveyManager surveyManager;

	// transient
	private Window rootEntityEditPopUp;

	private SchemaTreeModel treeModel;

	public enum TreeViewType {
		ENTRY, DATA
	}

	public SchemaVM() {
		super();
		fieldLabelKeyPrefixes.addAll(0, Arrays.asList("survey.schema.attribute", "survey.schema.node", "global.item"));
	}

	@Override
	@Init(superclass = false)
	public void init() {
		super.init();
	}

	@AfterCompose
	public void doAfterCompose(@ContextParam(ContextType.VIEW) Component view) {
		Selectors.wireComponents(view, this, false);
		Selectors.wireEventListeners(view, this);

		selectedTreeViewType = TreeViewType.ENTRY.name().toLowerCase(Locale.ENGLISH);

		// select first root entity
		List<EntityDefinition> rootEntities = getRootEntities();
		if (rootEntities.size() > 0) {
			EntityDefinition mainRootEntity = rootEntities.get(0);
			performNodeTreeFilterChange(mainRootEntity, null);
		}
	}

	@Command
	public void nodeSelected(@ContextParam(ContextType.BINDER) final Binder binder,
			@ContextParam(ContextType.VIEW) final Component view, @BindingParam("data") final SchemaNodeData data) {
		if (data != null) {
			checkCanLeaveForm(new CanLeaveFormCompleteConfirmHandler() {
				@Override
				public void onOk(boolean confirmed) {
					if (confirmed) {
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
	public void versionSelected(@BindingParam("version") Object version) {
		ModelVersion modelVersion;
		if (version == FormObject.VERSION_EMPTY_SELECTION) {
			modelVersion = null;
		} else {
			modelVersion = (ModelVersion) version;
		}
		nodesTreeFilterChanged(selectedRootEntity, modelVersion);
	}

	protected void nodesTreeFilterChanged(final EntityDefinition rootEntity, final ModelVersion version) {
		checkCanLeaveForm(new CanLeaveFormCompleteConfirmHandler() {
			@Override
			public void onOk(boolean confirmed) {
				performNodeTreeFilterChange(rootEntity, version);
			}

			@Override
			public void onCancel() {
				notifyChange("selectedRootEntity", "selectedVersion");
			}
		});
	}

	private void performNodeTreeFilterChange(EntityDefinition rootEntity, ModelVersion version) {
		selectedRootEntity = rootEntity;
		rootTabSet = survey.getUIOptions().getAssignedRootTabSet(rootEntity);
		selectedVersion = version;
		resetEditingStatus();
		refreshTreeModel();
		dispatchCurrentFormValidatedCommand(true, isCurrentFormBlocking());
		notifyChange("selectedTreeNode", "selectedRootEntity", "selectedVersion", "treeModel");
	}

	protected void performSelectNode(Binder binder, SchemaNodeData data) {
		selectedTreeNode = data;
		treeModel.select(data);
		SurveyObject surveyObject = data.getSurveyObject();
		EntityDefinition parentDefn = treeModel.getNearestParentEntityDefinition(surveyObject);
		editNode(binder, false, parentDefn, surveyObject);
	}

	@Command
	public void addRootEntity() {
		checkCanLeaveForm(new CanLeaveFormConfirmHandler() {
			@Override
			public void onOk(boolean confirmed) {
				resetNodeSelection();
				resetEditingStatus();
				EntityDefinition rootEntity = createRootEntityDefinition();
				selectedRootEntity = rootEntity;
				selectedVersion = null;
				refreshTreeModel();
				selectTreeNode(null);
				notifyChange("selectedRootEntity", "selectedVersion");
				editRootEntity();
			}
		});
	}

	@Command
	public void addEntity(@ContextParam(ContextType.BINDER) final Binder binder,
			@BindingParam("multiple") boolean multiple, @BindingParam("layout") String layout) {
		resetNodeSelection();
		addChildEntity(binder, multiple, layout, false);
	}

	@Command
	public void addChildEntity(@ContextParam(ContextType.BINDER) final Binder binder,
			@BindingParam("multiple") final boolean multiple, 
			@BindingParam("layout") final String layout,
			@BindingParam("virtual") final boolean virtual) {
		checkCanLeaveForm(new CanLeaveFormConfirmHandler() {
			@Override
			public void onOk(boolean confirmed) {
				EntityDefinition parentEntity = getSelectedNodeParentEntity();
				EntityDefinition newNode = createEntityDefinition();
				newNode.setMultiple(multiple);
				newNode.setVirtual(virtual);
				UIOptions uiOptions = survey.getUIOptions();
				Layout layoutEnum = Layout.valueOf(layout);
				SurveyObject selectedSurveyObject = selectedTreeNode.getSurveyObject();
				UITab parentTab = null;
				if (selectedSurveyObject instanceof UITab) {
					parentTab = (UITab) selectedSurveyObject;
				}
				if (uiOptions.isLayoutSupported(parentEntity, newNode.getId(), parentTab, multiple, layoutEnum)) {
					uiOptions.setLayout(newNode, layoutEnum);
					if (parentTab != null) {
						uiOptions.assignToTab(newNode, parentTab);
					}
					editNode(binder, true, parentEntity, newNode);
					afterNewNodeCreated(newNode, true);
				} else {
					MessageUtil.showWarning(LabelKeys.LAYOUT_NOT_SUPPORTED_MESSAGE_KEY);
				}
			}

		});
	}

	private EntityDefinition getSelectedNodeParentEntity() {
		if (selectedTreeNode == null) {
			return selectedRootEntity;
		} else {
			SurveyObject surveyObject = selectedTreeNode.getSurveyObject();
			if (surveyObject instanceof EntityDefinition) {
				return (EntityDefinition) surveyObject;
			} else {
				EntityDefinition parentEntity = treeModel.getNearestParentEntityDefinition(surveyObject);
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
			public void onOk(boolean confirmed) {
				AttributeType attributeTypeEnum = AttributeType.valueOf(attributeType);
				AttributeDefinition newNode = (AttributeDefinition) NodeType.createNodeDefinition(survey,
						NodeType.ATTRIBUTE, attributeTypeEnum);
				SurveyObject selectedSurveyObject = selectedTreeNode.getSurveyObject();
				if (selectedSurveyObject instanceof UITab) {
					UIOptions uiOptions = survey.getUIOptions();
					uiOptions.assignToTab(newNode, (UITab) selectedSurveyObject);
				}
				EntityDefinition parentEntity = getSelectedNodeParentEntity();
				editNode(binder, true, parentEntity, newNode);
				afterNewNodeCreated(newNode, true);
			}
		});
	}

	private void afterNewNodeCreated(SurveyObject surveyObject, boolean detached) {
		treeModel.appendNodeToSelected(surveyObject, detached);
		selectTreeNode(surveyObject);
		// workaround: tree nodes not refreshed when adding child to "leaf"
		// nodes (i.e. empty entity)
		notifyChange("treeModel");
	}

	@Command
	public void expandTree() {
		treeModel.openAllItems();
		notifyChange("treeModel");
	}

	@Command
	public void collapseTree() {
		treeModel.clearOpen();
		notifyChange("treeModel");
	}

	@Override
	public void undoLastChanges() {
		super.undoLastChanges();
		if (editedNode != null) {
			if (newNode) {
				treeModel.select(editedNode);
				treeModel.removeSelectedNode();
			} else {
				// restore committed label into tree node
				String committedLabel = editedNode instanceof NodeDefinition ? ((NodeDefinition) editedNode).getName()
						: ((UITab) editedNode).getLabel(currentLanguageCode);
				updateTreeNodeLabel(editedNode, committedLabel);

				// restore tree node icon
				if (editedNode instanceof KeyAttributeDefinition) {
					updateTreeNodeIcon(editedNode, ((KeyAttributeDefinition) editedNode).isKey());
				}
			}
			resetEditingStatus(false);
		}
	}

	@GlobalCommand
	public void editedNodeNameChanging(@BindingParam("item") SurveyObject item, @BindingParam("name") String name) {
		if (editedNode != null && editedNode == item) {
			updateTreeNodeLabel(editedNode, name);
		}
	}

	@GlobalCommand
	public void editedNodeKeyChanging(@BindingParam("item") SurveyObject item, @BindingParam("key") boolean key) {
		if (editedNode != null && editedNode == item) {
			updateTreeNodeIcon(editedNode, key);
		}
	}

	// TODO move it to tree model class
	private Treeitem getTreeItem(SurveyObject item) {
		for (Treeitem treeItem : nodesTree.getItems()) {
			SchemaTreeNode node = treeItem.getValue();
			SchemaNodeData data = node.getData();
			SurveyObject itemSO = data.getSurveyObject();
			if (itemSO == item) {
				return treeItem;
			}
		}
		return null;
	}

	private void updateTreeNodeLabel(SurveyObject item, String label) {
		treeModel.updateNodeLabel(item, label);
		Treeitem treeItem = getTreeItem(item);
		if (treeItem != null) {
			treeItem.setLabel(label);
		}
	}

	private void updateTreeNodeIcon(SurveyObject item, boolean key) {
		Treeitem treeItem = getTreeItem(item);
		if (treeItem != null) {
			SchemaNodeData data = treeModel.getNodeData(item);
			String icon = getIcon(data, key);
			treeItem.setImage(icon);
		}
	}

	@Override
	@GlobalCommand
	public void currentLanguageChanged() {
		super.currentLanguageChanged();
		refreshTreeModel();
	}

	@GlobalCommand
	public void schemaChanged() {
		refreshTreeModel();
	}

	@GlobalCommand
	public void nodeConverted(@ContextParam(ContextType.BINDER) Binder binder,
			@BindingParam("node") NodeDefinition nodeDef) {
		resetEditingStatus();
		refreshTreeModel();
		editNode(binder, false, nodeDef.getParentEntityDefinition(), nodeDef);
		selectTreeNode(nodeDef);
	}

	protected void resetEditingStatus() {
		resetEditingStatus(true);
	}

	protected void resetEditingStatus(boolean notifyChange) {
		resetNodeSelection();
		editedNode = null;
		editedNodeParentEntity = null;
		refreshNodeForm();
		if (notifyChange) {
			notifyChange("editedNodeParentEntity", "editedNode");
		}
	}

	protected void resetNodeSelection() {
		selectedTreeNode = null;
		notifyChange("selectedTreeNode");
		resetTreeSelection();
	}

	protected void resetTreeSelection() {
		if (treeModel != null) {
			treeModel.deselect();
		}
	}

	protected void selectTreeNode(SurveyObject surveyObject) {
		treeModel.select(surveyObject);
		selectedTreeNode = treeModel.getNodeData(surveyObject);
		notifyChange("selectedTreeNode");
		// BindUtils.postNotifyChange(null, null, selectedTreeNode, "*");
	}

	@Override
	@GlobalCommand
	public void versionsUpdated() {
		super.versionsUpdated();
		if (selectedVersion != null && !survey.getVersions().contains(selectedVersion)) {
			resetEditingStatus();
			selectedVersion = null;
			refreshTreeModel();
			notifyChange("selectedVersion");
		}
	}

	private void editNode(boolean newNode, EntityDefinition parentEntity, SurveyObject node) {
		editNode(null, newNode, parentEntity, node);
	}

	protected void editNode(Binder binder, boolean newNode, EntityDefinition parentEntity, SurveyObject node) {
		this.newNode = newNode;
		editedNodeParentEntity = parentEntity;
		editedNode = node;
		if (!newNode) {
			selectedTreeNode = treeModel.getNodeData(node);
		}
		refreshNodeForm();
		if (binder == null) {
			validateForm();
		} else {
			validateForm(binder);
		}
		notifyChange("selectedTreeNode", "editedNode");
	}

	protected void refreshNodeForm() {
		nodeFormInclude.setSrc(null);
		if (editedNode != null) {
			nodeFormInclude.setDynamicProperty("parentEntity", editedNodeParentEntity);
			nodeFormInclude.setDynamicProperty("item", editedNode);
			nodeFormInclude.setDynamicProperty("newItem", newNode);
			String nodeNameTextboxPath;
			String location;
			if (editedNode instanceof UITab) {
				location = Resources.Component.TAB.getLocation();
				nodeNameTextboxPath = TAB_NAME_LABEL_PATH;
			} else if (editedNode instanceof EntityDefinition) {
				location = Resources.Component.ENTITY.getLocation();
				nodeNameTextboxPath = ENTITY_NAME_TEXTBOX_PATH;
			} else {
				AttributeType attributeType = AttributeType.valueOf((AttributeDefinition) editedNode);
				String locationFormat = Resources.Component.ATTRIBUTE.getLocation();
				String attributeTypeShort = attributeType.name().toLowerCase(Locale.ENGLISH);
				location = MessageFormat.format(locationFormat, attributeTypeShort);
				nodeNameTextboxPath = ATTRIBUTE_NAME_TEXTBOX_PATH;
			}
			nodeFormInclude.setSrc(location);
			// set focus on name textbox
			Textbox nodeNameTextbox = (Textbox) Path.getComponent(nodeFormInclude.getSpaceOwner(), nodeNameTextboxPath);
			nodeNameTextbox.setFocus(true);
		}
	}

	protected void validateForm() {
		if (editedNode != null) {
			Binder binder = (Binder) nodeFormInclude.getAttribute("$BINDER$");
			if (binder != null) {
				validateForm(binder);
			}
		} else {
			dispatchCurrentFormValidatedCommand(true);
		}
	}

	protected void validateForm(@ContextParam(ContextType.BINDER) Binder binder) {
		Component view = binder.getView();
		IdSpace idSpace = view.getSpaceOwner();
		Binder formComponentBinder = getNodeFormBinder(idSpace);
		formComponentBinder.postCommand(VALIDATE_COMMAND, null);
	}

	protected Binder getNodeFormBinder(IdSpace idSpace) {
		Component formComponent = getNodeFormComponent(idSpace);
		Binder formComponentBinder = (Binder) formComponent.getAttribute("binder");
		return formComponentBinder;
	}

	protected Component getNodeFormComponent(IdSpace idSpace) {
		Component component = Path.getComponent(idSpace, "nodeFormInclude/nodeFormContainer");
		return component;
	}

	protected void applyChangesToForm(IdSpace idSpace) {
		Binder binder = getNodeFormBinder(idSpace);
		binder.postCommand(APPLY_CHANGES_COMMAND, null);
	}

	@Command
	public void removeNode() {
		if (checkCanDeleteSelectedNode()) {
			removeTreeNode(selectedTreeNode);
		}
	}

	private boolean checkCanDeleteSelectedNode() {
		if (isCollectEarthSurvey()) {
			if (! selectedTreeNode.isDetached()) {
				SurveyObject surveyObject = selectedTreeNode.getSurveyObject();
				if (surveyObject instanceof NodeDefinition) {
					NodeDefinition nodeDef = (NodeDefinition) surveyObject;
					if (isCollectEarthRequiredField(nodeDef)) {
						MessageUtil.showWarning("survey.schema.cannot_remove_ce_required_field", nodeDef.getName());
						return false;
					}
				}
			}
		}
		return true;
	}
	
	private boolean isCollectEarthRequiredField(NodeDefinition nodeDef) {
		return nodeDef.getParentEntityDefinition().isRoot() && 
				CollectEarthSurveyValidator.REQUIRED_FIELD_NAMES.contains(nodeDef.getName());
	}
	
	private void removeTreeNode(final SchemaNodeData data) {
		if (data.isDetached()) {
			performRemoveSelectedTreeNode();
		} else {
			SurveyObject surveyObject = data.getSurveyObject();
			if (surveyObject instanceof NodeDefinition) {
				removeNodeDefinition((NodeDefinition) surveyObject);
			} else {
				removeTab((UITab) surveyObject);
			}
		}
	}

	protected void removeNodeDefinition(final NodeDefinition nodeDefn) {
		String confirmMessageKey;
		Object[] extraMessageArgs = null;
		if (nodeDefn instanceof EntityDefinition && !((EntityDefinition) nodeDefn).getChildDefinitions().isEmpty()) {
			confirmMessageKey = CONFIRM_REMOVE_NON_EMPTY_ENTITY_MESSAGE_KEY;
		} else if (nodeDefn.hasDependencies()) {
			confirmMessageKey = CONFIRM_REMOVE_NODE_WITH_DEPENDENCIES_MESSAGE_KEY;
		} else if (nodeDefn instanceof AttributeDefinition
				&& !((AttributeDefinition) nodeDefn).getReferencingAttributes().isEmpty()) {
			confirmMessageKey = CONFIRM_REMOVE_REFERENCED_ATTRIBUTE_MESSAGE_KEY;
			List<String> referencedAttrNames = org.openforis.commons.collection.CollectionUtils
					.project(((AttributeDefinition) nodeDefn).getReferencingAttributes(), "name");
			extraMessageArgs = new String[] { StringUtils.join(referencedAttrNames, ", ") };
		} else {
			confirmMessageKey = CONFIRM_REMOVE_NODE_MESSAGE_KEY;
		}
		NodeType type = NodeType.valueOf(nodeDefn);
		String typeLabel = type.getLabel().toLowerCase(Locale.ENGLISH);
		boolean isRootEntity = nodeDefn.getParentDefinition() == null;
		if (isRootEntity) {
			typeLabel = Labels.getLabel("survey.schema.root_entity");
		}
		Object[] messageArgs = new String[] { typeLabel, nodeDefn.getName() };
		if (extraMessageArgs != null) {
			messageArgs = ArrayUtils.addAll(messageArgs, extraMessageArgs);
		}
		Object[] titleArgs = new String[] { typeLabel };
		MessageUtil.showConfirm(new MessageUtil.ConfirmHandler() {
			@Override
			public void onOk() {
				performRemoveNode(nodeDefn);
			}
		}, confirmMessageKey, messageArgs, CONFIRM_REMOVE_NODE_TITLE_KEY, titleArgs, "global.remove_item",
				"global.cancel");
	}

	@Command
	public void removeRootEntity() {
		removeNodeDefinition(selectedRootEntity);
	}

	@Command
	public void editRootEntity() {
		checkCanLeaveForm(new CanLeaveFormConfirmHandler() {
			@Override
			public void onOk(boolean confirmed) {
				if (confirmed) {
					undoLastChanges();
				}
				openRootEntityEditPopUp();
			}
		});
	}

	private void openRootEntityEditPopUp() {
		Map<String, Object> args = new HashMap<String, Object>();
		args.put("formLocation", Resources.Component.ENTITY.getLocation());
		args.put("title", Labels.getLabel("survey.layout.root_entity"));
		args.put("parentEntity", null);
		args.put("item", selectedRootEntity);
		args.put("newItem", false);
		args.put("doNotCommitChangesImmediately", true);
		rootEntityEditPopUp = openPopUp(Resources.Component.NODE_EDIT_POPUP.getLocation(), true, args);
	}

	@GlobalCommand
	public void applyChangesToEditedNodeInPopUp(@ContextParam(ContextType.BINDER) Binder binder) {
		Component nodeFormContainer = getNodeEditorForm();
		NodeDefinitionVM<?> vm = ComponentUtil.getViewModel(nodeFormContainer);
		vm.dispatchValidateCommand(ComponentUtil.getBinder(nodeFormContainer));
		if (vm.isCurrentFormValid()) {
			vm.commitChanges(binder);
			closeNodeEditPopUp();
		} else {
			checkCanLeaveForm(new CanLeaveFormConfirmHandler() {
				@Override
				public void onOk(boolean confirmed) {
					closeNodeEditPopUp();
				}
			});
		}
	}

	private Component getNodeEditorForm() {
		Component nodeFormContainer = rootEntityEditPopUp.getFellow("nodeFormInclude").getFellow("nodeFormContainer");
		return nodeFormContainer;
	}

	@GlobalCommand
	public void cancelChangesToEditedNodeInPopUp(@ContextParam(ContextType.BINDER) Binder binder) {
		Component nodeFormContainer = getNodeEditorForm();
		NodeDefinitionVM<?> vm = ComponentUtil.getViewModel(nodeFormContainer);
		vm.undoLastChanges();
		closeNodeEditPopUp();
	}

	private void closeNodeEditPopUp() {
		closePopUp(rootEntityEditPopUp);
		rootEntityEditPopUp = null;
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
		List<SurveyObject> siblings = getSiblingsInTree(surveyObject);
		int oldIndex = siblings.indexOf(surveyObject);
		int newIndexInTree = up ? oldIndex - 1 : oldIndex + 1;
		moveNode(newIndexInTree);
	}

	protected void moveNode(int newIndexInTree) {
		SurveyObject surveyObject = selectedTreeNode.getSurveyObject();
		List<SurveyObject> siblings = getSiblingsInTree(surveyObject);
		SurveyObject newIndexItem = siblings.get(newIndexInTree);

		SchemaTreeNode newIndexNode = treeModel.getTreeNode(newIndexItem);
		int newIndexInModel = newIndexNode.getIndexInModel();

		if (surveyObject instanceof NodeDefinition) {
			NodeDefinition nodeDefn = (NodeDefinition) surveyObject;
			EntityDefinition parentEntity = nodeDefn.getParentEntityDefinition();
			if (parentEntity != null) {
				parentEntity.moveChildDefinition(nodeDefn, newIndexInModel);
			} else {
				EntityDefinition rootEntity = nodeDefn.getRootEntity();
				Schema schema = rootEntity.getSchema();
				schema.moveRootEntityDefinition(rootEntity, newIndexInModel);
			}
		} else {
			UITab tab = (UITab) surveyObject;
			UITabSet parent = tab.getParent();
			parent.moveTab(tab, newIndexInModel);
		}
		treeModel.moveSelectedNode(newIndexInTree);
		notifyChange("treeModel", "moveNodeUpDisabled", "moveNodeDownDisabled");
		dispatchSurveyChangedCommand();
	}

	protected void performRemoveSelectedTreeNode() {
		treeModel.removeSelectedNode();
		notifyChange("treeModel");
		resetEditingStatus();
		dispatchCurrentFormValidatedCommand(true);
	}

	protected void performRemoveNode(NodeDefinition nodeDefn) {
		EntityDefinition parentDefn = (EntityDefinition) nodeDefn.getParentDefinition();
		if (parentDefn == null) {
			// root entity
			UIOptions uiOpts = survey.getUIOptions();
			UITabSet tabSet = uiOpts.getAssignedRootTabSet((EntityDefinition) nodeDefn);
			uiOpts.removeTabSet(tabSet);
			Schema schema = nodeDefn.getSchema();
			String nodeName = nodeDefn.getName();
			schema.removeRootEntityDefinition(nodeName);
			selectedRootEntity = null;
			rootTabSet = null;
			notifyChange("selectedRootEntity", "rootEntities");
			refreshTreeModel();
		} else {
			if (treeModel != null) {
				treeModel.removeSelectedNode();
				notifyChange("treeModel");
			}
			parentDefn.removeChildDefinition(nodeDefn);
		}
		survey.refreshSurveyDependencies();
		resetEditingStatus();
		dispatchCurrentFormValidatedCommand(true);
		dispatchSurveyChangedCommand();
	}

	@GlobalCommand
	public void editedNodeChanged(@ContextParam(ContextType.VIEW) Component view,
			@BindingParam("parentEntity") EntityDefinition parentEntity, @BindingParam("node") SurveyObject editedNode,
			@BindingParam("newItem") Boolean newNode) {
		if (parentEntity == null && editedNode instanceof EntityDefinition) {
			// root entity
			EntityDefinition rootEntity = (EntityDefinition) editedNode;
			updateRootTabLabel(view, rootEntity);
		} else {
			if (newNode) {
				// editing tab or nested node definition
				// update tree node
				selectedTreeNode.setDetached(false);
				BindUtils.postNotifyChange(null, null, selectedTreeNode, "detached");
				this.newNode = false;
				notifyChange("newNode");
				selectTreeNode(editedNode);
			}
			notifyChange("editedNodePath");
			// to be called when not notifying changes on treeModel
			refreshSelectedTreeNode(view);
		}
	}

	private void updateRootTabLabel(Component view, EntityDefinition rootEntity) {
		UITab mainTab = survey.getUIOptions().getMainTab(rootTabSet);
		if (DEFAULT_MAIN_TAB_LABEL.equals(mainTab.getLabel(currentLanguageCode))) {
			String label = rootEntity.getLabel(Type.INSTANCE, currentLanguageCode);
			if (StringUtils.isNotBlank(label)) {
				mainTab.setLabel(currentLanguageCode, label);

				updateTreeNodeLabel(mainTab, label);
			}
		}
	}

	protected void refreshSelectedTreeNode(Component view) {
		Treeitem selectedItem = nodesTree.getSelectedItem();
		SchemaTreeNode treeNode = selectedItem.getValue();
		SchemaNodeData data = treeNode.getData();
		// update context menu
		Menupopup popupMenu = getPopupMenu(data);
		selectedItem.setContext(popupMenu);
	}

	protected EntityDefinition createRootEntityDefinition() {
		EntityDefinition rootEntity = createEntityDefinition();
		rootEntity.setName(DEFAULT_ROOT_ENTITY_NAME);
		survey.getSchema().addRootEntityDefinition(rootEntity);

		UIOptions uiOptions = survey.getUIOptions();
		rootTabSet = uiOptions.createRootTabSet((EntityDefinition) rootEntity);
		UITab mainTab = uiOptions.getMainTab(rootTabSet);
		mainTab.setLabel(currentLanguageCode, DEFAULT_MAIN_TAB_LABEL);

		notifyChange("rootEntities");

		return rootEntity;
	}

	protected EntityDefinition createEntityDefinition() {
		Schema schema = survey.getSchema();
		EntityDefinition newNode = schema.createEntityDefinition();
		return newNode;
	}

	public SchemaTreeModel getTreeModel() {
		if (treeModel == null) {
			buildTreeModel();
		}
		return treeModel;
	}

	protected void buildTreeModel() {
		CollectSurvey survey = getSurvey();
		if (survey == null) {
			// TODO session expired...?
		} else {
			TreeViewType viewType = TreeViewType.valueOf(selectedTreeViewType.toUpperCase());
			SurveyObjectTreeModelCreator modelCreator;
			switch (viewType) {
			case ENTRY:
				modelCreator = new UITreeModelCreator(selectedVersion, null, false, true, currentLanguageCode);
				break;
			default:
				modelCreator = new SchemaTreeModelCreator(selectedVersion, null, false, true, currentLanguageCode);
			}
			treeModel = modelCreator.createModel(selectedRootEntity);
		}
	}

	protected boolean isVersionSelected() {
		return survey.getVersions().isEmpty() || selectedVersion != null;
	}

	protected void refreshTreeModel() {
		// keep track of previous opened nodes
		Set<SurveyObject> openNodes;
		if (treeModel == null) {
			openNodes = Collections.emptySet();
		} else {
			openNodes = treeModel.getOpenSchemaNodes();
		}
		buildTreeModel();
		if (treeModel != null) {
			treeModel.setOpenSchemaNodes(openNodes);
			treeModel.select(editedNode);
			treeModel.showSelectedNode();
			if (CollectionUtils.isEmpty(treeModel.getSelection())) {
				resetEditingStatus();
			}
		}
		notifyChange("treeModel");
	}

	public boolean isTab(SchemaNodeData data) {
		return data != null && data.getSurveyObject() instanceof UITab;
	}

	public boolean isMainTab(SchemaNodeData data) {
		if (isTab(data)) {
			UIOptions uiOptions = survey.getUIOptions();
			return uiOptions.isMainTab((UITab) data.getSurveyObject());
		} else {
			return false;
		}
	}

	public boolean isEntity(SchemaNodeData data) {
		return data != null && data.getSurveyObject() instanceof EntityDefinition;
	}

	public boolean isSingleEntity(SchemaNodeData data) {
		return isEntity(data) && !((NodeDefinition) data.getSurveyObject()).isMultiple();
	}

	public boolean isTableEntity(SchemaNodeData data) {
		if (isEntity(data)) {
			UIOptions uiOptions = survey.getUIOptions();
			EntityDefinition entityDefn = (EntityDefinition) data.getSurveyObject();
			Layout layout = uiOptions.getLayout(entityDefn);
			return layout == Layout.TABLE;
		} else {
			return false;
		}
	}

	protected List<SurveyObject> getSiblingsInTree(SurveyObject surveyObject) {
		List<SurveyObject> result = treeModel.getSiblingsAndSelf(surveyObject, true);
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
		if (newNode || selectedTreeNode == null || isMainTab(selectedTreeNode)) {
			return true;
		} else {
			SurveyObject surveyObject = selectedTreeNode.getSurveyObject();
			List<SurveyObject> siblings = getSiblingsInTree(surveyObject);
			int index = siblings.indexOf(surveyObject);
			return isMoveItemDisabled(siblings, index, up);
		}
	}

	protected boolean isMoveItemDisabled(List<?> siblings, int index, boolean up) {
		return up ? index <= 0 : index < 0 || index >= siblings.size() - 1;
	}

	@DependsOn({ "newNode", "editedNode" })
	public String getNodeTypeHeaderLabel() {
		if (editedNode != null) {
			if (editedNode instanceof NodeDefinition) {
				return NodeType.getHeaderLabel((NodeDefinition) editedNode, editedNodeParentEntity == null, newNode);
			} else {
				return Labels.getLabel("survey.schema.node.layout.tab");
			}
		} else {
			return null;
		}
	}

	@DependsOn("editedNode")
	public String getNodeType() {
		if (editedNode != null && editedNode instanceof NodeDefinition) {
			NodeType type = NodeType.valueOf((NodeDefinition) editedNode);
			return type.name();
		} else {
			return null;
		}
	}

	@DependsOn("editedNode")
	public String getAttributeType() {
		if (editedNode != null && editedNode instanceof AttributeDefinition) {
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
		if (StringUtils.isNotBlank(typeValue)) {
			AttributeType type = AttributeType.valueOf(typeValue);
			return type.getLabel();
		} else {
			return null;
		}
	}

	public List<String> getAttributeTypeValues() {
		if (survey == null) {
			return Collections.emptyList(); // TODO session expired
		}
		List<String> result = new ArrayList<String>();
		AttributeType[] values = AttributeType.values();
		for (AttributeType type : values) {
			if (isSupported(type)) {
				result.add(type.name());
			}
		}
		return result;
	}

	private boolean isSupported(AttributeType type) {
		switch (survey.getTarget()) {
		case COLLECT_EARTH:
			return SUPPORTED_COLLECT_EARTH_ATTRIBUTE_TYPES.contains(type);
		default:
			return true;
		}
	}

	public String getAttributeTypeLabelFromDefinition(AttributeDefinition attrDefn) {
		if (attrDefn != null) {
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
		boolean key = surveyObject instanceof KeyAttributeDefinition && ((KeyAttributeDefinition) surveyObject).isKey();
		return getIcon(data, key);
	}

	public static String getIcon(SchemaNodeData data, boolean key) {
		SurveyObject surveyObject = data.getSurveyObject();
		String imagesRootPath = NODE_TYPES_IMAGES_PATH;
		if (surveyObject instanceof UITab) {
			return imagesRootPath + "tab-small.png";
		} else if (surveyObject instanceof EntityDefinition) {
			return getEntityIcon((EntityDefinition) surveyObject);
		} else if (key) {
			return imagesRootPath + "key-small.png";
		} else if (surveyObject instanceof AttributeDefinition && ((AttributeDefinition) surveyObject).isCalculated()) {
			return imagesRootPath + "calculated-small.png";
		} else {
			AttributeType attributeType = AttributeType.valueOf((AttributeDefinition) surveyObject);
			return getAttributeIcon(attributeType.name());
		}
	}

	protected static String getEntityIcon(EntityDefinition entityDefn) {
		CollectSurvey survey = (CollectSurvey) entityDefn.getSurvey();
		UIOptions uiOptions = survey.getUIOptions();
		Layout layout = uiOptions.getLayout(entityDefn);
		String icon;
		if (entityDefn.isMultiple()) {
			switch (layout) {
			case TABLE:
				icon = "table-small.png";
				break;
			case FORM:
			default:
				icon = "form-small.png";
			}
		} else {
			icon = "grouping-small.png";
		}
		return NODE_TYPES_IMAGES_PATH + icon;
	}

	public static String getAttributeIcon(String type) {
		AttributeType attributeType = AttributeType.valueOf(type);
		String result = NODE_TYPES_IMAGES_PATH + attributeType.name().toLowerCase(Locale.ENGLISH) + "-small.png";
		return result;
	}

	@DependsOn("editedNode")
	public String getEditedNodePath() {
		if (editedNode == null) {
			return null;
		} else if (editedNode instanceof NodeDefinition) {
			if (newNode) {
				return editedNodeParentEntity.getPath() + "/" + PATH_NULL_VALUES_REPLACE;
			} else {
				return ((NodeDefinition) editedNode).getPath();
			}
		} else {
			// tab
			UITab tab = (UITab) editedNode;
			return tab.getPath(currentLanguageCode, PATH_NULL_VALUES_REPLACE);
		}
	}

	@Command
	@NotifyChange({ "treeModel", "selectedTab" })
	public void addTab(@ContextParam(ContextType.BINDER) Binder binder) {
		if (TreeViewType.DATA.name().equalsIgnoreCase(selectedTreeViewType)) {
			MessageUtil.showWarning("survey.schema.unsupported_operation_in_data_view");
		} else {
			treeModel.deselect();
			addTabInternal(binder, rootTabSet);
		}
	}

	@Command
	@NotifyChange({ "treeModel", "selectedTab" })
	public void addChildTab(@ContextParam(ContextType.BINDER) Binder binder) {
		if (checkCanAddChildTab()) {
			UITab parentTab = getSelectedNodeParentTab();
			addTabInternal(binder, parentTab);
		}
	}

	private boolean checkCanAddChildTab() {
		if (TreeViewType.DATA.name().equalsIgnoreCase(selectedTreeViewType)) {
			MessageUtil.showWarning("survey.schema.unsupported_operation_in_data_view");
			return false;
		} else {
			SurveyObject selectedSurveyObject = selectedTreeNode.getSurveyObject();
			if (selectedSurveyObject instanceof UITab) {
				UITab parentTab = getSelectedNodeParentTab();
				UIOptions uiOptions = survey.getUIOptions();
				if (parentTab != null && uiOptions.isAssociatedWithMultipleEntityForm(parentTab)) {
					MessageUtil.showWarning("survey.schema.cannot_add_nested_tab.form_entity_assosicated");
					return false;
				}
			}
			return true;
		}
	}

	protected void addTabInternal(final Binder binder, final UITabSet parentTabSet) {
		if (rootTabSet != null) {
			checkCanLeaveForm(new CanLeaveFormConfirmHandler() {
				@Override
				public void onOk(boolean confirmed) {
					CollectSurvey survey = getSurvey();
					UIOptions uiOptions = survey.getUIOptions();
					UITab tab = uiOptions.createTab();
					String label = Labels.getLabel("survey.schema.node.layout.default_tab_label");
					tab.setLabel(currentLanguageCode, label);
					parentTabSet.addTab(tab);

					editNode(binder, false, null, tab);
					afterNewNodeCreated(tab, false);

					// dispatchTabSetChangedCommand();
				}

			});
		}
	}

	@Command
	public void removeTab() {
		UITab tab = (UITab) selectedTreeNode.getSurveyObject();
		if (checkCanRemoveTab(tab)) {
			removeTab(tab);
		}
	}

	private boolean checkCanRemoveTab(UITab tab) {
		if (isCollectEarthSurvey()) {
			List<NodeDefinition> nodes = survey.getUIOptions().getNodesPerTab(tab, true);
			for (NodeDefinition nodeDef: nodes) {
				if (isCollectEarthRequiredField(nodeDef)) {
					MessageUtil.showWarning("survey.schema.cannot_remove_tab_containing_ce_required_field",
							tab.getLabel(currentLanguageCode),
							nodeDef.getName());
					return false;
				}
			}
		}
		return true;
	}

	private UITab getSelectedNodeParentTab() {
		UITab parentTab;
		SurveyObject selectedSurveyObject = selectedTreeNode.getSurveyObject();
		if (selectedSurveyObject instanceof UITab) {
			parentTab = (UITab) selectedSurveyObject;
		} else {
			UIOptions uiOptions = survey.getUIOptions();
			parentTab = uiOptions.getAssignedTab((NodeDefinition) selectedSurveyObject);
		}
		return parentTab;
	}

	private void removeTab(final UITab tab) {
		String confirmMessageKey = null;
		if (tab.getTabs().isEmpty()) {
			CollectSurvey survey = getSurvey();
			UIOptions uiOpts = survey.getUIOptions();
			List<NodeDefinition> nodesPerTab = uiOpts.getNodesPerTab(tab, false);
			if (!nodesPerTab.isEmpty()) {
				confirmMessageKey = "survey.layout.tab.remove.confirm.associated_nodes_present";
			}
		} else {
			confirmMessageKey = "survey.layout.tab.remove.confirm.nested_tabs_present";
		}
		if (confirmMessageKey != null) {
			MessageUtil.ConfirmParams params = new MessageUtil.ConfirmParams(new MessageUtil.ConfirmHandler() {
				@Override
				public void onOk() {
					performRemoveTab(tab);
				}
			}, confirmMessageKey);
			params.setOkLabelKey("global.delete_item");
			MessageUtil.showConfirm(params);
		} else {
			performRemoveTab(tab);
		}
	}

	protected void performRemoveTab(UITab tab) {
		// remove all nodes associated to the tab
		UIOptions uiOptions = tab.getUIOptions();
		List<NodeDefinition> nodesPerTab = uiOptions.getNodesPerTab(tab, false);
		for (NodeDefinition nodeDefn : nodesPerTab) {
			EntityDefinition parentDefn = nodeDefn.getParentEntityDefinition();
			parentDefn.removeChildDefinition(nodeDefn);
		}
		performRemoveSelectedTreeNode();

		UITabSet parent = tab.getParent();
		parent.removeTab(tab);

		refreshTreeModel();

		dispatchSurveyChangedCommand();
	}

	@Command
	public void updateTabLabel(@BindingParam("tab") UITab tab, @BindingParam("label") String label) {
		if (validateTabLabel(label)) {
			tab.setLabel(currentLanguageCode, label.trim());
			// dispatchTabChangedCommand(tab);
		}
	}

	protected boolean validateTabLabel(String label) {
		if (StringUtils.isBlank(label)) {
			MessageUtil.showWarning("survey.layout.tab.label.error.required");
			return false;
		} else {
			return true;
		}
	}

	@Command
	public void treeViewTypeSelected(@BindingParam("type") String type) {
		selectedTreeViewType = type;
		resetEditingStatus();
		refreshTreeModel();
	}

	public Menupopup getPopupMenu(SchemaNodeData data) {
		if (data == null) {
			return null;
		}
		Menupopup popupMenu;
		if (data.isDetached()) {
			popupMenu = detachedNodePopup;
		} else if (isTab(data)) {
			if (isMainTab(data)) {
				popupMenu = mainTabPopup;
			} else {
				popupMenu = tabPopup;
			}
		} else if (isEntity(data)) {
			if (isSingleEntity(data)) {
				popupMenu = singleEntityPopup;
			} else if (isTableEntity(data)) {
				popupMenu = tableEntityPopup;
			} else {
				popupMenu = formEntityPopup;
			}
		} else {
			popupMenu = attributePopup;
		}
		return popupMenu;
	}

	@Command
	public void openMoveNodePopup() {
		SchemaNodeData selectedTreeNode = getSelectedTreeNode();
		if (selectedTreeNode == null) {
			return;
		}
		SurveyObject selectedItem = selectedTreeNode.getSurveyObject();

		if (selectedItem instanceof NodeDefinition) {
			NodeDefinition selectedNode = (NodeDefinition) selectedItem;
			boolean changeParentNodeAllowed = checkChangeParentNodeAllowed(selectedNode);
			if (changeParentNodeAllowed) {
				openSelectParentNodePopupForReparent(selectedNode);
			}
		} else {
			// TODO support tab moving
			return;
		}
	}

	@Command
	public void openDuplicateNodePopup() {
		if (!checkCanLeaveForm()) {
			return;
		}
		SchemaNodeData selectedTreeNode = getSelectedTreeNode();
		if (selectedTreeNode == null) {
			return;
		}
		SurveyObject selectedItem = selectedTreeNode.getSurveyObject();

		if (selectedItem instanceof AttributeDefinition) {
			AttributeDefinition selectedNode = (AttributeDefinition) selectedItem;
			openSelectParentNodePopupForDuplicate(selectedNode);
		}
	}

	@Command
	public void openNodeConversionPopup() {
		if (!checkCanLeaveForm()) {
			return;
		}
		SurveyObject selectedItem = selectedTreeNode.getSurveyObject();

		if (selectedItem instanceof AttributeDefinition) {
			AttributeDefinition selectedNode = (AttributeDefinition) selectedItem;

			if (isDefinitionInPublishedSurvey(selectedNode) && !selectedNode.isCalculated()) {
				MessageUtil.showWarning("survey.schema.cannot_convert_published_survey_node");
				return;
			} else {
				AttributeConversionVM.openPopup(selectedNode);
			}
		}
	}

	private boolean isDefinitionInPublishedSurvey(NodeDefinition nodeDef) {
		if (isSurveyRelatedToPublishedSurvey()) {
			CollectSurvey publishedSurvey = surveyManager.getById(survey.getPublishedId());
			if (publishedSurvey.getSchema().containsDefinitionWithId(nodeDef.getId())) {
				return true;
			}
		}
		return false;
	}

	private boolean checkChangeParentNodeAllowed(NodeDefinition selectedNode) {
		UIOptions uiOptions = survey.getUIOptions();
		if (survey.isPublished()) {
			// only tab changing allowed
			final List<UITab> assignableTabs = uiOptions.getAssignableTabs(editedNodeParentEntity, selectedNode);
			if (assignableTabs.size() > 0) {
				return true;
			} else {
				MessageUtil.showWarning("survey.schema.move_node.published_survey.no_other_tabs_allowed");
				return false;
			}
		} else {
			return true;
		}
	}

	private void openSelectParentNodePopupForReparent(final NodeDefinition selectedItem) {
		UIOptions uiOptions = survey.getUIOptions();
		final Set<UITab> assignableTabs = new HashSet<UITab>(
				uiOptions.getAssignableTabs(editedNodeParentEntity, selectedItem));
		final EntityDefinition selectedItemParentDefn = selectedItem.getParentEntityDefinition();
		UITab inheritedTab = uiOptions.getAssignedTab(selectedItemParentDefn);
		assignableTabs.add(inheritedTab);

		Predicate<SurveyObject> includedNodePredicate = new Predicate<SurveyObject>() {
			public boolean evaluate(SurveyObject item) {
				if (item instanceof UITab) {
					return true;
				} else if (item instanceof NodeDefinition) {
					if (item instanceof EntityDefinition) {
						EntityDefinition entityItemDef = (EntityDefinition) item;
						if (entityItemDef.isVirtual()) {
							return false;
						} else if (selectedItem instanceof EntityDefinition
								&& entityItemDef.isDescendantOf((EntityDefinition) selectedItem)) {
							return false;
						} else {
							return true;
						}
					} else {
						return false;
					}
				} else {
					return false;
				}
			}
		};
		Predicate<SurveyObject> disabledPredicate = new Predicate<SurveyObject>() {
			@Override
			public boolean evaluate(SurveyObject item) {
				if (item instanceof UITab) {
					return survey.isPublished() && !assignableTabs.contains(item);
				} else if (item instanceof NodeDefinition) {
					NodeDefinition itemNodeDef = (NodeDefinition) item;
					if (itemNodeDef.equals(selectedItemParentDefn)) {
						return false;
					} else if (selectedItem instanceof EntityDefinition
							&& itemNodeDef.isDescendantOf((EntityDefinition) selectedItem)) {
						// is descendant of the selected item
						return true;
					} else if (!survey.isPublished() && itemNodeDef instanceof EntityDefinition
							&& !itemNodeDef.equals(selectedItem)) {
						// allow reparenting node only if survey is not
						// published
						return false;
					} else {
						return true;
					}
				} else {
					// do not allow selecting non-node definitions
					return true;
				}
			}
		};
		String nodeName = editedNode instanceof NodeDefinition ? ((NodeDefinition) editedNode).getName() : "";
		UITab assignedTab = survey.getUIOptions().getAssignedTab((NodeDefinition) editedNode);
		String assignedTabLabel = assignedTab.getLabel(currentLanguageCode);
		String title = Labels.getLabel("survey.schema.move_node_popup_title",
				new String[] { getNodeTypeHeaderLabel(), nodeName, assignedTabLabel });

		// calculate parent item (tab or entity)
		SchemaTreeNode treeNode = treeModel.getTreeNode(selectedItem);
		TreeNode<SchemaNodeData> parentTreeNode = treeNode.getParent();
		SurveyObject parentItem = parentTreeNode.getData().getSurveyObject();

		final Window popup = SchemaTreePopUpVM.openPopup(title, selectedRootEntity, null, includedNodePredicate, false,
				true, disabledPredicate, null, parentItem, false);
		popup.addEventListener(SchemaTreePopUpVM.NODE_SELECTED_EVENT_NAME, new EventListener<NodeSelectedEvent>() {
			public void onEvent(NodeSelectedEvent event) throws Exception {
				SurveyObject selectedParent = event.getSelectedItem();
				changeEditedNodeParent(selectedParent, false);
				refreshNodeForm();
				closePopUp(popup);
			}
		});
	}

	private void openSelectParentNodePopupForDuplicate(final NodeDefinition node) {
		Predicate<SurveyObject> includedNodePredicate = new Predicate<SurveyObject>() {
			@Override
			public boolean evaluate(SurveyObject item) {
				return item instanceof UITab || item instanceof EntityDefinition;
			}
		};
		Predicate<SurveyObject> disabledPredicate = new Predicate<SurveyObject>() {
			@Override
			public boolean evaluate(SurveyObject item) {
				return !(item instanceof UITab || item instanceof EntityDefinition);
			}
		};
		String nodeName = node.getName();
		UITab assignedTab = survey.getUIOptions().getAssignedTab((NodeDefinition) node);
		String assignedTabLabel = assignedTab.getLabel(currentLanguageCode);
		String title = Labels.getLabel("survey.schema.duplicate_node_popup_title",
				new String[] { getNodeTypeHeaderLabel(), nodeName, assignedTabLabel });

		// calculate parent item (tab or entity)
		SchemaTreeNode treeNode = treeModel.getTreeNode(node);
		TreeNode<SchemaNodeData> parentTreeNode = treeNode.getParent();
		SurveyObject parentItem = parentTreeNode.getData().getSurveyObject();

		final Window popup = SchemaTreePopUpVM.openPopup(title, selectedRootEntity, null, includedNodePredicate, false,
				true, disabledPredicate, null, parentItem, false);
		popup.addEventListener(SchemaTreePopUpVM.NODE_SELECTED_EVENT_NAME, new EventListener<NodeSelectedEvent>() {
			public void onEvent(NodeSelectedEvent event) throws Exception {
				SurveyObject selectedParent = event.getSelectedItem();
				duplicateEditedNodeInto(node, selectedParent);
				closePopUp(popup);
			}
		});
	}

	private void duplicateEditedNodeInto(NodeDefinition node, SurveyObject parent) {
		NodeDefinition clone = survey.getSchema().cloneDefinition(node);
		EntityDefinition parentEntity = determineRelatedEntity(parent);
		clone.setName(createDuplicateNodeName(node, parentEntity));
		editedNode = clone;
		changeEditedNodeParent(parent, true);
		editNode(false, parentEntity, editedNode);
	}

	/**
	 * Creates a name for node that will be the duplicate of the specified one.
	 * The new name will be unique inside the specified parent entity.
	 */
	private String createDuplicateNodeName(NodeDefinition nodeToBeDuplicate, EntityDefinition parent) {
		String name = nodeToBeDuplicate.getName();
		Matcher matcher = CLONED_NAME_PATTERN.matcher(name);
		String prefix;
		int currentProgressiveNum;
		if (matcher.matches()) {
			prefix = matcher.group(1);
			currentProgressiveNum = Integer.parseInt(matcher.group(2));
		} else {
			prefix = name;
			currentProgressiveNum = 0;
		}
		// find unique new name
		String newName;
		do {
			currentProgressiveNum++;
			newName = prefix + currentProgressiveNum;
		} while (parent.containsChildDefinition(newName));

		return newName;
	}

	private void changeEditedNodeParent(SurveyObject newParent, boolean forceReassignment) {
		EntityDefinition newParentEntityDef = determineRelatedEntity(newParent);
		NodeDefinition editedNodeDef = (NodeDefinition) editedNode;
		if (forceReassignment || editedNodeDef.getParentDefinition() != newParentEntityDef) {
			changeEditedNodeParentEntity(newParentEntityDef);
		}
		if (newParent instanceof UITab) {
			associateNodeToTab(editedNodeDef, (UITab) newParent);
		}
		dispatchSurveyChangedCommand();
	}

	private EntityDefinition determineRelatedEntity(SurveyObject obj) {
		if (obj instanceof UITab) {
			UITab tab = (UITab) obj;
			EntityDefinition newParentEntityDef = tab.getUIOptions().getParentEntityForAssignedNodes(tab);
			return newParentEntityDef;
		} else {
			return (EntityDefinition) obj;
		}
	}

	private void changeEditedNodeParentEntity(EntityDefinition newParentEntity) {
		// update parent entity
		NodeDefinition node = (NodeDefinition) editedNode;
		Schema schema = survey.getSchema();
		schema.changeParentEntity(node, newParentEntity);
		// update tab
		UIOptions uiOptions = survey.getUIOptions();
		uiOptions.removeTabAssociation(node);
		if (node instanceof AttributeDefinition) {
			survey.getAnnotations().setMeasurementAttribute((AttributeDefinition) node, false);
		}
		// update ui
		refreshTreeModel();
		editedNodeParentEntity = newParentEntity;
		selectTreeNode(editedNode);
		treeModel.showSelectedNode();
		notifyChange("selectedTreeNode", "editedNode");
	}

	private void associateNodeToTab(NodeDefinition node, UITab tab) {
		UIOptions uiOptions = survey.getUIOptions();
		uiOptions.assignToTab(node, tab);
		refreshTreeModel();
		selectTreeNode(node);
		treeModel.showSelectedNode();
		notifyChange("selectedTreeNode", "editedNode");
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

	@DependsOn("selectedRootEntity")
	public boolean isRootEntitySelected() {
		return selectedRootEntity != null;
	}

	public String getSelectedTreeViewType() {
		return selectedTreeViewType;
	}

	public String[] getTreeViewTypes() {
		TreeViewType[] values = TreeViewType.values();
		String[] result = new String[values.length];
		for (int i = 0; i < values.length; i++) {
			TreeViewType type = values[i];
			result[i] = type.name().toLowerCase(Locale.ENGLISH);
		}
		return result;
	}

	public String getTreeViewTypeLabel(String type) {
		return Labels.getLabel("survey.schema.tree.view_type." + type);
	}

}
