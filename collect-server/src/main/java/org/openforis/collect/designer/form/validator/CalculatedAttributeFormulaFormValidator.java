package org.openforis.collect.designer.form.validator;

import static org.openforis.collect.designer.viewmodel.CalculatedAttributeVM.*;
import org.openforis.idm.metamodel.CalculatedAttributeDefinition;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.NodeDefinition;
import org.zkoss.bind.ValidationContext;

/**
 * 
 * @author S. Ricci
 * 
 */
public class CalculatedAttributeFormulaFormValidator extends FormValidator {

	protected static final String EXPRESSION_FIELD = "expression";
	protected static final String CONDITION_FIELD = "condition";

	@Override
	protected void internalValidate(ValidationContext ctx) {
		validateExpression(ctx);
		validateCondition(ctx);
	}

	private void validateExpression(ValidationContext ctx) {
		if (validateRequired(ctx, EXPRESSION_FIELD)) {
			CalculatedAttributeDefinition attrDefn = getAttributeDefinition(ctx);
			EntityDefinition parentEntityDefn = getParentEntityDefinition(ctx);
			validateValueExpression(ctx, attrDefn, parentEntityDefn, EXPRESSION_FIELD);
		}
	}

	private void validateCondition(ValidationContext ctx) {
		NodeDefinition contextNode = getAttributeDefinition(ctx);
		validateBooleanExpression(ctx, contextNode, CONDITION_FIELD);
	}

	private CalculatedAttributeDefinition getAttributeDefinition(
			ValidationContext ctx) {
		CalculatedAttributeDefinition result = (CalculatedAttributeDefinition) ctx.getValidatorArg(FORMULA_POPUP_ATTRIBUTE_DEFINITION_ARG);
		return result;
	}

	private EntityDefinition getParentEntityDefinition(ValidationContext ctx) {
		EntityDefinition result = (EntityDefinition) ctx.getValidatorArg(FORMULA_POPUP_PARENT_ENTITY_DEFINITION_ARG);
		return result;
	}

}
