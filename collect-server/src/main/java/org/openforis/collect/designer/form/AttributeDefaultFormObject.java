package org.openforis.collect.designer.form;

import org.openforis.idm.metamodel.AttributeDefault;

/**
 * 
 * @author S. Ricci
 *
 */
public class AttributeDefaultFormObject extends FormObject<AttributeDefault> {
	
	private String value;
	private String expression;
	private String condition;

	@Override
	public void loadFrom(AttributeDefault source, String language) {
		value = source.getValue();
		expression = source.getExpression();
		condition = source.getCondition();
	}
	
	@Override
	public void saveTo(AttributeDefault dest, String language) {
		dest.setValue(value);
		dest.setExpression(expression);
		dest.setCondition(condition);
	}
	
	@Override
	protected void reset() {
		value = expression = condition = null;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
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
