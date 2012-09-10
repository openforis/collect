/**
 * 
 */
package org.openforis.collect.designer.viewmodel;


import org.openforis.collect.designer.form.FormObject;
import org.openforis.collect.designer.form.ItemFormObject;
import org.openforis.idm.metamodel.ModelVersion;
import org.openforis.idm.metamodel.Versionable;
import org.springframework.core.GenericTypeResolver;
import org.zkoss.bind.Form;
import org.zkoss.bind.annotation.AfterCompose;
import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.VariableResolver;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zkplus.databind.BindingListModelList;

/**
 * 
 * @author S. Ricci
 *
 */
@VariableResolver(org.zkoss.zkplus.spring.DelegatingVariableResolver.class)
public abstract class SurveyItemEditVM<T> extends SurveyEditVM {
	
	private final Class<T> genericType;
	protected ItemFormObject<T> formObject;
	protected T selectedItem;
	protected T editedItem;
	
	//UI comopnents
	@Wire("#fx")
    Form form;
	
	@AfterCompose
    public void afterCompose(@ContextParam(ContextType.VIEW) Component view){
        Selectors.wireComponents(view, this, false);
	}
	
	@SuppressWarnings("unchecked")
	public SurveyItemEditVM() {
		this.genericType = (Class<T>) GenericTypeResolver.resolveTypeArgument(getClass(), SurveyItemEditVM.class);
	}
	
	public abstract BindingListModelList<T> getItems();	
	
	@Command
	@NotifyChange({"formObject","editingItem","editedItem","items","selectedItem","editedItemSinceVersion","editedItemDeprecatedVersion"})
	public void newItem() {
		T newInstance = createItemInstance();
		setEditedItem(newInstance);
		addNewItemToSurvey();
		setSelectedItem(editedItem);
	}

	@Command
	@NotifyChange({"editedItem","selectedItem"})
	public void applyChanges() {
		formObject.saveTo(editedItem, selectedLanguageCode);
	}
	
	@Command
	public void selectionChanged(@BindingParam("selectedItem") T selectedItem) {
		if ( currentFormValid ) {
			setSelectedItem(selectedItem);
			setEditedItem(selectedItem);
		} else {
			setSelectedItem(this.selectedItem);
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
	@NotifyChange({"items","editingItem","editedItem","formObject"})
	public void deleteItem(@BindingParam("item") T item) {
		if ( currentFormValid ) {
			deleteItemFromSurvey(item);
		} else {
			//TODO show confirm cancel changes message
		}
		if ( item.equals(selectedItem) ) {
			formObject = null;
			editedItem = null;
			selectedItem = null;
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
			formObject.loadFrom(editedItem, selectedLanguageCode);
		}
	}
	
	public boolean isEditingItem() {
		return this.editedItem != null;
	}
	
	public ModelVersion getEditedItemSinceVersion() {
		if ( editedItem != null && editedItem instanceof Versionable ) {
			return ( (Versionable) editedItem).getSinceVersion();
		} else {
			return null;
		}
	}
	
	public void setEditedItemSinceVersion(ModelVersion value) {
		if ( editedItem != null && editedItem instanceof Versionable ) {
			ModelVersion modelVersion = value == FormObject.VERSION_EMPTY_SELECTION ? null: value;
			( (Versionable) editedItem).setSinceVersion(modelVersion);
		}
	}

	public ModelVersion getEditedItemDeprecatedVersion() {
		if ( editedItem != null && editedItem instanceof Versionable ) {
			return ( (Versionable) editedItem).getDeprecatedVersion();
		} else {
			return null;
		}
	}

	public void setEditedItemDeprecatedVersion(ModelVersion value) {
		if ( editedItem != null && editedItem instanceof Versionable ) {
			ModelVersion modelVersion = value == FormObject.VERSION_EMPTY_SELECTION ? null: value;
			( (Versionable) editedItem).setDeprecatedVersion(modelVersion);
		}
	}

}
