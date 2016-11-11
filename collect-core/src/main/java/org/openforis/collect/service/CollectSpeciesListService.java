package org.openforis.collect.service;

import java.util.List;

import org.openforis.collect.manager.SpeciesManager;
import org.openforis.collect.manager.TaxonSearchParameters;
import org.openforis.collect.model.CollectTaxonomy;
import org.openforis.idm.metamodel.SpeciesListService;
import org.openforis.idm.metamodel.Survey;
import org.openforis.idm.model.TaxonOccurrence;
import org.openforis.idm.model.species.Taxon.TaxonRank;
import org.springframework.beans.factory.annotation.Autowired;

public class CollectSpeciesListService implements SpeciesListService {
	
	public static final String SCIENTIFIC_NAME_ATTRIBUTE = "scientificName";
	public static final String FAMILY_ATTRIBUTE = "family";
	public static final String FAMILY_SCIENTIFIC_NAME_ATTRIBUTE = "familyScientificName";
	public static final String FAMILY_NAME_ATTRIBUTE = "familyName";
	
	@Autowired
	private SpeciesManager speciesManager;
	
	@Override
	public Object loadSpeciesListData(Survey survey, String taxonomyName, String attribute, String speciesCode) {
		CollectTaxonomy taxonomy = speciesManager.loadTaxonomyByName(survey.getId(), taxonomyName);
		TaxonSearchParameters parameters = new TaxonSearchParameters();
		parameters.setIncludeAncestorTaxons(true);
		List<TaxonOccurrence> result = speciesManager.findByCode(taxonomy.getId(), speciesCode, 1, parameters);
		if (result.isEmpty()) {
			return null;
		} else {
			TaxonOccurrence taxonOccurrence = result.get(0);
			if (FAMILY_ATTRIBUTE.equals(attribute) || 
					FAMILY_NAME_ATTRIBUTE.equals(attribute) ||
					FAMILY_SCIENTIFIC_NAME_ATTRIBUTE.equals(attribute)) {
				TaxonOccurrence familyTaxonOccurrence = taxonOccurrence.getAncestorTaxon(TaxonRank.FAMILY);
				if (familyTaxonOccurrence == null) {
					return null;
				} else {
					return familyTaxonOccurrence.getScientificName();
				}
			} else if (SCIENTIFIC_NAME_ATTRIBUTE.equals(attribute)) {
				return taxonOccurrence.getScientificName();
			} else {
				throw new IllegalArgumentException("Unsupported attribute: " + attribute);
			}
		}
	}
	
	public void setSpeciesManager(SpeciesManager speciesManager) {
		this.speciesManager = speciesManager;
	}

}
