/**
 * 
 */
package org.openforis.collect.api.command;

/**
 * @author D. Wiell
 * @author S. Ricci
 *
 */
public interface CommandQueue {

	void submit(Command command);
	
}
