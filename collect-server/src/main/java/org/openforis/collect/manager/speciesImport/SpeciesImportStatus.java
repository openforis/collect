package org.openforis.collect.manager.speciesImport;

import org.openforis.collect.manager.referenceDataImport.ParsingError;
import org.openforis.collect.manager.referenceDataImport.ReferenceDataImportStatus;

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