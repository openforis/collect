/**
 * 
 */
package org.openforis.collect.designer.viewmodel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.apache.commons.lang3.StringUtils;
import org.openforis.collect.designer.form.CodeListFormObject;
import org.openforis.collect.designer.form.CodeListFormObject.Type;
import org.openforis.collect.designer.form.FormObject;
import org.openforis.collect.designer.form.validator.BaseValidator;
import org.openforis.collect.designer.session.SessionStatus;
import org.openforis.collect.designer.util.ComponentUtil;
import org.openforis.collect.designer.util.MessageUtil;
import org.openforis.collect.designer.util.MessageUtil.ConfirmHandler;
import org.openforis.collect.designer.util.Resources;
import org.openforis.collect.manager.CodeListManager;
import org.openforis.collect.manager.SurveyManager;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.commons.collection.CollectionUtils;
import org.openforis.idm.metamodel.CodeAttributeDefinition;
import org.openforis.idm.metamodel.CodeList;
import org.openforis.idm.metamodel.CodeList.CodeScope;
import org.openforis.idm.metamodel.CodeListItem;
import org.openforis.idm.metamodel.CodeListLevel;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.metamodel.PersistedCodeListItem;
import org.openforis.idm.metamodel.Schema;
import org.zkoss.bind.BindUtils;
import org.zkoss.bind.Binder;
import org.zkoss.bind.ValidationContext;
import org.zkoss.bind.Validator;
import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.DependsOn;
import org.zkoss.bind.annotation.ExecutionArgParam;
import org.zkoss.bind.annotation.GlobalCommand;
import org.zkoss.bind.annotation.Init;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.event.DropEvent;
import org.zkoss.zk.ui.select.annotation.VariableResolver;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.Window;

/**
 * 
 * @author S. Ricci
 *
 */
@VariableResolver(org.zkoss.zkplus.spring.DelegatingVariableResolver.class)
public class CodeListsVM extends SurveyObjectBaseVM<CodeList> {

	public static final String EDITING_ATTRIBUTE_PARAM = "editingAttribute";
	public static final String SELECTED_CODE_LIST_PARAM = "selectedCodeList";
	private static final String CODE_LISTS_UPDATED_GLOBAL_COMMAND = "codeListsUpdated";
	private static final String SURVEY_CODE_LIST_GENERATED_LEVEL_NAME_LABEL_KEY = "survey.code_list.generated_level_name";
	public static final String CLOSE_CODE_LIST_ITEM_POP_UP_COMMAND = "closeCodeListItemPopUp";
	public static final String CLOSE_CODE_LIST_IMPORT_POP_UP_COMMAND = "closeCodeListImportPopUp";
	
	private List<List<CodeListItem>> itemsPerLevel;
	private boolean newChildItem;
	private CodeListItem editedChildItem;
	private CodeListItem editedChildItemParentItem;
	private int editedChildItemLevel;
	
	private List<CodeListItem> selectedItemsPerLevel;
	private Window codeListItemPopUp;
	private Window referencedNodesPopUp;
	private Window codeListImportPopUp;
	private boolean editingAttribute;
	
	@WireVariable
	private SurveyManager surveyManager;
	@WireVariable
	private CodeListManager codeListManager;
	
	@Init(superclass=false)
	public void init(@ExecutionArgParam(EDITING_ATTRIBUTE_PARAM) Boolean editingAttribute, 
			@ExecutionArgParam(SELECTED_CODE_LIST_PARAM) CodeList selectedCodeList) {
		super.init();
		if ( selectedCodeList != null ) {
			selectionChanged(selectedCodeList);
		}
		this.editingAttribute = editingAttribute != null && editingAttribute.booleanValue();
	}
	
	@Override
	protected List<CodeList> getItemsInternal() {
		CollectSurvey survey = getSurvey();
		List<CodeList> codeLists = survey.getCodeLists();
		codeLists = sort(codeLists);
		return codeLists;
	}

	@Override
	protected void addNewItemToSurvey() {
		CollectSurvey survey = getSurvey();
		survey.addCodeList(editedItem);
		dispatchCodeListsUpdatedCommand();
	}

	@Override
	protected void deleteItemFromSurvey(CodeList item) {
		codeListManager.delete(item);
		dispatchCodeListsUpdatedCommand();
	}
	
	@Override
	protected FormObject<CodeList> createFormObject() {
		return new CodeListFormObject();
	}

	protected void dispatchCodeListsUpdatedCommand() {
		BindUtils.postGlobalCommand(null, null, CODE_LISTS_UPDATED_GLOBAL_COMMAND, null);
	}
	
