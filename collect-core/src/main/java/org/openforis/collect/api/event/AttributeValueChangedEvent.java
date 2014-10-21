/**
 * 
 */
package org.openforis.collect.api.event;

import org.openforis.collect.api.command.Command;

/**
 * @author D. Wiell
 * @author S. Ricci
 *
 */
public class AttributeValueChangedEvent extends Event {

	public AttributeValueChangedEvent(Command trigger) {
		super(trigger);
	}

}
