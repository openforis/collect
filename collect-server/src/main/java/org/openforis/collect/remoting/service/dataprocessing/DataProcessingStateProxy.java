package org.openforis.collect.remoting.service.dataprocessing;

import org.granite.messaging.amf.io.util.externalizer.annotation.ExternalizedProperty;
import org.openforis.collect.Proxy;
import org.openforis.collect.manager.process.ProcessStatus;

/**
 * 
 * @author S. Ricci
 *
 */
public class DataProcessingStateProxy implements Proxy {

	protected transient ProcessStatus status;

	public DataProcessingStateProxy(ProcessStatus status) {
		super();
		this.status = status;
	}

	@ExternalizedProperty
	public boolean isRunning() {
		return status.isRunning();
	}

	@ExternalizedProperty
	public boolean isError() {
		return status.isError();
	}

	@ExternalizedProperty
	public boolean isCancelled() {
		return status.isCancelled();
	}

	@ExternalizedProperty
	public long getProcessed() {
		return status.getProcessed();
	}

	@ExternalizedProperty
	public long getTotal() {
		return status.getTotal();
	}

	@ExternalizedProperty
	public boolean isComplete() {
		return status.isComplete();
	}

	@ExternalizedProperty
	public String getErrorMessage() {
		return status.getErrorMessage();
	}
	
}
