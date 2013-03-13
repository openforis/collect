package org.openforis.collect.designer.form.validator;

import org.openforis.collect.designer.viewmodel.CheckVM;
import org.openforis.idm.metamodel.NodeDefinition;
import org.zkoss.bind.ValidationContext;

/**
 * 
 * @author S. Ricci
 *
 */
public abstract class CheckFormValidator extends FormValidator {
	
	private static final String PARENT_DEFINITION_ARG = "parentDefinition";

	@Override
	protected void internalValidate(ValidationContext ctx) {
	}
	
	protected NodeDefinition getParentDefintion(ValidationContext ctx) {
		NodeDefinition result = (NodeDefinition) ctx.getValidatorArg(PARENT_DEFINITION_ARG);
		return result;
	}

	protected CheckVM<?> getCheckVM(ValidationContext ctx) {
		Object vm = getVM(ctx);
		if ( vm instanceof CheckVM ) {
			return (CheckVM<?>) vm;
		} else {
			throw new  IllegalStateException("Unexpected view model class: " + vm.getClass().getName());
		}
	}
}
