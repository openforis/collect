/**
 * 
 */
package org.openforis.collect.remoting.service.speciesImport.proxy;

import java.util.List;

import org.granite.messaging.amf.io.util.externalizer.annotation.ExternalizedProperty;
import org.openforis.collect.manager.process.proxy.ProcessStatusProxy;
import org.openforis.collect.manager.speciesImport.SpeciesImportStatus;
import org.openforis.collect.manager.speciesImport.TaxonParsingError;

/**
 * @author S. Ricci
 *
 */
public class SpeciesImportStatusProxy extends ProcessStatusProxy {
	
	private transient SpeciesImportStatus status;

	public SpeciesImportStatusProxy(SpeciesImportStatus status) {
		super(status);
		this.status = status;
	}
	
	@ExternalizedProperty
	public List<TaxonParsingError> getErrors() {
		return status.getErrors();
	}

	@ExternalizedProperty
	public String getTaxonomyName() {
		return status.getTaxonomyName();
	}
	
}
