/**
 * 
 */
package org.openforis.idm.metamodel;

import org.openforis.idm.model.Field;
import org.openforis.idm.model.Node;

/**
 * @author S. Ricci
 * @author G. Miceli
 */
public final class FieldDefinition<T> extends NodeDefinition {

	private static final long serialVersionUID = 1L;
	
	private String alias;
	private String suffix;
	private Class<T> valueType;
	
	FieldDefinition(String name, String alias, String suffix, Class<T> valueType, AttributeDefinition parentDefinition) {
		super(parentDefinition.getSurvey(), 0);
		setName(name);
		setParentDefinition(parentDefinition);
		this.alias = alias;
		this.suffix = suffix;
		this.valueType = valueType;
	}

	public String getAlias() {
		return alias;
	}

	public Class<?> getValueType() {
		return valueType;
	}
	
	public String getSuffix() {
		return suffix;
	}
	
	public int getIndex() {
		return ((AttributeDefinition) getParentDefinition()).getFieldDefinitions().indexOf(this);
	}
	
	@Override
	public Node<?> createNode() {
		return new Field<T>(this, valueType); 
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((alias == null) ? 0 : alias.hashCode());
		result = prime * result + ((getName() == null) ? 0 : getName().hashCode());
		result = prime * result + ((suffix == null) ? 0 : suffix.hashCode());
		result = prime * result
				+ ((valueType == null) ? 0 : valueType.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		FieldDefinition<?> other = (FieldDefinition<?>) obj;
		if (alias == null) {
			if (other.alias != null)
				return false;
		} else if (!alias.equals(other.alias))
			return false;
		if (getName() == null) {
			if (other.getName() != null)
				return false;
		} else if (!getName().equals(other.getName()))
			return false;
		if (suffix == null) {
			if (other.suffix != null)
				return false;
		} else if (!suffix.equals(other.suffix))
			return false;
		if (valueType == null) {
			if (other.valueType != null)
				return false;
		} else if (!valueType.equals(other.valueType))
			return false;
		return true;
	}
	

	@Override
	public String getMaxCountExpression() {
		return "1";
	}
	
	public AttributeDefinition getAttributeDefinition() {
		return (AttributeDefinition) getParentDefinition();
	}
}
