package org.openforis.collect.designer.form.validator;

import java.util.List;

import org.openforis.collect.designer.viewmodel.CalculatedAttributeVM;
import org.openforis.idm.metamodel.CalculatedAttributeDefinition.Formula;
import org.zkoss.bind.ValidationContext;

/**
 * 
 * @author S. Ricci
 *
 */
public class CalculatedAttributeDefinitionFormValidator extends AttributeDefinitionFormValidator {
	
	private static final String FORMULAS_FIELD = "formulas";

	@Override
	protected void internalValidate(ValidationContext ctx) {
		super.internalValidate(ctx);
		
		validateFormulas(ctx);
	}

	private void validateFormulas(ValidationContext ctx) {
		CalculatedAttributeVM vm = (CalculatedAttributeVM) getVM(ctx);
		List<Formula> formulas = vm.getFormulas();
		validateRequired(ctx, FORMULAS_FIELD, formulas);
	}

}
