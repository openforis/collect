/**
 * 
 */
package org.openforis.collect.model.proxy;

import org.granite.messaging.amf.io.util.externalizer.annotation.ExternalizedProperty;
import org.openforis.collect.Proxy;
import org.openforis.idm.model.TaxonOccurrence;
import org.openforis.idm.model.species.Taxon.TaxonRank;

/**
 * @author S. Ricci
 *
 */
public class TaxonOccurrenceProxy implements Proxy {

	private transient TaxonOccurrence occurrence;

	public TaxonOccurrenceProxy(TaxonOccurrence occurence) {
		super();
		this.occurrence = occurence;
	}
	
	@ExternalizedProperty
	public String getCode() {
		return occurrence.getCode();
	}

	@ExternalizedProperty
	public String getScientificName() {
		return occurrence.getScientificName();
	}

	@ExternalizedProperty
	public String getVernacularName() {
		return occurrence.getVernacularName();
	}

	@ExternalizedProperty
	public String getLanguageCode() {
		return occurrence.getLanguageCode();
	}

	@ExternalizedProperty
	public String getLanguageVariety() {
		return occurrence.getLanguageVariety();
	}
	
	@ExternalizedProperty
	public TaxonRank getTaxonRank() {
		return occurrence.getTaxonRank();
	}
	
	@ExternalizedProperty
	public String getFamilyName() {
		TaxonOccurrence familyTaxon = occurrence.getAncestorTaxon(TaxonRank.FAMILY);
		return familyTaxon == null ? null : familyTaxon.getScientificName();
	}
	
	@ExternalizedProperty
	public String getFamilyCode() {
		TaxonOccurrence familyTaxon = occurrence.getAncestorTaxon(TaxonRank.FAMILY);
		return familyTaxon == null ? null : familyTaxon.getCode();
	}
	
}
