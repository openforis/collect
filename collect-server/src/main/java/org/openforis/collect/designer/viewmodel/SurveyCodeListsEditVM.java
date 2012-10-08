/**
 * 
 */
package org.openforis.collect.designer.viewmodel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openforis.collect.designer.form.CodeListFormObject;
import org.openforis.collect.designer.form.CodeListFormObject.Type;
import org.openforis.collect.designer.form.ItemFormObject;
import org.openforis.collect.designer.util.MessageUtil;
import org.openforis.collect.designer.util.MessageUtil.ConfirmHandler;
import org.openforis.collect.designer.util.Resources;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.idm.metamodel.CodeList;
import org.openforis.idm.metamodel.CodeListItem;
import org.openforis.idm.metamodel.CodeListLevel;
import org.zkoss.bind.Binder;
import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.DependsOn;
import org.zkoss.bind.annotation.GlobalCommand;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.select.annotation.VariableResolver;
import org.zkoss.zul.Window;

/**
 * 
 * @author S. Ricci
 *
 */
@VariableResolver(org.zkoss.zkplus.spring.DelegatingVariableResolver.class)
public class SurveyCodeListsEditVM extends SurveyItemEditVM<CodeList> {

	private static final String SURVEY_CODE_LIST_GENERATED_LEVEL_NAME_LABEL_KEY = "survey.code_list.generated_level_name";
	public static final String CLOSE_CODE_LIST_ITEM_POP_UP_COMMAND = "closeCodeListItemPopUp";

//	private DefaultTreeModel<CodeListItem> treeModel;
	
	private List<List<CodeListItem>> itemsPerLevel;
	
	private List<CodeListItem> selectedItemsPerLevel;
	private Window codeListItemPopUp;
	
	@Override
	protected List<CodeList> getItemsInternal() {
		CollectSurvey survey = getSurvey();
		List<CodeList> codeLists = survey.getCodeLists();
		return codeLists;
	}

	@Override
	protected void addNewItemToSurvey() {
		CollectSurvey survey = getSurvey();
		survey.addCodeList(editedItem);
	}

	@Override
	protected void deleteItemFromSurvey(CodeList item) {
		CollectSurvey survey = getSurvey();
		survey.removeCodeList(item);
	}
	
	@Override
	protected ItemFormObject<CodeList> createFormObject() {
		return new CodeListFormObject();
	}

	@Command
	@Override
	protected void performNewItemCreation(Binder binder) {
		super.performNewItemCreation(binder);
		notifyChange("listLevels","itemsPerLevel","selectedItemsPerLevel","lastSelectedLevelIndex");
	}
	
	@Override
	protected CodeList createItemInstance() {
		CodeList instance = super.createItemInstance();
		return instance;
	}
	
	@Override
	protected void performItemSelection(CodeList item) {
		super.performItemSelection(item);
		notifyChange("listLevels","itemsPerLevel","selectedItemsPerLevel","lastSelectedLevelIndex");
	}
	
	@Override
	protected void moveSelectedItem(int indexTo) {
		survey.moveCodeList(selectedItem, indexTo);
	}
	
	@Command
	public void typeChanged(@BindingParam("type") String type) {
		Type typeEnum = CodeListFormObject.Type.valueOf(type);
		List<CodeListLevel> levels = editedItem.getHierarchy();
		switch (typeEnum) {
		case HIERARCHICAL:
			if ( levels.size() == 0 ) {
				addLevel();
			}
			break;
		case FLAT:
			if ( levels.size() == 1 ) {
				CodeListLevel firstLevel = levels.get(0);
				editedItem.removeLevel(firstLevel.getId());
			}
			break;
		}
	}
	
	@Command
	@NotifyChange({"listLevels","lastSelectedLevelIndex"})
	public void addLevel() {
		List<CodeListLevel> levels = editedItem.getHierarchy();
		int levelPosition = levels.size() + 1;
		CodeListLevel level = new CodeListLevel();
		String generatedName = Labels.getLabel(SURVEY_CODE_LIST_GENERATED_LEVEL_NAME_LABEL_KEY, new Object[]{levelPosition});
		level.setName(generatedName);
		editedItem.addLevel(level);
	}

	@Command
	@NotifyChange({"listLevels","lastSelectedLevelIndex"})
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

	@NotifyChange({"listLevels","lastSelectedLevelIndex"})
	protected void performRemoveLevel(int levelIndex) {
		List<CodeListLevel> levels = editedItem.getHierarchy();
		CodeListLevel level = levels.get(levelIndex);
		editedItem.removeLevel(level.getId());
		deselectItemsAfterLevel(levelIndex);
		notifyChange("listLevels","lastSelectedLevelIndex");
	}
	
	@Command
	@NotifyChange({"itemsPerLevel"})
	public void addItemInLevel(@BindingParam("levelIndex") int levelIndex) {
		if ( checkCurrentFormValid() ) {
			CodeListItem item = new CodeListItem();
			String code = generateItemCode(item);
			item.setCode(code);
			item.setCodeList(editedItem);
			if ( levelIndex == 0 ) {
				editedItem.addItem(item);
			} else {
				CodeListItem parentItem = selectedItemsPerLevel.get(levelIndex - 1);
				parentItem.addChildItem(item);
			}
			List<CodeListItem> itemsForCurrentLevel = itemsPerLevel.get(levelIndex);
			itemsForCurrentLevel.add(item);
			openChildItemEditPopUp(item);
		}
	}
	
	@Override
	public void setEditedItem(CodeList editedItem) {
		super.setEditedItem(editedItem);
//		initTreeModel();
		selectedItemsPerLevel = new ArrayList<CodeListItem>();
		initItemsPerLevel();
	}
	
	@Command
	public void codeListItemDoubleClicked(@BindingParam("item") CodeListItem item) {
		openChildItemEditPopUp(item);
	}
	
	protected String generateItemCode(CodeListItem item) {
		return "item_" + item.getId();
	}

	public void openChildItemEditPopUp(CodeListItem item) {
		Map<String, Object> args = new HashMap<String, Object>();
		args.put("item", item);
		codeListItemPopUp = openPopUp(Resources.Component.CODE_LIST_ITEM_EDIT_POP_UP.getLocation(), true, args);
		Binder binder = (Binder) codeListItemPopUp.getAttribute("$BINDER$");
		validateForm(binder);
	}

	@Command
	@NotifyChange({"itemsPerLevel","selectedItemsPerLevel","lastSelectedLevelIndex"})
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
	
	private void deselectItemsAfterLevel(int levelIndex) {
		int maxSelectedLevelIndex = selectedItemsPerLevel.size() - 1;
		for (int i = maxSelectedLevelIndex; i >= levelIndex; i --) {
			selectedItemsPerLevel.remove(i);
		}
	}
	
	@GlobalCommand
	@NotifyChange({"itemsPerLevel"})
	public void closeCodeListItemPopUp() {
		closePopUp(codeListItemPopUp);
	}

	private void initItemsPerLevel() {
		itemsPerLevel = new ArrayList<List<CodeListItem>>();
		if ( editedItem != null ) {
			List<CodeListItem> items = new ArrayList<CodeListItem>(editedItem.getItems());
			itemsPerLevel.add(items);
			for (CodeListItem selectedItem : selectedItemsPerLevel) {
				List<CodeListItem> childItems = new ArrayList<CodeListItem>(selectedItem.getChildItems());
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
	
	public int getLastSelectedLevelIndex() {
		return selectedItemsPerLevel.size() - 1;
	}
	
	public List<List<CodeListItem>> getItemsPerLevel() {
		return itemsPerLevel;
	}
	
}
