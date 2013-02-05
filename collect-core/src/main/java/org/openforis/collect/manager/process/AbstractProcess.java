package org.openforis.collect.manager.process;


/**
 * 
 * @author S. Ricci
 * @param <V>
 *
 */
public abstract class AbstractProcess<V, S extends ProcessStatus> implements org.openforis.collect.manager.Process<V> {

	protected S status;
	
	public AbstractProcess() {
		init();
	}

	protected void init() {
		initStatus();
	}

	protected abstract void initStatus();
	
	@Override
	public void cancel() {
		status.cancel();
	}
	
	@Override
	public S getStatus() {
		return status;
	}
	

}
