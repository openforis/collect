package org.openforis.collect.designer.form;

import org.apache.commons.lang3.StringUtils;
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
	public void loadFrom(UniquenessCheck source, String languageCode) {
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
