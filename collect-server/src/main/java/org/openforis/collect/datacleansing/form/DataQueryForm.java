package org.openforis.collect.datacleansing.form;

import org.openforis.collect.datacleansing.DataQuery;
import org.openforis.collect.datacleansing.DataQuery.ErrorSeverity;

import jakarta.validation.constraints.NotNull;

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
	@NotNull
	private Integer typeId;
	@NotNull
	private ErrorSeverity errorSeverity;

	//calculated members
	private transient String typeCode;
	private transient String prettyFormatTitle;
	
	public DataQueryForm() {
		super();
	}
	
	public DataQueryForm(DataQuery query) {
		super(query);
		this.typeCode = query == null ? null: query.getType() == null ? null: query.getType().getCode();
		this.prettyFormatTitle = String.format("Type: %s - Title: %s", typeCode, query.getTitle());
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
	
	public Integer getTypeId() {
		return typeId;
	}
	
	public void setTypeId(Integer typeId) {
		this.typeId = typeId;
	}

	public String getPrettyFormatTitle() {
		return prettyFormatTitle;
	}
	
	public String getTypeCode() {
		return typeCode;
	}
	
	public ErrorSeverity getErrorSeverity() {
		return errorSeverity;
	}
	
	public void setErrorSeverity(ErrorSeverity errorSeverity) {
		this.errorSeverity = errorSeverity;
	}
	
}
