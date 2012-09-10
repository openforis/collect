/**
 * 
 */
package org.openforis.collect.designer.viewmodel;

import org.openforis.collect.designer.form.ItemFormObject;
import org.openforis.collect.designer.form.SurveyMainInfoFormObject;
import org.openforis.collect.model.CollectSurvey;
import org.zkoss.zk.ui.select.annotation.VariableResolver;
import org.zkoss.zkplus.databind.BindingListModelList;

/**
 * 
 * @author S. Ricci
 *
 */
@VariableResolver(org.zkoss.zkplus.spring.DelegatingVariableResolver.class)
public class SurveyMainInfoEditVM extends SurveyItemEditVM<CollectSurvey> {
	
	@Override
	public CollectSurvey getEditedItem() {
		if ( editedItem == null ) {
			editedItem = getSurvey();
		}
		return editedItem;
	}
	
	@Override
	public ItemFormObject<CollectSurvey> getFormObject() {
		if ( formObject == null ) {
			CollectSurvey survey = getSurvey();
			formObject = createFormObject();
			formObject.loadFrom(survey, selectedLanguageCode);
		}
		return formObject;
	}
	
	@Override
	protected ItemFormObject<CollectSurvey> createFormObject() {
		return new SurveyMainInfoFormObject();
	}

	@Override
	public BindingListModelList<CollectSurvey> getItems() {
		return null;
	}

	@Override
	protected void addNewItemToSurvey() {
	}

	@Override
	protected void deleteItemFromSurvey(CollectSurvey item) {
	}

}
