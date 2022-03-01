package org.openforis.collect.designer.form;

import org.apache.commons.lang3.StringUtils;
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
	public void loadFrom(CustomCheck source, String languageCode) {
		super.loadFrom(source, languageCode);
		expression = source.getExpression();
	}

	public String getExpression() {
		return expression;
	}

	public void setExpression(String expression) {
		this.expression = expression;
	}
	
	
}
