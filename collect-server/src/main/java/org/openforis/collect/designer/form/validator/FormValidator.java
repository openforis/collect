package org.openforis.collect.designer.form.validator;

import org.openforis.collect.designer.viewmodel.SurveyBaseVM;
import org.zkoss.bind.BindContext;
import org.zkoss.bind.Binder;
import org.zkoss.bind.ValidationContext;

/**
 * 
 * @author S. Ricci
 *
 */
public abstract class FormValidator extends BaseValidator {

	protected boolean blocking;
	
	@Override
	public void validate(ValidationContext ctx) {
		internalValidate(ctx);
		afterValidate(ctx);
	}
	
	protected void afterValidate(ValidationContext ctx) {
		Object vm = getVM(ctx);
		if ( vm instanceof SurveyBaseVM) {
			((SurveyBaseVM) vm).dispatchCurrentFormValidatedCommand(ctx.isValid(), blocking);
		}
	}

	protected abstract void internalValidate(ValidationContext ctx);

	protected Object getVM(ValidationContext ctx) {
		BindContext bindContext = ctx.getBindContext();
		Binder binder = bindContext.getBinder();
		Object vmObject = binder.getViewModel();
		if ( vmObject == null ) {
			throw new IllegalStateException("Unable to find view model instance");
		}
		return vmObject;
	}

	public boolean isBlocking() {
		return blocking;
	}
	
}
