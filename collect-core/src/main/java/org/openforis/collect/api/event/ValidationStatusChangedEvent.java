/**
 * 
 */
package org.openforis.collect.api.event;

import java.util.Map;

import org.openforis.collect.api.command.Command;

/**
 * @author D. Wiell
 * @author S. Ricci
 *
 */
public class ValidationStatusChangedEvent extends Event {

	public static enum Status {
		OK, WARNING, ERROR
	}
	
	public final Map<String, Status> statusByNodeId;
	
	public ValidationStatusChangedEvent(Command trigger, Map<String, Status> statusByNodeId) {
		super(trigger);
		this.statusByNodeId = statusByNodeId;
	}
	
}
