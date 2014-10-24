/**
 * 
 */
package org.openforis.idm.metamodel;

import org.apache.commons.lang3.StringUtils;
import org.openforis.idm.model.Coordinate;
import org.openforis.idm.model.CoordinateAttribute;
import org.openforis.idm.model.Node;
import org.openforis.idm.model.Value;

/**
 * @author G. Miceli
 * @author M. Togna
 */
public class CoordinateAttributeDefinition extends AttributeDefinition  {

	private static final long serialVersionUID = 1L;

	public static final String SRS_FIELD_NAME = "srs";
	public static final String X_FIELD_NAME = "x";
	public static final String Y_FIELD_NAME = "y";

	private final FieldDefinitionMap fieldDefinitionByName = new FieldDefinitionMap(
		new FieldDefinition<Double>(X_FIELD_NAME, "x", "x", Double.class, this),
		new FieldDefinition<Double>(Y_FIELD_NAME, "y", "y", Double.class, this),
		new FieldDefinition<String>(SRS_FIELD_NAME, "srs", "srs", String.class, this)
	);

	CoordinateAttributeDefinition(Survey survey, int id) {
		super(survey, id);
	}

	@Override
	public Node<?> createNode() {
		return new CoordinateAttribute(this);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public Coordinate createValue(String string) {
		if ( StringUtils.isBlank(string) ) {
			return null;
		} else {
			return Coordinate.parseCoordinate(string);
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
		return Coordinate.class;
	}
}
