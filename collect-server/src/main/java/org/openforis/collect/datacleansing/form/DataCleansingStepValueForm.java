package org.openforis.collect.datacleansing.form;

import java.util.List;

import org.openforis.collect.datacleansing.DataCleansingStepValue;
import org.openforis.collect.datacleansing.DataCleansingStepValue.UpdateType;

/**
 * 
 * @author S. Ricci
 *
 */
public class DataCleansingStepValueForm {

	private String condition;
	private UpdateType updateType;
	private String fixExpression;
	private List<String> fieldFixExpressions;

	public DataCleansingStepValueForm() {
		super();
	}
	
	public DataCleansingStepValueForm(DataCleansingStepValue stepValue) {
		condition = stepValue.getCondition();
		updateType = stepValue.getUpdateType();
		fixExpression = stepValue.getFixExpression();
		fieldFixExpressions = stepValue.getFieldFixExpressions();
	}
	
	public String getCondition() {
		return condition;
	}

	public void setCondition(String condition) {
		this.condition = condition;
	}

	public String getFixExpression() {
		return fixExpression;
	}
	
	public void setFixExpression(String fixExpression) {
		this.fixExpression = fixExpression;
	}
	
	public List<String> getFieldFixExpressions() {
		return fieldFixExpressions;
	}
	
	public void setFieldFixExpressions(List<String> fieldFixExpressions) {
		this.fieldFixExpressions = fieldFixExpressions;
	}
	
	public UpdateType getUpdateType() {
		return updateType;
	}
	
	public void setUpdateType(UpdateType updateType) {
		this.updateType = updateType;
	}
	
}
