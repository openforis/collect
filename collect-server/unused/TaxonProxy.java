package org.openforis.collect.model.proxy;

import org.granite.messaging.amf.io.util.externalizer.annotation.ExternalizedProperty;
import org.openforis.collect.Proxy;
import org.openforis.idm.model.species.Taxon;
import org.openforis.idm.model.species.Taxon.TaxonRank;

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
	public Integer getSystemId() {
		return taxon.getSystemId();
	}

	@ExternalizedProperty
	public Integer getTaxonomyId() {
		return taxon.getTaxonomyId();
	}

	@ExternalizedProperty
	public Integer getTaxonId() {
		return taxon.getTaxonId();
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
	public TaxonRank getTaxonRank() {
		return taxon.getTaxonRank();
	}

	@ExternalizedProperty
	public Integer getStep() {
		return taxon.getStep();
	}

	public Integer getParentId() {
		return taxon.getParentId();
	}
	
	
}
