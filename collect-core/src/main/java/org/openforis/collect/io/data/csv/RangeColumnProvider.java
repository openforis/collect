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
	
	public RangeColumnProvider(RangeAttributeDefinition defn) {
		super(defn);
	}


	@Override
	protected String[] getFieldNames() {
		List<String> result = new ArrayList<String>();
		result.add(RangeAttributeDefinition.FROM_FIELD);
		result.add(RangeAttributeDefinition.TO_FIELD);
		if ( ! defn.getUnits().isEmpty() ) {
			result.add(RangeAttributeDefinition.UNIT_FIELD);
		}
		return result.toArray(new String[0]);
	}
	
	@Override
	protected String getFieldHeading(String fieldName) {
		return super.getFieldHeading(fieldName);
	}
}
