package org.openforis.collect.designer.form.validator;

import org.openforis.idm.metamodel.NodeDefinition;
import org.zkoss.bind.ValidationContext;

/**
 * 
 * @author S. Ricci
 *
 */
public class PrecisionFormValidator extends FormValidator {
	
	private static final String PARENT_DEFINITION_ARG = "parentDefinition";

	@Override
	protected void internalValidate(ValidationContext ctx) {
	}
	
	protected NodeDefinition getParentDefintion(ValidationContext ctx) {
		NodeDefinition result = (NodeDefinition) ctx.getValidatorArg(PARENT_DEFINITION_ARG);
		return result;
	}

}
