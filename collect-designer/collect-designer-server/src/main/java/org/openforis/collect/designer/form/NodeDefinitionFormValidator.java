package org.openforis.collect.designer.form;

import java.util.Map;

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

		//first let's check the passwords match
		validateName(ctx, (String)beanProps.get("name").getValue());
		validateDescription(ctx, (Integer)beanProps.get("description").getValue());
	}

	protected void validateDescription(ValidationContext ctx, Integer value) {
		// TODO Auto-generated method stub
		
	}

	protected void validateName(ValidationContext ctx, String value) {
		// TODO Auto-generated method stub
		
	}


}
