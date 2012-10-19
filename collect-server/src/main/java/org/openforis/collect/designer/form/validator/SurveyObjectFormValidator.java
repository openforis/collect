package org.openforis.collect.designer.form.validator;

import org.openforis.collect.designer.viewmodel.SurveyObjectBaseVM;
import org.openforis.idm.metamodel.SurveyObject;
import org.zkoss.bind.ValidationContext;

/**
 * 
 * @author S. Ricci
 *
 */
public abstract class SurveyObjectFormValidator<T extends SurveyObject> extends FormValidator {

	@SuppressWarnings("unchecked")
	protected SurveyObjectBaseVM<T> getSurveyObjectVM(ValidationContext ctx) {
		Object vm = getVM(ctx);
		if ( vm instanceof SurveyObjectBaseVM ) {
			return (SurveyObjectBaseVM<T>) vm;
		} else {
			throw new  IllegalStateException("Unexpected view model class: " + vm.getClass().getName());
		}
	}
}
