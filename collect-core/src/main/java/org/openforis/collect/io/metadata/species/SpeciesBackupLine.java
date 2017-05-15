package org.openforis.collect.io.metadata.species;

import java.util.List;
import java.util.Map;

import org.openforis.collect.io.metadata.parsing.ReferenceDataLine;
import org.openforis.idm.model.species.Taxon.TaxonRank;

/**
 * 
 * @author S. Ricci
 *
 */
public class SpeciesBackupLine extends ReferenceDataLine {
	
	private Integer Id;
	private Integer parentId;
	private TaxonRank rank;
	private Integer no;
	private String code;
	private String scientificName;
	private Map<String, List<String>> languageToVernacularNames;

	public List<String> getVernacularNames(String langCode) {
		if ( languageToVernacularNames != null ) {
			return languageToVernacularNames.get(langCode);
		} else {
			return null;
		}
	}

	public Integer getId() {
		return Id;
	}
	
	public void setId(Integer id) {
		Id = id;
	}
	
	public Integer getParentId() {
		return parentId;
	}
	
	public void setParentId(Integer parentId) {
		this.parentId = parentId;
	}
	
	public TaxonRank getRank() {
		return rank;
	}
	
	public void setRank(TaxonRank rank) {
		this.rank = rank;
	}
	
	public Integer getNo() {
		return no;
	}
	
	public void setNo(Integer no) {
		this.no = no;
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
	
	public Map<String, List<String>> getLanguageToVernacularNames() {
		return languageToVernacularNames;
	}

	public void setLanguageToVernacularNames(
			Map<String, List<String>> languageToVernacularNames) {
		this.languageToVernacularNames = languageToVernacularNames;
	}

}