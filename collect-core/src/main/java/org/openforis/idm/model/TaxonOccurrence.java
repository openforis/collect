package org.openforis.idm.model;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.openforis.idm.model.species.Taxon;
import org.openforis.idm.model.species.TaxonVernacularName;

/**
 * @author G. Miceli
 * @author M. Togna
 * @author S. Ricci
 */
public final class TaxonOccurrence implements Value {

	private Integer taxonId;
	private String code;
	private String scientificName;
	private String vernacularName;
	private String languageCode;
	private String languageVariety;

	public TaxonOccurrence() {
		super();
	}

	public TaxonOccurrence(String code, String scientificName) {
		super();
		this.code = code;
		this.scientificName = scientificName;
	}

	public TaxonOccurrence(Integer taxonId, String code, String scientificName) {
		super();
		this.taxonId = taxonId;
		this.code = code;
		this.scientificName = scientificName;
	}
	
	public TaxonOccurrence(String code, String scientificName, String vernacularName, 
			String languageCode, String languageVariety) {
		super();
		this.code = code;
		this.scientificName = scientificName;
		this.vernacularName = vernacularName;
		this.languageCode = languageCode;
		this.languageVariety = languageVariety;
	}
	
	public TaxonOccurrence(Integer taxonId, String code, String scientificName, String vernacularName, 
			String languageCode, String languageVariety) {
		super();
		this.taxonId = taxonId;
		this.code = code;
		this.scientificName = scientificName;
		this.vernacularName = vernacularName;
		this.languageCode = languageCode;
		this.languageVariety = languageVariety;
	}

	public TaxonOccurrence(Taxon taxon, TaxonVernacularName vernacularName) {
		this(taxon.getTaxonId(), taxon.getCode(), taxon.getScientificName(), 
					vernacularName.getVernacularName(), vernacularName.getLanguageCode(),
					vernacularName.getLanguageVariety());
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

	public String getVernacularName() {
		return vernacularName;
	}

	public String getLanguageCode() {
		return languageCode;
	}

	public String getLanguageVariety() {
		return languageVariety;
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
			.append("taxonId", taxonId)
			.append("code", code)
			.append("scientificName", scientificName)
			.append("vernacularName", vernacularName)
			.append("languageCode", languageCode)
			.append("languageVariety", languageVariety)
			.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((code == null) ? 0 : code.hashCode());
		result = prime * result
				+ ((languageCode == null) ? 0 : languageCode.hashCode());
		result = prime * result
				+ ((languageVariety == null) ? 0 : languageVariety.hashCode());
		result = prime * result
				+ ((scientificName == null) ? 0 : scientificName.hashCode());
		result = prime * result + ((taxonId == null) ? 0 : taxonId.hashCode());
		result = prime * result
				+ ((vernacularName == null) ? 0 : vernacularName.hashCode());
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
		if (code == null) {
			if (other.code != null)
				return false;
		} else if (!code.equals(other.code))
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
		if (vernacularName == null) {
			if (other.vernacularName != null)
				return false;
		} else if (!vernacularName.equals(other.vernacularName))
			return false;
		return true;
	}

}
