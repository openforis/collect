package org.openforis.collect.datacleansing.form;

import org.openforis.collect.datacleansing.DataQuery;
import org.openforis.collect.model.CollectRecord.Step;
import org.openforis.commons.web.SimpleObjectForm;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 
 * @author S. Ricci
 *
 */
public class DataUpdateForm extends SimpleObjectForm<DataQuery> {

	@NotNull
	private Integer entityDefinitionId;
	@NotNull
	private Integer attributeDefinitionId;
	@NotBlank
	private String conditions;
	@NotBlank
	private String updateExpression;
	
	private Step recordStep;
	
	public DataUpdateForm() {
		super();
	}
	
	public DataUpdateForm(DataQuery query) {
		super(query);
	}
	
	public Integer getEntityDefinitionId() {
		return entityDefinitionId;
	}

	public void setEntityDefinitionId(Integer entityDefinitionId) {
		this.entityDefinitionId = entityDefinitionId;
	}

	public Integer getAttributeDefinitionId() {
		return attributeDefinitionId;
	}

	public void setAttributeDefinitionId(Integer attributeDefinitionId) {
		this.attributeDefinitionId = attributeDefinitionId;
	}

	public String getConditions() {
		return conditions;
	}

	public void setConditions(String conditions) {
		this.conditions = conditions;
	}
	
	public String getUpdateExpression() {
		return updateExpression;
	}
	
	public void setUpdateExpression(String updateExpression) {
		this.updateExpression = updateExpression;
	}

	public Step getRecordStep() {
		return recordStep;
	}

	public void setRecordStep(Step recordStep) {
		this.recordStep = recordStep;
	}

}
