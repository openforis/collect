/**
 * 
 */
package org.openforis.idm.metamodel;

import org.apache.commons.lang3.StringUtils;
import org.openforis.idm.model.Node;
import org.openforis.idm.model.Time;
import org.openforis.idm.model.TimeAttribute;
import org.openforis.idm.model.Value;

/**
 * @author G. Miceli
 * @author M. Togna
 */
public class TimeAttributeDefinition extends AttributeDefinition {

	private static final long serialVersionUID = 1L;
	
	public static final String MINUTE_FIELD = "minute";
	public static final String HOUR_FIELD = "hour";

	private final FieldDefinitionMap fieldDefinitionByName = new FieldDefinitionMap(
		new FieldDefinition<Integer>(HOUR_FIELD, "h", "h", Integer.class, this), 
		new FieldDefinition<Integer>(MINUTE_FIELD, "m", "m", Integer.class, this)
	);
	
	protected TimeAttributeDefinition(Survey survey, int id) {
		super(survey, id);
	}

	@Override
	public Node<?> createNode() {
		return new TimeAttribute(this);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public Time createValue(String string) {
		if ( StringUtils.isBlank(string) ) {
			return null;
		} else {
			return Time.parseTime(string);
		}
	}
	
	@Override
	protected FieldDefinitionMap getFieldDefinitionMap() {
		return fieldDefinitionByName;
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
