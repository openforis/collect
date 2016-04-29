/**
 * 
 */
package org.openforis.collect.designer.form;

import java.util.ArrayList;
import java.util.List;

import org.openforis.collect.metamodel.CollectAnnotations;
import org.openforis.collect.metamodel.CollectAnnotations.Annotation;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.TaxonAttributeDefinition;
import org.openforis.idm.model.species.Taxon.TaxonRank;

/**
 * @author S. Ricci
 *
 */
public class TaxonAttributeDefinitionFormObject extends AttributeDefinitionFormObject<TaxonAttributeDefinition> {

	private String taxonomy;
	private String highestRank;
	private List<String> qualifiers;
	private Boolean includeUniqueVernacularName;
	private Boolean showFamily;
	
	TaxonAttributeDefinitionFormObject() {
	}
	
	TaxonAttributeDefinitionFormObject(EntityDefinition parentDefn) {
		super(parentDefn);
		includeUniqueVernacularName = (Boolean) Annotation.TAXON_ATTRIBUTE_INCLUDE_UNIQUE_VERNACULAR_NAME.getDefaultValue();
		showFamily = (Boolean) Annotation.TAXON_ATTRIBUTE_SHOW_FAMILY.getDefaultValue();
	}

	@Override
	public void loadFrom(TaxonAttributeDefinition source, String languageCode) {
		super.loadFrom(source, languageCode);
		taxonomy = source.getTaxonomy();
		highestRank = source.getHighestTaxonRank() == null ? null: source.getHighestTaxonRank().getName();
		qualifiers = new ArrayList<String>(source.getQualifiers());
		
		CollectSurvey survey = (CollectSurvey) source.getSurvey();
		CollectAnnotations annotations = survey.getAnnotations();
		showFamily = annotations.isShowFamily(source);
		includeUniqueVernacularName = annotations.isIncludeUniqueVernacularName(source);
	}

	@Override
	public void saveTo(TaxonAttributeDefinition dest, String languageCode) {
		super.saveTo(dest, languageCode);
		dest.setTaxonomy(taxonomy);
		dest.setHighestTaxonRank(TaxonRank.fromName(highestRank));
		dest.setQualifiers(qualifiers);
		
		CollectSurvey survey = (CollectSurvey) dest.getSurvey();
		CollectAnnotations annotations = survey.getAnnotations();
		annotations.setShowFamily(dest, showFamily);
		annotations.setIncludeUniqueVernacularName(dest, includeUniqueVernacularName);
	}

	public String getTaxonomy() {
		return taxonomy;
	}

	public void setTaxonomy(String taxonomy) {
		this.taxonomy = taxonomy;
	}

	public String getHighestRank() {
		return highestRank;
	}

	public void setHighestRank(String highestRank) {
		this.highestRank = highestRank;
	}

	public List<String> getQualifiers() {
		return qualifiers;
	}

	public void setQualifiers(List<String> qualifiers) {
		this.qualifiers = qualifiers;
	}

	public Boolean getIncludeUniqueVernacularName() {
		return includeUniqueVernacularName;
	}

	public void setIncludeUniqueVernacularName(Boolean includeUniqueVernacularName) {
		this.includeUniqueVernacularName = includeUniqueVernacularName;
	}
	
	public Boolean getShowFamily() {
		return showFamily;
	}
	
	public void setShowFamily(Boolean showFamily) {
		this.showFamily = showFamily;
	}
	
}
