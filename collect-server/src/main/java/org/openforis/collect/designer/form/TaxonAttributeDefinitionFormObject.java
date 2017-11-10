/**
 * 
 */
package org.openforis.collect.designer.form;

import static org.openforis.idm.metamodel.TaxonAttributeDefinition.CODE_FIELD_NAME;
import static org.openforis.idm.metamodel.TaxonAttributeDefinition.FAMILY_CODE_FIELD_NAME;
import static org.openforis.idm.metamodel.TaxonAttributeDefinition.FAMILY_SCIENTIFIC_NAME_FIELD_NAME;
import static org.openforis.idm.metamodel.TaxonAttributeDefinition.LANGUAGE_CODE_FIELD_NAME;
import static org.openforis.idm.metamodel.TaxonAttributeDefinition.LANGUAGE_VARIETY_FIELD_NAME;
import static org.openforis.idm.metamodel.TaxonAttributeDefinition.SCIENTIFIC_NAME_FIELD_NAME;
import static org.openforis.idm.metamodel.TaxonAttributeDefinition.VERNACULAR_NAME_FIELD_NAME;

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
	private String codeFieldLabel;
	private String scientificNameFieldLabel;
	private String vernacularNameFieldLabel;
	private String languageCodeFieldLabel;
	private String languageVarietyFieldLabel;
	private String familyCodeFieldLabel;
	private String familyNameFieldLabel;
	private boolean allowUnlisted;
	
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
		allowUnlisted = annotations.isAllowUnlisted(source);
		
		codeFieldLabel = source.getFieldLabel(CODE_FIELD_NAME, languageCode);
		scientificNameFieldLabel = source.getFieldLabel(SCIENTIFIC_NAME_FIELD_NAME, languageCode);
		vernacularNameFieldLabel = source.getFieldLabel(VERNACULAR_NAME_FIELD_NAME, languageCode);
		languageCodeFieldLabel = source.getFieldLabel(LANGUAGE_CODE_FIELD_NAME, languageCode);
		languageVarietyFieldLabel = source.getFieldLabel(LANGUAGE_VARIETY_FIELD_NAME, languageCode);
		familyCodeFieldLabel = source.getFieldLabel(FAMILY_CODE_FIELD_NAME, languageCode);
		familyNameFieldLabel = source.getFieldLabel(FAMILY_SCIENTIFIC_NAME_FIELD_NAME, languageCode);
	}

	@Override
	public void saveTo(TaxonAttributeDefinition dest, String languageCode) {
		super.saveTo(dest, languageCode);
		dest.setTaxonomy(taxonomy);
		dest.setHighestTaxonRank(TaxonRank.fromName(highestRank));
		dest.setQualifiers(qualifiers);
		dest.setFieldLabel(CODE_FIELD_NAME, languageCode, codeFieldLabel);
		dest.setFieldLabel(SCIENTIFIC_NAME_FIELD_NAME, languageCode, scientificNameFieldLabel);
		dest.setFieldLabel(VERNACULAR_NAME_FIELD_NAME, languageCode, vernacularNameFieldLabel);
		dest.setFieldLabel(LANGUAGE_CODE_FIELD_NAME, languageCode, languageCodeFieldLabel);
		dest.setFieldLabel(LANGUAGE_VARIETY_FIELD_NAME, languageCode, languageVarietyFieldLabel);
		dest.setFieldLabel(FAMILY_CODE_FIELD_NAME, languageCode, familyCodeFieldLabel);
		dest.setFieldLabel(FAMILY_SCIENTIFIC_NAME_FIELD_NAME, languageCode, familyNameFieldLabel);
		
		CollectSurvey survey = (CollectSurvey) dest.getSurvey();
		CollectAnnotations annotations = survey.getAnnotations();
		annotations.setShowFamily(dest, showFamily);
		annotations.setIncludeUniqueVernacularName(dest, includeUniqueVernacularName);
		annotations.setAllowUnlisted(dest, allowUnlisted);
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

	public String getCodeFieldLabel() {
		return codeFieldLabel;
	}

	public void setCodeFieldLabel(String codeFieldLabel) {
		this.codeFieldLabel = codeFieldLabel;
	}

	public String getScientificNameFieldLabel() {
		return scientificNameFieldLabel;
	}

	public void setScientificNameFieldLabel(String scientificNameFieldLabel) {
		this.scientificNameFieldLabel = scientificNameFieldLabel;
	}

	public String getVernacularNameFieldLabel() {
		return vernacularNameFieldLabel;
	}

	public void setVernacularNameFieldLabel(String vernacularNameFieldLabel) {
		this.vernacularNameFieldLabel = vernacularNameFieldLabel;
	}

	public String getLanguageCodeFieldLabel() {
		return languageCodeFieldLabel;
	}

	public void setLanguageCodeFieldLabel(String languageCodeFieldLabel) {
		this.languageCodeFieldLabel = languageCodeFieldLabel;
	}

	public String getLanguageVarietyFieldLabel() {
		return languageVarietyFieldLabel;
	}

	public void setLanguageVarietyFieldLabel(String languageVarietyFieldLabel) {
		this.languageVarietyFieldLabel = languageVarietyFieldLabel;
	}

	public String getFamilyCodeFieldLabel() {
		return familyCodeFieldLabel;
	}

	public void setFamilyCodeFieldLabel(String familyCodeFieldLabel) {
		this.familyCodeFieldLabel = familyCodeFieldLabel;
	}

	public String getFamilyNameFieldLabel() {
		return familyNameFieldLabel;
	}

	public void setFamilyNameFieldLabel(String familyNameFieldLabel) {
		this.familyNameFieldLabel = familyNameFieldLabel;
	}
	
	public boolean isAllowUnlisted() {
		return allowUnlisted;
	}
	
	public void setAllowUnlisted(boolean allowUnlisted) {
		this.allowUnlisted = allowUnlisted;
	}
}
