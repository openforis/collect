package org.openforis.collect.designer.form.validator;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.openforis.collect.designer.form.NodeDefinitionFormObject;
import org.zkoss.bind.AnnotateBinder;
import org.zkoss.bind.BindContext;
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
public class NodeDefinitionFormValidator extends AbstractValidator {

	protected static final String DESCRIPTION_FIELD = "description";
	protected static final String NAME_FIELD = "name";
	
	protected static final String FIELD_REQUIRED_MESSAGE_KEY = "survey.schema.node.validation.field_required";

	public void validate(ValidationContext ctx) {
		validateName(ctx);
		validateDescription(ctx);
	}

	protected void validateName(ValidationContext ctx) {
		validateRequired(ctx, NAME_FIELD);
	}
	
	protected void validateDescription(ValidationContext ctx) {
		//TODO
		//Object value = getValue(ctx, DESCRIPTION_FIELD);
	}
	
	protected void validateRequired(ValidationContext ctx,  String fieldName, Object value) {
		if ( value == null || value instanceof String && StringUtils.isBlank((String) value)) {
			this.addInvalidMessage(ctx, fieldName, Labels.getLabel(FIELD_REQUIRED_MESSAGE_KEY));
		}
	}
	
	protected void validateRequired(ValidationContext ctx, String fieldName) {
		Object value = getValue(ctx, fieldName);
		validateRequired(ctx, fieldName, value);
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
		Form form = binder.getForm(formContainer, "nodeDefnFx");
		return form;
	}
	
	protected Object getValueFromForm(ValidationContext ctx, String fieldName) {
		Form form = getForm(ctx);
		Object value = form.getField(fieldName);
		return value;
	}

}
