/**
 * 
 */
package org.openforis.collect.manager;

import java.util.ArrayList;
import java.util.List;

import org.openforis.collect.persistence.TaxonDAO;
import org.openforis.collect.persistence.TaxonVernacularNameDAO;
import org.openforis.collect.persistence.TaxonomyDAO;
import org.openforis.idm.model.TaxonOccurrence;
import org.openforis.idm.model.species.Taxon;
import org.openforis.idm.model.species.TaxonVernacularName;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author S. Ricci
 *
 */
public class TaxonManager {

	@Autowired
	private TaxonDAO taxonDao;
	
	@Autowired
	private TaxonVernacularNameDAO taxonVernacularNameDao;
	
	@Autowired
	private TaxonomyDAO taxonomyDao;
	
	public List<TaxonOccurrence> findByCode(String searchString, int maxResults) {
		List<Taxon> list = taxonDao.findByCode(searchString, maxResults);
		List<TaxonOccurrence> result = new ArrayList<TaxonOccurrence>();
		for (Taxon taxon : list) {
			TaxonOccurrence o = new TaxonOccurrence(taxon);
			result.add(o);
		}
		return result;
	}

	public List<TaxonOccurrence> findByScientificName(String searchString, int maxResults) {
		List<Taxon> list = taxonDao.findByScientificName(searchString, maxResults);
		List<TaxonOccurrence> result = new ArrayList<TaxonOccurrence>();
		for (Taxon taxon : list) {
			TaxonOccurrence o = new TaxonOccurrence(taxon);
			result.add(o);
		}
		return result;
	}
	
	public List<TaxonOccurrence> findByVernacularName(String searchString, int maxResults) {
		List<TaxonVernacularName> list = taxonVernacularNameDao.findByVernacularName(searchString, maxResults);
		List<TaxonOccurrence> result = new ArrayList<TaxonOccurrence>();
		for (TaxonVernacularName taxonVernacularName : list) {
			Integer taxonId = taxonVernacularName.getTaxonId();
			Taxon taxon = taxonDao.load(taxonId);
			TaxonOccurrence o = new TaxonOccurrence(taxon, taxonVernacularName);
			result.add(o);
		}
		return result;
	}
	
}
