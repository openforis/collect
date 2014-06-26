/**
 * 
 */
package org.openforis.collect.designer.form.validator;

import java.util.Collection;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.validator.routines.UrlValidator;
import org.zkoss.bind.Property;
import org.zkoss.bind.ValidationContext;
import org.zkoss.bind.validator.AbstractValidator;
import org.zkoss.util.resource.Labels;

/**
 * @author S. Ricci
 *
 */
public abstract class BaseValidator extends AbstractValidator {

	protected static final String INTERNAL_NAME_REGEX = "[a-z][a-z0-9_]*";
	
	protected static final String INTERNAL_NAME_INVALID_VALUE_ERROR_KEY = "global.validation.internal_name.invalid_value";
	protected static final String FIELD_REQUIRED_MESSAGE_KEY = "global.item.validation.required_field";
	protected static final String INVALID_URI_MESSAGE_KEY = "global.item.validation.invalid_uri";
	protected static final String GREATER_THAN_MESSAGE_KEY = "global.item.validation.greater_than";
	protected static final String GREATER_THAN_EQUAL_MESSAGE_KEY = "global.item.validation.greater_than_equal";
	protected static final String LESS_THAN_MESSAGE_KEY = "global.item.validation.less_than";
	protected static final String LESS_THAN_EQUAL_MESSAGE_KEY = "global.item.validation.less_than_equal";
	protected static final String ITEM_NAME_ALREADY_DEFINED_MESSAGE_KEY = "global.item.validation.name_already_defined";

	protected boolean validateRequired(ValidationContext ctx, String validationMessageKey, Object value) {
		if ( 
				value == null || 
				value instanceof String && StringUtils.isBlank((String) value) ||
				value instanceof Collection && ((Collection<?>) value).isEmpty() 
				) {
			this.addInvalidMessage(ctx, validationMessageKey, Labels.getLabel(FIELD_REQUIRED_MESSAGE_KEY));
			return false;
		} else {
			return true;
		}
	}

	protected boolean validateRequired(ValidationContext ctx, String fieldName) {
		return validateRequired(ctx, fieldName, fieldName);
	}
	
	protected boolean validateRequired(ValidationContext ctx, String fieldName, String validationMessageKey) {
		Object value = getValue(ctx, fieldName);
		return validateRequired(ctx, validationMessageKey, value);
	}
	
	protected boolean validateInternalName(ValidationContext ctx, String fieldName) {
		return validateInternalName(ctx, fieldName, fieldName);
	}
	
	protected boolean validateInternalName(ValidationContext ctx, String fieldName, String validationMessageKey) {
		return validateRegEx(ctx, INTERNAL_NAME_REGEX, fieldName, INTERNAL_NAME_INVALID_VALUE_ERROR_KEY, validationMessageKey);
	}
	
	protected boolean validateRegEx(ValidationContext ctx, String regex,
			String fieldName, String errorMessageKey) {
		return validateRegEx(ctx, regex, fieldName, errorMessageKey, fieldName);
	}
	
	protected boolean validateRegEx(ValidationContext ctx,
			String regex, String fieldName,
			String errorMessageKey, String validationMessageKey) {
		Object value = getValue(ctx, fieldName);
		if ( value != null && value instanceof String && StringUtils.isNotBlank((String) value) ) {
			Pattern pattern = Pattern.compile(regex);  
			Matcher matcher = pattern.matcher((String) value);
			if ( ! matcher.matches() ) {  
				String message = Labels.getLabel(errorMessageKey);
				if ( validationMessageKey != null ) {
					addInvalidMessage(ctx, validationMessageKey, message);
				} else {
					addInvalidMessage(ctx, message);
				}
				return false;
			}
		}
		return true;
	}

	protected boolean validateUri(ValidationContext ctx, String fieldName) {
		Object value = getValue(ctx, fieldName);
		if ( value != null && value instanceof String && StringUtils.isNotBlank((String) value) ) {
			UrlValidator validator = UrlValidator.getInstance();
			if ( ! validator.isValid((String) value) ) {
				String message = Labels.getLabel(INVALID_URI_MESSAGE_KEY);
				this.addInvalidMessage(ctx, fieldName, message);
				return false;
			}
		}
		return true;
	}
	
	protected boolean validateGreaterThan(ValidationContext ctx,
			String fieldName, Number value) {
		return validateGreaterThan(ctx, fieldName, value, null, true);
	}
	
