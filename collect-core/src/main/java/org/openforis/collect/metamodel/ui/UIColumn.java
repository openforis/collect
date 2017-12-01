/**
 * 
 */
package org.openforis.collect.metamodel.ui;

import org.openforis.idm.metamodel.AttributeDefinition;
import org.openforis.idm.metamodel.NodeDefinition;

/**
 * @author S. Ricci
 *
 */
public class UIColumn extends UITableHeadingComponent implements NodeDefinitionUIComponent {

	private static final long serialVersionUID = 1L;
	
	private Integer attributeDefinitionId;
	private AttributeDefinition attributeDefinition;
	
	UIColumn(UITableHeadingContainer parent, int id) {
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
	
	@Override
	public int getColSpan() {
		return 1;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + attributeDefinitionId;
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
		UIColumn other = (UIColumn) obj;
		if (attributeDefinitionId != other.attributeDefinitionId)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Attribute: " + getAttributeDefinition().getPath();
	}

}
