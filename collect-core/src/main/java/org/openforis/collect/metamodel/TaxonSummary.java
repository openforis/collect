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

import org.openforis.idm.model.species.Taxon.TaxonRank;
import org.openforis.idm.util.CollectionUtil;

/**
 * @author riccist
 *
 */
public class TaxonSummary {

	private int taxonSystemId;
	private Integer taxonId;
	private String code;
	private String scientificName;
	private TaxonRank rank;
	private Map<String, List<String>> languageToVernacularNames;
	
	public List<String> getVernacularLanguages() {
		if ( languageToVernacularNames == null ) {
			return null;
		} else {
			Set<String> langCodes = languageToVernacularNames.keySet();
			return CollectionUtil.unmodifiableList(new ArrayList<String>(langCodes));
		}
	}
	
	public void addVernacularName(String langCode, String vernacularName) {
		if ( languageToVernacularNames == null ) {
			languageToVernacularNames = new HashMap<String, List<String>>();
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
			return CollectionUtil.unmodifiableList(names);
		}
	}
	
	public Map<String, List<String>> getLanguageToVernacularNames() {
		if ( languageToVernacularNames == null ) {
			return Collections.emptyMap();
		} else {
			return Collections.unmodifiableMap(languageToVernacularNames);
		}
	}
	
	public int getTaxonSystemId() {
		return taxonSystemId;
	}
	
	public void setTaxonSystemId(int taxonSystemId) {
		this.taxonSystemId = taxonSystemId;
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
