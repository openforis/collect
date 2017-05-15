package org.openforis.idm.metamodel;

import java.util.List;

public interface SpeciesListService {

	List<String> loadSpeciesListNames(Survey survey);
	
	Object loadSpeciesListData(Survey survey, String taxonomy, String attribute, String speciesCode);
	
}
