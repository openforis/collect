/**
 * 
 */
package org.openforis.idm.metamodel;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.openforis.commons.lang.Numbers;
import org.openforis.idm.model.IntegerRange;
import org.openforis.idm.model.IntegerRangeAttribute;
import org.openforis.idm.model.Node;
import org.openforis.idm.model.NumericRange;
import org.openforis.idm.model.RealRange;
import org.openforis.idm.model.RealRangeAttribute;
import org.openforis.idm.model.Value;

/**
 * @author G. Miceli
 * @author M. Togna
 */
public class RangeAttributeDefinition extends NumericAttributeDefinition {

	private static final long serialVersionUID = 1L;
	
	public static final String FROM_FIELD = "from";
	public static final String TO_FIELD = "to";

	private final FieldDefinition<Integer> integerFromField = new FieldDefinition<Integer>(FROM_FIELD, "f", FROM_FIELD, Integer.class, this);
	private final FieldDefinition<Integer> integerToField = new FieldDefinition<Integer>(TO_FIELD, "t", TO_FIELD, Integer.class, this);
	private final FieldDefinition<Double> realFromField = new FieldDefinition<Double>(FROM_FIELD, "f", FROM_FIELD, Double.class, this);
	private final FieldDefinition<Double> realToField = new FieldDefinition<Double>(TO_FIELD, "t", TO_FIELD, Double.class, this);
	private final FieldDefinition<String> unitNameField = new FieldDefinition<String>(UNIT_NAME_FIELD, "u_name", "unit", String.class, this);;
	private final FieldDefinition<Integer> unitIdField = new FieldDefinition<Integer>(UNIT_FIELD, "u", "unit_id", Integer.class, this);;
	
	private final FieldDefinitionMap integerFieldDefinitionByName = new FieldDefinitionMap(integerFromField, integerToField, unitNameField, unitIdField);
	private final FieldDefinitionMap realFieldDefinitionByName = new FieldDefinitionMap(realFromField, realToField, unitNameField, unitIdField);
	
	RangeAttributeDefinition(Survey survey, int id) {
		super(survey, id);
	}

	RangeAttributeDefinition(Survey survey, RangeAttributeDefinition source, int id) {
		super(survey, source, id);
	}
	
	@Override
	public Node<?> createNode() {
		Type effectiveType = getType();
		switch (effectiveType) {
		case INTEGER:
			return new IntegerRangeAttribute(this);
		case REAL:
			return new RealRangeAttribute(this);
		default:
			throw new UnsupportedOperationException("Unknown type");
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public NumericRange<? extends Number> createValue(String string) {
		if ( StringUtils.isBlank(string) ) {
			return null;
		} 
		Unit unit = getDefaultUnit();
		if (isInteger()) {
			return IntegerRange.parseIntegerRange(string, unit);
		} else if (isReal()) {
			return RealRange.parseRealRange(string, unit);
		}
		throw new RuntimeException("Invalid range type " + getType());
	}
	
	public NumericRange<? extends Number> createValue(String from, String to) {
		if ( StringUtils.isBlank(from) && StringUtils.isBlank(to) ) {
			return null;
		} 
		Unit unit = getDefaultUnit();
		Integer unitId = unit == null ? null : unit.getId();
		if (isInteger()) {
			return new IntegerRange(Numbers.toIntegerObject(from), Numbers.toIntegerObject(to), unitId);
		} else if (isReal()) {
			return new RealRange(Numbers.toDoubleObject(from), Numbers.toDoubleObject(to), unitId);
		}
		throw new RuntimeException("Invalid range type " + getType());
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public NumericRange<? extends Number> createValue(Object val) {
		if (val == null) {
			return null;
		} else {
			return createValue(val.toString());
		}
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <V extends Value> V createValueFromKeyFieldValues(List<String> fieldValues) {
		return (V) createValue(fieldValues.get(0), fieldValues.get(1));
	}
	
	@Override
	public FieldDefinition<?> getFieldDefinition(String name) {
		for (FieldDefinition<?> def : getFieldDefinitions()) {
			if(def.getName().equals(name)) {
				return def;
			}
		}
		return null;
	}
	
	@Override
	public Class<? extends Value> getValueType() {
		Type type = getType();
		switch (type) {
		case INTEGER:
			return IntegerRange.class;
		case REAL:
			return RealRange.class;
		default:
			throw new UnsupportedOperationException("Unknown type");
		}
	}
	
	@Override
	public boolean hasMainField() {
		return false;
	}
	
	@Override
	public String getMainFieldName() {
		throw new IllegalArgumentException("Main field not defined");
	}
	
	@Override
	protected FieldDefinitionMap getFieldDefinitionMap() {
		switch (getType()) {
		case INTEGER:
			return integerFieldDefinitionByName;
		case REAL:
			return realFieldDefinitionByName;
		default:
			throw new UnsupportedOperationException("Unknown type: " + getType().name());
		}
	}
	
	public FieldDefinition<?> getFromFieldDefinition() {
		switch (getType()) {
		case INTEGER:
			return integerFromField;
		case REAL:
			return realFromField;
		default:
			throw new UnsupportedOperationException("Unknown type: " + getType().name());
		}
	}
	
	public FieldDefinition<?> getToFieldDefinition() {
		switch (getType()) {
		case INTEGER:
			return integerToField;
		case REAL:
			return realToField;
		default:
			throw new UnsupportedOperationException("Unknown type: " + getType().name());
		}
	}
	
}
