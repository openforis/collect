/**
 * 
 */
package org.openforis.idm.metamodel;

import org.openforis.idm.model.Node;
import org.openforis.idm.model.TextAttribute;
import org.openforis.idm.model.TextValue;
import org.openforis.idm.model.Value;

/**
 * @author G. Miceli
 * @author M. Togna
 */
public class TextAttributeDefinition extends AttributeDefinition implements KeyAttributeDefinition {

	private static final long serialVersionUID = 1L;
	
	private static final String VALUE_FIELD = "value";
	
	private final FieldDefinitionMap fieldDefinitionByName = new FieldDefinitionMap(
		new FieldDefinition<String>(VALUE_FIELD, "v", null, String.class, this)
	);
	
	public enum Type {
		SHORT, MEMO
	}
	
	private Type type;
	private Boolean key;
	
	protected TextAttributeDefinition(Survey survey, int id) {
		super(survey, id);
	}

	public Type getType() {
		return this.type;
	}
	
	public void setType(Type type) {
		this.type = type;
	}
	
	@Override
	public boolean isKey() {
		return this.key == null ? false : key;
	}
	
	@Override
	public void setKey(boolean key) {
		this.key = key;
	}

	@Override
	public Node<?> createNode() {
		return new TextAttribute(this);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public TextValue createValue(String string) {
		return new TextValue(string);
	}
	
	@Override
	protected FieldDefinitionMap getFieldDefinitionMap() {
		return fieldDefinitionByName;
	}
	
	@Override
	public String getMainFieldName() {
		return VALUE_FIELD;
	}
	
	@Override
	public Class<? extends Value> getValueType() {
		return TextValue.class;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((key == null) ? 0 : key.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
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
		TextAttributeDefinition other = (TextAttributeDefinition) obj;
		if (key == null) {
			if (other.key != null)
				return false;
		} else if (!key.equals(other.key))
			return false;
		if (type != other.type)
			return false;
		return true;
	}
	
}
