/**
 * 
 */
package org.openforis.collect.io.data.csv;

import java.util.ArrayList;
import java.util.List;

import org.openforis.idm.metamodel.NumberAttributeDefinition;
import org.openforis.idm.metamodel.Unit;
import org.openforis.idm.model.Attribute;
import org.openforis.idm.model.NumberAttribute;

/**
 * @author S. Ricci
 *
 */
public class NumberColumnProvider extends CompositeAttributeColumnProvider<NumberAttributeDefinition> {
	
	public NumberColumnProvider(CSVDataExportParameters config, NumberAttributeDefinition defn) {
		super(config, defn);
	}

	@Override
	protected String[] getFieldNames() {
		List<String> result = new ArrayList<String>();
		result.add(NumberAttributeDefinition.VALUE_FIELD);
		if ( ! attributeDefinition.getUnits().isEmpty() ) {
			result.add(NumberAttributeDefinition.UNIT_NAME_FIELD);
		}
		return result.toArray(new String[result.size()]);
	}
	
	@Override
	protected String generateFieldHeading(String fieldName) {
		if ( NumberAttributeDefinition.VALUE_FIELD.equals(fieldName) ) {
			return ColumnProviders.generateHeadingPrefix(attributeDefinition, config);
		} else {
			return super.generateFieldHeading(fieldName);
		}
	}
	
	@Override
	protected String extractValue(Attribute<?, ?> attr, String fieldName) {
		if (NumberAttributeDefinition.UNIT_NAME_FIELD.equals(fieldName)) {
			NumberAttribute<?, ?> numAttr = (NumberAttribute<?, ?>) attr;
			Unit unit = numAttr.getUnit();
			if (unit == null) {
				return "";
			} else {
				return unit.getName();
			}
		} else {
			return super.extractValue(attr, fieldName);
		}
	}
	
}
