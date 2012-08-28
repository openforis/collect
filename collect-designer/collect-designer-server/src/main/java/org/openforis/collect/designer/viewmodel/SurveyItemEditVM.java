/**
 * 
 */
package org.openforis.collect.designer.viewmodel;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.beanutils.BeanUtils;
import org.openforis.idm.metamodel.ModelVersion;
import org.openforis.idm.metamodel.Versionable;
import org.springframework.core.GenericTypeResolver;
import org.zkoss.bind.annotation.Command;
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
public abstract class SurveyItemEditVM<T> extends SurveyEditVM {
	
	private final Class<T> genericType;

	protected T selectedItem;
	
	protected T editedItem;
	
	@SuppressWarnings("unchecked")
	public SurveyItemEditVM() {
		this.genericType = (Class<T>) GenericTypeResolver.resolveTypeArgument(getClass(), SurveyItemEditVM.class);
	}
	
	public abstract BindingListModelList<T> getItems();	
	
	@NotifyChange({"editingItem","editedItem","items","selectedItem","editedItemSinceVersion","editedItemDeprecatedVersion"})
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

	@NotifyChange("versionsForCombo")
	@GlobalCommand
	public void versionsUpdated() {}
	
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

	@NotifyChange({"editedItem","editedItemSinceVersion","editedItemDeprecatedVersion"})
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

	@NotifyChange({"editingItem","editedItemSinceVersion","editedItemDeprecatedVersion"})
	public void setEditedItem(T editedItem) {
		this.editedItem = editedItem;
	}
	
	public boolean isEditingItem() {
		return this.editedItem != null;
	}
	
	public List<ModelVersion> getVersionsForCombo() {
		List<ModelVersion> result = new ArrayList<ModelVersion>(survey.getVersions());
		result.add(0, VERSION_EMPTY_SELECTION);
		return new BindingListModelList<ModelVersion>(result, false);
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
			ModelVersion modelVersion = value == VERSION_EMPTY_SELECTION ? null: value;
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
			ModelVersion modelVersion = value == VERSION_EMPTY_SELECTION ? null: value;
			( (Versionable) editedItem).setDeprecatedVersion(modelVersion);
		}
	}

}
