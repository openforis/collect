package org.openforis.collect.datacleansing.form;

import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotBlank;
import org.openforis.collect.datacleansing.DataCleansingStep;

/**
 * 
 * @author S. Ricci
 *
 */
public class DataCleansingStepForm extends DataCleansingItemForm<DataCleansingStep> {

	@NotNull
	private Integer queryId;
	@NotBlank
	private String title;
	@NotBlank
	private String fixExpression;

	private String description;

	//calculated members
	private String queryTitle;
	private String queryDescription;

	public DataCleansingStepForm() {
		super();
	}
	
	public DataCleansingStepForm(DataCleansingStep step) {
		super(step);
		queryTitle = step == null ? null: step.getQuery().getTitle();
		queryDescription = step == null ? null: step.getQuery().getDescription();
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
	
}
