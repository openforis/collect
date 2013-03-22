/**
 * 
 */
package org.openforis.collect.remoting.service.codelistimport.proxy;

import org.openforis.collect.manager.codelistimport.CodeListImportStatus;
import org.openforis.collect.manager.referencedataimport.proxy.ReferenceDataImportStatusProxy;


/**
 * @author S. Ricci
 *
 */
public class CodeListImportStatusProxy extends ReferenceDataImportStatusProxy {
	
	public CodeListImportStatusProxy(CodeListImportStatus status) {
		super(status);
	}
	
}
