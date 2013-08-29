/**
 * 
 */
package org.openforis.collect.csv;

import org.openforis.idm.metamodel.CoordinateAttributeDefinition;
import org.openforis.idm.model.Attribute;
import org.openforis.idm.model.CoordinateAttribute;
import org.openforis.idm.model.Field;

/**
 * @author M. Togna
 * 
 * @deprecated replaced with idm-transform api
 */
@Deprecated
public class CoordinateColumnProvider extends CompositeAttributeColumnProvider {

	private static final String FIELD_SEPARATOR = "_";

	public CoordinateColumnProvider(String attributeName) {
		super(attributeName);
	}

	@Override
	protected String[] getFieldsHeadings() {
		return new String[] { 
				getAttributeName() + FIELD_SEPARATOR + CoordinateAttributeDefinition.SRS_FIELD_NAME, 
				getAttributeName() + FIELD_SEPARATOR + CoordinateAttributeDefinition.X_FIELD_NAME, 
				getAttributeName() + FIELD_SEPARATOR + CoordinateAttributeDefinition.Y_FIELD_NAME };
	}

	@Override
	protected Field<?>[] getFieldsToExtract(Attribute<?, ?> attr) {
		CoordinateAttribute coordinateAttr = (CoordinateAttribute) attr;
		return new Field[] { 
				coordinateAttr.getSrsIdField(), 
				coordinateAttr.getXField(), 
				coordinateAttr.getYField()
		};
	}

}
