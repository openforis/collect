package org.openforis.collect.designer.form.validator;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.zkoss.bind.Property;
import org.zkoss.bind.ValidationContext;
import org.zkoss.bind.validator.AbstractValidator;
import org.zkoss.util.resource.Labels;

/**
 * 
 * @author S. Ricci
 *
 */
public class NodeDefinitionFormValidator extends AbstractValidator {

	protected static final String DESCRIPTION_FIELD = "description";
	protected static final String NAME_FIELD = "name";
	
	protected static final String FIELD_REQUIRED_LABEL_KEY = "survey.schema.node.validation.field_required";

	public void validate(ValidationContext ctx) {
		//all the bean properties
		Map<String,Property> beanProps = ctx.getProperties(ctx.getProperty().getBase());

		validateName(ctx, beanProps);
		validateDescription(ctx, beanProps);
	}

	protected void validateName(ValidationContext ctx, Map<String,Property> beanProps) {
		validateRequired(ctx, beanProps, NAME_FIELD);
	}
	
	protected void validateDescription(ValidationContext ctx,  Map<String,Property> beanProps) {
		Property property = beanProps.get(DESCRIPTION_FIELD);
		Object value = property.getValue();
		//TODO
	}

	protected void validateRequired(ValidationContext ctx,  String fieldName, Object value) {
		if ( value == null || value instanceof String && StringUtils.isBlank((String) value)) {
			this.addInvalidMessage(ctx, fieldName, Labels.getLabel(FIELD_REQUIRED_LABEL_KEY));
		}
	}
	
	protected void validateRequired(ValidationContext ctx, Map<String,Property> beanProps, String fieldName) {
		Property property = beanProps.get(fieldName);
		Object value = property.getValue();
		validateRequired(ctx, fieldName, value);
	}

}
