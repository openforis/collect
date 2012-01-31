package org.openforis.collect.model.species;

/**
 * @author G. Miceli
 */
public class Taxon {
	private Integer id;
	private Integer parentId;
	private Integer taxonomyId;
	private String scientificName;
	private String taxonomicRank;
	private int step;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Integer getTaxonomyId() {
		return taxonomyId;
	}

	public void setTaxonomyId(Integer taxonomyId) {
		this.taxonomyId = taxonomyId;
	}

	public String getScientificName() {
		return scientificName;
	}

	public void setScientificName(String scientificName) {
		this.scientificName = scientificName;
	}

	public String getTaxonomicRank() {
		return taxonomicRank;
	}

	public void setTaxonomicRank(String taxonomicRank) {
		this.taxonomicRank = taxonomicRank;
	}

	public int getStep() {
		return step;
	}

	public void setStep(int step) {
		this.step = step;
	}

	public Integer getParentId() {
		return parentId;
	}

	public void setParentId(Integer parentId) {
		this.parentId = parentId;
	}
}
