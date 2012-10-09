/**
 * 
 */
package org.openforis.collect.designer.viewmodel;


import java.util.List;

import org.openforis.collect.designer.form.ItemFormObject;
import org.openforis.collect.designer.util.MessageUtil;
import org.openforis.collect.designer.util.MessageUtil.ConfirmHandler;
import org.springframework.core.GenericTypeResolver;
import org.zkoss.bind.BindUtils;
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
public abstract class SurveyItemEditVM<T> extends SurveyEditBaseVM {
	
	private final Class<T> genericType;
	protected ItemFormObject<T> formObject;
	protected T selectedItem;
	protected T editedItem;
	
	@SuppressWarnings("unchecked")
	public SurveyItemEditVM() {
		this.genericType = (Class<T>) GenericTypeResolver.resolveTypeArgument(getClass(), SurveyItemEditVM.class);
	}
	
	public BindingListModelList<T> getItems() {
		List<T> items = getItemsInternal();
		return new BindingListModelList<T>(items, false);
	}
	
	protected abstract List<T> getItemsInternal();

	@Command
	public void newItem(@ContextParam(ContextType.BINDER) Binder binder) {
		if ( checkCurrentFormValid() ) {
			performNewItemCreation(binder);
		}
	}

	protected void performNewItemCreation(Binder binder) {
		T newInstance = createItemInstance();
		setEditedItem(newInstance);
		addNewItemToSurvey();
		setSelectedItem(newInstance);
		notifyChange("editedItem", "formObject", "items", "selectedItem","moveSelectedItemUpDisabled","moveSelectedItemDownDisabled");
		validateForm(binder);
	}

	protected void validateForm(Binder binder) {
		//post apply changes command to force validation
		dispatchApplyChangesCommand(binder);
	}

	protected void dispatchApplyChangesCommand(Binder binder) {
		binder.postCommand("applyChanges", null);
	}
	
	@Override
	@GlobalCommand
	public void currentLanguageChanged() {
		super.currentLanguageChanged();
		if ( isEditingItem() ) {
			performItemSelection(editedItem);
		}
	}

	@Command
	public void applyChanges() {
		T editedItem = getEditedItem();
		formObject.saveTo(editedItem, currentLanguageCode);
		notifyChange("editedItem","selectedItem");
	}
	
	@Command
	public void selectionChanged(@BindingParam("selectedItem") T item) {
		if ( checkCurrentFormValid() ) {
			performItemSelection(item);
		} else {
			setSelectedItem(this.selectedItem);
			BindUtils.postNotifyChange(null, null, this, "selectedItem");
		}
	}

	protected void performItemSelection(T item) {
		setSelectedItem(item);
		setEditedItem(item);
		notifyChange("formObject","editedItem","moveSelectedItemUpDisabled","moveSelectedItemDownDisabled");
	}
	
	@Command
	@NotifyChange({"items","moveSelectedItemUpDisabled","moveSelectedItemDownDisabled"})
	public void moveSelectedItemUp() {
		moveSelectedItem(true);
	}
	
	@Command
	@NotifyChange({"items","moveSelectedItemUpDisabled","moveSelectedItemDownDisabled"})
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

	public boolean isMoveSelectedItemUpDisabled() {
		int index = getSelectedItemIndex();
		return index <= 0;
	}
	
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

	protected abstract ItemFormObject<T> createFormObject();
	
	protected T createItemInstance() {
		T newInstance = null;
		try {
			newInstance = genericType.newInstance();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return newInstance;
	}
	
	@NotifyChange("items")
	protected abstract void addNewItemToSurvey();
	
	@Command
	public void deleteItem(@BindingParam("item") final T item) {
		boolean deleteEditedItem = item.equals(selectedItem);
		if ( deleteEditedItem || checkCurrentFormValid() ) {
			ConfirmHandler handler = new ConfirmHandler() {
				@Override
				public void onOk() {
					performRemoveItem(item);
				}
			};
			MessageUtil.showConfirm(handler, "global.item.confirm_remove");
		} else {
			//TODO show confirm cancel changes message
		}
	}

	protected void performRemoveItem(T item) {
		deleteItemFromSurvey(item);
		notifyChange("items");
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

	@NotifyChange({"selectedItem"})
	public void setSelectedItem(T item) {
		selectedItem = item;
		if ( item != null ) {
			/*
			try {
				//T clonedInstance = (T) BeanUtils.cloneBean(item);
				T clonedInstance = createItemInstance();
				BeanUtils.copyProperties(clonedInstance, item);
				setEditedItem(clonedInstance);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
			*/
		}
	}
	
	public ItemFormObject<T> getFormObject() {
		return formObject;
	}

	public T getEditedItem() {
		return editedItem;
	}

	@NotifyChange({"editedItem","formObject"})
	public void setEditedItem(T editedItem) {
		this.editedItem = editedItem;
		formObject = createFormObject();
		if ( editedItem != null ) {
			formObject.loadFrom(editedItem, currentLanguageCode);
		}
	}
	
	@DependsOn("editedItem")
	public boolean isEditingItem() {
		return this.editedItem != null;
	}
	
}
