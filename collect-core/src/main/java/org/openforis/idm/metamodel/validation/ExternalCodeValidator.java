/**
 * 
 */
package org.openforis.idm.metamodel.validation;

import org.openforis.idm.metamodel.CodeAttributeDefinition;
import org.openforis.idm.metamodel.ExternalCodeListItem;
import org.openforis.idm.metamodel.ExternalCodeListProvider;
import org.openforis.idm.metamodel.Survey;
import org.openforis.idm.metamodel.SurveyContext;
import org.openforis.idm.model.CodeAttribute;

/**
 * @author M. Togna
 * 
 */
public class ExternalCodeValidator implements ValidationRule<CodeAttribute> {

	@Override
	public ValidationResultFlag evaluate(CodeAttribute codeAttribute) {
		if ( codeAttribute.getSurvey().getId() == null ) {
			return ValidationResultFlag.OK;
		}
		ExternalCodeListProvider externalCodeListProvider = getExternalCodeListProvider(codeAttribute);
		ExternalCodeListItem item = externalCodeListProvider.getItem(codeAttribute);
		if (item == null || item.getCode() == null || !item.getCode().equals(codeAttribute.getValue().getCode())) {
			if ( isUnlistedAllowed(codeAttribute) ) {
				return ValidationResultFlag.WARNING;
			} else {
				return ValidationResultFlag.ERROR;
			}
		} else {
			return ValidationResultFlag.OK;
		}
	}

	private ExternalCodeListProvider getExternalCodeListProvider(CodeAttribute codeAttribute) {
		Survey survey = codeAttribute.getSurvey();
		SurveyContext surveyContext = survey.getContext();
		return surveyContext.getExternalCodeListProvider();
	}

	private boolean isUnlistedAllowed(CodeAttribute attribute) {
		CodeAttributeDefinition definition = attribute.getDefinition();
		return definition.isAllowUnlisted();
	}

}
