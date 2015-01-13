package org.openforis.collect.datacleansing.form;

import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotBlank;
import org.openforis.collect.datacleansing.DataQuery;
import org.openforis.commons.web.SimpleObjectForm;

/**
 * 
 * @author S. Ricci
 *
 */
public class DataQueryForm extends SimpleObjectForm<DataQuery> {

	@NotNull
	private Integer entityDefinitionId;
	@NotNull
	private Integer attributeDefinitionId;
	@NotBlank
	private String conditions;
	
	public DataQueryForm() {
		super();
	}
	
	public DataQueryForm(DataQuery query) {
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

}
