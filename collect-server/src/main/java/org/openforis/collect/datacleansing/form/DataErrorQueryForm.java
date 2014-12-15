package org.openforis.collect.datacleansing.form;

import org.openforis.collect.datacleansing.DataErrorQuery;
import org.openforis.commons.web.PersistedObjectForm;

/**
 * 
 * @author S. Ricci
 *
 */
public class DataErrorQueryForm extends PersistedObjectForm<DataErrorQuery> {

	private int typeId;
	private String title;
	private String description;
	private int entityDefinitionId;
	private int attributeDefinitionId;
	private String conditions;

	public DataErrorQueryForm() {
	}
	
	public DataErrorQueryForm(DataErrorQuery query) {
		super(query);
	}
	
	public int getTypeId() {
		return typeId;
	}
	
	public void setTypeId(int typeId) {
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

	public int getEntityDefinitionId() {
		return entityDefinitionId;
	}

	public void setEntityDefinitionId(int entityDefinitionId) {
		this.entityDefinitionId = entityDefinitionId;
	}

	public int getAttributeDefinitionId() {
		return attributeDefinitionId;
	}

	public void setAttributeDefinitionId(int attributeDefinitionId) {
		this.attributeDefinitionId = attributeDefinitionId;
	}

	public String getConditions() {
		return conditions;
	}

	public void setConditions(String conditions) {
		this.conditions = conditions;
	}

}
