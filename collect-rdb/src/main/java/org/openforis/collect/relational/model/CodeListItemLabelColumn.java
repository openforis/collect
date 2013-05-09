/**
 * 
 */
package org.openforis.collect.relational.model;

import java.sql.Types;

import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.relational.DatabaseExporterConfig;
import org.openforis.idm.metamodel.CodeListItem;
import org.openforis.idm.metamodel.SurveyObject;

/**
 * @author S. Ricci
 *
 */
public class CodeListItemLabelColumn extends AbstractColumn<CodeListItem> {

	private static final int MAX_LENGTH = 255;
	
	private String languageCode;

	CodeListItemLabelColumn(String languageCode, String name) {
		super(name, Types.VARCHAR, "varchar", MAX_LENGTH, true);
		this.languageCode = languageCode;
	}

	public String getLanguageCode() {
		return languageCode;
	}

	@Override
	public Object extractValue(CodeListItem source) {
		return extractValue(DatabaseExporterConfig.createDefault(), source);
	}
	
	@Override
	public Object extractValue(DatabaseExporterConfig config,
			CodeListItem source) {
		String label = source.getLabel(languageCode);
		if ( label == null && source instanceof SurveyObject ) {
			CollectSurvey survey = (CollectSurvey) ((SurveyObject) source).getSurvey();
			if ( survey.isDefaultLanguage(languageCode) ) {
				label = source.getLabel(null);
			}
		}
		return label;
	}

}
