/**
 * 
 */
package org.openforis.collect.io.data.csv;

import java.util.ArrayList;
import java.util.List;

import org.openforis.idm.metamodel.NumberAttributeDefinition;

/**
 * @author S. Ricci
 *
 */
public class NumberColumnProvider extends CompositeAttributeColumnProvider<NumberAttributeDefinition> {
	
	public NumberColumnProvider(NumberAttributeDefinition defn) {
		super(defn);
	}

	@Override
	protected String[] getFieldNames() {
		List<String> result = new ArrayList<String>();
		result.add(NumberAttributeDefinition.VALUE_FIELD);
		if ( ! defn.getUnits().isEmpty() ) {
			result.add(NumberAttributeDefinition.UNIT_FIELD);
		}
		return result.toArray(new String[0]);
	}
	
	@Override
	protected String getFieldHeading(String fieldName) {
		if ( NumberAttributeDefinition.VALUE_FIELD.equals(fieldName) ) {
			return defn.getName();
		} else {
			return super.getFieldHeading(fieldName);
		}
	}
	
}
