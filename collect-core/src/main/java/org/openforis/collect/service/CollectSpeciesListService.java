package org.openforis.collect.service;

import java.util.ArrayList;
import java.util.List;

import org.openforis.collect.manager.SpeciesManager;
import org.openforis.collect.manager.TaxonSearchParameters;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.CollectTaxonomy;
import org.openforis.idm.metamodel.ReferenceDataSchema.TaxonomyDefinition;
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
	public List<String> loadSpeciesListNames(Survey survey) {
		List<CollectTaxonomy> taxonomies = speciesManager.loadTaxonomiesBySurvey((CollectSurvey) survey);
		List<String> result = new ArrayList<String>(taxonomies.size());
		for (CollectTaxonomy taxonomy : taxonomies) {
			result.add(taxonomy.getName());
		}
		return result;
	}
	
	@Override
	public Object loadSpeciesListData(Survey survey, String taxonomyName, String attribute, String speciesCode) {
		CollectTaxonomy taxonomy = speciesManager.loadTaxonomyByName((CollectSurvey) survey, taxonomyName);
		TaxonSearchParameters parameters = new TaxonSearchParameters();
		parameters.setIncludeAncestorTaxons(true);
		List<TaxonOccurrence> result = speciesManager.findByCode(taxonomy, speciesCode, 1, parameters);
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
				TaxonomyDefinition taxonDefinition = survey.getReferenceDataSchema().getTaxonomyDefinition(taxonomyName);
				if (taxonDefinition == null) {
					throw new IllegalArgumentException("No reference data schema found for taxonomy: " + taxonomyName);
				} else {
					int attributeIndex = taxonDefinition.getAttributeNames().indexOf(attribute);
					if (attributeIndex < 0) {
						throw new IllegalArgumentException("Unsupported attribute: " + attribute);
					} else {
						return taxonOccurrence.getInfoAttribute(attributeIndex);
					}
				}
			}
		}
	}
	
	public void setSpeciesManager(SpeciesManager speciesManager) {
		this.speciesManager = speciesManager;
	}

}
