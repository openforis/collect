/**
 * 
 */
package org.openforis.collect.designer.viewmodel;

import java.util.List;

import org.openforis.collect.designer.form.FormObject;
import org.openforis.collect.designer.form.SurveyMainInfoFormObject;
import org.openforis.collect.model.CollectSurvey;
import org.zkoss.bind.Binder;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.Init;
import org.zkoss.zk.ui.select.annotation.VariableResolver;

/**
 * 
 * @author S. Ricci
 *
 */
@VariableResolver(org.zkoss.zkplus.spring.DelegatingVariableResolver.class)
public class SurveyMainInfoVM extends SurveyObjectBaseVM<CollectSurvey> {
	
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
	protected void moveSelectedItem(int indexTo) {}
}
