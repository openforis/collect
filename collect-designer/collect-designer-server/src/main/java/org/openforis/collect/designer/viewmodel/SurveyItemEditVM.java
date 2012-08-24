/**
 * 
 */
package org.openforis.collect.designer.viewmodel;

import org.apache.commons.beanutils.BeanUtils;
import org.springframework.core.GenericTypeResolver;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zkplus.databind.BindingListModelList;
import org.zkoss.zul.Textbox;

/**
 * 
 * @author S. Ricci
 *
 */
public abstract class SurveyItemEditVM<T> extends SurveyEditVM {
	
	private final Class<T> genericType;

	protected T selectedItem;
	
	protected T editedItem;
	
	@Wire
	Textbox labelTextBox;

	@SuppressWarnings("unchecked")
	public SurveyItemEditVM() {
		this.genericType = (Class<T>) GenericTypeResolver.resolveTypeArgument(getClass(), SurveyItemEditVM.class);
	}
	
	public abstract BindingListModelList<T> getItems();	
	
	@NotifyChange({"editingItem","editedItem","items","selectedItem"})
	@Command
	public void newItem() {
		//setSelectedItem(null);
		T newInstance = createItemInstance();
		editedItem = newInstance;
		addNewItemToSurvey();
		setSelectedItem(editedItem);
	}

	protected T createItemInstance() {
		T newInstance = null;
		try {
			newInstance = genericType.newInstance();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return newInstance;
	}
	
	/*
	@NotifyChange("items")
	@Command
	public void saveItem() {
		if ( selectedItem == null ) {
			addNewItemToSurvey();
			setSelectedItem(editedItem);
		} else {
			applyChangesToSelectedItem();
		}
	}
	*/
	
	@NotifyChange("items")
	protected abstract void addNewItemToSurvey();
	
	@NotifyChange({"items","editingItem","editedItem"})
	@Command
	public void deleteItem() {
		deleteItemFromSurvey();
		setEditedItem(null);
		setSelectedItem(null);
	}

	protected abstract void deleteItemFromSurvey();

	protected void applyChangesToSelectedItem() {
		try {
			BeanUtils.copyProperties(selectedItem, editedItem);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public T getSelectedItem() {
		return selectedItem;
	}

	@NotifyChange("editedItem")
	public void setSelectedItem(T item) {
		selectedItem = item;
		if ( item != null ) {
			setEditedItem(item);
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

	public T getEditedItem() {
		return editedItem;
	}

	@NotifyChange("editingItem")
	public void setEditedItem(T editedItem) {
		this.editedItem = editedItem;
	}
	
	public boolean isEditingItem() {
		return this.editedItem != null;
	}

	
}
