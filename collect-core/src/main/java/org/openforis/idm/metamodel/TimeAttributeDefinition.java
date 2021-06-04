/**
 * 
 */
package org.openforis.idm.metamodel;

import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.openforis.commons.lang.Numbers;
import org.openforis.idm.model.Node;
import org.openforis.idm.model.Time;
import org.openforis.idm.model.TimeAttribute;
import org.openforis.idm.model.Value;

/**
 * @author G. Miceli
 * @author M. Togna
 */
public class TimeAttributeDefinition extends AttributeDefinition implements KeyAttributeDefinition {

	private static final long serialVersionUID = 1L;
	
	public static final String MINUTE_FIELD = "minute";
	public static final String HOUR_FIELD = "hour";

	private final FieldDefinitionMap fieldDefinitionByName = new FieldDefinitionMap(
		new FieldDefinition<Integer>(HOUR_FIELD, "h", "h", Integer.class, this), 
		new FieldDefinition<Integer>(MINUTE_FIELD, "m", "m", Integer.class, this)
	);
	
	TimeAttributeDefinition(Survey survey, int id) {
		super(survey, id);
	}

	TimeAttributeDefinition(Survey survey, TimeAttributeDefinition source, int id) {
		super(survey, source, id);
	}
	
	@Override
	public Node<?> createNode() {
		return new TimeAttribute(this);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public Time createValue(String string) {
		return Time.parseTime(string);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public Time createValue(Object val) {
		if (val == null) {
			return null;
		} else if (val instanceof Integer) {
			return Time.fromNumericValue((Integer) val);
		} else if (val instanceof Date) {
			return Time.parse((Date) val);
		} else {
			return Time.parseTime(val.toString());
		}
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <V extends Value> V createValueFromKeyFieldValues(List<String> fieldValues) {
		if (StringUtils.isAnyBlank(fieldValues.toArray(new String[fieldValues.size()]))) {
			return null;
		}
		return (V) new Time(
				Numbers.toIntegerObject(fieldValues.get(0)), 
				Numbers.toIntegerObject(fieldValues.get(1))
		);
	}
	
	@Override
	protected FieldDefinitionMap getFieldDefinitionMap() {
		return fieldDefinitionByName;
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
	public Class<? extends Value> getValueType() {
		return Time.class;
	}
}
