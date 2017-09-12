/**
 * 
 */
package org.openforis.collect.io.data.csv;

import java.util.ArrayList;
import java.util.List;

import org.openforis.idm.metamodel.RangeAttributeDefinition;

/**
 * @author S. Ricci
 *
 */
public class RangeColumnProvider extends CompositeAttributeColumnProvider<RangeAttributeDefinition> {
	
	public RangeColumnProvider(CSVDataExportParameters config, RangeAttributeDefinition defn) {
		super(config, defn);
	}


	@Override
	protected String[] getFieldNames() {
		List<String> result = new ArrayList<String>();
		result.add(RangeAttributeDefinition.FROM_FIELD);
		result.add(RangeAttributeDefinition.TO_FIELD);
		if ( ! attributeDefinition.getUnits().isEmpty() ) {
			result.add(RangeAttributeDefinition.UNIT_FIELD);
		}
		return result.toArray(new String[result.size()]);
	}
	
	@Override
	protected String generateFieldHeading(String fieldName) {
		return super.generateFieldHeading(fieldName);
	}
}
