/**
 * 
 */
package org.openforis.collect.designer.form;

import java.util.ArrayList;
import java.util.List;

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
	
	TaxonAttributeDefinitionFormObject(EntityDefinition parentDefn) {
		super(parentDefn);
	}

	@Override
	public void loadFrom(TaxonAttributeDefinition source, String languageCode, String defaultLanguage) {
		super.loadFrom(source, languageCode, defaultLanguage);
		taxonomy = source.getTaxonomy();
		highestRank = source.getHighestTaxonRank() == null ? null: source.getHighestTaxonRank().getName();
		qualifiers = new ArrayList<String>(source.getQualifiers());
	}

	@Override
	public void saveTo(TaxonAttributeDefinition dest, String languageCode) {
		super.saveTo(dest, languageCode);
		dest.setTaxonomy(taxonomy);
		dest.setHighestTaxonRank(TaxonRank.fromName(highestRank));
		dest.setQualifiers(qualifiers);
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

}
