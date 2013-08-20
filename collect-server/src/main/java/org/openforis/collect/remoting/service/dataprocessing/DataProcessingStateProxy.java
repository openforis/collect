package org.openforis.collect.remoting.service.dataprocessing;

import org.granite.messaging.amf.io.util.externalizer.annotation.ExternalizedProperty;
import org.openforis.collect.Proxy;

/**
 * 
 * @author S. Ricci
 *
 */
public class DataProcessingStateProxy implements Proxy {

	protected transient DataProcessingState state;

	public DataProcessingStateProxy(DataProcessingState state) {
		super();
		this.state = state;
	}

	@ExternalizedProperty
	public boolean isRunning() {
		return state.isRunning();
	}

	@ExternalizedProperty
	public boolean isError() {
		return state.isError();
	}

	@ExternalizedProperty
	public boolean isCancelled() {
		return state.isCancelled();
	}

	@ExternalizedProperty
	public int getCount() {
		return state.getCount();
	}

	@ExternalizedProperty
	public int getTotal() {
		return state.getTotal();
	}

	@ExternalizedProperty
	public boolean isComplete() {
		return state.isComplete();
	}

	@ExternalizedProperty
	public String getErrorMessage() {
		return state.getErrorMessage();
	}
	
}