	@Command
	@Override
	protected void performNewItemCreation(Binder binder) {
		super.performNewItemCreation(binder);
		notifyChange("listLevels","itemsPerLevel","selectedItemsPerLevel");
	}
	
	@Override
	protected CodeList createItemInstance() {
		CodeList instance = survey.createCodeList();
		return instance;
	}
	
	@Override
	protected void performItemSelection(CodeList item) {
		super.performItemSelection(item);
		notifyChange("listLevels","itemsPerLevel","selectedItemsPerLevel");
	}
	
	@Override
	protected void moveSelectedItem(int indexTo) {
		survey.moveCodeList(selectedItem, indexTo);
	}
	
	@Override
	@Command
	public void deleteItem(@BindingParam("item") final CodeList item) {
		List<NodeDefinition> references = getReferences(item);
		if ( ! references.isEmpty() ) {
			String title = Labels.getLabel("global.message.title.warning");
			String message = Labels.getLabel("survey.code_list.alert.cannot_delete_used_list");
			referencedNodesPopUp = SurveyErrorsPopUpVM.openPopUp(title, message, 
					references, new MessageUtil.ConfirmHandler() {
				@Override
				public void onOk() {
					closeReferencedNodesPopUp();
				}
			}, true);
		} else {
			super.deleteItem(item);
		}
	}

	protected void closeReferencedNodesPopUp() {
		closePopUp(referencedNodesPopUp);
		referencedNodesPopUp = null;
	}

	protected List<NodeDefinition> getReferences(CodeList item) {
		List<NodeDefinition> references = new ArrayList<NodeDefinition>();
		Schema schema = survey.getSchema();
		List<EntityDefinition> rootEntities = schema.getRootEntityDefinitions();
		Stack<NodeDefinition> stack = new Stack<NodeDefinition>();
		stack.addAll(rootEntities);
		while ( ! stack.isEmpty() ) {
			NodeDefinition defn = stack.pop();
			if ( defn instanceof EntityDefinition ) {
				stack.addAll(((EntityDefinition) defn).getChildDefinitions());
			} else if ( defn instanceof CodeAttributeDefinition ) {
				CodeList list = ((CodeAttributeDefinition) defn).getList();
				if ( list.equals(item) ) {
					references.add(defn);
				}
			};
		}
		return references;
	}
	
	@Command
	public void typeChanged(@BindingParam("type") String type) {
		Type typeEnum = CodeListFormObject.Type.valueOf(type);
		CodeScope scope;
		switch (typeEnum) {
		case HIERARCHICAL:
			scope = CodeScope.LOCAL;
			addLevel();
			break;
		default:
			editedItem.removeLevel(0);
			scope = CodeScope.SCHEME;
		}
		editedItem.setCodeScope(scope);
		CodeListFormObject fo = (CodeListFormObject) formObject;
		fo.setCodeScope(scope.name());
		fo.setType(type);
		notifyChange("formObject","listLevels");
	}
	
	@Command
	@NotifyChange({"listLevels"})
	public void addLevel() {
		List<CodeListLevel> levels = editedItem.getHierarchy();
		int levelPosition = levels.size() + 1;
		CodeListLevel level = new CodeListLevel();
		String generatedName = Labels.getLabel(SURVEY_CODE_LIST_GENERATED_LEVEL_NAME_LABEL_KEY, new Object[]{levelPosition});
		level.setName(generatedName);
		editedItem.addLevel(level);
	}

	@Command
	public void removeLevel() {
		final List<CodeListLevel> levels = editedItem.getHierarchy();
		if ( ! levels.isEmpty() ) {
			final int levelIndex = levels.size() - 1;
			if ( editedItem.hasItemsInLevel(levelIndex) ) {
				ConfirmHandler handler = new ConfirmHandler() {
					@Override
					public void onOk() {
						performRemoveLevel(levelIndex);
					}
				};
				MessageUtil.showConfirm(handler, "survey.code_list.alert.cannot_delete_non_empty_level");
			} else {
				performRemoveLevel(levelIndex);
			}
		}
	}

	public String getHierarchyLevelNameValidationKey(int levelIdx) {
		return "hiearchyLevelName_" + levelIdx;
	}
	
	public Validator getHierarchyLevelNameValidator(final int levelIdx) {
		return new BaseValidator() {
			@Override
			public void validate(ValidationContext ctx) {
				String validationKey = getHierarchyLevelNameValidationKey(levelIdx);
				if ( validateRequired(ctx, null, validationKey) ) {
					validateInternalName(ctx, null, validationKey);
				}
			}
		};
	}
	
