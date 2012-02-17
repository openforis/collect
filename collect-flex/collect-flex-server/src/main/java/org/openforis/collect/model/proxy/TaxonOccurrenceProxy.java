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
	public TaxonProxy getTaxon() {
		if(occurrence.getTaxon() != null) {
			return new TaxonProxy(occurrence.getTaxon());
		} else {
			return null;
		}
	}

	@ExternalizedProperty
	public TaxonVernacularNameProxy getVernacularName() {
		if(occurrence.getVernacularName() != null) {
			return new TaxonVernacularNameProxy(occurrence.getVernacularName());
		} else {
			return null;
		}
	}
	
	
	
}
