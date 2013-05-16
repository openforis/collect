/**
 * 
 */
package org.openforis.collect.csv;

import org.openforis.idm.model.Attribute;
import org.openforis.idm.model.Field;

/**
 * @author S. Ricci
 *
 * @deprecated replaced with idm-transform api
 */
@Deprecated
public class TimeColumnProvider extends CompositeAttributeColumnProvider {
	
	public TimeColumnProvider(String attributeName) {
		super(attributeName);
	}

	@Override
	protected String[] getFieldsHeadings() {
		return new String[] { getAttributeName() + "_hour", getAttributeName() + "_minute" };
	}

	@Override
	protected Field<?>[] getFieldsToExtract(Attribute<?, ?> attr) {
		return new Field[] { attr.getField(0), attr.getField(1) };
	}
}
