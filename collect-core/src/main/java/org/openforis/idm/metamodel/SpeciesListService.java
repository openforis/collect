package org.openforis.idm.metamodel;

public interface SpeciesListService {

	Object loadSpeciesListData(Survey survey, String taxonomy, String attribute, String speciesCode);
	
}
