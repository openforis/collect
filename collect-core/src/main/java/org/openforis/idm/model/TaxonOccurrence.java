package org.openforis.idm.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openforis.idm.metamodel.TaxonAttributeDefinition;
import org.openforis.idm.model.species.Taxon;
import org.openforis.idm.model.species.Taxon.TaxonRank;
import org.openforis.idm.model.species.TaxonVernacularName;

/**
 * @author G. Miceli
 * @author M. Togna
 * @author S. Ricci
 */
public final class TaxonOccurrence extends AbstractValue {

	private Integer taxonId;
	private String code;
	private String scientificName;
	private String vernacularName;
	private String languageCode;
	private String languageVariety;
	private TaxonRank taxonRank;
	private List<String> infoAttributes = new ArrayList<String>();
	private List<TaxonOccurrence> ancestorTaxons;

	@Override
	@SuppressWarnings("serial")
	public Map<String, Object> toMap() {
		return new HashMap<String, Object>() {{
			put(TaxonAttributeDefinition.CODE_FIELD_NAME, code);
			put(TaxonAttributeDefinition.SCIENTIFIC_NAME_FIELD_NAME, scientificName);
			put(TaxonAttributeDefinition.VERNACULAR_NAME_FIELD_NAME, vernacularName);
			put(TaxonAttributeDefinition.LANGUAGE_CODE_FIELD_NAME, languageCode);
			put(TaxonAttributeDefinition.LANGUAGE_VARIETY_FIELD_NAME, languageVariety);
		}};
	}
	
	public TaxonOccurrence() {
		this((String) null, (String) null);
	}

	public TaxonOccurrence(String code, String scientificName) {
		this(null, code, scientificName);
	}

	public TaxonOccurrence(Integer taxonId, String code, String scientificName) {
		this(taxonId, code, scientificName, null, null, null);
	}
	
	public TaxonOccurrence(String code, String scientificName, String vernacularName, 
			String languageCode, String languageVariety) {
		this(null, code, scientificName, vernacularName, languageCode, languageVariety);
	}
	
	public TaxonOccurrence(TaxonOccurrence o) {
		this(o.code, o.scientificName, o.vernacularName,
				o.languageCode, o.languageVariety);
		this.taxonId = o.taxonId;
		this.taxonRank = o.taxonRank;
	}
	
	public TaxonOccurrence(Taxon taxon) {
		this(taxon, (TaxonVernacularName) null);
	}
	
	public TaxonOccurrence(Taxon taxon, TaxonVernacularName vernacularName) {
		this(taxon.getTaxonId(), taxon.getCode(), taxon.getScientificName());
		taxonRank = taxon.getTaxonRank();
		
		if (vernacularName != null) {
			this.vernacularName = vernacularName.getVernacularName();
			this.languageCode = vernacularName.getLanguageCode();
			this.languageVariety = vernacularName.getLanguageVariety();
		}
		
		this.infoAttributes = taxon.getInfoAttributes();
	}
	
	public TaxonOccurrence(Integer taxonId, String code, String scientificName, String vernacularName, 
			String languageCode, String languageVariety) {
		this.taxonId = taxonId;
		this.code = code;
		this.scientificName = scientificName;
		this.vernacularName = vernacularName;
		this.languageCode = languageCode;
		this.languageVariety = languageVariety;
		this.ancestorTaxons = new ArrayList<TaxonOccurrence>();
	}
	
	public TaxonOccurrence getAncestorTaxon(TaxonRank rank) {
		for (TaxonOccurrence ancestorTaxon : ancestorTaxons) {
			if (rank == ancestorTaxon.getTaxonRank()) {
				return ancestorTaxon;
			}
		}
		return null;
	}

	public void addAncestorTaxon(TaxonOccurrence ancestorTaxon) {
		ancestorTaxons.add(ancestorTaxon);
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

	public String getScientificName() {
		return scientificName;
	}
	
	public void setScientificName(String scientificName) {
		this.scientificName = scientificName;
	}

	public String getVernacularName() {
		return vernacularName;
	}
	
	public void setVernacularName(String vernacularName) {
		this.vernacularName = vernacularName;
	}

	public String getLanguageCode() {
		return languageCode;
	}
	
	public void setLanguageCode(String languageCode) {
		this.languageCode = languageCode;
	}

	public String getLanguageVariety() {
		return languageVariety;
	}
	
	public void setLanguageVariety(String languageVariety) {
		this.languageVariety = languageVariety;
	}
	
	public List<TaxonOccurrence> getAncestorTaxons() {
		return ancestorTaxons;
	}
	
	public void setAncestorTaxons(List<TaxonOccurrence> ancestorTaxons) {
		this.ancestorTaxons = ancestorTaxons;
	}

	public TaxonRank getTaxonRank() {
		return taxonRank;
	}
	
	public void setTaxonRank(TaxonRank taxonRank) {
		this.taxonRank = taxonRank;
	}
	
	public List<String> getInfoAttributes() {
		return infoAttributes;
	}

	public String getInfoAttribute(int index) {
		return infoAttributes.get(index);
	}

	public void setInfoAttributes(List<String> infos) {
		this.infoAttributes = infos;
	}
	
	@Override
	public String toPrettyFormatString() {
		return toInternalString();
	}
	
	@Override
	public String toInternalString() {
		return String.format("code:%s; scientific_name:%s;", code, scientificName);
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((ancestorTaxons == null) ? 0 : ancestorTaxons.hashCode());
		result = prime * result + ((code == null) ? 0 : code.hashCode());
		result = prime * result + ((infoAttributes == null) ? 0 : infoAttributes.hashCode());
		result = prime * result + ((languageCode == null) ? 0 : languageCode.hashCode());
		result = prime * result + ((languageVariety == null) ? 0 : languageVariety.hashCode());
		result = prime * result + ((scientificName == null) ? 0 : scientificName.hashCode());
		result = prime * result + ((taxonId == null) ? 0 : taxonId.hashCode());
		result = prime * result + ((taxonRank == null) ? 0 : taxonRank.hashCode());
		result = prime * result + ((vernacularName == null) ? 0 : vernacularName.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TaxonOccurrence other = (TaxonOccurrence) obj;
		if (ancestorTaxons == null) {
			if (other.ancestorTaxons != null)
				return false;
		} else if (!ancestorTaxons.equals(other.ancestorTaxons))
			return false;
		if (code == null) {
			if (other.code != null)
				return false;
		} else if (!code.equals(other.code))
			return false;
		if (infoAttributes == null) {
			if (other.infoAttributes != null)
				return false;
		} else if (!infoAttributes.equals(other.infoAttributes))
			return false;
		if (languageCode == null) {
			if (other.languageCode != null)
				return false;
		} else if (!languageCode.equals(other.languageCode))
			return false;
		if (languageVariety == null) {
			if (other.languageVariety != null)
				return false;
		} else if (!languageVariety.equals(other.languageVariety))
			return false;
		if (scientificName == null) {
			if (other.scientificName != null)
				return false;
		} else if (!scientificName.equals(other.scientificName))
			return false;
		if (taxonId == null) {
			if (other.taxonId != null)
				return false;
		} else if (!taxonId.equals(other.taxonId))
			return false;
		if (taxonRank != other.taxonRank)
			return false;
		if (vernacularName == null) {
			if (other.vernacularName != null)
				return false;
		} else if (!vernacularName.equals(other.vernacularName))
			return false;
		return true;
	}

}
