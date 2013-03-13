/**
 * 
 */
package org.openforis.collect.designer.viewmodel;


import java.util.List;

import org.openforis.collect.designer.form.FormObject;
import org.openforis.collect.designer.util.MessageUtil;
import org.openforis.collect.designer.util.MessageUtil.ConfirmHandler;
import org.openforis.collect.model.CollectSurvey;
import org.zkoss.bind.Binder;
import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.DependsOn;
import org.zkoss.bind.annotation.GlobalCommand;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.zk.ui.select.annotation.VariableResolver;
import org.zkoss.zkplus.databind.BindingListModelList;

/**
 * 
 * @author S. Ricci
 *
 */
@VariableResolver(org.zkoss.zkplus.spring.DelegatingVariableResolver.class)
public abstract class SurveyObjectBaseVM<T> extends SurveyBaseVM {
	
	public static final String VALIDATE_COMMAND = "validate";
	public static final String APPLY_CHANGES_COMMAND = "applyChanges";
	public static final String COMMIT_CHANGES_COMMAND = "commitChanges";
	
	protected boolean newItem;
	protected T selectedItem;
	protected T editedItem;
	protected boolean changed;
	protected FormObject<T> formObject;
	private boolean commitChangesOnApply;
	
	public SurveyObjectBaseVM() {
		commitChangesOnApply = true;
	}
	
	public BindingListModelList<T> getItems() {
		List<T> items = getItemsInternal();
		return new BindingListModelList<T>(items, false);
	}
	
	protected abstract List<T> getItemsInternal();

	@Command
	public void newItem(@ContextParam(ContextType.BINDER) final Binder binder) {
		checkCanLeaveForm(new CanLeaveFormConfirmHandler() {
			@Override
			public void onOk(boolean confirmed) {
				performNewItemCreation(binder);
			}
		});
	}

	protected void performNewItemCreation(Binder binder) {
		T newInstance = createItemInstance();
		newItem = true;
		setEditedItem(newInstance);
		setSelectedItem(null);
		changed = false;
		notifyChange("editedItem","formObject","items","selectedItem","changed");
		validateForm(binder);
	}

	protected void validateForm(Binder binder) {
		//post apply changes command to force validation
		dispatchValidateCommand(binder);
	}

	protected void dispatchValidateCommand(Binder binder) {
		binder.postCommand(VALIDATE_COMMAND, null);
	}

	protected void dispatchApplyChangesCommand(Binder binder) {
		binder.postCommand(APPLY_CHANGES_COMMAND, null);
	}
	
	@Override
	@GlobalCommand
	public void currentLanguageChanged() {
		super.currentLanguageChanged();
		if ( isEditingItem() ) {
			performItemSelection(editedItem);
		}
	}

	@GlobalCommand
	public void validateAll(@ContextParam(ContextType.BINDER) Binder binder) {
		dispatchValidateCommand(binder);
	}
	
	@Command
	public void validate() {
	}
	
	@Command
	public void applyChanges() {
		if ( commitChangesOnApply ) {
			commitChanges();
		}
	}

	protected void commitChanges() {
		formObject.saveTo(editedItem, currentLanguageCode);
		if ( newItem ) {
			addNewItemToSurvey();
			setSelectedItem(editedItem);
			newItem = false;
		}
		notifyChange("items","selectedItem","changed");
		dispatchSurveyChangedCommand();
	}
	
	@Command
	public void selectionChanged(@BindingParam("selectedItem") final T item) {
		checkCanLeaveForm(new CanLeaveFormCompleteConfirmHandler() {
			@Override
			public void onOk(boolean confirmed) {
				performItemSelection(item);
			}
			@Override
			public void onCancel() {
				setSelectedItem(selectedItem);
				notifyChange("selectedItem");
			}
		});
	}

	protected void performItemSelection(T item) {
		newItem = false;
		changed = false;
		setSelectedItem(item);
		setEditedItem(item);
		notifyChange("selectedItem","formObject","editedItem");
		dispatchCurrentFormValidatedCommand(true);
	}
	
	@Command
	@NotifyChange({"items"})
	public void moveSelectedItemUp() {
		moveSelectedItem(true);
	}
	
	@Command
	@NotifyChange({"items"})
	public void moveSelectedItemDown() {
		moveSelectedItem(false);
	}
	
	protected int getSelectedItemIndex() {
		List<T> items = getItemsInternal();
		int index = items.indexOf(selectedItem);
		return index;
	}
	
	protected void moveSelectedItem(boolean up) {
		int indexFrom = getSelectedItemIndex();
		int indexTo = up ? indexFrom - 1: indexFrom + 1;
		moveSelectedItem(indexTo);
	}
	
	protected abstract void moveSelectedItem(int indexTo);

	@DependsOn({"items","selectedItem"})
	public boolean isMoveSelectedItemUpDisabled() {
		int index = getSelectedItemIndex();
		return index <= 0;
	}
	
	@DependsOn({"items","selectedItem"})
	public boolean isMoveSelectedItemDownDisabled() {
		if ( selectedItem != null ) {
			List<T> items = getItemsInternal();
			int size = items.size();
			int index = getSelectedItemIndex();
			return index < 0 || index >= size - 1;
		} else {
			return true;
		}
	}

	protected abstract FormObject<T> createFormObject();
	
	protected abstract T createItemInstance();
	
	@NotifyChange("items")
	protected abstract void addNewItemToSurvey();
	
	@Command
	public void deleteItem(@BindingParam("item") final T item) {
		MessageUtil.showConfirm(new ConfirmHandler() {
			@Override
			public void onOk() {
				performDeleteItem(item);
			}}, "global.item.confirm_remove");
	}

	protected void performDeleteItem(T item) {
		deleteItemFromSurvey(item);
		changed = false;
		notifyChange("items","changed");
		if ( item.equals(selectedItem) ) {
			formObject = null;
			editedItem = null;
			selectedItem = null;
			dispatchCurrentFormValidatedCommand(true);
			notifyChange("formObject", "editedItem", "selectedItem", "currentFormValid");
		}
	}

	protected abstract void deleteItemFromSurvey(T item);

	public T getSelectedItem() {
		return selectedItem;
	}

	public void setSelectedItem(T item) {
		selectedItem = item;
	}
	
	public FormObject<T> getFormObject() {
		return formObject;
	}

	public T getEditedItem() {
		return editedItem;
	}

	public void setEditedItem(T editedItem) {
		this.editedItem = editedItem;
		formObject = createFormObject();
		if ( editedItem != null ) {
			CollectSurvey survey = getSurvey();
			if ( survey == null ) {
				//session expired
			} else {
				String defaultLanguage = survey.getDefaultLanguage();
				formObject.loadFrom(editedItem, currentLanguageCode, defaultLanguage);
			}
		}
		notifyChange("editedItem","formObject");
	}
	
	@DependsOn("editedItem")
	public boolean isEditingItem() {
		return this.editedItem != null;
	}
	
	public boolean isChanged() {
		return changed;
	}

	public boolean isCommitChangesOnApply() {
		return commitChangesOnApply;
	}

	public void setCommitChangesOnApply(boolean commitChangesOnApply) {
		this.commitChangesOnApply = commitChangesOnApply;
	}
	
}
