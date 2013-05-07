/**
 * 
 */
package org.openforis.collect.csv;

import org.openforis.idm.model.Attribute;
import org.openforis.idm.model.Field;

/**
 * @author M. Togna
 * 
 * @deprecated replaced with idm-transform api
 */
@Deprecated
public class CoordinateColumnProvider extends CompositeAttributeColumnProvider {

	public CoordinateColumnProvider(String attributeName) {
		super(attributeName);
	}

	@Override
	protected String[] getFieldsHeadings() {
		return new String[] { getAttributeName() + "_srs_id", getAttributeName() + "_x", getAttributeName() + "_y" };
	}

	@Override
	protected Field<?>[] getFieldsToExtract(Attribute<?, ?> attr) {
		return new Field[] { attr.getField(2), attr.getField(0), attr.getField(1) };
	}

}
