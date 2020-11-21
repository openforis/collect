package org.openforis.collect.datacleansing.json;

import static org.openforis.idm.metamodel.CoordinateAttributeDefinition.SRS_FIELD_NAME;
import static org.openforis.idm.metamodel.CoordinateAttributeDefinition.X_FIELD_NAME;
import static org.openforis.idm.metamodel.CoordinateAttributeDefinition.Y_FIELD_NAME;
import static org.openforis.idm.metamodel.TaxonAttributeDefinition.CODE_FIELD_NAME;
import static org.openforis.idm.metamodel.TaxonAttributeDefinition.LANGUAGE_CODE_FIELD_NAME;
import static org.openforis.idm.metamodel.TaxonAttributeDefinition.LANGUAGE_VARIETY_FIELD_NAME;
import static org.openforis.idm.metamodel.TaxonAttributeDefinition.SCIENTIFIC_NAME_FIELD_NAME;
import static org.openforis.idm.metamodel.TaxonAttributeDefinition.VERNACULAR_NAME_FIELD_NAME;
import static org.openforis.idm.metamodel.TimeAttributeDefinition.HOUR_FIELD;
import static org.openforis.idm.metamodel.TimeAttributeDefinition.MINUTE_FIELD;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.json.simple.parser.JSONParser;
import org.openforis.collect.datacleansing.ValueParser;
import org.openforis.idm.metamodel.AttributeDefinition;
import org.openforis.idm.metamodel.BooleanAttributeDefinition;
import org.openforis.idm.metamodel.CodeAttributeDefinition;
import org.openforis.idm.metamodel.CoordinateAttributeDefinition;
import org.openforis.idm.metamodel.DateAttributeDefinition;
import org.openforis.idm.metamodel.FileAttributeDefinition;
import org.openforis.idm.metamodel.NumberAttributeDefinition;
import org.openforis.idm.metamodel.NumericAttributeDefinition.Type;
import org.openforis.idm.metamodel.RangeAttributeDefinition;
import org.openforis.idm.metamodel.TaxonAttributeDefinition;
import org.openforis.idm.metamodel.TextAttributeDefinition;
import org.openforis.idm.metamodel.TimeAttributeDefinition;
import org.openforis.idm.metamodel.Unit;
import org.openforis.idm.model.BooleanValue;
import org.openforis.idm.model.Code;
import org.openforis.idm.model.Coordinate;
import org.openforis.idm.model.Date;
import org.openforis.idm.model.File;
import org.openforis.idm.model.IntegerRange;
import org.openforis.idm.model.IntegerValue;
import org.openforis.idm.model.NumberValue;
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
public class JSONValueParser implements ValueParser {
	
	@Override
	public Value parseValue(AttributeDefinition def, String value) {
		if (def instanceof BooleanAttributeDefinition) {
			return parseBoolean(value);
		} else if (def instanceof CodeAttributeDefinition) {
			return parseCode(value);
		} else if (def instanceof CoordinateAttributeDefinition) {
			return parseCoordinate(value);
		} else if (def instanceof DateAttributeDefinition) {
			return parseDate(value);
		} else if (def instanceof FileAttributeDefinition) {
			return parseFile(value);
		} else if (def instanceof NumberAttributeDefinition) {
			if (((NumberAttributeDefinition) def).getType() == Type.INTEGER) {
				return parseInteger((NumberAttributeDefinition) def, value);
			} else {
				return parseReal((NumberAttributeDefinition) def, value);
			}
		} else if (def instanceof RangeAttributeDefinition) {
			if (((RangeAttributeDefinition) def).getType() == Type.INTEGER) {
				return parseIntegerRange((RangeAttributeDefinition) def, value);
			} else {
				return parseRealRange((RangeAttributeDefinition) def, value);
			}
		} else if (def instanceof TaxonAttributeDefinition) {
			return parseTaxonOccurrence(value);
		} else if (def instanceof TextAttributeDefinition) {
			return parseText(value);
		} else if (def instanceof TimeAttributeDefinition) {
			return parseTime(value);
		} else {
			throw new UnsupportedOperationException("Attribute type not supported: " + def.getClass());
		}
	}

	public BooleanValue parseBoolean(String value) {
		Map<String, Object> map = parseJSONToMap(value, BooleanValue.VALUE_FIELD);
		return map == null ? null: new BooleanValue((Boolean) map.get(BooleanValue.VALUE_FIELD));
	}
	
	public Code parseCode(String value) {
		Map<String, Object> map = parseJSONToMap(value, CodeAttributeDefinition.CODE_FIELD);
		return map == null ? null: new Code(
				(String) map.get(CodeAttributeDefinition.CODE_FIELD), 
				(String) map.get(CodeAttributeDefinition.QUALIFIER_FIELD)
		);
	}
	
	public Coordinate parseCoordinate(String value) {
		Map<String, Object> map = parseJSONToMap(value);
		return map == null ? null: new Coordinate(
				getDouble(map, X_FIELD_NAME), 
				getDouble(map, Y_FIELD_NAME), 
				(String) map.get(SRS_FIELD_NAME)
			);
	}
	
	public Date parseDate(String value) {
		Map<String, Object> map = parseJSONToMap(value, null);
		return map == null ? null: new Date(
				getInteger(map, Date.YEAR_FIELD), 
				getInteger(map, Date.MONTH_FIELD), 
				getInteger(map, Date.DAY_FIELD)
		);
	}
	
