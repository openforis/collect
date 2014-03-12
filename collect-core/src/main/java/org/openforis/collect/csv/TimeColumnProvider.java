/**
 * 
 */
package org.openforis.collect.csv;

import org.openforis.idm.metamodel.TimeAttributeDefinition;

/**
 * @author S. Ricci
 *
 * @deprecated replaced with idm-transform api
 */
@Deprecated
public class TimeColumnProvider extends CompositeAttributeColumnProvider<TimeAttributeDefinition> {
	
	public TimeColumnProvider(TimeAttributeDefinition defn) {
		super(defn);
	}

	@Override
	protected String[] getFieldNames() {
		return new String[] {
				TimeAttributeDefinition.HOUR_FIELD,
				TimeAttributeDefinition.MINUTE_FIELD
		};
	}
	
}
