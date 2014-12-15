package org.openforis.idm.metamodel.xml.internal.marshal;

import static org.openforis.idm.metamodel.xml.IdmlConstants.HIGHEST_RANK;
import static org.openforis.idm.metamodel.xml.IdmlConstants.QUALIFIERS;
import static org.openforis.idm.metamodel.xml.IdmlConstants.TAXON;
import static org.openforis.idm.metamodel.xml.IdmlConstants.TAXONOMY;

import java.io.IOException;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.openforis.idm.metamodel.TaxonAttributeDefinition;
import org.openforis.idm.model.species.Taxon.TaxonRank;

/**
 * 
 * @author G. Miceli
 * @author S. Ricci
 *
 */
class TaxonAttributeXS extends AttributeDefinitionXS<TaxonAttributeDefinition> {

	TaxonAttributeXS() {
		super(TAXON);
	}
	
	@Override
	protected void attributes(TaxonAttributeDefinition defn) throws IOException {
		super.attributes(defn);
		//attribute(QUALIFIABLE, defn.getQualifiers());
		attribute(TAXONOMY, defn.getTaxonomy());
		TaxonRank highestRank = defn.getHighestTaxonRank();
		attribute(HIGHEST_RANK, highestRank == null ? null: highestRank.getName());
		writeQualifiers(defn);
	}

	protected void writeQualifiers(TaxonAttributeDefinition defn) throws IOException {
		List<String> qualifiers = defn.getQualifiers();
		String qualifiersValue;
		if ( qualifiers == null || qualifiers.isEmpty() ) {
			qualifiersValue = null;
		} else {
			qualifiersValue = StringUtils.join(qualifiers, TaxonAttributeDefinition.QUALIFIER_SEPARATOR);
		}
		attribute(QUALIFIERS, qualifiersValue);
	}
}
