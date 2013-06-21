package org.openforis.collect.manager.process.proxy;

import org.granite.messaging.amf.io.util.externalizer.annotation.ExternalizedProperty;
import org.openforis.collect.Proxy;
import org.openforis.collect.manager.process.ProcessStatus;
import org.openforis.collect.manager.process.ProcessStatus.Step;

/**
 * 
 * @author S. Ricci
 *
 */
public class ProcessStatusProxy implements Proxy {
	
	private transient ProcessStatus status;

	public ProcessStatusProxy(ProcessStatus status) {
		super();
		this.status = status;
	}

	@ExternalizedProperty
	public Step getStep() {
		return status.getStep();
	}

	@ExternalizedProperty
	public long getTotal() {
		return status.getTotal();
	}

	@ExternalizedProperty
	public long getProcessed() {
		return status.getProcessed();
	}

	@ExternalizedProperty
	public String getErrorMessage() {
		return status.getErrorMessage();
	}

	@ExternalizedProperty
	public boolean isInit() {
		return status.isInit();
	}

	@ExternalizedProperty
	public boolean isRunning() {
		return status.isRunning();
	}

	@ExternalizedProperty
	public boolean isComplete() {
		return status.isComplete();
	}

	@ExternalizedProperty
	public boolean isError() {
		return status.isError();
	}
	
	
}
