/**
 * 
 */
package org.openforis.collect.designer.viewmodel;

import java.util.List;

import org.openforis.collect.designer.form.FormObject;
import org.openforis.collect.designer.form.SurveyMainInfoFormObject;
import org.openforis.collect.manager.SurveyManager;
import org.openforis.collect.model.CollectSurvey;
import org.zkoss.bind.Binder;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.Init;
import org.zkoss.zk.ui.select.annotation.VariableResolver;
import org.zkoss.zk.ui.select.annotation.WireVariable;

/**
 * 
 * @author S. Ricci
 *
 */
@VariableResolver(org.zkoss.zkplus.spring.DelegatingVariableResolver.class)
public class SurveyMainInfoVM extends SurveyObjectBaseVM<CollectSurvey> {
	
	@WireVariable
	private SurveyManager surveyManager;
	
	@Init(superclass=false)
	public void init(@ContextParam(ContextType.BINDER) Binder binder) {
		super.init();
		setEditedItem(getSurvey());
		validateForm(binder);
	}
	
	@Override
	protected void performItemSelection(CollectSurvey item) {
		super.performItemSelection(item);
		dispatchValidateAllCommand();
	}
	
	@Override
	protected CollectSurvey createItemInstance() {
		//do nothing, no child instances created
		return null;
	}
	
	@Override
	protected FormObject<CollectSurvey> createFormObject() {
		return new SurveyMainInfoFormObject();
	}

	@Override
	protected List<CollectSurvey> getItemsInternal() {
		return null;
	}
	
	@Override
	protected void addNewItemToSurvey() {}

	@Override
	protected void deleteItemFromSurvey(CollectSurvey item) {}
	
	@Override
	protected void moveSelectedItemInSurvey(int indexTo) {}

	public SurveyManager getSurveyManager() {
		return surveyManager;
	}
	
	public Integer getEditedSurveyPublishedId() {
		return getSessionStatus().getPublishedSurveyId();
	}
}
