package org.openforis.collect.datacleansing.form;

import java.util.ArrayList;
import java.util.List;

import org.openforis.collect.datacleansing.DataCleansingStepValue;
import org.openforis.collect.datacleansing.DataCleansingStepValue.UpdateType;
import org.openforis.commons.web.SimpleObjectForm;

/**
 * 
 * @author S. Ricci
 *
 */
public class DataCleansingStepValueForm extends SimpleObjectForm<DataCleansingStepValue> {

	private int queryId;
	private int index;
	
	private UpdateType updateType;
	private String condition;
	private String fixExpression;
	private List<String> fieldFixExpressions = new ArrayList<String>();
	
	public DataCleansingStepValueForm() {
	}
	
	public DataCleansingStepValueForm(DataCleansingStepValue obj) {
		super(obj);
	}

	public int getQueryId() {
		return queryId;
	}
	
	public void setQueryId(int queryId) {
		this.queryId = queryId;
	}
	
	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public UpdateType getUpdateType() {
		return updateType;
	}

	public void setUpdateType(UpdateType updateType) {
		this.updateType = updateType;
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

}
