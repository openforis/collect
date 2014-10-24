package org.openforis.idm.metamodel.validation;

import org.apache.commons.lang3.StringUtils;
import org.openforis.idm.model.TaxonAttribute;

/**
 * @author S. Ricci
 */
public class TaxonVernacularLanguageValidator implements ValidationRule<TaxonAttribute> {

	@Override
	public ValidationResultFlag evaluate(TaxonAttribute attribute) {
		boolean vernacularNameBlank = StringUtils.isBlank(attribute.getVernacularName());
		boolean langCodeBlank = StringUtils.isBlank(attribute.getLanguageCode());
		boolean langVarietyBlank = StringUtils.isBlank(attribute.getLanguageVariety());
		
		if ( (vernacularNameBlank && langCodeBlank && langVarietyBlank) ||
			(!vernacularNameBlank && !langCodeBlank) ) {
			return ValidationResultFlag.OK;
		} else {
			return ValidationResultFlag.ERROR;
		}
	}

}
