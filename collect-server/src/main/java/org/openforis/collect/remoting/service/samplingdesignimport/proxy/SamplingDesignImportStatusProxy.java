/**
 * 
 */
package org.openforis.collect.remoting.service.samplingdesignimport.proxy;

import org.openforis.collect.io.metadata.samplingdesign.SamplingDesignImportStatus;
import org.openforis.collect.manager.referencedataimport.proxy.ReferenceDataImportStatusProxy;

/**
 * @author S. Ricci
 *
 */
public class SamplingDesignImportStatusProxy extends ReferenceDataImportStatusProxy {
	
	public SamplingDesignImportStatusProxy(SamplingDesignImportStatus status) {
		super(status);
	}
	
}
