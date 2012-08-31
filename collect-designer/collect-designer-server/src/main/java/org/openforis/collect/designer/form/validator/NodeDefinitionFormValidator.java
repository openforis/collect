package org.openforis.collect.designer.form.validator;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.openforis.idm.metamodel.NodeDefinition;
import org.zkoss.bind.Property;
import org.zkoss.bind.ValidationContext;
import org.zkoss.bind.validator.AbstractValidator;

/**
 * 
 * @author S. Ricci
 *
 */
public class NodeDefinitionFormValidator extends AbstractValidator {

	public void validate(ValidationContext ctx) {
		//all the bean properties
		Map<String,Property> beanProps = ctx.getProperties(ctx.getProperty().getBase());

		validateName(ctx, beanProps);
		validateDescription(ctx, (String) beanProps.get("description").getValue());
	}

	protected void validateName(ValidationContext ctx, Map<String,Property> beanProps) {
		validateRequired(ctx, beanProps, "name");
	}
	
	protected void validateDescription(ValidationContext ctx, String value) {
	}

	protected void validateRequired(ValidationContext ctx,  String fieldName, Object value) {
		if ( value == null || value instanceof String && StringUtils.isBlank((String) value)) {
			this.addInvalidMessage(ctx, fieldName, "Field required");
		}
	}
	
	protected void validateRequired(ValidationContext ctx, Map<String,Property> beanProps, String fieldName) {
		Property property = beanProps.get(fieldName);
		Object value = property.getValue();
		validateRequired(ctx, fieldName, value);
	}

}
