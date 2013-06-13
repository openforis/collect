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

}
