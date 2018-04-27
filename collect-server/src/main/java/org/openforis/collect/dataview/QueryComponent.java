package org.openforis.collect.dataview;

public abstract class QueryComponent {
	
	private int attributeDefinitionId;
	private QueryCondition filterCondition;
	
	public int getAttributeDefinitionId() {
		return attributeDefinitionId;
	}

	public void setAttributeDefinitionId(int attributeDefinitionId) {
		this.attributeDefinitionId = attributeDefinitionId;
	}

	public QueryCondition getFilterCondition() {
		return filterCondition;
	}

	public void setFilterCondition(QueryCondition filterCondition) {
		this.filterCondition = filterCondition;
	}
}