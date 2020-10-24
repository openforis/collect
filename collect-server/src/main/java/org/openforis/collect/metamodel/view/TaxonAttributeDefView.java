package org.openforis.collect.metamodel.view;

import java.util.List;

import org.openforis.collect.designer.metamodel.AttributeType;
import org.openforis.idm.model.species.Taxon.TaxonRank;

public class TaxonAttributeDefView extends AttributeDefView {

	private String taxonomyName;
	private TaxonRank highestRank;
	private boolean codeVisible;
	private boolean scientificNameVisible;
	private boolean vernacularNameVisible;
	private boolean languageCodeVisible;
	private boolean languageVarietyVisible;
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
	
	public boolean isCodeVisible() {
		return codeVisible;
	}
	
	public void setCodeVisible(boolean codeVisible) {
		this.codeVisible = codeVisible;
	}

	public boolean isScientificNameVisible() {
		return scientificNameVisible;
	}
	
	public void setScientificNameVisible(boolean scientificNameVisible) {
		this.scientificNameVisible = scientificNameVisible;
	}

	public boolean isVernacularNameVisible() {
		return vernacularNameVisible;
	}
	
	public void setVernacularNameVisible(boolean vernacularNameVisible) {
		this.vernacularNameVisible = vernacularNameVisible;
	}

	public boolean isLanguageCodeVisible() {
		return languageCodeVisible;
	}
	
	public void setLanguageCodeVisible(boolean languageCodeVisible) {
		this.languageCodeVisible = languageCodeVisible;
	}

	public boolean isLanguageVarietyVisible() {
		return languageVarietyVisible;
	}
	
	public void setLanguageVarietyVisible(boolean languageVarietyVisible) {
		this.languageVarietyVisible = languageVarietyVisible;
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
