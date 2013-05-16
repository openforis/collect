package org.openforis.collect.designer.form;

import liquibase.util.StringUtils;

import org.openforis.idm.metamodel.validation.UniquenessCheck;

/**
 * 
 * @author S. Ricci
 *
 */
public class UniquenessCheckFormObject extends CheckFormObject<UniquenessCheck> {
	
	private String expression;
	
	@Override
	public void saveTo(UniquenessCheck dest, String languageCode) {
		super.saveTo(dest, languageCode);
		dest.setExpression(StringUtils.trimToNull(expression));
	}
	
	@Override
	public void loadFrom(UniquenessCheck source, String languageCode, String defaultLanguage) {
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
