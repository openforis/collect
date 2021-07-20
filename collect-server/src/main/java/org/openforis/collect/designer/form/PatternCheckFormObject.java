package org.openforis.collect.designer.form;

import org.apache.commons.lang3.StringUtils;
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
	public void loadFrom(PatternCheck source, String languageCode) {
		super.loadFrom(source, languageCode);
		regularExpression = source.getRegularExpression();
	}
	
	public String getRegularExpression() {
		return regularExpression;
	}

	public void setRegularExpression(String regularExpression) {
		this.regularExpression = regularExpression;
	}

}
