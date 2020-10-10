package org.openforis.collect.event;

import org.openforis.idm.model.NumericRange;

/**
 * 
 * @author D. Wiell
 * @author S. Ricci
 *
 */
public abstract class RangeAttributeUpdatedEvent<V extends NumericRange<? extends Number>>
		extends AttributeValueUpdatedEvent<V> {
}
