package org.openforis.idm.model.expression.internal;

import java.util.List;

import org.openforis.idm.metamodel.NumberAttributeDefinition;
import org.openforis.idm.metamodel.RangeAttributeDefinition;
import org.openforis.idm.metamodel.Unit;
import org.openforis.idm.model.Attribute;
import org.openforis.idm.model.BooleanValue;
import org.openforis.idm.model.Code;
import org.openforis.idm.model.Coordinate;
import org.openforis.idm.model.Date;
import org.openforis.idm.model.IntegerRange;
import org.openforis.idm.model.IntegerRangeAttribute;
import org.openforis.idm.model.IntegerValue;
import org.openforis.idm.model.NumberAttribute;
import org.openforis.idm.model.NumberValue;
import org.openforis.idm.model.NumericRange;
import org.openforis.idm.model.NumericRangeAttribute;
import org.openforis.idm.model.RealRange;
import org.openforis.idm.model.RealValue;
import org.openforis.idm.model.TaxonOccurrence;
import org.openforis.idm.model.TextValue;
import org.openforis.idm.model.Time;
import org.openforis.idm.model.Value;

/**
 * 
 * @author S. Ricci
 *
 */
public class AttributeValueUtils {

	@SuppressWarnings("unchecked")
	public static Object extractMainFieldValue(Attribute<?, ?> attribute, boolean normalizeNumbers) {
		Value value = attribute.getValue();
		if (value instanceof BooleanValue 
				|| value instanceof Code
				|| value instanceof Coordinate
				|| value instanceof Date
				|| value instanceof TaxonOccurrence
				|| value instanceof TextValue
				|| value instanceof Time
			) {
			return extractMainFieldValue(value, normalizeNumbers);
		} else if ( value instanceof IntegerValue || value instanceof RealValue) {
			return extractNumericValue((NumberAttribute<Number, NumberValue<Number>>) attribute, normalizeNumbers);
		} else if ( value instanceof NumericRange ) {
			return extractNumericRangeValue((NumericRangeAttribute<?, ?>) attribute, normalizeNumbers);
		} else {
			throw new UnsupportedOperationException("Unsupported value type of "+attribute.getClass());
		}
	}

	public static Object extractMainFieldValue(Value value, boolean normalizeNumbers) {
		if ( value instanceof BooleanValue ) {		
			return ((BooleanValue) value).getValue(); 
		} else if (value instanceof Code) {
			return ((Code) value).getCode();
		} else if (value instanceof Coordinate) {
			return value == null ? null : value.toString();
		} else if (value instanceof Date) {
			Date date = (Date) value;
			return date.isComplete() ? date.getNumericValue() : null;
		} else if ( value instanceof IntegerValue || value instanceof RealValue) {
			return ((NumberValue<?>) value).getValue();
		} else if ( value instanceof NumericRange ) {
			return ((NumericRange<?>) value);
		} else if (value instanceof TaxonOccurrence) {
			return ((TaxonOccurrence) value).getCode();
		} else if ( value instanceof TextValue ) {
			return ((TextValue) value).getValue(); 
		} else if (value instanceof Time ) {
			return ((Time) value).getNumericValue();
		} else {
			throw new UnsupportedOperationException("Unsupported value of type "+ value.getClass());
		}
	}
	
	private static Object extractNumericValue(NumberAttribute<?, ?> attr, boolean normalizeNumbers) {
		if ( normalizeNumbers ) {
			return extractNormalizedValue(attr);
		} else {
			return attr.getValue().getValue();
		}
	}
	
	private static Object extractNormalizedValue(NumberAttribute<?, ?> attr) {
		NumberAttributeDefinition defn = attr.getDefinition();
		List<Unit> units = defn.getUnits();
		if ( units != null && units.size() > 1 ) {
			Unit unit = attr.getUnit();
			Unit defaultUnit = defn.getDefaultUnit();
			if ( unit != null && defaultUnit != null ) {
				NumberValue<?> numberValue = attr.getValue();
				double normalizedValue = getNormalizedValue(numberValue.getValue(), attr.getUnit(), defaultUnit).doubleValue();
				return normalizedValue;
			}
		}
		NumberValue<?> value = attr.getValue();
		return value.getValue();
	}

	private static Object extractNumericRangeValue(NumericRangeAttribute<?, ?> attr, boolean normalizeNumbers) {
		if ( normalizeNumbers ) {
			return extractNormalizedValue(attr);
		} else {
			return attr.getValue();
		}
	}
	
	private static Object extractNormalizedValue(NumericRangeAttribute<?, ?> attr) {
		NumericRange<?> value = attr.getValue();
		RangeAttributeDefinition defn = attr.getDefinition();
		List<Unit> units = defn.getUnits();
		if ( units != null && units.size() > 1 ) {
			Unit unit = attr.getUnit();
			Unit defaultUnit = defn.getDefaultUnit();
			if ( unit != null && defaultUnit != null && unit != defaultUnit ) {
				Number from = value.getFrom();
				Number to = value.getTo();
				NumericRange<?> normalizedValue;
				int defaultUnitId = defaultUnit.getId();
				if (attr instanceof IntegerRangeAttribute) {
					int normalizedFrom = getNormalizedValue(from, unit, defaultUnit).intValue();
					int normalizedTo = getNormalizedValue(to, unit, defaultUnit).intValue();
					normalizedValue = new IntegerRange(normalizedFrom, normalizedTo, defaultUnitId);
				} else {
					double normalizedFrom = getNormalizedValue(from, unit, defaultUnit).doubleValue();
					double normalizedTo = getNormalizedValue(to, unit, defaultUnit).doubleValue();
					normalizedValue = new RealRange(normalizedFrom, normalizedTo, defaultUnitId);
				}
				return normalizedValue;
			} else {
				return value;
			}
		} else {
			return value;
		}
	}
	
	private static Number getNormalizedValue(Number value, Unit unit, Unit defaultUnit) {
		if ( value != null && unit.getConversionFactor() != null && defaultUnit.getConversionFactor() != null ) {
			double unitConvFact = unit.getConversionFactor();
			double defaultUnitConvFact = defaultUnit.getConversionFactor();
			double doubleValue = value.doubleValue();
			double normalized = doubleValue * unitConvFact;
			double normalizedToDefault = normalized / defaultUnitConvFact;
			return normalizedToDefault;
		} else {
			return value;
		}
	}
	
}