	protected void performRemoveLevel(int levelIndex) {
		editedItem.removeLevel(levelIndex);
		deselectItemsAfterLevel(levelIndex);
		initItemsPerLevel();
		notifyChange("listLevels","selectedItemsPerLevel","itemsPerLevel");
	}
	
	@Command
	@NotifyChange({"itemsPerLevel"})
	public void addItemInLevel(@BindingParam("levelIndex") int levelIndex) {
		if ( checkCanLeaveForm() ) {
			newChildItem = true;
			editedChildItemLevel = levelIndex;
			editedChildItem = createChildItem();
			if ( editedChildItemLevel == 0 ) {
				editedChildItemParentItem = null;
			} else {
				editedChildItemParentItem = selectedItemsPerLevel.get(editedChildItemLevel - 1);
			}
			openChildItemEditPopUp();
		}
	}

	protected CodeListItem createChildItem() {
		if ( editedItem.isExternal() ) {
			throw new UnsupportedOperationException("Cannot instantiate ExternalCodeListItem object");
		} else if ( editedItem.isEmpty() ) {
			return new PersistedCodeListItem(editedItem);
		} else {
			return editedItem.createItem();
		}
	}
	
	@Command
	@NotifyChange({"itemsPerLevel"})
	public void deleteCodeListItem(@BindingParam("item") final CodeListItem item) {
		if ( isSurveyPublished() && isEnumeratingCodeList() ) {
			MessageUtil.showWarning("survey.code_list.cannot_delete_enumerating_code_list_items");
		} else {
			String messageKey;
			if ( codeListManager.hasChildItems(item) ) {
				messageKey = "survey.code_list.confirm.delete_non_empty_item";
			} else {
				messageKey = "survey.code_list.confirm.delete_item";
			}
			MessageUtil.showConfirm(new MessageUtil.ConfirmHandler() {
				@Override
				public void onOk() {
					performDeleteCodeListItem(item);
				}
			}, messageKey);
		}
	}

	protected boolean isEnumeratingCodeList() {
		return editedItem.isEnumeratingList();
	}
	
	protected void performDeleteCodeListItem(CodeListItem item) {
		boolean selected = isCodeListItemSelected(item);
		int itemLevelIndex = getLevelIndex(item);
		codeListManager.delete(item);
		if ( selected ) {
			deselectItemsAfterLevel(itemLevelIndex);
		}
		initItemsPerLevel();
		notifyChange("itemsPerLevel","selectedItemsPerLevel");
	}
	
	@Command
	public void moveChildItem(@ContextParam(ContextType.TRIGGER_EVENT) DropEvent event) {
		Listitem dragged = (Listitem) event.getDragged();
		Listitem dropped = (Listitem) event.getTarget();
		CodeListItem draggedItem = dragged.getValue();
		CodeListItem droppedItem = dropped.getValue();
		int indexTo = getItemIndex(droppedItem);
		moveChildItem(draggedItem, indexTo);
	}
	
	@Override
	public void setEditedItem(CodeList editedItem) {
		super.setEditedItem(editedItem);
		selectedItemsPerLevel = new ArrayList<CodeListItem>();
		initItemsPerLevel();
	}
	
	@Command
	public void editCodeListItem(@BindingParam("item") CodeListItem item) {
		newChildItem = false;
		editedChildItem = item;
		editedChildItemParentItem = item.getParentItem();
		openChildItemEditPopUp();
	}
	
	protected String generateItemCode(CodeListItem item) {
		return "item_" + item.getId();
	}

	public void openChildItemEditPopUp() {
		Map<String, Object> args = new HashMap<String, Object>();
		args.put(CodeListItemVM.ITEM_ARG, editedChildItem);
		args.put(CodeListItemVM.PARENT_ITEM_ARG, editedChildItemParentItem);
		args.put(CodeListItemVM.ENUMERATING_CODE_LIST_ARG, isSurveyPublished() && isEnumeratingCodeList());
		codeListItemPopUp = openPopUp(Resources.Component.CODE_LIST_ITEM_EDIT_POP_UP.getLocation(), true, args);
		Binder binder = ComponentUtil.getBinder(codeListItemPopUp);
		validateForm(binder);
	}

	@Command
	@NotifyChange({"itemsPerLevel","selectedItemsPerLevel"})
	public void listItemSelected(@BindingParam("item") CodeListItem item, 
			@BindingParam("levelIndex") int levelIndex) {
		deselectItemsAfterLevel(levelIndex);
		selectedItemsPerLevel.add(item);
		initItemsPerLevel();
	}
	
