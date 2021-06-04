/**
 * 
 */
package org.openforis.idm.metamodel;

import java.util.List;

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
	
	public static final String VALUE_FIELD = "value";
	public static final Type DEFAULT_TYPE = Type.SHORT;
	
	private final FieldDefinitionMap fieldDefinitionByName = new FieldDefinitionMap(
		new FieldDefinition<String>(VALUE_FIELD, "v", null, String.class, this)
	);
	
	public enum Type {
		SHORT, MEMO
	}
	
	private Type type = DEFAULT_TYPE;
	
	protected TextAttributeDefinition(Survey survey, int id) {
		super(survey, id);
	}

	protected TextAttributeDefinition(Survey survey, TextAttributeDefinition source, int id) {
		super(survey, source, id);
		this.type = source.type;
	}

	public Type getType() {
		return this.type;
	}
	
	public void setType(Type type) {
		this.type = type;
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
	
	@SuppressWarnings("unchecked")
	@Override
	public TextValue createValue(Object val) {
		return val == null ? null : createValue(val.toString());
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <V extends Value> V createValueFromKeyFieldValues(List<String> fieldValues) {
		return (V) createValue(fieldValues.get(0));
	}
	
	@Override
	protected FieldDefinitionMap getFieldDefinitionMap() {
		return fieldDefinitionByName;
	}
	
	@Override
	public boolean hasMainField() {
		return true;
	}
	
	@Override
	public String getMainFieldName() {
		return VALUE_FIELD;
	}
	
	@Override
	public boolean isSingleFieldKeyAttribute() {
		return true;
	}
	
	@Override
	public Class<? extends Value> getValueType() {
		return TextValue.class;
	}

	@Override
	public boolean deepEquals(Object obj) {
		if (this == obj)
			return true;
		if (!super.deepEquals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		TextAttributeDefinition other = (TextAttributeDefinition) obj;
		if (type != other.type)
			return false;
		return true;
	}
	
}
