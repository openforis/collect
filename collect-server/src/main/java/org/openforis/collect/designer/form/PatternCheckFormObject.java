package org.openforis.collect.designer.form;

import liquibase.util.StringUtils;

import org.openforis.idm.metamodel.validation.PatternCheck;

/**
 * 
 * @author S. Ricci
 *
 */
public class PatternCheckFormObject extends CheckFormObject<PatternCheck> {
	
	private String regularExpression;

	@Override
	public void saveTo(PatternCheck dest, String languageCode) {
		super.saveTo(dest, languageCode);
		dest.setRegularExpression(StringUtils.trimToNull(regularExpression));
	}
	
	@Override
	public void loadFrom(PatternCheck source, String languageCode, String defaultLanguage) {
		super.loadFrom(source, languageCode, defaultLanguage);
		regularExpression = source.getRegularExpression();
	}
	
	public String getRegularExpression() {
		return regularExpression;
	}

	public void setRegularExpression(String regularExpression) {
		this.regularExpression = regularExpression;
	}

}
