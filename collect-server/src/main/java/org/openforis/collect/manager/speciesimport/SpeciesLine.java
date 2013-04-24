package org.openforis.collect.manager.speciesimport;

import java.util.List;
import java.util.Map;

import org.openforis.collect.manager.referencedataimport.Line;
import org.openforis.idm.model.species.Taxon.TaxonRank;

/**
 * 
 * @author S. Ricci
 *
 */
public class SpeciesLine extends Line {
	
	private Integer taxonId;
	private String code;
	private TaxonRank rank;
	private String familyName;
	private String genus;
	private String speciesName;
	private String rawScientificName;
	private String canonicalScientificName;
	private Map<String, List<String>> languageToVernacularNames;

	public List<String> getVernacularNames(String langCode) {
		if ( languageToVernacularNames != null ) {
			return languageToVernacularNames.get(langCode);
		} else {
			return null;
		}
	}

	public Integer getTaxonId() {
		return taxonId;
	}

	public void setTaxonId(Integer taxonId) {
		this.taxonId = taxonId;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public TaxonRank getRank() {
		return rank;
	}

	public void setRank(TaxonRank rank) {
		this.rank = rank;
	}

	public String getFamilyName() {
		return familyName;
	}

	public void setFamilyName(String familyName) {
		this.familyName = familyName;
	}

	public String getGenus() {
		return genus;
	}

	public void setGenus(String genus) {
		this.genus = genus;
	}

	public String getSpeciesName() {
		return speciesName;
	}

	public void setSpeciesName(String speciesName) {
		this.speciesName = speciesName;
	}
	
	public String getRawScientificName() {
		return rawScientificName;
	}

	public void setRawScientificName(String rawScientificName) {
		this.rawScientificName = rawScientificName;
	}

	public String getCanonicalScientificName() {
		return canonicalScientificName;
	}

	public void setCanonicalScientificName(String canonicalScientificName) {
		this.canonicalScientificName = canonicalScientificName;
	}

	public Map<String, List<String>> getLanguageToVernacularNames() {
		return languageToVernacularNames;
	}

	public void setLanguageToVernacularNames(
			Map<String, List<String>> languageToVernacularNames) {
		this.languageToVernacularNames = languageToVernacularNames;
	}

}