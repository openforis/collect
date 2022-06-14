/**
 * 
 */
package org.openforis.collect.designer.viewmodel;


import java.util.List;
import java.util.Locale;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.WordUtils;
import org.openforis.collect.designer.form.FormObject;
import org.openforis.collect.designer.util.MessageUtil;
import org.openforis.collect.designer.util.MessageUtil.ConfirmHandler;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.idm.metamodel.SurveyObject;
import org.zkoss.bind.Binder;
import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.DependsOn;
import org.zkoss.bind.annotation.GlobalCommand;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.HtmlBasedComponent;
import org.zkoss.zk.ui.Path;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.Wire;

/**
 * 
 * @author S. Ricci
 *
 */
public abstract class SurveyObjectBaseVM<T> extends SurveyBaseVM {
	
	private static final String NAME_TEXTBOX_ID = "nameTextbox";
	public static final String VALIDATE_COMMAND = "validate";
	public static final String APPLY_CHANGES_COMMAND = "applyChanges";
	public static final String COMMIT_CHANGES_COMMAND = "commitChanges";
	
	protected boolean newItem;
	protected T selectedItem;
	protected T editedItem;
	protected boolean changed;
	protected FormObject<T> formObject;
	protected boolean commitChangesOnApply;
	
	@Wire
	private Component formContainer;
	
	public SurveyObjectBaseVM() {
		commitChangesOnApply = true;
		formObject = createFormObject();
	}
	
	protected void doAfterCompose(@ContextParam(ContextType.VIEW) Component view){
		Selectors.wireComponents(view, this, false);
		Selectors.wireEventListeners(view, this);
	}
	
	@Override
	public void undoLastChanges() {
		super.undoLastChanges();
		resetEditedItem();
	}
	
	public List<T> getItems() {
		List<T> items = getItemsInternal();
		return items;
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
		setFocusOnNameTextbox();
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
	public void applyChanges(@ContextParam(ContextType.BINDER) Binder binder) {
		changed = true;
		if ( commitChangesOnApply ) {
			commitChanges(binder);
		}
	}

	@Command
	public void commitChanges(@ContextParam(ContextType.BINDER) Binder binder) {
		formObject.saveTo(editedItem, currentLanguageCode);
		if ( newItem ) {
			addNewItemToSurvey();
			setSelectedItem(editedItem);
			newItem = false;
			notifyChange("newItem");
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
	
	protected void moveSelectedItem(int indexTo) {
		dispatchSurveyChangedCommand();
		moveSelectedItemInSurvey(indexTo);
	}
	
	protected abstract void moveSelectedItemInSurvey(int indexTo);

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
		MessageUtil.ConfirmParams params = new MessageUtil.ConfirmParams(new ConfirmHandler() {
			@Override
			public void onOk() {
				performDeleteItem(item);
			}}, getConfirmDeleteMessageKey());
		params.setOkLabelKey("global.delete_item");
		MessageUtil.showConfirm(params);
	}

	protected String getConfirmDeleteMessageKey() {
		return "global.item.confirm_remove";
	}

	protected void performDeleteItem(T item) {
		deleteItemFromSurvey(item);
		changed = false;
		notifyChange("items","changed");
		if ( item.equals(selectedItem) ) {
			resetEditedItem();
			dispatchCurrentFormValidatedCommand(true);
			notifyChange("currentFormValid");
		}
		dispatchSurveyChangedCommand();
	}

	protected void resetEditedItem() {
		formObject = createFormObject();
		editedItem = null;
		selectedItem = null;
		notifyChange("formObject", "editedItem", "selectedItem");
	}

	protected String suggestInternalName(String label) {
		String name = label.trim().toLowerCase(Locale.ENGLISH).replaceAll("\\W", "_");
		name = StringUtils.strip(name, "_");
		return name;
	}
	
	protected String suggestLabel(String internalName) {
		String label = internalName.replaceAll("_", " ");
		label = WordUtils.capitalize(label);
		return label;
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
				formObject.loadFrom(editedItem, currentLanguageCode);
			}
		}
		notifyChange("editedItem","formObject");
	}

	protected void setFocusOnNameTextbox() {
		HtmlBasedComponent textbox = getNameTextbox();
		if ( textbox != null ) {
			textbox.setFocus(true);
		}
	}

	protected HtmlBasedComponent getNameTextbox() {
		if ( formContainer == null ) {
			return null;
		} else {
			HtmlBasedComponent textbox = (HtmlBasedComponent) Path.getComponent(formContainer.getSpaceOwner(), NAME_TEXTBOX_ID);
			return textbox;
		}
	}
	
	@DependsOn("editedItem")
	public boolean isEditingItem() {
		return this.editedItem != null;
	}
	
	public boolean isChanged() {
		return changed;
	}

	public boolean isNewItem() {
		return newItem;
	}
	
	public boolean isCommitChangesOnApply() {
		return commitChangesOnApply;
	}

	public void setCommitChangesOnApply(boolean commitChangesOnApply) {
		this.commitChangesOnApply = commitChangesOnApply;
	}

	public boolean isLocked() {
		return editedItem != null && (
				editedItem instanceof SurveyObject && survey.getAnnotations().isLocked((SurveyObject) editedItem) 
		);
	}
	
}
