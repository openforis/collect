/**
 * 
 */
package org.openforis.collect.designer.viewmodel;


import org.openforis.collect.designer.form.ItemFormObject;
import org.openforis.collect.designer.util.MessageUtil;
import org.openforis.collect.designer.util.MessageUtil.ConfirmHandler;
import org.springframework.core.GenericTypeResolver;
import org.zkoss.bind.BindUtils;
import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.DependsOn;
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
	
	public abstract BindingListModelList<T> getItems();	
	
	@Command
	@NotifyChange({"formObject","editingItem","editedItem","items","selectedItem"})
	public void newItem() {
		if ( checkCurrentFormValid() ) {
			T newInstance = createItemInstance();
			setEditedItem(newInstance);
			addNewItemToSurvey();
			T editedItem = getEditedItem();
			setSelectedItem(editedItem);
		}
	}

	@Command
	@NotifyChange({"editedItem","selectedItem"})
	public void applyChanges() {
		T editedItem = getEditedItem();
		formObject.saveTo(editedItem, currentLanguageCode);
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

	@NotifyChange({"formObject","editedItem"})
	protected void performItemSelection(T item) {
		setSelectedItem(item);
		setEditedItem(item);
		BindUtils.postNotifyChange(null, null, this, "formObject");
		BindUtils.postNotifyChange(null, null, this, "editedItem");
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
		BindUtils.postNotifyChange(null, null, this, "items");
		if ( item.equals(selectedItem) ) {
			formObject = null;
			editedItem = null;
			selectedItem = null;
			currentFormValid = true;
			BindUtils.postNotifyChange(null, null, this, "formObject");
			BindUtils.postNotifyChange(null, null, this, "editedItem");
			BindUtils.postNotifyChange(null, null, this, "selectedItem");
			BindUtils.postNotifyChange(null, null, this, "currentFormValid");
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

	@NotifyChange({"editingItem","formObject"})
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
