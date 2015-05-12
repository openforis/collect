package org.openforis.collect.designer.form.validator;

import org.openforis.collect.designer.viewmodel.CheckVM;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.metamodel.validation.Check;
import org.zkoss.bind.ValidationContext;

/**
 * 
 * @author S. Ricci
 *
 */
public abstract class CheckFormValidator extends FormValidator {
	
	private static final String PARENT_DEFINITION_ARG = "parentDefinition";

	protected static final String CONDITION_FIELD = "condition";
	
	@Override
	protected void internalValidate(ValidationContext ctx) {
		NodeDefinition node = getContextNode(ctx);
		validateBooleanExpression(ctx, node, CONDITION_FIELD);
	}

	protected NodeDefinition getContextNode(ValidationContext ctx) {
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
	
	protected Check<?> getEditedCheck(ValidationContext ctx) {
		CheckVM<?> vm = getCheckVM(ctx);
		Check<?> check = vm.getEditedItem();
		return check;
		
	}
}
