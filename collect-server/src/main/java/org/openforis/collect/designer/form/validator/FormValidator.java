package org.openforis.collect.designer.form.validator;

import java.util.HashMap;
import java.util.Map;

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

	protected static final String FIELD_REQUIRED_MESSAGE_KEY = "global.item.validation.required_field";
	protected static final String INVALID_URI_MESSAGE_KEY = "global.item.validation.invalid_uri";

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

	protected void validateRequired(ValidationContext ctx,  String fieldName, Object value) {
		if ( value == null || value instanceof String && StringUtils.isBlank((String) value)) {
			this.addInvalidMessage(ctx, fieldName, Labels.getLabel(FIELD_REQUIRED_MESSAGE_KEY));
		}
	}
	
	protected void validateRequired(ValidationContext ctx, String fieldName) {
		Object value = getValue(ctx, fieldName);
		validateRequired(ctx, fieldName, value);
	}
	
	protected void validateUri(ValidationContext ctx, String fieldName) {
		Object value = getValue(ctx, fieldName);
		if ( value instanceof String ) {
			UrlValidator validator = UrlValidator.getInstance();
			if ( ! validator.isValid((String) value) ) {
				this.addInvalidMessage(ctx, fieldName, Labels.getLabel(INVALID_URI_MESSAGE_KEY));
			}
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
