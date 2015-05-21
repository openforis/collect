package org.openforis.collect.datacleansing.form;

import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotBlank;
import org.openforis.collect.datacleansing.DataQuery;
import org.openforis.collect.datacleansing.form.validation.ValidExpression;
import org.openforis.collect.datacleansing.form.validation.ValidExpression.ExpressionType;

/**
 * 
 * @author S. Ricci
 *
 */
@ValidExpression(expressionType = ExpressionType.BOOLEAN, contextNodeDefinitionIdFieldName = "entityDefinitionId", 
	experssionFieldName = "conditions", thisNodeDefinitionIdFieldName = "attributeDefinitionId")
public class DataQueryForm extends DataCleansingItemForm<DataQuery> {

	@NotBlank
	private String title;
	@NotNull
	private Integer entityDefinitionId;
	@NotNull
	private Integer attributeDefinitionId;
	@NotBlank
	private String conditions;

	private String description;
	
	public DataQueryForm() {
		super();
	}
	
	public DataQueryForm(DataQuery query) {
		super(query);
	}
	
	public String getTitle() {
		return title;
	}
	
	public void setTitle(String title) {
		this.title = title;
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
	
	public String getDescription() {
		return description;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}
	
}
