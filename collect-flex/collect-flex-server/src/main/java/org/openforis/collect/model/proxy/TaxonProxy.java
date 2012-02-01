package org.openforis.collect.model.proxy;

import org.granite.messaging.amf.io.util.externalizer.annotation.ExternalizedProperty;
import org.openforis.collect.Proxy;
import org.openforis.idm.model.Taxon;

/**
 * 
 * @author S. Ricci
 *
 */
public class TaxonProxy implements Proxy {

	private transient Taxon taxon;

	public TaxonProxy(Taxon taxon) {
		super();
		this.taxon = taxon;
	}

	@ExternalizedProperty
	public String getCode() {
		return taxon.getCode();
	}

	@ExternalizedProperty
	public String getLanguageCode() {
		return taxon.getLanguageCode();
	}

	@ExternalizedProperty
	public String getLanguageVariant() {
		return taxon.getLanguageVariant();
	}

	@ExternalizedProperty
	public String getScientificName() {
		return taxon.getScientificName();
	}

	@ExternalizedProperty
	public String getVernacularName() {
		return taxon.getVernacularName();
	}
	
}
