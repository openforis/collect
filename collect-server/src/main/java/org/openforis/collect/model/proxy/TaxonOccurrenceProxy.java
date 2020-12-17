/**
 * 
 */
package org.openforis.collect.model.proxy;

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

	public String getCode() {
		return occurrence.getCode();
	}

	public String getScientificName() {
		return occurrence.getScientificName();
	}

	public String getVernacularName() {
		return occurrence.getVernacularName();
	}

	public String getLanguageCode() {
		return occurrence.getLanguageCode();
	}

	public String getLanguageVariety() {
		return occurrence.getLanguageVariety();
	}

	public TaxonRank getTaxonRank() {
		return occurrence.getTaxonRank();
	}

	public String getFamilyCode() {
		if (occurrence.getTaxonRank() == TaxonRank.FAMILY) {
			return occurrence.getCode();
		} else {
			TaxonOccurrence familyTaxon = occurrence.getAncestorTaxon(TaxonRank.FAMILY);
			return familyTaxon == null ? null : familyTaxon.getCode();
		}
	}

	public String getFamilyScientificName() {
		if (occurrence.getTaxonRank() == TaxonRank.FAMILY) {
			return occurrence.getScientificName();
		} else {
			TaxonOccurrence familyTaxon = occurrence.getAncestorTaxon(TaxonRank.FAMILY);
			return familyTaxon == null ? null : familyTaxon.getScientificName();
		}
	}

}
