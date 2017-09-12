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
	
	public TimeColumnProvider(CSVDataExportParameters config, TimeAttributeDefinition defn) {
		super(config, defn);
	}

	@Override
	protected String[] getFieldNames() {
		return new String[] {
				TimeAttributeDefinition.HOUR_FIELD,
				TimeAttributeDefinition.MINUTE_FIELD
		};
	}
	
}
