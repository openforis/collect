/**
 * 
 */
package org.openforis.collect.model.proxy;

import org.granite.messaging.amf.io.util.externalizer.annotation.ExternalizedProperty;
import org.openforis.collect.Proxy;
import org.openforis.idm.model.species.TaxonVernacularName;

/**
 * @author S. Ricci
 *
 */
public class TaxonVernacularNameProxy implements Proxy {

	private transient TaxonVernacularName vernacularName;

	public TaxonVernacularNameProxy(TaxonVernacularName vernacularName) {
		super();
		this.vernacularName = vernacularName;
	}

	@ExternalizedProperty
	public Integer getId() {
		return vernacularName.getId();
	}

	@ExternalizedProperty
	public String getVernacularName() {
		return vernacularName.getVernacularName();
	}

	@ExternalizedProperty
	public String getLanguageCode() {
		return vernacularName.getLanguageCode();
	}

	@ExternalizedProperty
	public String getLanguageVariety() {
		return vernacularName.getLanguageVariety();
	}

	@ExternalizedProperty
	public Integer getTaxonId() {
		return vernacularName.getTaxonSystemId();
	}

	@ExternalizedProperty
	public int getStep() {
		return vernacularName.getStep();
	}
	
}
