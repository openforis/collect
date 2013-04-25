package org.openforis.collect.manager.process;


/**
 * 
 * @author S. Ricci
 * 
 * @param <V> the result type of method <tt>call</tt>
 * @param <S> the type of ProcessStatus object return by <tt>getStatus</tt>
 *
 */
public abstract class AbstractProcess<V, S extends ProcessStatus> implements org.openforis.collect.manager.Process<V> {

	protected S status;
	
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
	
	@Override
	public V call() throws Exception {
		startProcessing();
		if ( status.isRunning() ) {
			status.complete();
		}
		return null;
	}

	@Override
	public void startProcessing() throws Exception {
		if ( status == null ) {
			init();
		} else if ( status.isRunning() ) {
			throw new IllegalStateException("Process already running");
		}
		status.start();
	}
	
}
