/**
 * 
 */
package org.openforis.collect.designer.viewmodel;

import org.openforis.idm.metamodel.Unit;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.zkplus.databind.BindingListModelList;

/**
 * 
 * @author S. Ricci
 *
 */
public class SurveyUnitsEditVM extends SurveyItemEditVM<Unit> {

	@Override
	public BindingListModelList<Unit> getItems() {
		return new BindingListModelList<Unit>(survey.getUnits(), false);
	}

	@Override
	protected void addNewItemToSurvey() {
		survey.addUnit(editedItem);
	}

	@Override
	protected void deleteItemFromSurvey() {
		survey.removeUnit(selectedItem);
	}

	@NotifyChange({"selectedItem","editedItem","editingItem","itemLabel","itemAbbreviation"})
	@Command
	public void selectionChanged() {
	}
	
	@Override
	@NotifyChange({"editingItem","editedItem","items","selectedItem","itemLabel","itemAbbreviation"})
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

	public String getItemAbbreviation() {
		return editedItem != null ? editedItem.getAbbreviation(selectedLanguageCode): null;
	}

	public void setItemAbbreviation(String abbreviation) {
		if ( editedItem != null ) {
			editedItem.setAbbreviation(selectedLanguageCode, abbreviation);
		}
	}
	
}
