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
import org.openforis.collect.designer.viewmodel.BaseVM;
import org.openforis.idm.metamodel.Survey;
import org.zkoss.bind.BindContext;
import org.zkoss.bind.Binder;
import org.zkoss.bind.Property;
import org.zkoss.bind.ValidationContext;
import org.zkoss.bind.validator.AbstractValidator;
import org.zkoss.util.resource.Labels;

/**
 * @author S. Ricci
 *
 */
public abstract class BaseValidator extends AbstractValidator {

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
		return validateRegEx(ctx, Survey.INTERNAL_NAME_REGEX, fieldName, INTERNAL_NAME_INVALID_VALUE_ERROR_KEY, validationMessageKey);
	}
	
	protected boolean validateRegEx(ValidationContext ctx, String regex,
			String fieldName, String errorMessageKey) {
		return validateRegEx(ctx, regex, fieldName, errorMessageKey, fieldName);
	}
	
	protected boolean validateRegEx(ValidationContext ctx,
			String regex, String fieldName,
			String errorMessageKey, String validationMessageKey) {
		Pattern pattern = Pattern.compile(regex);  
		return validateRegEx(ctx, pattern, fieldName, errorMessageKey, validationMessageKey);
	}

	protected boolean validateRegEx(ValidationContext ctx,
			Pattern pattern, String fieldName,
			String errorMessageKey) {
		return validateRegEx(ctx, pattern, fieldName, errorMessageKey, fieldName);
	}
	
	protected boolean validateRegEx(ValidationContext ctx,
			Pattern pattern, String fieldName,
			String errorMessageKey, String validationMessageKey) {
		Object value = getValue(ctx, fieldName);
		return validateRegExValue(ctx, pattern, value, errorMessageKey, validationMessageKey);
	}

	protected boolean validateRegExValue(ValidationContext ctx, Pattern pattern, Object value, String errorMessageKey,
			String validationMessageKey) {
		if ( value != null && value instanceof String && StringUtils.isNotBlank((String) value) ) {
			Matcher matcher = pattern.matcher((String) value);
			if ( ! matcher.matches() ) {  
				String message = Labels.getLabel(errorMessageKey);
				if ( validationMessageKey == null ) {
					addInvalidMessage(ctx, message);
				} else {
					addInvalidMessage(ctx, validationMessageKey, message);
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
		Double compareValue = getNumericValue(ctx, compareFieldName);
		return validateGreaterThan(ctx, fieldName, compareValue, compareFieldLabel, strict);
	}

	protected boolean validateGreaterThan(ValidationContext ctx,
			String fieldName, Number compareValue, String compareValueLabel, boolean strict) {
		Double value = getNumericValue(ctx, fieldName);
		if ( value == null ) {
			return true;
		}
		if ( value < compareValue.doubleValue() || strict && value == compareValue.doubleValue()) {
			String message = createCompareMessage(GREATER_THAN_MESSAGE_KEY, GREATER_THAN_EQUAL_MESSAGE_KEY, strict, 
					compareValue, compareValueLabel);
			addInvalidMessage(ctx, fieldName, message );
			return false;
		} else {
			return true;
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
		Double value = getNumericValue(ctx, fieldName);
		if ( value == null ) {
			return true;
		}
		if ( value > compareValue.doubleValue() || strict && value == compareValue.doubleValue()) {
			String message = createCompareMessage(LESS_THAN_MESSAGE_KEY, LESS_THAN_EQUAL_MESSAGE_KEY, strict, 
					compareValue, compareValueLabel);
			addInvalidMessage(ctx, fieldName, message );
			return false;
		} else {
			return true;
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
	
	protected <T> T getValueWithDefault(ValidationContext ctx, String fieldName, T defaultValue) {
		T value = getValue(ctx, fieldName, false);
		if ( value == null ) {
			return (T) defaultValue;
		} else {
			return value;
		}
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
	
	protected Double getNumericValue(ValidationContext ctx, String field) {
		Object value = getValue(ctx, field);
		if (value == null) {
			return null;
		}
		double doubleValue;
		if ( value instanceof Number ) {
			doubleValue = ((Number) value).doubleValue();
		} else {
			doubleValue = Double.parseDouble(value.toString());
		}
		return doubleValue;
	}

	protected boolean isNumber(ValidationContext ctx, String field) {
		Object value = getValue(ctx, field);
		return isNumber(value);
	}

	protected boolean isNumber(Object value) {
		if (value instanceof Number) {
			return true;
		}
		try {
			Double.parseDouble(value.toString());
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}

	protected Map<String, Property> getProperties(ValidationContext ctx) {
		return ctx.getProperties(ctx.getProperty().getBase());
	}

	@SuppressWarnings("unchecked")
	protected <T extends BaseVM> T getVM(ValidationContext ctx) {
		BindContext bindContext = ctx.getBindContext();
		Binder binder = bindContext.getBinder();
		Object vmObject = binder.getViewModel();
		if ( vmObject == null ) {
			throw new IllegalStateException("Unable to find view model instance");
		}
		return (T) vmObject;
	}

}
