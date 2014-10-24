/**
 * 
 */
package org.openforis.collect.api.event;

import java.util.Map;

import org.apache.commons.lang3.Validate;
import org.openforis.collect.api.command.Command;
import org.openforis.idm.model.Value;

/**
 * @author D. Wiell
 * @author S. Ricci
 *
 */
public class AttributeValuesChangedEvent extends Event {

	public final Map<String, Value> valuesByAttributeId; 
	
	public AttributeValuesChangedEvent(Command trigger, Map<String, Value> valuesByAttributeId) {
		super(trigger);
		Validate.notNull(valuesByAttributeId, "valuesByAttributeId is null");
		this.valuesByAttributeId = valuesByAttributeId;
	}

}
