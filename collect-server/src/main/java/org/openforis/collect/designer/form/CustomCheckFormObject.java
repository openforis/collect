package org.openforis.collect.designer.form;

import liquibase.util.StringUtils;

import org.openforis.idm.metamodel.validation.CustomCheck;

/**
 * 
 * @author S. Ricci
 *
 */
public class CustomCheckFormObject extends CheckFormObject<CustomCheck> {
	
	private String expression;
	
	@Override
	public void saveTo(CustomCheck dest, String languageCode) {
		super.saveTo(dest, languageCode);
		dest.setExpression(StringUtils.trimToNull(expression));
	}
	
	@Override
	public void loadFrom(CustomCheck source, String languageCode, String defaultLanguage) {
		super.loadFrom(source, languageCode, defaultLanguage);
		expression = source.getExpression();
	}

	public String getExpression() {
		return expression;
	}

	public void setExpression(String expression) {
		this.expression = expression;
	}
	
	
}
