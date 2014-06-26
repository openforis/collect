package org.openforis.collect.designer.form;

import org.openforis.idm.metamodel.CalculatedAttributeDefinition.Formula;

/**
 * 
 * @author S. Ricci
 *
 */
public class CalculatedAttributeFormulaFormObject extends FormObject<Formula> {
	
	private String expression;
	private String condition;

	@Override
	public void loadFrom(Formula source, String language) {
		expression = source.getExpression();
		condition = source.getCondition();
	}
	
	@Override
	public void saveTo(Formula dest, String language) {
		dest.setExpression(expression);
		dest.setCondition(condition);
	}
	
	@Override
	protected void reset() {
		expression = null;
		condition = null;
	}

	public String getExpression() {
		return expression;
	}

	public void setExpression(String expression) {
		this.expression = expression;
	}

	public String getCondition() {
		return condition;
	}

	public void setCondition(String condition) {
		this.condition = condition;
	}

}
