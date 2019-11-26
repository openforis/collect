/**
 * 
 */
package org.openforis.collect.metamodel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.lang3.StringUtils;
import org.openforis.commons.collection.CollectionUtils;
import org.openforis.idm.metamodel.ReferenceDataSchema.ReferenceDataDefinition.Attribute;
import org.openforis.idm.metamodel.ReferenceDataSchema.TaxonomyDefinition;
import org.openforis.idm.model.species.Taxon;
import org.openforis.idm.model.species.Taxon.TaxonRank;
import org.openforis.idm.model.species.TaxonVernacularName;

/**
 * @author S. Ricci
 *
 */
public class TaxonSummary {

	private static final String SYNONYM_VERNACULAR_LANG_CODE = "lat";
	private static final String VERNACULAR_NAMES_SEPARATOR = ", ";

	private long taxonSystemId;
	private Long parentSystemId;
	private Integer taxonId;
	private String code;
	private String familyName;
	private String scientificName;
	private TaxonRank rank;
	private Map<String, List<String>> languageToVernacularNames;
	private Map<String, String> infoByName = new HashMap<String, String>();
	
	public TaxonSummary() {
	}
	
	public TaxonSummary(TaxonomyDefinition taxonDefinition, Taxon taxon, List<TaxonVernacularName> vernacularNames, Taxon familyTaxon) {
		this.setCode(taxon.getCode());
		this.setRank(taxon.getTaxonRank());
		this.setParentSystemId(taxon.getParentId());
		this.setScientificName(taxon.getScientificName());
		this.setTaxonId(taxon.getTaxonId());
		this.setTaxonSystemId(taxon.getSystemId());
		
		for (TaxonVernacularName taxonVernacularName : vernacularNames) {
			//if lang code is blank, vernacular name will be considered as synonym
			String languageCode = StringUtils.trimToEmpty(taxonVernacularName.getLanguageCode());
			this.addVernacularName(languageCode, taxonVernacularName.getVernacularName());
		}
		
		List<Attribute> taxonAttributes = taxonDefinition.getAttributes();
		for (int i = 0; i < taxonAttributes.size(); i++) {
			Attribute taxonAttr = taxonAttributes.get(i);
			this.addInfo(taxonAttr.getName(), taxon.getInfoAttribute(i));
		}
		
		if ( familyTaxon != null ) {
			this.setFamilyName(familyTaxon.getScientificName());
		}
	}
	
	public List<String> getVernacularLanguages() {
		if ( languageToVernacularNames == null ) {
			return Collections.emptyList();
		} else {
			Set<String> langCodes = languageToVernacularNames.keySet();
			return CollectionUtils.unmodifiableList(new ArrayList<String>(langCodes));
		}
	}
	
	public void addVernacularName(String langCode, String vernacularName) {
		if ( languageToVernacularNames == null ) {
			languageToVernacularNames = new TreeMap<String, List<String>>();
		}
		List<String> vernacularNames = languageToVernacularNames.get(langCode);
		if ( vernacularNames == null ) {
			vernacularNames = new ArrayList<String>();
			languageToVernacularNames.put(langCode, vernacularNames);
		}
		vernacularNames.add(vernacularName);
	}
	
	public List<String> getVernacularNames(String langCode) {
		if ( languageToVernacularNames == null ) {
			return Collections.emptyList();
		} else {
			List<String> names = languageToVernacularNames.get(langCode);
			return CollectionUtils.unmodifiableList(names);
		}
	}
	
	public Map<String, List<String>> getLanguageToVernacularNames() {
		if ( languageToVernacularNames == null ) {
			return Collections.emptyMap();
		} else {
			return Collections.unmodifiableMap(languageToVernacularNames);
		}
	}
	
	public String getJointVernacularNames(String langCode) {
		return getJointVernacularNames(langCode, VERNACULAR_NAMES_SEPARATOR);
	}
	
	public String getJointVernacularNames(String langCode, String separator) {
		List<String> vernacularNames = getVernacularNames(langCode);
		String jointVernacularNames = StringUtils.join(vernacularNames, separator);
		return jointVernacularNames;
	}
	
	public List<String> getSynonyms() {
		List<String> synonyms1 = getVernacularNames("");
		List<String> synonyms2 = getVernacularNames(SYNONYM_VERNACULAR_LANG_CODE);
		List<String> synonyms = new ArrayList<String>();
		synonyms.addAll(synonyms1);
		synonyms.addAll(synonyms2);
		return synonyms;
	}
	
	public String getJointSynonyms() {
		return getJointSynonyms(VERNACULAR_NAMES_SEPARATOR);
	}
	
	public String getJointSynonyms(String separator) {
		List<String> synonyms = getSynonyms();
		return StringUtils.join(synonyms, separator);
	}
	
	public String getInfo(String name) {
		return infoByName.get(name);
	}

	public void addInfo(String name, String value) {
		infoByName.put(name, value);
	}
	
	public void removeInfo(String name) {
		infoByName.remove(name);
	}
	
	public Map<String, String> getInfoByName() {
		return infoByName;
	}
	
	public long getTaxonSystemId() {
		return taxonSystemId;
	}
	
	public void setTaxonSystemId(long taxonSystemId) {
		this.taxonSystemId = taxonSystemId;
	}
	
	public Long getParentSystemId() {
		return parentSystemId;
	}

	public void setParentSystemId(Long parentSystemId) {
		this.parentSystemId = parentSystemId;
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
	
	public String getFamilyName() {
		return familyName;
	}
	
	public void setFamilyName(String familyName) {
		this.familyName = familyName;
	}
	
	public String getScientificName() {
		return scientificName;
	}
	
	public void setScientificName(String scientificName) {
		this.scientificName = scientificName;
	}
	
	public TaxonRank getRank() {
		return rank;
	}
	
	public void setRank(TaxonRank rank) {
		this.rank = rank;
	}

}
