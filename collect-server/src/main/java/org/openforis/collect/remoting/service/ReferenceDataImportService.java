package org.openforis.collect.remoting.service;

import org.openforis.collect.manager.process.AbstractProcess;
import org.openforis.collect.manager.referencedataimport.proxy.ReferenceDataImportStatusProxy;
import org.openforis.collect.utils.ExecutorServiceUtil;
import org.springframework.security.access.annotation.Secured;

/**
 * 
 * @author S. Ricci
 *
 */
public abstract class ReferenceDataImportService<S extends ReferenceDataImportStatusProxy, P extends AbstractProcess<Void, ?>> {
	
	protected P importProcess;
	
	protected void init() {
	}
	
	protected void startProcessThread() {
		ExecutorServiceUtil.executeInCachedPool(importProcess);
	}

	@Secured("ROLE_ADMIN")
	public void cancel() {
		if ( importProcess != null ) {
			importProcess.cancel();
		}
	}
	
	@Secured("ROLE_ADMIN")
	public abstract S getStatus();
	
}
