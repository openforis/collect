/**
 * 
 */
package org.openforis.collect.datacleansing.form.validation;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.openforis.collect.manager.SessionManager;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.metamodel.Survey;
import org.openforis.idm.metamodel.expression.ExpressionValidator;
import org.openforis.idm.metamodel.expression.ExpressionValidator.ExpressionType;
import org.openforis.idm.metamodel.expression.ExpressionValidator.ExpressionValidationResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.MessageSource;
import org.springframework.core.GenericTypeResolver;
import org.springframework.util.Assert;
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
	@Qualifier("sessionManager")
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
	public void validate(Object target, Errors errors) {
		if (genericType.isAssignableFrom(target.getClass())) {
			@SuppressWarnings("unchecked")
			F form = (F) target;
			validateForm(form, errors);
		}
	}
	
	public abstract void validateForm(F target, Errors errors);

	protected CollectSurvey getActiveSurvey() {
		return sessionManager.getActiveSurvey();
	}
	
	protected boolean validateRequiredFields(Errors errors, String... fields) {
		boolean result = true;
		for (String field : fields) {
			result = result & validateRequiredField(errors, field);
		}
		return result;
	}
	
	protected boolean validateRequiredField(Errors errors, String field) {
		Assert.notNull(errors, "Errors object must not be null");
		
		Object value = errors.getFieldValue(field);
		if (value == null || StringUtils.isBlank(value.toString())) {
			rejectRequiredFields(errors, field);
			return false;
		} else {
			return true;
		}
	}

	protected void rejectRequiredFields(Errors errors, String... fields) {
		String errorCode = "validation.required_field";
		String defaultMessage = messageSource.getMessage(errorCode, null, Locale.ENGLISH);
		for (String field : fields) {
			errors.rejectValue(field, errorCode, defaultMessage);
		}
	}

	protected boolean validateBooleanExpression(Errors errors,
			NodeDefinition contextNodeDef, NodeDefinition thisNodeDef,
			String field, String expression) {
		return validateExpression(errors, contextNodeDef, thisNodeDef, field, expression, ExpressionType.BOOLEAN);
	}

	protected boolean validateValueExpression(Errors errors,
			NodeDefinition contextNodeDef, NodeDefinition thisNodeDef,
			String field, String expression) {
		return validateExpression(errors, contextNodeDef, thisNodeDef, field, expression, ExpressionType.VALUE);
	}
	
	protected boolean validateExpression(Errors errors,
			NodeDefinition contextNodeDef, NodeDefinition thisNodeDef,
			String field, String expression, ExpressionType type) {
		ExpressionValidationResult result = expressionValidator.validateExpression(type, contextNodeDef, thisNodeDef, expression);
		if (result.isError()) {
			String errorCode = "validation.invalid_expression";
			String validationMessage = StringUtils.defaultString(result.getDetailedMessage(), result.getMessage());
			String[] errorMessageArgs = new String[] {validationMessage};
			String defaultMessage = messageSource.getMessage(errorCode, errorMessageArgs, Locale.ENGLISH);
			errors.rejectValue(field, errorCode, defaultMessage);
		}
		return result.isOk();
	}
	
	protected boolean validateInternalName(Errors errors, String fieldName) {
		return validateRegEx(errors, fieldName, Survey.INTERNAL_NAME_REGEX, "generic.validation.invalid_internal_name");
	}
	
	protected boolean validateRegEx(Errors errors, String fieldName, String regex, String errorMessageKey) {
		return validateRegEx(errors, fieldName, Pattern.compile(regex), errorMessageKey);
	}
	
	protected boolean validateRegEx(Errors errors, String fieldName, Pattern pattern, String errorMessageKey) {
		Object value = errors.getFieldValue(fieldName);
		if ( value != null && value instanceof String && StringUtils.isNotBlank((String) value) ) {
			Matcher matcher = pattern.matcher((String) value);
			if ( ! matcher.matches() ) {  
				errors.rejectValue(fieldName, errorMessageKey);
				return false;
			}
		}
		return true;
	}

	protected void rejectDuplicateValue(Errors errors, String field, Object... args) {
		rejectDuplicateValues(errors, Arrays.asList(field), args);
	}

	protected void rejectDuplicateValues(Errors errors, List<String> fields, Object... args) {
		String errorCode = "validation.duplicate_value";
		for (String field : fields) {
			errors.rejectValue(field, errorCode, args, messageSource.getMessage(errorCode, args, Locale.ENGLISH));
		}
	}
}
