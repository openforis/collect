package org.openforis.collect.manager;

import java.util.concurrent.Callable;

import org.openforis.collect.manager.process.ProcessStatus;

/**
 * 
 * @author S. Ricci
 * @param <V> the result type of method <tt>call</tt>
 *
 */
public interface Process<V> extends Callable<V> {

	void cancel();
	
	ProcessStatus getStatus();

	void startProcessing() throws Exception;
	
}
