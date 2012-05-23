/**
 * 
 */
package org.openforis.collect.manager;

import java.util.ArrayList;
import java.util.List;

import org.openforis.collect.persistence.TaxonDao;
import org.openforis.collect.persistence.TaxonVernacularNameDao;
import org.openforis.collect.persistence.TaxonomyDao;
import org.openforis.idm.model.TaxonOccurrence;
import org.openforis.idm.model.species.Taxon;
import org.openforis.idm.model.species.TaxonVernacularName;
import org.openforis.idm.model.species.Taxonomy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author S. Ricci
 * @author M. Togna
 * 
 */
public class SpeciesManager {

	@Autowired
	private TaxonDao taxonDao;

	@Autowired
	private TaxonVernacularNameDao taxonVernacularNameDao;

	@Autowired
	private TaxonomyDao taxonomyDao;

	@Transactional
	public List<TaxonOccurrence> findByCode(String taxonomyName, String searchString, int maxResults) {
		Taxonomy taxonomy = taxonomyDao.load(taxonomyName);
		List<Taxon> list = taxonDao.findByCode(taxonomy.getId(), searchString, maxResults);
		List<TaxonOccurrence> result = new ArrayList<TaxonOccurrence>();
		for (Taxon taxon : list) {
			TaxonOccurrence o = new TaxonOccurrence(taxon.getCode(), taxon.getScientificName());
			result.add(o);
		}
		return result;
	}

	@Transactional
	public List<TaxonOccurrence> findByScientificName(String taxonomyName, String searchString, int maxResults) {
		Taxonomy taxonomy = taxonomyDao.load(taxonomyName);
		List<Taxon> list = taxonDao.findByScientificName(taxonomy.getId(), searchString, maxResults);
		List<TaxonOccurrence> result = new ArrayList<TaxonOccurrence>();
		for (Taxon taxon : list) {
			TaxonOccurrence o = new TaxonOccurrence(taxon.getCode(), taxon.getScientificName());
			result.add(o);
		}
		return result;
	}

	@Transactional
	public List<TaxonOccurrence> findByVernacularName(String taxonomyName, String searchString, int maxResults) {
		Taxonomy taxonomy = taxonomyDao.load(taxonomyName);
		List<TaxonVernacularName> list = taxonVernacularNameDao.findByVernacularName(taxonomy.getId(), searchString, maxResults);
		List<TaxonOccurrence> result = new ArrayList<TaxonOccurrence>();
		for (TaxonVernacularName taxonVernacularName : list) {
			Integer taxonId = taxonVernacularName.getTaxonSystemId();
			Taxon taxon = taxonDao.loadById(taxonId);
			TaxonOccurrence o = new TaxonOccurrence(taxon.getCode(), taxon.getScientificName(), taxonVernacularName.getVernacularName(), taxonVernacularName.getLanguageCode(),
					taxonVernacularName.getLanguageVariety());
			result.add(o);
		}
		return result;
	}

}
