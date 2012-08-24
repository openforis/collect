/**
 * 
 */
package org.openforis.collect.designer.viewmodel;

import java.util.Date;

import org.openforis.collect.util.DateUtil;
import org.openforis.idm.metamodel.ModelVersion;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.zkplus.databind.BindingListModelList;

/**
 * 
 * @author S. Ricci
 *
 */
public class SurveyVersioningEditVM extends SurveyItemEditVM<ModelVersion> {
	
	@Override
	protected void deleteItemFromSurvey() {
		survey.removeVersion(selectedItem);
	}

	@Override
	protected void addNewItemToSurvey() {
		survey.addVersion(editedItem);
	}

	@Override
	public BindingListModelList<ModelVersion> getItems() {
		return new BindingListModelList<ModelVersion>(survey.getVersions(), false);
	}
	
	@NotifyChange({"selectedItem","editedItem","editingItem","itemLabel","itemDescription","itemDate"})
	@Command
	public void selectionChanged() {
	}
	
	@Override
	@NotifyChange({"editingItem","editedItem","items","selectedItem","itemLabel","itemDescription","itemDate"})
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

	public Date getItemDate() {
		return editedItem != null ? DateUtil.parseXMLDateTime(editedItem.getDate()): null;
	}

	public void setItemDate(Date date) {
		if ( editedItem != null ) {
			String formattedDate = DateUtil.formatDateToXML(date);
			editedItem.setDate(formattedDate);
		}
	}

}
