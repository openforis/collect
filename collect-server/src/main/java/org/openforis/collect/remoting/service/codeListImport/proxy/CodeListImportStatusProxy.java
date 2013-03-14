/**
 * 
 */
package org.openforis.collect.remoting.service.codeListImport.proxy;

import org.openforis.collect.manager.codeListImport.CodeListImportStatus;
import org.openforis.collect.manager.referenceDataImport.proxy.ReferenceDataImportStatusProxy;

/**
 * @author S. Ricci
 *
 */
public class CodeListImportStatusProxy extends ReferenceDataImportStatusProxy {
	
	public CodeListImportStatusProxy(CodeListImportStatus status) {
		super(status);
	}
	
}
