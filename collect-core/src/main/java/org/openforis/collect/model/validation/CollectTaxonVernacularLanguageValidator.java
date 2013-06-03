package org.openforis.collect.model.validation;

import org.apache.commons.lang3.StringUtils;
import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.CollectRecord.Step;
import org.openforis.collect.model.FieldSymbol;
import org.openforis.idm.metamodel.validation.TaxonVernacularLanguageValidator;
import org.openforis.idm.metamodel.validation.ValidationResultFlag;
import org.openforis.idm.model.TaxonAttribute;

/**
 * @author S. Ricci
 */
public class CollectTaxonVernacularLanguageValidator extends TaxonVernacularLanguageValidator {

	@Override
	public ValidationResultFlag evaluate(TaxonAttribute attribute) {
		boolean vernacularNameBlank = StringUtils.isBlank(attribute.getVernacularName());
		boolean langCodeBlank = StringUtils.isBlank(attribute.getLanguageCode());
		boolean langVarietyBlank = StringUtils.isBlank(attribute.getLanguageVariety());
		
		if ( (vernacularNameBlank && langCodeBlank && langVarietyBlank) ||
			(!vernacularNameBlank && !langCodeBlank) ) {
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
