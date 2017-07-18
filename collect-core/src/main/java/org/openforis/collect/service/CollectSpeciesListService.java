package org.openforis.collect.service;

import java.util.ArrayList;
import java.util.List;

import org.openforis.collect.manager.SpeciesManager;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.CollectTaxonomy;
import org.openforis.collect.model.TaxonTree;
import org.openforis.collect.model.TaxonTree.Node;
import org.openforis.idm.metamodel.Languages;
import org.openforis.idm.metamodel.Languages.Standard;
import org.openforis.idm.metamodel.ReferenceDataSchema.TaxonomyDefinition;
import org.openforis.idm.metamodel.SpeciesListService;
import org.openforis.idm.metamodel.Survey;
import org.openforis.idm.model.species.Taxon;
import org.openforis.idm.model.species.Taxon.TaxonRank;
import org.openforis.idm.model.species.TaxonVernacularName;
import org.springframework.beans.factory.annotation.Autowired;

public class CollectSpeciesListService implements SpeciesListService {
	
	public static final String CODE_ATTRIBUTE = "code";
	public static final String FAMILY_ATTRIBUTE = "family";
	public static final String FAMILY_SCIENTIFIC_NAME_ATTRIBUTE = "familyScientificName";
	public static final String FAMILY_NAME_ATTRIBUTE = "familyName";
	public static final String GENUS_ATTRIBUTE = "genus";
	public static final String SCIENTIFIC_NAME_ATTRIBUTE = "scientificName";
	
	public static final String[] GENERIC_ATTRIBUTES = new String[]{
		CODE_ATTRIBUTE, FAMILY_ATTRIBUTE, FAMILY_NAME_ATTRIBUTE, FAMILY_SCIENTIFIC_NAME_ATTRIBUTE, GENUS_ATTRIBUTE, SCIENTIFIC_NAME_ATTRIBUTE
	};
	
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
		TaxonTree taxonTree = speciesManager.loadTaxonTree(taxonomy);
		Taxon taxon = taxonTree.findTaxonByCode(speciesCode);
		if (taxon == null) {
			return null;
		} else {
			Node taxonNode = taxonTree.getNodeByTaxonId(taxon.getTaxonId());
			if (FAMILY_ATTRIBUTE.equals(attribute) || 
					FAMILY_NAME_ATTRIBUTE.equals(attribute) ||
					FAMILY_SCIENTIFIC_NAME_ATTRIBUTE.equals(attribute)) {
				//family name
				Node familyNode = taxonNode.getAncestor(TaxonRank.FAMILY);
				return familyNode == null ? null : familyNode.getTaxon().getScientificName();
			} else if (GENUS_ATTRIBUTE.equals(attribute)) {
				Node genusNode = taxonNode.getAncestor(TaxonRank.GENUS);
				return genusNode == null ? null : genusNode.getTaxon().getScientificName();
			} else if (SCIENTIFIC_NAME_ATTRIBUTE.equals(attribute)) {
				return taxon.getScientificName();
			} else if (CODE_ATTRIBUTE.equals(attribute)) {
				return taxon.getCode();
			} else if (Languages.exists(Standard.ISO_639_3, attribute)) {
				TaxonVernacularName vernacularName = taxonNode.getVernacularName(attribute);
				return vernacularName == null ? null : vernacularName.getVernacularName();
			} else {
				//info (extra) attribute
				TaxonomyDefinition taxonDefinition = survey.getReferenceDataSchema().getTaxonomyDefinition(taxonomyName);
				if (taxonDefinition == null) {
					throw new IllegalArgumentException("No reference data schema found for taxonomy: " + taxonomyName);
				} else {
					int attributeIndex = taxonDefinition.getAttributeNames().indexOf(attribute);
					if (attributeIndex < 0) {
						throw new IllegalArgumentException("Unsupported attribute: " + attribute);
					} else {
						return taxon.getInfoAttribute(attributeIndex);
					}
				}
			}
		}
	}
	
	public void setSpeciesManager(SpeciesManager speciesManager) {
		this.speciesManager = speciesManager;
	}

}
