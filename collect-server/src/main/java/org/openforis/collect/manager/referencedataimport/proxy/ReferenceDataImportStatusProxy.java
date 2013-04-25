/**
 * 
 */
package org.openforis.collect.manager.referencedataimport.proxy;

import java.util.List;

import org.granite.messaging.amf.io.util.externalizer.annotation.ExternalizedProperty;
import org.openforis.collect.manager.process.proxy.ProcessStatusProxy;
import org.openforis.collect.manager.referencedataimport.ParsingError;
import org.openforis.collect.manager.referencedataimport.ReferenceDataImportStatus;

/**
 * @author S. Ricci
 *
 */
public class ReferenceDataImportStatusProxy extends ProcessStatusProxy {
	
	private transient ReferenceDataImportStatus<ParsingError> status;

	public ReferenceDataImportStatusProxy(ReferenceDataImportStatus<ParsingError> status) {
		super(status);
		this.status = status;
	}
	
	@ExternalizedProperty
	public List<ParsingErrorProxy> getErrors() {
		List<ParsingError> errors = status.getErrors();
		return ParsingErrorProxy.fromList(errors);
	}

}
