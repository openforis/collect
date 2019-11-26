package org.openforis.idm.model.species;

import java.util.ArrayList;
import java.util.List;

import org.openforis.commons.collection.CollectionUtils;

/**
 * @author G. Miceli
 * @author M. Togna
 * @author E. Wibowo
 */
public class TaxonVernacularName {

	private Long id;
	private String vernacularName;
	private String languageCode;
	private String languageVariety;
	private Long taxonSystemId;
	private int step;
	private List<String> qualifiers;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getVernacularName() {
		return vernacularName;
	}

	public void setVernacularName(String name) {
		this.vernacularName = name;
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

	public Long getTaxonSystemId() {
		return taxonSystemId;
	}

	public void setTaxonSystemId(Long taxonId) {
		this.taxonSystemId = taxonId;
	}

	public int getStep() {
		return step;
	}

	public void setStep(int step) {
		this.step = step;
	}

	public List<String> getQualifiers() {
		return CollectionUtils.unmodifiableList(qualifiers);
	}

	public void setQualifiers(List<String> qualifiers) {
		this.qualifiers = new ArrayList<String>(qualifiers);
	}
}
