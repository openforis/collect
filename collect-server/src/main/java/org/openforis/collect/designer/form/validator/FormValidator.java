package org.openforis.collect.designer.form.validator;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.validator.routines.UrlValidator;
import org.zkoss.bind.AnnotateBinder;
import org.zkoss.bind.BindContext;
import org.zkoss.bind.BindUtils;
import org.zkoss.bind.Form;
import org.zkoss.bind.Property;
import org.zkoss.bind.ValidationContext;
import org.zkoss.bind.validator.AbstractValidator;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;

/**
 * 
 * @author S. Ricci
 *
 */
public abstract class FormValidator extends AbstractValidator {

	protected static final String INTERNAL_NAME_REGEX = "[a-z][a-z0-9_]*";
	protected static final String INTERNAL_NAME_INVALID_VALUE_ERROR_KEY = "global.validation.internal_name.invalid_value";
	protected static final String FIELD_REQUIRED_MESSAGE_KEY = "global.item.validation.required_field";
	protected static final String INVALID_URI_MESSAGE_KEY = "global.item.validation.invalid_uri";
	protected static final String GREATER_THAN_MESSAGE_KEY = "global.item.validation.greater_than";
	protected static final String GREATER_THAN_EQUAL_MESSAGE_KEY = "global.item.validation.greater_than_equal";

	private static final String DEFAULT_FORM_ID = "fx";

	private String formId;
	
	public FormValidator() {
		formId = DEFAULT_FORM_ID;
	}
	
	@Override
	public void validate(ValidationContext ctx) {
		internalValidate(ctx);
		afterValidate(ctx);
	}
	
	protected void afterValidate(ValidationContext ctx) {
		Map<String, Object> args = new HashMap<String, Object>();
		args.put("valid", ctx.isValid());
		BindUtils.postGlobalCommand(null, null, "currentFormValidated", args);
	}

	protected abstract void internalValidate(ValidationContext ctx);

	protected boolean validateRequired(ValidationContext ctx,  String fieldName, Object value) {
		if ( value == null || value instanceof String && StringUtils.isBlank((String) value)) {
			this.addInvalidMessage(ctx, fieldName, Labels.getLabel(FIELD_REQUIRED_MESSAGE_KEY));
			return false;
		} else {
			return true;
		}
	}
	
	protected boolean validateRequired(ValidationContext ctx, String fieldName) {
		Object value = getValue(ctx, fieldName);
		return validateRequired(ctx, fieldName, value);
	}
	
	protected boolean validateInternalName(ValidationContext ctx, String fieldName) {
		return validateRegEx(ctx, INTERNAL_NAME_REGEX, fieldName, INTERNAL_NAME_INVALID_VALUE_ERROR_KEY);
	}
	
	protected boolean validateRegEx(ValidationContext ctx, String regex,
			String fieldName, String errorMessageKey) {
		Object value = getValue(ctx, fieldName);
		if ( value instanceof String && StringUtils.isNotBlank((String) value) ) {
			Pattern pattern = Pattern.compile(regex);  
			Matcher matcher = pattern.matcher((String) value);
			if ( ! matcher.matches() ) {  
				String message = Labels.getLabel(errorMessageKey);
				this.addInvalidMessage(ctx, fieldName, message);
				return false;
			}
		}
		return true;
	}

	protected boolean validateUri(ValidationContext ctx, String fieldName) {
		Object value = getValue(ctx, fieldName);
		if ( value instanceof String && StringUtils.isNotBlank((String) value) ) {
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
		return validateGreaterThan(ctx, fieldName, value, false);
	}
	
	protected boolean validateGreaterThan(ValidationContext ctx,
			String fieldName, Number value, boolean equal) {
		Object fieldValue = getValue(ctx, fieldName);
		if ( fieldValue instanceof Number ) {
			double fieldDoubleValue = ((Number) fieldValue).doubleValue();
			if ( fieldDoubleValue < value.doubleValue() || ! equal && fieldDoubleValue == value.doubleValue()) {
				String messageKey = equal ? GREATER_THAN_EQUAL_MESSAGE_KEY: GREATER_THAN_MESSAGE_KEY;
				String message = Labels.getLabel(messageKey, new Object[] {value});
				addInvalidMessage(ctx, fieldName, message);
				return false;
			} else {
				return true;
			}
		} else {
			throw new IllegalArgumentException("Number field value expected: " + fieldName);
		}
	}
	
	protected Object getValue(ValidationContext ctx, String fieldName) {
		Map<String, Property> properties = getProperties(ctx);
		Property property = properties.get(fieldName);
		Object value = property.getValue();
		return value;
	}

	protected Map<String, Property> getProperties(ValidationContext ctx) {
		Object base = ctx.getProperty().getBase();
		Map<String, Property> properties = ctx.getProperties(base);
		return properties;
	}

	protected Form getForm(ValidationContext ctx) {
		BindContext bindContext = ctx.getBindContext();
		AnnotateBinder binder = (AnnotateBinder) bindContext.getBinder();
		Component formContainer = bindContext.getComponent();
		Form form = binder.getForm(formContainer, formId);
		return form;
	}
	
	protected Object getValueFromForm(ValidationContext ctx, String fieldName) {
		Form form = getForm(ctx);
		Object value = form.getField(fieldName);
		return value;
	}

	public String getFormId() {
		return formId;
	}

	public void setFormId(String formId) {
		this.formId = formId;
	}

	

}
