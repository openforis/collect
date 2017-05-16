package org.openforis.collect.designer.form.validator;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.openforis.collect.designer.viewmodel.CheckVM;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.metamodel.expression.ExpressionValidator.ExpressionType;
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
	protected static final String MESSAGE_FIELD = "message";

	@Override
	protected void internalValidate(ValidationContext ctx) {
		NodeDefinition nodeDef = getContextNode(ctx);
		validateBooleanExpressionField(ctx, nodeDef, CONDITION_FIELD);
		validateNestedExpressionsInMessage(ctx);
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
			throw new IllegalStateException("Unexpected view model class: " + vm.getClass().getName());
		}
	}
	
	protected Check<?> getEditedCheck(ValidationContext ctx) {
		CheckVM<?> vm = getCheckVM(ctx);
		Check<?> check = vm.getEditedItem();
		return check;
	}
	
	private boolean validateNestedExpressionsInMessage(ValidationContext ctx) {
		NodeDefinition nodeDef = getContextNode(ctx);
		String message = getValue(ctx, MESSAGE_FIELD, true);
		if (StringUtils.isBlank(message)) {
			return true;
		}
		Check<?> check = getEditedCheck(ctx);
		List<String> expressions = check.extractExpressionsFromMessage(message);
		for (String expr : expressions) {
			boolean result = validateExpression(ctx, ExpressionType.VALUE, MESSAGE_FIELD, nodeDef.getParentEntityDefinition(), nodeDef, expr);
			if (! result) {
				return false;
			}
		}
		return true;
	}

}
