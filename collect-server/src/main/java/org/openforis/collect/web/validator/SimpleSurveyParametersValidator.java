package org.openforis.collect.web.validator;

import org.openforis.collect.datacleansing.form.validation.SimpleValidator;
import org.openforis.collect.web.controller.SimpleSurveyParameters;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;

@Component
public class SimpleSurveyParametersValidator extends SimpleValidator<SimpleSurveyParameters> {

	@Override
	public void validateForm(SimpleSurveyParameters parameters, Errors errors) {
		
	}
	
}