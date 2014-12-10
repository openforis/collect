/**
 * 
 */
package org.openforis.idm.metamodel;

import org.apache.commons.lang3.StringUtils;
import org.openforis.idm.model.IntegerAttribute;
import org.openforis.idm.model.IntegerValue;
import org.openforis.idm.model.Node;
import org.openforis.idm.model.NumberValue;
import org.openforis.idm.model.RealAttribute;
import org.openforis.idm.model.RealValue;
import org.openforis.idm.model.Value;

/**
 * @author G. Miceli
 * @author M. Togna
 */
public class NumberAttributeDefinition extends NumericAttributeDefinition implements KeyAttributeDefinition {

	private static final long serialVersionUID = 1L;
	
	public static final String VALUE_FIELD = "value";

	private final FieldDefinition<Integer> integerValueField = new FieldDefinition<Integer>(VALUE_FIELD, "v", null, Integer.class, this);
	private final FieldDefinition<Double> realValueField = new FieldDefinition<Double>(VALUE_FIELD, "v", null, Double.class, this);
	private final FieldDefinition<String> unitNameField = new FieldDefinition<String>(UNIT_NAME_FIELD, "u_name", UNIT_NAME_FIELD, String.class, this);
	private final FieldDefinition<Integer> unitIdField = new FieldDefinition<Integer>(UNIT_FIELD, "u", "unit_id", Integer.class, this);
	
	private final FieldDefinitionMap integerFieldDefinitionByName = new FieldDefinitionMap(integerValueField, unitNameField, unitIdField);
	private final FieldDefinitionMap realFieldDefinitionByName = new FieldDefinitionMap(realValueField, unitNameField, unitIdField);
	
	private boolean key;

	NumberAttributeDefinition(Survey survey, int id) {
		super(survey, id);
	}

	@Override
	public boolean isKey() {
		return key;
	}
	
	@Override
	public void setKey(boolean key) {
		this.key = key;
	}

	@Override
	public Node<?> createNode() {
		Type effectiveType = getType();
		switch (effectiveType) {
		case INTEGER:
			return new IntegerAttribute(this);
		case REAL:
			return new RealAttribute(this);
		default:
			throw new UnsupportedOperationException("Unknown type");
		}
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public NumberValue<? extends Number> createValue(String string) {
		if ( StringUtils.isBlank(string) ) {
			return null;
		} 
		Unit unit = getDefaultUnit();
		if(isInteger()){
			return new IntegerValue(Double.valueOf(string).intValue(), unit);
		} else if(isReal()) {
			return new RealValue(Double.valueOf(string), unit);
		}
		throw new RuntimeException("Invalid type " + getType());
	}
	
	@Override
	protected FieldDefinitionMap getFieldDefinitionMap() {
		switch (getType()) {
		case INTEGER:
			return integerFieldDefinitionByName;
		case REAL:
			return realFieldDefinitionByName;
		default:
			throw new UnsupportedOperationException("Unknown type");
		}
	}
	
	@Override
	public Class<? extends Value> getValueType() {
		switch (getType()) {
		case INTEGER:
			return IntegerValue.class;
		case REAL:
			return RealValue.class;
		default:
			throw new UnsupportedOperationException("Unknown type");
		}
	}
	
	@Override
	public String getMainFieldName() {
		return VALUE_FIELD;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + (key ? 1231 : 1237);
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
		NumberAttributeDefinition other = (NumberAttributeDefinition) obj;
		if (key != other.key)
			return false;
		return true;
	}
}
