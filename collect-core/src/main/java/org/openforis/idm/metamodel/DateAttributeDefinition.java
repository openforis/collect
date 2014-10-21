/**
 * 
 */
package org.openforis.idm.metamodel;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
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

	private final FieldDefinition<?>[] FIELD_DEFINITIONS = {
			new FieldDefinition<Integer>(YEAR_FIELD_NAME, "y", "y", Integer.class, this),
			new FieldDefinition<Integer>(MONTH_FIELD_NAME, "m", "m", Integer.class, this),
			new FieldDefinition<Integer>(DAY_FIELD_NAME, "d", "d", Integer.class, this)
	};
	
	
	DateAttributeDefinition(Survey survey, int id) {
		super(survey, id);
	}

	@Override
	public Node<?> createNode() {
		return new DateAttribute(this);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public Date createValue(String string) {
		if ( StringUtils.isBlank(string) ) {
			return null;
		} else {
			return Date.parse(string);
		}
	}
	
	@Override
	public List<FieldDefinition<?>> getFieldDefinitions() {
		return Collections.unmodifiableList(Arrays.asList(FIELD_DEFINITIONS));
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
