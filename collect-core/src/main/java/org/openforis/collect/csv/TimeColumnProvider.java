/**
 * 
 */
package org.openforis.collect.csv;

import org.openforis.idm.metamodel.TimeAttributeDefinition;
import org.openforis.idm.model.Attribute;
import org.openforis.idm.model.Field;
import org.openforis.idm.model.TimeAttribute;

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
		return new String[] {
				getFieldHeading(TimeAttributeDefinition.HOUR_FIELD),
				getFieldHeading(TimeAttributeDefinition.MINUTE_FIELD)
		};
	}
	
	@Override
	protected Field<?>[] getFieldsToExtract(Attribute<?, ?> attr) {
		TimeAttribute timeAttr = (TimeAttribute) attr;
		return new Field[] { 
				timeAttr.getHourField(),
				timeAttr.getMinuteField()
		};
	}
}
