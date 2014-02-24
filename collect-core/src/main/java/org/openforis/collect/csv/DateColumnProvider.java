/**
 * 
 */
package org.openforis.collect.csv;

import org.openforis.idm.metamodel.DateAttributeDefinition;

/**
 * @author S. Ricci
 *
 * @deprecated replaced with idm-transform api
 */
@Deprecated
public class DateColumnProvider extends CompositeAttributeColumnProvider<DateAttributeDefinition> {
	
	public DateColumnProvider(DateAttributeDefinition defn) {
		super(defn);
	}

	@Override
	protected String[] getFieldNames() {
		return new String[] {
				DateAttributeDefinition.YEAR_FIELD_NAME,
				DateAttributeDefinition.MONTH_FIELD_NAME,
				DateAttributeDefinition.DAY_FIELD_NAME
		};
	}

}
