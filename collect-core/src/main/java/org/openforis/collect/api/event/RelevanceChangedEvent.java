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
public class RelevanceChangedEvent extends Event {

	public final Map<String, Boolean> relevanceByNodeId;
	
	public RelevanceChangedEvent(Command trigger, Map<String, Boolean> relevanceByNodeId) {
		super(trigger);
		this.relevanceByNodeId = relevanceByNodeId;
	}
	
}
