/**
 * 
 */
package org.openforis.idm.model.expression.internal;

import java.util.List;
import java.util.Locale;

import org.apache.commons.jxpath.DynamicPropertyHandler;
import org.apache.commons.jxpath.ri.QName;
import org.apache.commons.jxpath.ri.model.NodePointer;
import org.apache.commons.jxpath.ri.model.dynamic.DynamicPointer;
import org.openforis.idm.metamodel.NumberAttributeDefinition;
import org.openforis.idm.metamodel.RangeAttributeDefinition;
import org.openforis.idm.metamodel.Unit;
import org.openforis.idm.model.Attribute;
import org.openforis.idm.model.BooleanValue;
import org.openforis.idm.model.Code;
import org.openforis.idm.model.Date;
import org.openforis.idm.model.IntegerAttribute;
import org.openforis.idm.model.IntegerRange;
import org.openforis.idm.model.IntegerRangeAttribute;
import org.openforis.idm.model.IntegerValue;
import org.openforis.idm.model.NumberAttribute;
import org.openforis.idm.model.NumberValue;
import org.openforis.idm.model.NumericRange;
import org.openforis.idm.model.NumericRangeAttribute;
import org.openforis.idm.model.RealAttribute;
import org.openforis.idm.model.RealRange;
import org.openforis.idm.model.RealRangeAttribute;
import org.openforis.idm.model.RealValue;
import org.openforis.idm.model.TaxonOccurrence;
import org.openforis.idm.model.TextValue;
import org.openforis.idm.model.Time;

/**
 * @author M. Togna
 * @author S. Ricci
 * 
 */
public class ModelNodePointer extends DynamicPointer {

	private static final long serialVersionUID = 1L;
	
	private boolean normalizeNumbers;
	
	protected ModelNodePointer(NodePointer parent, QName name, Object bean, DynamicPropertyHandler handler) {
		super(parent, name, bean, handler);
		normalizeNumbers = false;
	}

	protected ModelNodePointer(QName name, Object bean, DynamicPropertyHandler handler, Locale locale) {
		super(name, bean, handler, locale);
		normalizeNumbers = false;
	}

	@Override
	public Object getValue() {
		Object node = super.getValue();
		if (node instanceof Attribute) {
			return getValue((Attribute<?, ?>) node);
		} else {
			return node;
		}
	}

	@SuppressWarnings("unchecked")
	private Object getValue(Attribute<?, ?> attribute) {
		Object value = attribute.getValue();
		if ( value instanceof TextValue ) {
			return ((TextValue) value).getValue(); 
		} else if ( value instanceof IntegerValue || value instanceof RealValue) {
			return getNumericValue((NumberAttribute<Number, NumberValue<Number>>) attribute);
		} else if ( value instanceof NumericRange ) {
			return getNumericRangeValue((NumericRangeAttribute<?, ?>) attribute);
		} else if ( value instanceof BooleanValue ) {		
			return ((BooleanValue) value).getValue(); 
		} else if (value instanceof Time ) {
			if ( attribute.isFilled() ) {
				Time time = (Time) value;
				return time.getHour() * 100 + time.getMinute();
			} else {
				return null;
			}
		} else if (value instanceof Date) {
			if ( attribute.isFilled() ) {
				Date date = (Date) value;
				return (date.getYear() * 10000) + (date.getMonth() * 100) + date.getDay();
			} else {
				return null;
			}
		} else if (value instanceof Code) {
			Code code = (Code) value;
			return code.getCode();
		} else if (value instanceof TaxonOccurrence) {
			TaxonOccurrence taxonOcc = (TaxonOccurrence) value;
			return taxonOcc.getCode();
		} else {
			throw new UnsupportedOperationException("Unsupported value type of "+attribute.getClass());
		}
	}
	
	private Number getNumericValue(NumberAttribute<?, ?> attr) {
		if ( normalizeNumbers ) {
			if ( attr instanceof IntegerAttribute ) {
				return getNormalizedValue((IntegerAttribute) attr);
			} else {
				return getNormalizedValue((RealAttribute) attr);
			}
		} else {
			return attr.getValue().getValue();
		}
	}
	
