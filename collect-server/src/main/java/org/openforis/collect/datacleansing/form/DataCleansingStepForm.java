package org.openforis.collect.datacleansing.form;

import java.util.ArrayList;
import java.util.List;

import org.openforis.collect.datacleansing.DataCleansingStep;
import org.openforis.collect.datacleansing.DataCleansingStepValue;
import org.openforis.collect.datacleansing.DataQuery;

/**
 * 
 * @author S. Ricci
 *
 */
public class DataCleansingStepForm extends DataCleansingItemForm<DataCleansingStep> {

	private Integer queryId;
	private String title;
	private String fixExpression;
	private List<String> fieldFixExpressions;

	private String description;

	private List<DataCleansingStepValueForm> values;
	
	//calculated members
	private String queryTitle;
	private String queryDescription;
	
	public DataCleansingStepForm() {
		super();
	}
	
	public DataCleansingStepForm(DataCleansingStep step) {
		super(step);
		DataQuery query = step.getQuery();
		if (query != null) {
			queryTitle = step == null ? null: query.getTitle();
			queryDescription = step == null ? null: query.getDescription();
		}
		List<DataCleansingStepValue> values = step.getValues();
		this.values = new ArrayList<DataCleansingStepValueForm>(values.size());
		for (DataCleansingStepValue stepValue : values) {
			DataCleansingStepValueForm valueForm = new DataCleansingStepValueForm(stepValue);
			this.values.add(valueForm);
		}
	}
	
	public String getQueryTitle() {
		return queryTitle;
	}
	
	public String getQueryDescription() {
		return queryDescription;
	}
	
	public Integer getQueryId() {
		return queryId;
	}
	
	public void setQueryId(Integer queryId) {
		this.queryId = queryId;
	}
	
	public String getTitle() {
		return title;
	}
	
	public void setTitle(String title) {
		this.title = title;
	}
	
	public String getDescription() {
		return description;
	}
	
	public void setDescription(String description) {
		this.description = description;
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
	
	public List<DataCleansingStepValueForm> getValues() {
		return values;
	}
	
	public void setValues(List<DataCleansingStepValueForm> values) {
		this.values = values;
	}
}
