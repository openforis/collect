package org.openforis.collect.web.validator;

import org.openforis.collect.datacleansing.form.validation.SimpleValidator;
import org.openforis.collect.metamodel.SingleAttributeSurveyCreationParameters;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;

@Component
public class SimpleSurveyParametersValidator extends SimpleValidator<SingleAttributeSurveyCreationParameters> {

	@Override
	public void validateForm(SingleAttributeSurveyCreationParameters parameters, Errors errors) {
		
	}
	
}