	protected boolean validateGreaterThan(ValidationContext ctx,
			String fieldName, Number value, boolean strict) {
		return validateGreaterThan(ctx, fieldName, value, null, strict);
	}
	
	protected boolean validateGreaterThan(ValidationContext ctx, String fieldName, 
			String compareFieldName, String compareFieldLabel, boolean strict) {
		Integer compareValue = getValue(ctx, compareFieldName);
		return validateGreaterThan(ctx, fieldName, compareValue, compareFieldLabel, strict);
	}

	protected boolean validateGreaterThan(ValidationContext ctx,
			String fieldName, Number compareValue, String compareValueLabel, boolean strict) {
		Object fieldValue = getValue(ctx, fieldName);
		if ( fieldValue == null ) {
			return true;
		}
		if ( ! (fieldValue instanceof Number) ) {
			throw new IllegalArgumentException("Number field value expected: " + fieldName);				
		} else {
			double fieldDoubleValue = ((Number) fieldValue).doubleValue();
			if ( fieldDoubleValue < compareValue.doubleValue() || strict && fieldDoubleValue == compareValue.doubleValue()) {
				String message = createCompareMessage(GREATER_THAN_MESSAGE_KEY, GREATER_THAN_EQUAL_MESSAGE_KEY, strict, 
						compareValue, compareValueLabel);
				addInvalidMessage(ctx, fieldName, message );
				return false;
			} else {
				return true;
			}
		}
	}
	
	protected boolean validateLessThan(ValidationContext ctx, String fieldName, Number value) {
		return validateLessThan(ctx, fieldName, value, null, true);
	}

	protected boolean validateLessThan(ValidationContext ctx,
			String fieldName, Number value, boolean strict) {
		return validateLessThan(ctx, fieldName, value, null, strict);
	}
	
	protected boolean validateLessThan(ValidationContext ctx, String fieldName, 
			String compareFieldName, String compareFieldLabel, boolean strict) {
		Integer compareValue = getValue(ctx, compareFieldName);
		return validateLessThan(ctx, fieldName, compareValue, compareFieldLabel, strict);
	}

	protected boolean validateLessThan(ValidationContext ctx,
			String fieldName, Number compareValue, String compareValueLabel, boolean strict) {
		Object fieldValue = getValue(ctx, fieldName);
		if ( fieldValue == null ) {
			return true;
		}
		if ( ! (fieldValue instanceof Number) ) {
			throw new IllegalArgumentException("Number field value expected: " + fieldName);				
		} else {
			double fieldDoubleValue = ((Number) fieldValue).doubleValue();
			if ( fieldDoubleValue > compareValue.doubleValue() || strict && fieldDoubleValue == compareValue.doubleValue()) {
				String message = createCompareMessage(LESS_THAN_MESSAGE_KEY, LESS_THAN_EQUAL_MESSAGE_KEY, strict, 
						compareValue, compareValueLabel);
				addInvalidMessage(ctx, fieldName, message );
				return false;
			} else {
				return true;
			}
		}
	}
	
	protected String createCompareMessage(String strictMessageKey, String notStrictMessageKey, boolean strict, 
			Number compareValue, String compareValueLabel) {
		String messageKey = strict ? strictMessageKey: notStrictMessageKey;
		String compareValueParam = compareValueLabel == null ? compareValue.toString(): compareValueLabel;
		String message = Labels.getLabel(messageKey, new Object[] {compareValueParam});
		return message;
	}
	
	/**
	 * Returns the actual value of a field.
	 * 
	 * @param ctx
	 * @param fieldName (Optional)
	 * @return
	 */
	protected <T> T getValue(ValidationContext ctx, String fieldName) {
		return getValue(ctx, fieldName, true);
	}
	
	@SuppressWarnings("unchecked")
	protected <T> T getValue(ValidationContext ctx, String fieldName, boolean required) {
		Object value;
		if ( fieldName == null ) {
			value = ctx.getProperty().getValue();
		} else {
			Map<String, Property> properties = getProperties(ctx);
			Property property = properties.get(fieldName);
			if ( property == null ) {
				if ( required ) {
					throw new RuntimeException("Required property not found in form object: " + fieldName);
				} else {
					value = null;
				}
			} else {
				value = property.getValue();
			}
		}
		return (T) value;
	}

	protected Map<String, Property> getProperties(ValidationContext ctx) {
		Object base = ctx.getProperty().getBase();
		Map<String, Property> properties = ctx.getProperties(base);
		return properties;
	}

}
