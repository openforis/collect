package org.openforis.collect.designer.form.validator;

import org.openforis.idm.metamodel.NodeDefinition;
import org.zkoss.bind.ValidationContext;

/**
 * 
 * @author S. Ricci
 *
 */
public class FileAttributeDefinitionFormValidator extends AttributeDefinitionFormValidator {
	
	protected static final int MAX_FILE_SIZE = 1024; //in MB = 1GB
	protected static final String MAX_SIZE_FIELD = "maxSize";
	protected static final String FILE_NAME_EXPRESSION_FIELD = "fileNameExpression";
	protected static final String EXPRESSIONS_REGEX = "([a-zA-Z0-9]+\\s?)+";
	protected static final String INVALID_EXTENSIONS_ERROR_KEY = "survey.schema.attribute.file.validation.error.extensions";
	
	@Override
	protected void internalValidate(ValidationContext ctx) {
		super.internalValidate(ctx);
		validateMaxSize(ctx);
		validateFileNameExpression(ctx);
	}

	protected void validateMaxSize(ValidationContext ctx) {
		String field = MAX_SIZE_FIELD;
		if ( validateRequired(ctx, field) ) {
			if ( validateGreaterThan(ctx, field, 0) ) {
				validateLessThan(ctx, field, MAX_FILE_SIZE, false);
			}
		}
	}
	
	protected void validateFileNameExpression(ValidationContext ctx) {
		NodeDefinition contextNode = getEditedNode(ctx);
		validateValueExpressionField(ctx, contextNode, FILE_NAME_EXPRESSION_FIELD);
	}

}
