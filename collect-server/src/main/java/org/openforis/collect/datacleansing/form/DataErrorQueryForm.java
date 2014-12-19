package org.openforis.collect.datacleansing.form;

import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotBlank;
import org.openforis.collect.datacleansing.DataErrorQuery;
import org.openforis.commons.web.PersistedObjectForm;

/**
 * 
 * @author S. Ricci
 *
 */
public class DataErrorQueryForm extends PersistedObjectForm<DataErrorQuery> {

	@NotNull
	private Integer typeId;
	@NotBlank
	private String title;
	
	private String description;
	@NotNull
	private Integer entityDefinitionId;
	@NotNull
	private Integer attributeDefinitionId;
	@NotBlank
	private String conditions;
	
	//calculated members
	private String typeCode;

	public DataErrorQueryForm() {
		super();
	}
	
	public DataErrorQueryForm(DataErrorQuery query) {
		super(query);
		typeCode = query == null ? null: query.getType() == null ? null: query.getType().getCode();
	}
	
	public String getTypeCode() {
		return typeCode;
	}
	
	public Integer getTypeId() {
		return typeId;
	}
	
	public void setTypeId(Integer typeId) {
		this.typeId = typeId;
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
