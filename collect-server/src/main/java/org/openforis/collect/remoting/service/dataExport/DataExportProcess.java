/**
 * 
 */
package org.openforis.collect.remoting.service.dataExport;

/**
 * @author S. Ricci
 *
 */
public interface DataExportProcess {

	public DataExportState getState();

	public boolean isRunning();

	public void cancel();

	public boolean isComplete();
	
}
