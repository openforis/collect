package org.openforis.collect.event;

import org.openforis.idm.model.NumberValue;

/**
 * 
 * @author D. Wiell
 * @author S. Ricci
 *
 */
public abstract class NumberAttributeUpdatedEvent<V extends NumberValue<? extends Number>>
		extends AttributeValueUpdatedEvent<V> {

}
