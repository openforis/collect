/**
 * 
 */
package org.openforis.collect.csv;

import org.openforis.idm.metamodel.DateAttributeDefinition;
import org.openforis.idm.model.Attribute;
import org.openforis.idm.model.DateAttribute;
import org.openforis.idm.model.Field;

/**
 * @author S. Ricci
 *
 * @deprecated replaced with idm-transform api
 */
@Deprecated
public class DateColumnProvider extends CompositeAttributeColumnProvider {
	
	public DateColumnProvider(String attributeName) {
		super(attributeName);
	}

	@Override
	protected String[] getFieldsHeadings() {
		return new String[] {
				getFieldHeading(DateAttributeDefinition.YEAR_FIELD_NAME),
				getFieldHeading(DateAttributeDefinition.MONTH_FIELD_NAME),
				getFieldHeading(DateAttributeDefinition.DAY_FIELD_NAME)
		};
	}

	@Override
	protected Field<?>[] getFieldsToExtract(Attribute<?, ?> attr) {
		DateAttribute dateAttr = (DateAttribute) attr;
		return new Field[] { 
				dateAttr.getYearField(),
				dateAttr.getMonthField(),
				dateAttr.getDayField()
		};
	}
		
}
