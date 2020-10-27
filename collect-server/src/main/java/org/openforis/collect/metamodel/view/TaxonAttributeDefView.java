package org.openforis.collect.metamodel.view;

import java.util.List;

import org.openforis.collect.designer.metamodel.AttributeType;
import org.openforis.idm.model.species.Taxon.TaxonRank;

public class TaxonAttributeDefView extends AttributeDefView {

	private String taxonomyName;
	private TaxonRank highestRank;
	private boolean showFamily;
	private boolean includeUniqueVernacularName;
	private boolean allowUnlisted;

	public TaxonAttributeDefView(int id, String name, String label, AttributeType type, List<String> fieldNames,
			boolean key, boolean multiple, boolean showInRecordSummaryList, boolean qualifier) {
		super(id, name, label, type, fieldNames, key, multiple, showInRecordSummaryList, qualifier);
	}

	public String getTaxonomyName() {
		return taxonomyName;
	}
	
	public void setTaxonomyName(String taxonomyName) {
		this.taxonomyName = taxonomyName;
	}

	public TaxonRank getHighestRank() {
		return highestRank;
	}
	
	public void setHighestRank(TaxonRank highestRank) {
		this.highestRank = highestRank;
	}
	
	public boolean isShowFamily() {
		return showFamily;
	}
	
	public void setShowFamily(boolean showFamily) {
		this.showFamily = showFamily;
	}

	public boolean isIncludeUniqueVernacularName() {
		return includeUniqueVernacularName;
	}
	
	public void setIncludeUniqueVernacularName(boolean includeUniqueVernacularName) {
		this.includeUniqueVernacularName = includeUniqueVernacularName;
	}
	
	public boolean isAllowUnlisted() {
		return allowUnlisted;
	}
	
	public void setAllowUnlisted(boolean allowUnlisted) {
		this.allowUnlisted = allowUnlisted;
	}
}
