/**
 * 
 */
package org.openforis.collect.relational.model;

import java.sql.Types;

import org.openforis.collect.model.CollectSurvey;
import org.openforis.idm.metamodel.CodeListItem;
import org.openforis.idm.metamodel.SurveyObject;

/**
 * @author S. Ricci
 *
 */
public class CodeListDescriptionColumn extends AbstractColumn<CodeListItem> {

	private static final int MAX_LENGTH = 511;
	
	private String languageCode;

	CodeListDescriptionColumn(String languageCode, String name) {
		super(name, Types.VARCHAR, "varchar", MAX_LENGTH, true);
		this.languageCode = languageCode;
	}

	public String getLanguageCode() {
		return languageCode;
	}

	@Override
	public Object extractValue(CodeListItem source) {
		String label = source.getDescription(languageCode);
		if ( label == null && source instanceof SurveyObject ) {
			CollectSurvey survey = (CollectSurvey) ((SurveyObject) source).getSurvey();
			if ( survey.isDefaultLanguage(languageCode) ) {
				label = source.getDescription(null);
			}
		}
		return label;
	}
	
}
