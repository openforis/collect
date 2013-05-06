/**
 * 
 */
package org.openforis.collect.relational.model;

import java.sql.Types;

import org.openforis.collect.model.CollectSurvey;
import org.openforis.idm.metamodel.Labelable;
import org.openforis.idm.metamodel.SurveyObject;

/**
 * @author S. Ricci
 *
 */
public class LabelColumn extends AbstractColumn<Labelable> {

	private static final int MAX_LENGTH = 255;
	
	private String languageCode;

	LabelColumn(String languageCode, String name) {
		super(name, Types.VARCHAR, "varchar", MAX_LENGTH, true);
		this.languageCode = languageCode;
	}

	public String getLanguageCode() {
		return languageCode;
	}

	@Override
	public Object extractValue(Labelable source) {
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
