/**
 * 
 */
package org.openforis.collect.remoting.service.speciesimport.proxy;

import org.granite.messaging.amf.io.util.externalizer.annotation.ExternalizedProperty;
import org.openforis.collect.manager.referencedataimport.proxy.ReferenceDataImportStatusProxy;
import org.openforis.collect.manager.speciesimport.SpeciesImportStatus;

/**
 * @author S. Ricci
 *
 */
public class SpeciesImportStatusProxy extends ReferenceDataImportStatusProxy {
	
	private transient SpeciesImportStatus speciesImportStatus;
	
	public SpeciesImportStatusProxy(SpeciesImportStatus status) {
		super(status);
		this.speciesImportStatus = status;
	}
	
	@ExternalizedProperty
	public int getTaxonomyId() {
		return speciesImportStatus.getTaxonomyId();
	}
	
}
