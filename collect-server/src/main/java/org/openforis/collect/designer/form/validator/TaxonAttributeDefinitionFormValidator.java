package org.openforis.collect.designer.form.validator;

import org.zkoss.bind.ValidationContext;

/**
 * 
 * @author S. Ricci
 *
 */
public class TaxonAttributeDefinitionFormValidator extends AttributeDefinitionFormValidator {
	
	protected static final String TAXONOMY_FIELD = "taxonomy";
	
	@Override
	protected void internalValidate(ValidationContext ctx) {
		super.internalValidate(ctx);
		validateRequired(ctx, TAXONOMY_FIELD);
	}

}
