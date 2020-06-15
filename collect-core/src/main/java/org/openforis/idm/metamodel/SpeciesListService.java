package org.openforis.idm.metamodel;

import java.util.List;

import org.openforis.idm.model.species.Taxon;
import org.openforis.idm.model.species.Taxonomy;

public interface SpeciesListService {

	Taxonomy loadTaxonomyByName(Survey survey, String name);
	
	List<String> loadSpeciesListNames(Survey survey);
	
	Object loadSpeciesListData(Survey survey, String taxonomy, String attribute, String speciesCode);
	
	Taxon loadTaxonByCode(Survey survey, String taxonomy, String code);
	
}
