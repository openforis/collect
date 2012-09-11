/**
 * 
 */
package org.openforis.collect.designer.viewmodel;

import org.openforis.collect.designer.form.CodeListItemFormObject;
import org.openforis.collect.designer.form.ItemFormObject;
import org.openforis.idm.metamodel.CodeListItem;
import org.zkoss.zkplus.databind.BindingListModelList;

/**
 * 
 * @author S. Ricci
 *
 */
public class SurveyCodeListItemEditVM extends SurveyItemEditVM<CodeListItem> {

	@Override
	protected void addNewItemToSurvey() {
		//do nothing
	}
	
	@Override
	protected void deleteItemFromSurvey(CodeListItem item) {
		//do nothing
	}
	
	@Override
	public BindingListModelList<CodeListItem> getItems() {
		//do nothing
		return null;
	}
	
	@Override
	protected ItemFormObject<CodeListItem> createFormObject() {
		return new CodeListItemFormObject();
	}
	
}