	public File parseFile(String value) {
		Map<String, Object> map = parseJSONToMap(value, File.FILENAME_FIELD);
		return map == null ? null: new File(
				(String) map.get(File.FILENAME_FIELD), 
				getLong(map, File.SIZE_FIELD)
		);
	}
	
	public IntegerValue parseInteger(NumberAttributeDefinition attrDef, String value) {
		Map<String, Object> map = parseJSONToMap(value, NumberValue.VALUE_FIELD);
		if (map == null) {
			return null;
		}
		Integer unitId = getInteger(map, NumberValue.UNIT_ID_FIELD);
		Unit unit = attrDef.getUnitOrDefault(unitId);
		Integer unitIdActual = unit == null ? null : unit.getId();
		return new IntegerValue(getInteger(map, NumberValue.VALUE_FIELD), unitIdActual);
	}
	
	public IntegerRange parseIntegerRange(RangeAttributeDefinition attrDef, String value) {
		Map<String, Object> map = parseJSONToMap(value);
		if (map == null) {
			return null;
		}
		Integer from = getInteger(map, IntegerRange.FROM_FIELD);
		Integer to = getInteger(map, IntegerRange.TO_FIELD);
		if (to == null) {
			to = from;
		}
		Integer unitId = getInteger(map, NumberValue.UNIT_ID_FIELD);
		Unit unit = attrDef.getUnitOrDefault(unitId);
		return new IntegerRange(from, to, unit == null ? null : unit.getId());
	}
	
	public RealValue parseReal(NumberAttributeDefinition attrDef, String value) {
		Map<String, Object> map = parseJSONToMap(value, RealValue.VALUE_FIELD);
		if (map == null) {
			return null;
		}
		Integer unitId = getInteger(map, RealValue.UNIT_ID_FIELD);
		Unit unit = attrDef.getUnitOrDefault(unitId);
		Integer unitIdActual = unit == null ? null : unit.getId();
		return new RealValue(getDouble(map, RealValue.VALUE_FIELD), unitIdActual);
	}
	
	public RealRange parseRealRange(RangeAttributeDefinition attrDef, String value) {
		Map<String, Object> map = parseJSONToMap(value);
		if (map == null) {
			return null;
		}
		Double from = getDouble(map, RealRange.FROM_FIELD);
		Double to = getDouble(map, RealRange.TO_FIELD);
		if (to == null) {
			to = from;
		}
		Integer unitId = getInteger(map, RealRange.UNIT_ID_FIELD);
		Unit unit = attrDef.getUnitOrDefault(unitId);
		Integer unitIdActual = unit == null ? null : unit.getId();
		return new RealRange(from, to, unitIdActual);
	}
	
	public TaxonOccurrence parseTaxonOccurrence(String value) {
		Map<String, Object> map = parseJSONToMap(value, TaxonAttributeDefinition.CODE_FIELD_NAME);
		return map == null ? null: new TaxonOccurrence(
				(String) map.get(CODE_FIELD_NAME), 
				(String) map.get(SCIENTIFIC_NAME_FIELD_NAME), 
				(String) map.get(VERNACULAR_NAME_FIELD_NAME), 
				(String) map.get(LANGUAGE_CODE_FIELD_NAME), 
				(String) map.get(LANGUAGE_VARIETY_FIELD_NAME)
		);
	}

	private TextValue parseText(String value) {
		Map<String, Object> map = parseJSONToMap(value);
		return map == null ? null: new TextValue((String) map.get(TextValue.VALUE_FIELD));
	}

	public Time parseTime(String value) {
		Map<String, Object> map = parseJSONToMap(value);
		return map == null ? null: new Time(getInteger(map, HOUR_FIELD), getInteger(map, MINUTE_FIELD));
	}
	
	private Map<String, Object> parseJSONToMap(final String value) {
		return parseJSONToMap(value, null);
	}
	
	@SuppressWarnings("serial")
	private Map<String, Object> parseJSONToMap(final String value, final String singleValueField) {
		if ( StringUtils.isBlank(value) ) {
			return null;
		}
		if (value.startsWith("{")) { //is JSON
			try {
				@SuppressWarnings("unchecked")
				Map<String, Object> map = (Map<String, Object>) new JSONParser().parse(value);
				return map;
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		} else {
			return new HashMap<String, Object>() {{
				put(StringUtils.trimToEmpty(singleValueField), value);
			}};
		}
	}
	
	protected static Integer getInteger(Map<String, Object> map, String field) {
		Object val = map.get(field);
		if (val == null) {
			return null;
		} else if (val instanceof Number) {
			return Integer.valueOf(((Number) val).intValue());
		} else {
			return Integer.parseInt(val.toString());
		}
	}

	protected static Double getDouble(Map<String, Object> map, String field) {
		Object val = map.get(field);
		if (val == null) {
			return null;
		} else if (val instanceof Number) {
			return Double.valueOf(((Number) val).doubleValue());
		} else {
			return Double.parseDouble(val.toString());
		}
	}
	
	protected static Long getLong(Map<String, Object> map, String field) {
		Object val = map.get(field);
		if (val == null) {
			return null;
		} else if (val instanceof Number) {
			return Long.valueOf(((Number) val).longValue());
		} else {
			return Long.parseLong(val.toString());
		}
	}
	
}
