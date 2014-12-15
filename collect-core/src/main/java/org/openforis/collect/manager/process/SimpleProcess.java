/**
 * 
 */
package org.openforis.collect.manager.process;

/**
 * @author S. Ricci
 *
 */
public class SimpleProcess extends AbstractProcess<Void, ProcessStatus> {

	@Override
	protected void initStatus() {
		status = new ProcessStatus();
	}
	
}
