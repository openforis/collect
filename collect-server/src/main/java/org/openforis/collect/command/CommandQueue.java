package org.openforis.collect.command;

/**
 * 
 * @author D. Wiell
 * @author S. Ricci
 *
 */
public interface CommandQueue {

	void publish(Command command);
	
	boolean isEnabled();
	
	void setEnabled(boolean enabled);
	
}