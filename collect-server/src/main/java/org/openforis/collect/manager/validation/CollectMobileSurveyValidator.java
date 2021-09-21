package org.openforis.collect.manager.validation;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.openforis.collect.manager.validation.SurveyValidator.SurveyValidationResult.Flag;
import org.openforis.idm.metamodel.AttributeDefinition;
import org.openforis.idm.metamodel.AttributeType;
import org.openforis.idm.metamodel.CodeAttributeDefinition;
import org.openforis.idm.metamodel.TextAttributeDefinition;
import org.springframework.stereotype.Component;

@Component
public class CollectMobileSurveyValidator extends SurveyValidator { 

	private static final String COLLECT_MOBILE_UNSUPPORTED_MULTIPLE_ATTRIBUTE_MESSAGE_KEY = 
			"survey.validation.attribute.collect_mobile_unsupported_multiple_attribute";

	private static final List<?> COLLECT_MOBILE_SUPPORTED_MULTIPLE_ATTRIBUTE_TYPES = Arrays.asList(
			CodeAttributeDefinition.class, 
			TextAttributeDefinition.class
	);
	
	@Override
	protected List<SurveyValidationResult> validateAttribute(AttributeDefinition attrDef,
			ValidationParameters validationParameters) {
		List<SurveyValidationResult> results = super.validateAttribute(attrDef, validationParameters);
		if (attrDef.isMultiple()) {
			addIfNotOk(results, validateMultipleAttribute(attrDef));
		}
		return results;
	}
	
	private SurveyValidationResult validateMultipleAttribute(AttributeDefinition attrDef) {
		if (attrDef.isMultiple() && !COLLECT_MOBILE_SUPPORTED_MULTIPLE_ATTRIBUTE_TYPES.contains(attrDef.getClass())) {
			String attributeTypeLabel = AttributeType.valueOf(attrDef).name().toLowerCase(Locale.ENGLISH);
			return new SurveyValidationResult(Flag.WARNING, attrDef.getPath(), 
					COLLECT_MOBILE_UNSUPPORTED_MULTIPLE_ATTRIBUTE_MESSAGE_KEY, attributeTypeLabel);
		}
		return new SurveyValidationResult();
	}
	
}