	@DependsOn("listLevels")
	public boolean isMultipleLevelsPresent() {
		if ( editedItem != null ) {
			return editedItem.getHierarchy().size() > 1;
		} else {
			return false;
		}
	}
	
	protected void deselectItemsAfterLevel(int levelIndex) {
		int maxSelectedLevelIndex = selectedItemsPerLevel.size() - 1;
		for (int i = maxSelectedLevelIndex; i >= levelIndex; i --) {
			selectedItemsPerLevel.remove(i);
		}
	}
	
	protected void moveChildItem(CodeListItem item, int toIndex) {
		codeListManager.shiftItem(item, toIndex);
		int levelIdx = getLevelIndex(item);
		List<CodeListItem> siblings = itemsPerLevel.get(levelIdx);
		CollectionUtils.shiftItem(siblings, item, toIndex);
		itemsPerLevel.set(levelIdx, siblings);
		if ( item instanceof PersistedCodeListItem ) {
			reloadSiblingsSortOrder((PersistedCodeListItem) item);
		}
		notifyChange("itemsPerLevel");
	}
	
	/**
	 * Reloads the siblings from the database.
	 * The sort order of these items changes after calling codeListManager.shiftItem method.
	 * 
	 * @param item
	 */
	protected void reloadSiblingsSortOrder(PersistedCodeListItem item) {
		int levelIdx = getLevelIndex(item);
		List<CodeListItem> newItems;
		if ( levelIdx == 0 ) {
			newItems = codeListManager.loadRootItems(item.getCodeList());
		} else {
			CodeListItem parentItem = codeListManager.loadParentItem(item);
			newItems = codeListManager.loadChildItems(parentItem);
		}
		List<CodeListItem> items = itemsPerLevel.get(levelIdx);
		for(int i=0; i < items.size(); i++) {
			CodeListItem oldItem = items.get(i);
			CodeListItem newItem = newItems.get(i);
			((PersistedCodeListItem) oldItem).setSortOrder(((PersistedCodeListItem) newItem).getSortOrder());
		}
	}

	protected int getLevelIndex(CodeListItem item) {
		for ( int index = 0; index < itemsPerLevel.size(); index++) {
			List<CodeListItem> items = itemsPerLevel.get(index);
			if ( items.contains(item) ) {
				return index;
			}
		}
		throw new IllegalArgumentException("Item not found in cache");
	}
	
	protected List<CodeListItem> getSiblings(CodeListItem item) {
		int levelIdx = getLevelIndex(item);
		List<CodeListItem> siblings = itemsPerLevel.get(levelIdx);
		return siblings;
	}
	
	protected int getItemIndex(CodeListItem item) {
		List<CodeListItem> siblings = getSiblings(item);
		int index = siblings.indexOf(item);
		return index;
	}
	
	@GlobalCommand
	public void closeCodeListItemPopUp(@BindingParam("undoChanges") boolean undoChanges) {
		closePopUp(codeListItemPopUp);
		codeListItemPopUp = null;
		if ( undoChanges ) {
			dispatchCurrentFormValidatedCommand(true);
		} else {
			if ( newChildItem ) {
				addChildItemToCodeList();
			} else {
				if ( editedChildItem instanceof PersistedCodeListItem ) {
					codeListManager.save((PersistedCodeListItem) editedChildItem);
				}
				BindUtils.postNotifyChange(null, null, editedChildItem, "*");
			}
		}
	}
	
	@Override
	protected void commitChanges() {
		super.commitChanges();
		dispatchCodeListsUpdatedCommand();
	}

	@Command
	public void openCodeListImportPopUp() {
		if ( canImportCodeList() ) {
			Map<String, Object> args = new HashMap<String, Object>();
			args.put("codeListId", editedItem.getId());
			codeListImportPopUp = openPopUp(Resources.Component.CODE_LIST_IMPORT_POPUP.getLocation(), true, args);
		} else if ( isExternalCodeList() ) {
			MessageUtil.showWarning("survey.code_list.cannot_import_items_on_external_code_list");
		} else {
			MessageUtil.showWarning("survey.code_list.cannot_import_items_on_enumerating_code_list");
		}
	}

	protected boolean canImportCodeList() {
		return ! isExternalCodeList() && ! isUsedAsEnumeratorInPublishedSurvey();
	}

