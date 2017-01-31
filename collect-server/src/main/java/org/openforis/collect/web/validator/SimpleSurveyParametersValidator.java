package org.openforis.collect.web.validator;

import org.openforis.collect.datacleansing.form.validation.SimpleValidator;
import org.openforis.collect.metamodel.SimpleSurveyCreationParameters;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;

@Component
public class SimpleSurveyParametersValidator extends SimpleValidator<SimpleSurveyCreationParameters> {

	@Override
	public void validateForm(SimpleSurveyCreationParameters parameters, Errors errors) {
		
	}
	
}