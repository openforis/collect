/**
 * 
 */
package org.openforis.collect.metamodel.ui;

import java.util.List;

import org.openforis.idm.metamodel.AttributeDefinition;
import org.openforis.idm.metamodel.NodeDefinition;

/**
 * @author S. Ricci
 *
 */
public class UIField extends UIModelObject implements UIFormComponent, NodeDefinitionUIComponent {

	private static final long serialVersionUID = 1L;

	private AttributeDefinition attributeDefinition;
	private Integer attributeDefinitionId;
	private String autoCompleteGroup;
	private Enum<?> fieldsOrder;
	private List<String> visibleFields;
	private boolean hidden;
	private int column;
	private int columnSpan;
	private int row;
	
	<P extends UIFormContentContainer> UIField(P parent, int id) {
		super(parent, id);
	}
	
	@Override
	public int getNodeDefinitionId() {
		return getAttributeDefinitionId();
	}
	
	@Override
	public NodeDefinition getNodeDefinition() {
		return getAttributeDefinition();
	}

	public Integer getAttributeDefinitionId() {
		return attributeDefinitionId;
	}

	public void setAttributeDefinitionId(Integer attributeDefinitionId) {
		this.attributeDefinitionId = attributeDefinitionId;
	}

	public AttributeDefinition getAttributeDefinition() {
		if ( attributeDefinitionId != null && attributeDefinition == null ) {
			this.attributeDefinition = (AttributeDefinition) getNodeDefinition(attributeDefinitionId);
		}
		return attributeDefinition;
	}
	
	public void setAttributeDefinition(AttributeDefinition attributeDefinition) {
		this.attributeDefinition = attributeDefinition;
		this.attributeDefinitionId = attributeDefinition == null ? null: attributeDefinition.getId();
	}
	
	public String getAutoCompleteGroup() {
		return autoCompleteGroup;
	}
	
	public void setAutoCompleteGroup(String autoCompleteGroup) {
		this.autoCompleteGroup = autoCompleteGroup;
	}
	
	public List<String> getVisibleFields() {
		return visibleFields;
	}
	
	public void setVisibleFields(List<String> visibleFields) {
		this.visibleFields = visibleFields;
	}
	
	public boolean isVisibleField(String name) {
		return visibleFields == null || visibleFields.size() == 0 || visibleFields.contains(name);
	}
	
	@Override
	public boolean isHidden() {
		return this.hidden;
	}
	
	@Override
	public void setHidden(boolean hidden) {
		this.hidden = hidden;
	}
	
	public Enum<?> getFieldsOrder() {
		return fieldsOrder;
	}

	public void setFieldsOrder(Enum<?> fieldsOrder) {
		this.fieldsOrder = fieldsOrder;
	}
	
	@Override
	public int getColumn() {
		return column;
	}
	
	@Override
	public void setColumn(int column) {
		this.column = column;
	}

	@Override
	public int getColumnSpan() {
		return columnSpan;
	}
	
	@Override
	public void setColumnSpan(int columnSpan) {
		this.columnSpan = columnSpan;
	}

	@Override
	public int getRow() {
		return row;
	}
	
	@Override
	public void setRow(int row) {
		this.row = row;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + attributeDefinitionId;
		result = prime
				* result
				+ ((autoCompleteGroup == null) ? 0 : autoCompleteGroup
						.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		UIField other = (UIField) obj;
		if (attributeDefinitionId != other.attributeDefinitionId)
			return false;
		return true;
	}
	
	@Override
	public String toString() {
		return "Attribute: " + getAttributeDefinition().getPath();
	}

}
