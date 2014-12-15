/**
 * 
 */
package org.openforis.collect.io.data.csv;

import org.openforis.idm.metamodel.TimeAttributeDefinition;

/**
 * @author S. Ricci
 *
 */
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
