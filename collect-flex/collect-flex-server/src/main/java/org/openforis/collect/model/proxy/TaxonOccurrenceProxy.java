/**
 * 
 */
package org.openforis.collect.model.proxy;

import org.granite.messaging.amf.io.util.externalizer.annotation.ExternalizedProperty;
import org.openforis.collect.Proxy;
import org.openforis.idm.model.TaxonOccurrence;

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
	
}
