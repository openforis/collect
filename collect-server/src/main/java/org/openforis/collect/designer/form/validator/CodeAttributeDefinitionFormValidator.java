package org.openforis.collect.designer.form.validator;

import org.openforis.idm.metamodel.NodeDefinition;
import org.zkoss.bind.ValidationContext;

/**
 * 
 * @author S. Ricci
 *
 */
public class CodeAttributeDefinitionFormValidator extends AttributeDefinitionFormValidator {
	
	protected static final String LIST_FIELD = "list";
	protected static final String PARENT_EXPR_FIELD = "parentExpression";
	
	@Override
	protected void internalValidate(ValidationContext ctx) {
		super.internalValidate(ctx);
		validateRequired(ctx, LIST_FIELD);
		NodeDefinition contextNode = getEditedNode(ctx);
		validatePathExpression(ctx, contextNode, PARENT_EXPR_FIELD);
	}

}
