/**
 * 
 */
package org.openforis.idm.metamodel;

import java.util.List;

import org.openforis.idm.model.Date;
import org.openforis.idm.model.DateAttribute;
import org.openforis.idm.model.Node;
import org.openforis.idm.model.Value;

/**
 * @author G. Miceli
 * @author M. Togna
 */
public class DateAttributeDefinition extends AttributeDefinition {

	private static final long serialVersionUID = 1L;
	
	public static final String YEAR_FIELD_NAME = "year";
	public static final String MONTH_FIELD_NAME = "month";
	public static final String DAY_FIELD_NAME = "day";

	private final FieldDefinitionMap fieldDefinitionByName = new FieldDefinitionMap(
		new FieldDefinition<Integer>(YEAR_FIELD_NAME, "y", "y", Integer.class, this),
		new FieldDefinition<Integer>(MONTH_FIELD_NAME, "m", "m", Integer.class, this),
		new FieldDefinition<Integer>(DAY_FIELD_NAME, "d", "d", Integer.class, this)
	);
	
	DateAttributeDefinition(Survey survey, int id) {
		super(survey, id);
	}

	DateAttributeDefinition(DateAttributeDefinition obj, int id) {
		super(obj, id);
	}
	
	@Override
	public Node<?> createNode() {
		return new DateAttribute(this);
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public Date createValue(String string) {
		return Date.parse(string);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public Date createValue(Object val) {
		if (val == null) {
			return null;
		} else if (val instanceof java.util.Date) {
			return Date.parse((java.util.Date) val);
		} else if (val instanceof Integer) {
			return Date.fromNumericValue((Integer) val); 
		} else {
			return Date.parse((String) val);
		}
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <V extends Value> V createValueFromFieldStringValues(List<String> fieldValues) {
		return (V) new Date(
				Integer.parseInt(fieldValues.get(0)), 
				Integer.parseInt(fieldValues.get(1)), 
				Integer.parseInt(fieldValues.get(2)));
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
		return Date.class;
	}
}
