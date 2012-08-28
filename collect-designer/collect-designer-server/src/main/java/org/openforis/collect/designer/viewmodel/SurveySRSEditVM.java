/**
 * 
 */
package org.openforis.collect.designer.viewmodel;

import org.openforis.idm.metamodel.SpatialReferenceSystem;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.zk.ui.select.annotation.VariableResolver;
import org.zkoss.zkplus.databind.BindingListModelList;

/**
 * 
 * @author S. Ricci
 *
 */
@VariableResolver(org.zkoss.zkplus.spring.DelegatingVariableResolver.class)
public class SurveySRSEditVM extends SurveyItemEditVM<SpatialReferenceSystem> {

	@Override
	public BindingListModelList<SpatialReferenceSystem> getItems() {
		return new BindingListModelList<SpatialReferenceSystem>(survey.getSpatialReferenceSystems(), false);
	}

	@Override
	protected void addNewItemToSurvey() {
		survey.addSpatialReferenceSystem(editedItem);
	}

	@Override
	protected void deleteItemFromSurvey() {
		survey.removeSpatialReferenceSystem(selectedItem);
	}
	
	@NotifyChange({"selectedItem","editedItem","editingItem","itemLabel","itemDescription","itemDate"})
	@Command
	public void selectionChanged() {
	}
	
	@NotifyChange({"items","selectedItem","editedItem","editingItem","itemLabel","itemDescription","itemDate"})
	@Override
	@Command
	public void newItem() {
		super.newItem();
	}
	
	public String getItemLabel() {
		return editedItem != null ? editedItem.getLabel(selectedLanguageCode): null;
	}
	
	public void setItemLabel(String label) {
		if ( editedItem != null ) {
			editedItem.setLabel(selectedLanguageCode, label);
		}
	}

	public String getItemDescription() {
		return editedItem != null ? editedItem.getDescription(selectedLanguageCode): null;
	}

	public void setItemDescription(String description) {
		if ( editedItem != null ) {
			editedItem.setDescription(selectedLanguageCode, description);
		}
	}

}
