/**
 * 
 */
package org.openforis.collect.metamodel.proxy;

import org.granite.messaging.amf.io.util.externalizer.annotation.ExternalizedProperty;
import org.openforis.idm.metamodel.TaxonAttributeDefinition;
import org.openforis.idm.model.species.Taxon.TaxonRank;

/**
 * @author S. Ricci
 *
 */
public class TaxonAttributeDefinitionProxy extends AttributeDefinitionProxy {

	private transient TaxonAttributeDefinition attributeDefn;
	
	public TaxonAttributeDefinitionProxy(EntityDefinitionProxy parent, TaxonAttributeDefinition attributeDefinition) {
		super(parent, attributeDefinition);
		this.attributeDefn = attributeDefinition;
	}
	
	@ExternalizedProperty
	public String getTaxonomy() {
		return attributeDefn.getTaxonomy();
	}

	@ExternalizedProperty
	public String getHighestRank() {
		TaxonRank rank = attributeDefn.getHighestTaxonRank();
		return rank == null ? null: rank.getName();
	}
	
	@ExternalizedProperty
	public boolean isCodeVisible() {
		return isFieldVisible(TaxonAttributeDefinition.CODE_FIELD_NAME);
	}

	@ExternalizedProperty
	public boolean isScientificNameVisible() {
		return isFieldVisible(TaxonAttributeDefinition.SCIENTIFIC_NAME_FIELD_NAME);
	}

	@ExternalizedProperty
	public boolean isVernacularNameVisible() {
		return isFieldVisible(TaxonAttributeDefinition.VERNACULAR_NAME_FIELD_NAME);
	}

	@ExternalizedProperty
	public boolean isLanguageCodeVisible() {
		return isFieldVisible(TaxonAttributeDefinition.LANGUAGE_CODE_FIELD_NAME);
	}

	@ExternalizedProperty
	public boolean isLanguageVarietyVisible() {
		return isFieldVisible(TaxonAttributeDefinition.LANGUAGE_VARIETY_FIELD_NAME);
	}
	
	@ExternalizedProperty
	public boolean isShowFamily() {
		return getAnnotations().isShowFamily(attributeDefn);
	}

	@ExternalizedProperty
	public boolean isIncludeUniqueVernacularName() {
		return getAnnotations().isIncludeUniqueVernacularName(attributeDefn);
	}
	
	@ExternalizedProperty
	public boolean isAllowUnlisted() {
		return getAnnotations().isAllowUnlisted(attributeDefn);
	}
}
