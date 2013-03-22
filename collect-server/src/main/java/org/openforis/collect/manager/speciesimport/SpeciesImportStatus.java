package org.openforis.collect.manager.speciesimport;

import org.openforis.collect.manager.referencedataimport.ParsingError;
import org.openforis.collect.manager.referencedataimport.ReferenceDataImportStatus;

/**
 * 
 * @author S. Ricci
 *
 */
public class SpeciesImportStatus extends ReferenceDataImportStatus<ParsingError> {
	
	private int taxonomyId;
	
	public SpeciesImportStatus(int taxonomyName) {
		super();
		this.taxonomyId = taxonomyName;
	}
	
	public int getTaxonomyId() {
		return taxonomyId;
	}
	
}