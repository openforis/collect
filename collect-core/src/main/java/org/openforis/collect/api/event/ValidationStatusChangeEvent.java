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
public class ValidationStatusChangeEvent extends Event {

	public final Map<Integer, Status> statusByNodeId;
	
	public ValidationStatusChangeEvent(Command trigger, Map<Integer, Status> statusByNodeId) {
		super(trigger);
		this.statusByNodeId = statusByNodeId;
	}

	public static enum Status {
		OK, WARNING, ERROR
	}
	
}
