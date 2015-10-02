package org.openforis.collect.datacleansing.form;

import org.openforis.collect.datacleansing.DataQuery;

/**
 * 
 * @author S. Ricci
 *
 */
public class DataQueryForm extends DataCleansingItemForm<DataQuery> {

	private String title;
	private Integer entityDefinitionId;
	private Integer attributeDefinitionId;
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
