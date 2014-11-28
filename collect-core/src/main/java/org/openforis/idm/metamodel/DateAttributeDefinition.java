/**
 * 
 */
package org.openforis.idm.metamodel;

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

	@Override
	public Node<?> createNode() {
		return new DateAttribute(this);
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public Date createValue(String string) {
		return Date.parse(string);
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
		return Date.class;
	}
}
