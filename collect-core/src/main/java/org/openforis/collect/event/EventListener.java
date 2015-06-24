package org.openforis.collect.event;

import java.util.List;

/**
 * 
 * @author D. Wiell
 * @author S. Ricci
 *
 */
public interface EventListener {

	void onEvents(List<? extends RecordEvent> events);
	
}
