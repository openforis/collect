package org.openforis.idm.metamodel;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.openforis.commons.collection.CollectionUtils;
import org.openforis.commons.lang.Objects;

/**
 * @author G. Miceli
 */
public abstract class NumericAttributeDefinition extends AttributeDefinition {

	private static final long serialVersionUID = 1L;

	public static final String UNIT_FIELD = "unit";
	public static final String UNIT_NAME_FIELD = "unit_name";
	
	public enum Type {
		INTEGER(Integer.class), REAL(Double.class);

		private Class<? extends Number> numberType;
		
		private Type(Class<? extends Number> numberType) {
			this.numberType = numberType;
		}
		public Class<? extends Number> getNumberType() {
			return numberType;
		}
	}

	private Type type; 
	private List<Precision> precisionDefinitions;

	protected NumericAttributeDefinition(Survey survey, int id) {
		super(survey, id);
	}

	protected NumericAttributeDefinition(Survey survey, NumericAttributeDefinition source, int id) {
		super(survey, source, id);
		this.type = source.type;
		this.precisionDefinitions = Objects.clone(source.precisionDefinitions);
	}

	public Type getType() {
		return type == null ? Type.REAL : type;
	}

	public void setType(Type type) {
		this.type = type;
	}
	
	public boolean isInteger() {
		return getType() == Type.INTEGER;
	}

	public boolean isReal() {
		return getType() == Type.REAL;
	}

	public List<Precision> getPrecisionDefinitions() {
		return CollectionUtils.unmodifiableList(precisionDefinitions);
	}

	public void addPrecisionDefinition(Precision precision) {
		if ( precisionDefinitions == null ) {
			precisionDefinitions = new ArrayList<Precision>();
		}
		precisionDefinitions.add(precision);
	}
	
	public void removePrecisionDefinition(Precision precision) {
		precisionDefinitions.remove(precision);
	}
	
	public void removePrecisionDefinitions(Unit unit) {
		if ( precisionDefinitions != null ) {
			int unitId = unit.getId();
			Iterator<Precision> it = precisionDefinitions.iterator();
			while (it.hasNext()) {
				Precision precision = (Precision) it.next();
				Unit un = precision.getUnit();
				if ( un != null && un.getId() == unitId ) {
					it.remove();
				}
			}
		}
	}
	
	public void movePrecisionDefinition(Precision precision, int toIndex) {
		CollectionUtils.shiftItem(precisionDefinitions, precision, toIndex);
	}
	
	/**
	 * @return true if the unit may be user-defined, false if the value is always measured with the same (or no) unit  
	 */
	public boolean isVariableUnit() {
		List<Unit> units = getUnits();
		
		return units.size() > 1;
	}

	/**
	 * Returns the precision with default="true".  If no default precision is
	 * specified returns the first one.  Null if none are specified.
	 * @return
	 */
	public Precision getDefaultPrecision() {
		if ( precisionDefinitions == null || precisionDefinitions.isEmpty() ) {
			return null;
		}
		for (Precision pd : precisionDefinitions) {
			if ( pd.isDefaultPrecision() ) {
				return pd;
			}
		} 
		return precisionDefinitions.get(0);
	}

	/**
	 * @return the unit of the default precision.  If no default precision is specified,
	 * returns the first unit defined
	 */
	public Unit getDefaultUnit() {
		Unit unit = null;
		Precision defaultPrecision = getDefaultPrecision();
		if ( defaultPrecision != null) {
			unit = defaultPrecision.getUnit();
		}
		if ( unit == null && precisionDefinitions != null ) {
			for (Precision pd : precisionDefinitions) {
				unit = pd.getUnit();
				if ( unit != null ) {
					break;
				}
			}
		}
		return unit;
	}
	
	public Unit getUnitOrDefault(Integer unitId) {
		Unit unit = unitId == null ? null: getSurvey().getUnit(unitId.intValue());
		if (unit == null) {
			unit = getDefaultUnit();
		}
		return unit;
	}
	
	public List<Unit> getUnits() {
		List<Unit> units = new ArrayList<Unit>();
		if ( precisionDefinitions != null ) {
			for (Precision precision : precisionDefinitions) {
				Unit unit = precision.getUnit();
				if ( unit != null ) {
					units.add(unit);
				}
			}
		}
		return CollectionUtils.unmodifiableList(units);
	}
	
	@Override
	public boolean deepEquals(Object obj) {
		if (this == obj)
			return true;
		if (!super.deepEquals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		NumericAttributeDefinition other = (NumericAttributeDefinition) obj;
		if (precisionDefinitions == null) {
			if (other.precisionDefinitions != null)
				return false;
		} else if (!precisionDefinitions.equals(other.precisionDefinitions))
			return false;
		if (type != other.type)
			return false;
		return true;
	}

}