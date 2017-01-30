package org.openforis.collect.web.validator;

import org.openforis.collect.datacleansing.form.validation.SimpleValidator;
import org.openforis.collect.metamodel.CEOSurveyCreationParameters;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;

@Component
public class CEOSurveyParametersValidator extends SimpleValidator<CEOSurveyCreationParameters> {

	@Override
	public void validateForm(CEOSurveyCreationParameters parameters, Errors errors) {
		
	}
	
}