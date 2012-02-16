package org.openforis.collect.model.proxy;

import org.granite.messaging.amf.io.util.externalizer.annotation.ExternalizedProperty;
import org.openforis.collect.Proxy;
import org.openforis.idm.model.species.Taxon;

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
	public Integer getId() {
		return taxon.getId();
	}

	@ExternalizedProperty
	public Integer getTaxonomyId() {
		return taxon.getTaxonomyId();
	}

	@ExternalizedProperty
	public String getCode() {
		return taxon.getCode();
	}

	@ExternalizedProperty
	public String getScientificName() {
		return taxon.getScientificName();
	}

	@ExternalizedProperty
	public String getTaxonomicRank() {
		return taxon.getTaxonomicRank();
	}

	@ExternalizedProperty
	public Integer getStep() {
		return taxon.getStep();
	}

	public Integer getParentId() {
		return taxon.getParentId();
	}
	
	
}
