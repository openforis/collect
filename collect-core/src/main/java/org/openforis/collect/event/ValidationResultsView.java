package org.openforis.collect.event;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.openforis.collect.manager.MessageSource;
import org.openforis.collect.model.validation.SpecifiedValidator;
import org.openforis.collect.model.validation.ValidationMessageBuilder;
import org.openforis.idm.metamodel.validation.ValidationResult;
import org.openforis.idm.metamodel.validation.ValidationResults;
import org.openforis.idm.model.Attribute;

public class ValidationResultsView {

	private List<String> errors;
	private List<String> warnings;
	private boolean specifiedErrorPresent;

	public ValidationResultsView(Attribute<?, ?> attribute,
			MessageSource messageSource, Locale locale) {
		ValidationResults validationResults = attribute.getValidationResults();
		if (validationResults == null) {
			validationResults = new ValidationResults();
		}
		this.errors = extractValidationResultMessages(validationResults.getErrors(), attribute, messageSource, locale);
		this.warnings = extractValidationResultMessages(validationResults.getWarnings(), attribute, messageSource,
				locale);
		this.specifiedErrorPresent = calculateSpecifiedErrorPresent(validationResults.getErrors());
	}

	public List<String> getErrors() {
		return errors;
	}

	public List<String> getWarnings() {
		return warnings;
	}

	public boolean isSpecifiedErrorPresent() {
		return specifiedErrorPresent;
	}

	private boolean calculateSpecifiedErrorPresent(List<ValidationResult> validationResultList) {
		for (ValidationResult validationResult : validationResultList) {
			if(validationResult.getValidator() instanceof SpecifiedValidator) {
				return true;
			}
		}
		return false;
	}

	private List<String> extractValidationResultMessages(List<ValidationResult> validationResultList,
			Attribute<?, ?> attribute, MessageSource messageSource, Locale locale) {
		List<String> result = new ArrayList<String>();
		ValidationMessageBuilder validationMessageBuilder = ValidationMessageBuilder.createInstance(messageSource);
		for (ValidationResult validationResult : validationResultList) {
			result.add(validationMessageBuilder.getValidationMessage(attribute, validationResult, locale));
		}
		return result;
	}

}
