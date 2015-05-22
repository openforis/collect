/**
 * 
 */
package org.openforis.collect.datacleansing.form.validation;

import java.util.Locale;

import org.openforis.collect.manager.SessionManager;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.metamodel.expression.ExpressionValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.core.GenericTypeResolver;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

/**
 * 
 * @author S. Ricci
 *
 */
public abstract class SimpleValidator<F> implements Validator {

	final Class<F> genericType;

	@Autowired
	protected SessionManager sessionManager;
	@Autowired
	protected ExpressionValidator expressionValidator;
	@Autowired
	protected MessageSource messageSource;

	@SuppressWarnings("unchecked")
	public SimpleValidator() {
		 this.genericType = (Class<F>) GenericTypeResolver.resolveTypeArgument(getClass(), SimpleValidator.class);
	}
	
	@Override
	public boolean supports(Class<?> clazz) {
		return genericType.isAssignableFrom(clazz);
	}
	
	@Override
	public final void validate(Object target, Errors errors) {
		@SuppressWarnings("unchecked")
		F form = (F) target;
		validateForm(form, errors);
	}
	
	public abstract void validateForm(F target, Errors errors);

	protected boolean validateRequiredField(Errors errors, String field) {
		Assert.notNull(errors, "Errors object must not be null");
		
		String code = "validation.required_field";
		String[] messageArgs = new String[0];
		String defaultMessage = messageSource.getMessage(code, messageArgs, Locale.ENGLISH);
		
		Object value = errors.getFieldValue(field);
		if (value == null || ! StringUtils.hasText(value.toString())) {
			errors.rejectValue(field, code, messageArgs, defaultMessage);
			return false;
		} else {
			return true;
		}
	}

	protected boolean validateBooleanExpression(Errors errors,
			NodeDefinition contextNodeDef, NodeDefinition thisNodeDef,
			String field, String expression) {
		boolean valid = expressionValidator.validateBooleanExpression(contextNodeDef, thisNodeDef, expression);
		if (! valid) {
			String code = "validation.invalid_expression";
			String[] messageArgs = new String[0];
			String defaultMessage = messageSource.getMessage(code, new String[0], Locale.ENGLISH);
			errors.rejectValue(field, code, messageArgs, defaultMessage);
		}
		return valid;
	}

	protected boolean validateValueExpression(Errors errors,
			NodeDefinition contextNodeDef, NodeDefinition thisNodeDef,
			String field, String expression) {
		boolean valid = expressionValidator.validateValueExpression(contextNodeDef, thisNodeDef, expression);
		if (! valid) {
			String code = "validation.invalid_expression";
			String[] messageArgs = new String[0];
			String defaultMessage = messageSource.getMessage(code, new String[0], Locale.ENGLISH);
			errors.rejectValue(field, code, messageArgs, defaultMessage);
		}
		return valid;
	}
}