	private Number getNormalizedValue(IntegerAttribute attr) {
		NumberAttributeDefinition defn = attr.getDefinition();
		List<Unit> units = defn.getUnits();
		IntegerValue value = attr.getValue();
		if ( units != null && units.size() > 1 ) {
			Unit unit = attr.getUnit();
			Unit defaultUnit = defn.getDefaultUnit();
			if ( unit != null && defaultUnit != null ) {
				double normalizedValue = getNormalizedValue(value, defaultUnit).doubleValue();
				return normalizedValue;
			}
		}
		return value.getValue();
	}

	private Number getNormalizedValue(RealAttribute attr) {
		NumberAttributeDefinition defn = attr.getDefinition();
		List<Unit> units = defn.getUnits();
		RealValue value = attr.getValue();
		if ( units != null && units.size() > 1 ) {
			Unit unit = attr.getUnit();
			Unit defaultUnit = defn.getDefaultUnit();
			if ( unit != null && defaultUnit != null ) {
				double normalizedValue = getNormalizedValue(value, defaultUnit).doubleValue();
				return normalizedValue;
			}
		}
		return value.getValue();
	}
	
	private NumericRange<?> getNumericRangeValue(NumericRangeAttribute<?, ?> attr) {
		if ( normalizeNumbers ) {
			if ( attr instanceof RealRangeAttribute ) {
				return getNormalizedValue((RealRangeAttribute) attr);
			} else {
				return getNormalizedValue((IntegerRangeAttribute) attr);
			}
		} else {
			return attr.getValue();
		}
	}
	

	private NumericRange<?> getNormalizedValue(IntegerRangeAttribute attr) {
		IntegerRange value = attr.getValue();
		RangeAttributeDefinition defn = attr.getDefinition();
		List<Unit> units = defn.getUnits();
		if ( units != null && units.size() > 1 ) {
			Unit unit = attr.getUnit();
			Unit defaultUnit = defn.getDefaultUnit();
			if ( unit != null && defaultUnit != null && unit != defaultUnit ) {
				Integer from = value.getFrom();
				Integer to = value.getTo();
				double normalizedFrom = getNormalizedValue(from, unit, defaultUnit).doubleValue();
				double normalizedTo = getNormalizedValue(to, unit, defaultUnit).doubleValue();
				RealRange normalizedValue = new RealRange(normalizedFrom, normalizedTo, defaultUnit);
				return normalizedValue;
			} else {
				return value;
			}
		} else {
			return value;
		}
	}
	
	private NumericRange<?> getNormalizedValue(RealRangeAttribute attr) {
		RealRange value = attr.getValue();
		RangeAttributeDefinition defn = attr.getDefinition();
		List<Unit> units = defn.getUnits();
		if ( units != null && units.size() > 1 ) {
			Unit unit = attr.getUnit();
			Unit defaultUnit = defn.getDefaultUnit();
			if ( unit != null && defaultUnit != null && unit != defaultUnit ) {
				Double from = value.getFrom();
				Double to = value.getTo();
				double normalizedFrom = getNormalizedValue(from, unit, defaultUnit).doubleValue();
				double normalizedTo = getNormalizedValue(to, unit, defaultUnit).doubleValue();
				RealRange normalizedValue = new RealRange(normalizedFrom, normalizedTo, defaultUnit);
				return normalizedValue;
			} else {
				return value;
			}
		} else {
			return value;
		}
	}
	
	private Number getNormalizedValue(Number value, Unit unit, Unit defaultUnit) {
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
	
	private Number getNormalizedValue(IntegerValue value, Unit defaultUnit) {
		return getNormalizedValue(value.getValue(), value.getUnit(), defaultUnit);
	}

	private Number getNormalizedValue(RealValue value, Unit defaultUnit) {
		return getNormalizedValue(value.getValue(), value.getUnit(), defaultUnit);
	}

	void setNormalizeNumbers(boolean normalizeNumbers) {
		this.normalizeNumbers = normalizeNumbers;
	}
	
	boolean isNormalizeNumbers() {
		return normalizeNumbers;
	}
}
