package org.openforis.collect.model.species;

/**
 * @author G. Miceli
 */
public class TaxonName {
	private Integer id;
	private String name;
	private String languageCode;
	private String languageVariety;
	private Integer taxonId;
	private int step;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
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

	public Integer getTaxonId() {
		return taxonId;
	}

	public void setTaxonId(Integer taxonId) {
		this.taxonId = taxonId;
	}

	public int getStep() {
		return step;
	}

	public void setStep(int step) {
		this.step = step;
	}
}