	private boolean isUsedAsEnumeratorInPublishedSurvey() {
		return isSurveyPublished() && isEnumeratingCodeList() && isCodeListInPublishedSurvey();
	}
	
	private boolean isExternalCodeList() {
		return StringUtils.isNotBlank(editedItem.getLookupTable());
	}

	protected boolean isCodeListInPublishedSurvey() {
		SessionStatus sessionStatus = getSessionStatus();
		Integer publishedSurveyId = sessionStatus.getPublishedSurveyId();
		if ( publishedSurveyId != null ) {
			CollectSurvey publishedSurvey = surveyManager.getById(publishedSurveyId);
			CodeList oldPublishedCodeList = publishedSurvey.getCodeListById(editedItem.getId());
			return oldPublishedCodeList != null;
		} else {
			return false;
		}
	}

	@GlobalCommand
	public void closeCodeListImportPopUp() {
		closePopUp(codeListImportPopUp);
		codeListImportPopUp = null;
		boolean hasMultipleLevels = editedItem.getHierarchy().size() > 1;
		Type type = hasMultipleLevels ? Type.HIERARCHICAL: Type.FLAT;
		CodeListFormObject fo = (CodeListFormObject) formObject;
		fo.setType(type.name());
		String codeScopeName = getEditedItemCodeScopeName();
		fo.setCodeScope(codeScopeName);
		selectedItemsPerLevel = new ArrayList<CodeListItem>();
		initItemsPerLevel();
		notifyChange("formObject","listLevels","selectedItemsPerLevel");
	}

	protected String getEditedItemCodeScopeName() {
		CodeScope codeScope = editedItem.getCodeScope();
		if ( codeScope == null ) {
			codeScope = CodeListFormObject.DEFAULT_SCOPE;
		}
		return codeScope.name();
	}
	
	private void addChildItemToCodeList() {
		if ( editedItem.isEmpty() ) {
			PersistedCodeListItem persistedChildItem = (PersistedCodeListItem) editedChildItem;
			if ( editedChildItemParentItem != null ) {
				PersistedCodeListItem parentId = (PersistedCodeListItem) editedChildItemParentItem;
				persistedChildItem.setParentId(parentId.getSystemId());
			}
			codeListManager.save(persistedChildItem);
		} else if ( editedChildItemParentItem == null ) {
			editedItem.addItem(editedChildItem);
		} else {
			editedChildItemParentItem.addChildItem(editedChildItem);
		}
		List<CodeListItem> itemsForCurrentLevel = itemsPerLevel.get(editedChildItemLevel);
		itemsForCurrentLevel.add(editedChildItem);
		deselectItemsAfterLevel(editedChildItemLevel);
		selectedItemsPerLevel.add(editedChildItem);
		initItemsPerLevel();
		notifyChange("itemsPerLevel","selectedItemsPerLevel");
	}

	protected void initItemsPerLevel() {
		itemsPerLevel = new ArrayList<List<CodeListItem>>();
		if ( editedItem != null && ! editedItem.isExternal() ) {
			List<CodeListItem> items = codeListManager.loadRootItems(editedItem);
			itemsPerLevel.add(items);
			for (CodeListItem selectedItem : selectedItemsPerLevel) {
				List<CodeListItem> childItems = codeListManager.loadChildItems(selectedItem);
				itemsPerLevel.add(childItems);
			}
		}
	}
	
	public List<CodeListLevel> getListLevels() {
		List<CodeListLevel> levels = null;
		if ( editedItem != null ) {
			levels = editedItem.getHierarchy();
			if ( levels.isEmpty() ) {
				CodeListLevel fakeFirstLevel = new CodeListLevel();
				return Arrays.asList(fakeFirstLevel);
			}
		}
		return levels;
	}
	
	public List<CodeListItem> getSelectedItemsPerLevel() {
		return selectedItemsPerLevel;
	}
	
	@DependsOn("selectedItemsPerLevel")
	public int getLastSelectedLevelIndex() {
		return selectedItemsPerLevel.size() - 1;
	}
	
	public List<List<CodeListItem>> getItemsPerLevel() {
		return itemsPerLevel;
	}
	
	public boolean isCodeListItemSelected(CodeListItem item) {
		return selectedItemsPerLevel.contains(item);
	}
	
	public String getCodeListItemLabel(CodeListItem item) {
		String label = item.getLabel(currentLanguageCode);
		if ( label == null && isDefaultLanguage() ) {
			//try to get the label associated to default language
			label = item.getLabel(null);
		}
		return label;
	}

	public boolean isEditingAttribute() {
		return editingAttribute;
	}
	
}
