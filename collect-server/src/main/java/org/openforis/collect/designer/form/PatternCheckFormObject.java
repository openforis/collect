package org.openforis.collect.designer.form;

import org.openforis.idm.metamodel.validation.PatternCheck;

/**
 * 
 * @author S. Ricci
 *
 */
public class PatternCheckFormObject extends CheckFormObject<PatternCheck> {
	
	private String regularExpression;

	public String getRegularExpression() {
		return regularExpression;
	}

	public void setRegularExpression(String regularExpression) {
		this.regularExpression = regularExpression;
	}

	@Override
	public void saveTo(PatternCheck dest, String languageCode) {
		super.saveTo(dest, languageCode);
		dest.setRegularExpression(regularExpression);
	}
	
	@Override
	public void loadFrom(PatternCheck source, String languageCode, String defaultLanguage) {
		super.loadFrom(source, languageCode, defaultLanguage);
		regularExpression = source.getRegularExpression();
	}
}
