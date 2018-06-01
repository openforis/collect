/**
 * 
 */
package org.openforis.idm.metamodel;

import java.util.List;

import org.openforis.idm.model.BooleanAttribute;
import org.openforis.idm.model.BooleanValue;
import org.openforis.idm.model.Node;
import org.openforis.idm.model.Value;

/**
 * @author G. Miceli
 * @author M. Togna
 * @author S. Ricci
 */
public class BooleanAttributeDefinition extends AttributeDefinition {

	private static final long serialVersionUID = 1L;

	public static final String VALUE_FIELD = "value";
	
	private final FieldDefinitionMap fieldDefinitionByName = new FieldDefinitionMap(
		new FieldDefinition<Boolean>(VALUE_FIELD, "v", null, Boolean.class, this)
	);
	
	private boolean affirmativeOnly;

	BooleanAttributeDefinition(Survey survey, int id) {
		super(survey, id);
	}

	BooleanAttributeDefinition(Survey survey, BooleanAttributeDefinition source, int id) {
		super(survey, source, id);
		this.affirmativeOnly = source.affirmativeOnly;
	}
	
	public boolean isAffirmativeOnly() {
		return affirmativeOnly;
	}
	
	public void setAffirmativeOnly(boolean affirmativeOnly) {
		this.affirmativeOnly = affirmativeOnly;
	}

	@Override
	public Node<?> createNode() {
		return new BooleanAttribute(this);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public BooleanValue createValue(String string) {
		return new BooleanValue(string);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <V extends Value> V createValueFromKeyFieldValues(List<String> fieldValues) {
		return (V) createValue(fieldValues.get(0));
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public BooleanValue createValue(Object val) {
		if (val == null) {
			return null;
		} else if (val instanceof Boolean) {
			return new BooleanValue((Boolean) val);
		} else {
			return createValue(val.toString());
		}
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
		return BooleanValue.class;
	}

	@Override
	public boolean deepEquals(Object obj) {
		if (this == obj)
			return true;
		if (!super.deepEquals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		BooleanAttributeDefinition other = (BooleanAttributeDefinition) obj;
		if (affirmativeOnly != other.affirmativeOnly)
			return false;
		return true;
	}
}
