package org.openforis.idm.model.species;

/**
 * @author G. Miceli
 * @author M. Togna
 * @author S. Ricci
 */
public class Taxon {

	public enum TaxonRank {
		FAMILY		("family"), 
		GENUS		("genus"), 
		SERIES		("series"), 
		SPECIES		("species"), 
		SUBSPECIES	("subspecies"), 
		VARIETY		("variety"), 
		FORM		("form");
		
		private final String name;

		private TaxonRank(String name) {
			this.name = name;
		}

		public static TaxonRank fromName(String name) {
			return fromName(name, false);
		}
		
		public static TaxonRank fromName(String name, boolean ignoreCase) {
			TaxonRank[] values = values();
			for (TaxonRank taxonRank : values) {
				boolean match;
				String currentRankName = taxonRank.getName();
				if ( ignoreCase ) {
					match = currentRankName.equalsIgnoreCase(name);
				} else {
					match = currentRankName.equals(name);
				}
				if ( match ) {
					return taxonRank;
				}
			}
			return null;
		}
		
		public static String[] names() {
			TaxonRank[] values = values();
			String[] result = new String[values.length];
			for (int i = 0; i < values.length; i++) {
				TaxonRank taxonRank = values[i];
				result[i] = taxonRank.getName();
			}
			return result;
		}
		
		public TaxonRank getParent() {
			switch (this) {
			case FORM:
			case VARIETY:
				return SPECIES;
			case SUBSPECIES:
				return SPECIES;
			case SPECIES:
				return GENUS;
			case GENUS:
			case SERIES:
				return FAMILY;
			default:
				return null;
			}
		}
		
		public TaxonRank getChild() {
			switch (this) {
			case FAMILY:
				return GENUS;
			case GENUS:
				return SPECIES;
			case SPECIES:
				return SUBSPECIES;
			case SUBSPECIES:
				return VARIETY;
			case VARIETY:
				return null;
			default:
				return null;
			}
		}
		
		public String getName() {
			return name;
		}
		
	}
	
	private Integer systemId;
	private Integer taxonId;
	private Integer parentId;
	private Integer taxonomyId;
	private String code;
	private String scientificName;
	private TaxonRank taxonRank;
	private int step;

	public Integer getSystemId() {
		return systemId;
	}

	public void setSystemId(Integer systemId) {
		this.systemId = systemId;
	}

	public Integer getTaxonId() {
		return taxonId;
	}

	public void setTaxonId(Integer taxonId) {
		this.taxonId = taxonId;
	}

	public Integer getTaxonomyId() {
		return taxonomyId;
	}

	public void setTaxonomyId(Integer taxonomyId) {
		this.taxonomyId = taxonomyId;
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

	public TaxonRank getTaxonRank() {
		return taxonRank;
	}

	public void setTaxonRank(TaxonRank taxonRank) {
		this.taxonRank = taxonRank;
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
