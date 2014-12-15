package org.openforis.collect.manager.speciesimport;

import org.openforis.collect.io.ReferenceDataImportStatus;
import org.openforis.collect.io.metadata.parsing.ParsingError;

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