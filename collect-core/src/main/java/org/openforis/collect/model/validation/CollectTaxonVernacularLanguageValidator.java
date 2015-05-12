package org.openforis.collect.model.validation;

import org.apache.commons.lang3.StringUtils;
import org.openforis.collect.metamodel.ui.UIOptions;
import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.CollectRecord.Step;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.FieldSymbol;
import org.openforis.idm.metamodel.TaxonAttributeDefinition;
import org.openforis.idm.metamodel.validation.TaxonVernacularLanguageValidator;
import org.openforis.idm.metamodel.validation.ValidationResultFlag;
import org.openforis.idm.model.TaxonAttribute;

/**
 * @author S. Ricci
 */
public class CollectTaxonVernacularLanguageValidator extends TaxonVernacularLanguageValidator {

	/**
	 * Returns:
	 * - OK if vernacular name, language code and language variety are all blank or all specified or 
	 * 		vernacular name is specified and language code and language variety are not visible in the UI
	 * - ERROR if vernacular name is specified but language code not
	 * - WARNING if vernacular name is specified and reason blank for language code is specified too
	 */
	@Override
	public ValidationResultFlag evaluate(TaxonAttribute attribute) {
		TaxonAttributeDefinition defn = attribute.getDefinition();
		CollectSurvey survey = (CollectSurvey) defn.getSurvey();
		UIOptions uiOptions = survey.getUIOptions();
		
		boolean vernacularNameBlank = StringUtils.isBlank(attribute.getVernacularName());
		boolean langCodeBlank = StringUtils.isBlank(attribute.getLanguageCode());
		boolean langCodeVisible = uiOptions.isVisibleField(defn, TaxonAttributeDefinition.LANGUAGE_CODE_FIELD_NAME);
		boolean langVarietyBlank = StringUtils.isBlank(attribute.getLanguageVariety());
		
		if ( (vernacularNameBlank && langCodeBlank && langVarietyBlank) ||
			( !vernacularNameBlank &&  !( langCodeVisible && langCodeBlank ) ) ) {
			return ValidationResultFlag.OK;
		} else {
			CollectRecord record = (CollectRecord) attribute.getRecord();
			Step step = record.getStep();
			if ( step == Step.ENTRY && langCodeBlank && FieldSymbol.isReasonBlankSpecified(attribute.getLanguageCodeField()) ) {
				return ValidationResultFlag.WARNING;
			} else {
				return ValidationResultFlag.ERROR;
			}
		}
	}

}
