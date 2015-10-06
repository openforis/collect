package org.openforis.collect.event;

/**
 * 
 * @author D. Wiell
 * @author S. Ricci
 *
 */
public interface EventSource {
	
	void register(EventListener listener);

}
