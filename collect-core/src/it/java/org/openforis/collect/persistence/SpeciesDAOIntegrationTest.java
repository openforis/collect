package org.openforis.collect.persistence;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.Stack;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.openforis.idm.model.species.Taxon;
import org.openforis.idm.model.species.TaxonVernacularName;
import org.openforis.idm.model.species.Taxonomy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author G. Miceli
 */
@RunWith( SpringJUnit4ClassRunner.class )
@ContextConfiguration( locations = {"classpath:test-context.xml"} )
@TransactionConfiguration(defaultRollback=true)
@Transactional
public class SpeciesDAOIntegrationTest {
//	private final Log log = LogFactory.getLog(ModelDAOIntegrationTest.class);

	@Autowired
	private TaxonomyDAO taxonomyDao;

	@Autowired
	private TaxonDAO taxonDao;

	@Autowired
	private TaxonVernacularNameDAO taxonVernacularNameDAO;
	
	@Test
	public void testCRUD() throws Exception  {
		// Create taxonomy
		Taxonomy taxonomy1 = testInsertAndLoadTaxonomy("it_bamboo");
		testUpdateAndLoadTaxonomy(taxonomy1, "it_trees");
		
		// Create taxa
		Stack<Taxon> taxa = new Stack<Taxon>();
		taxa.push(testInsertAndLoadTaxon(taxonomy1, "JUG","Juglandaceaex", "familyx", 9, null));
		taxa.push(testInsertAndLoadTaxon(taxonomy1,"JUG2", "sJuglans sp.", "sadgenus", 0, null));
		taxa.push(testUpdateAndLoadTaxon(taxa.pop(), "Juglans sp.", "family", 9, taxa.get(0).getId()));
		taxa.push(testInsertAndLoadTaxon(taxonomy1,"JUG3", "Juglans regia", "species", 9, taxa.get(1).getId()));
		
		// Create vernacular names
		Stack<TaxonVernacularName> names = new Stack<TaxonVernacularName>();
		names.push(testInsertAndLoadVernacularName(taxa.get(0), "Walnut family", "eng", "", 9));
		names.push(testInsertAndLoadVernacularName(taxa.get(0), "Walnuts", "en", "Cockney", 0));
		names.push(testUpdateAndLoadVernacularName(names.pop(), taxa.get(1), "Walnut", "eng", "", 9));
		names.push(testInsertAndLoadVernacularName(taxa.get(1), "Noce", "ita", "", 9));
		names.push(testInsertAndLoadVernacularName(taxa.get(2), "Persian walnut", "eng", "", 9));
		names.push(testInsertAndLoadVernacularName(taxa.get(2), "English walnut", "eng", "", 9));
		names.push(testInsertAndLoadVernacularName(taxa.get(2), "Noce bianco", "ita", "", 9));
		names.push(testInsertAndLoadVernacularName(taxa.get(2), "Орех грецкий", "rus", "", 9));

		// Remove names
		while (!names.isEmpty()) {
			testDeleteAndLoadVernacularName(names.pop());
		}
		
		// Remove taxa
		while (!taxa.isEmpty()) {
			testDeleteAndLoadTaxon(taxa.pop());
		}

		// Remove taxonomy
		testDeleteAndLoadTaxonomy(taxonomy1);
	}

	private Taxonomy testInsertAndLoadTaxonomy(String name) {
		// Insert
		Taxonomy t = new Taxonomy();
		t.setName(name);
		taxonomyDao.insert(t);

		// Confirm saved
		t = taxonomyDao.load(t.getId());
		assertEquals(name, t.getName());
		return t;
	}

	private void testUpdateAndLoadTaxonomy(Taxonomy t, String newName) {
		// Update
		Integer id = t.getId();
		t.setName(newName);
		taxonomyDao.update(t);
		
		// Confirm saved
		t = taxonomyDao.load(id);
		assertEquals(newName, t.getName());
	}
	
	private Taxon testInsertAndLoadTaxon(Taxonomy taxonomy, String code, String scientificName, String rank, int step, Integer parentId) {
		// Insert
		Taxon t = new Taxon();
		t.setCode(code);
		t.setScientificName(scientificName);
		t.setTaxonomicRank(rank);
		t.setStep(step);
		t.setTaxonomyId(taxonomy.getId());
		t.setParentId(parentId);
		taxonDao.insert(t);
		
		// Confirm saved
		t = taxonDao.load(t.getId());
		assertNotNull(t);
		assertEquals(scientificName, t.getScientificName());
		assertEquals(rank, t.getTaxonomicRank());
		assertEquals(taxonomy.getId(), t.getTaxonomyId());
		assertEquals(step, t.getStep());
		assertEquals(parentId, t.getParentId());
		return t;
	}

	
	private Taxon testUpdateAndLoadTaxon(Taxon t, String scientificName, String rank, int step, Integer parentId) {
		// Insert
		t.setScientificName(scientificName);
		t.setTaxonomicRank(rank);
		t.setStep(step);
		t.setParentId(parentId);
		taxonDao.update(t);
		
		// Confirm saved
		t = taxonDao.load(t.getId());
		assertNotNull(t);
		assertEquals(scientificName, t.getScientificName());
		assertEquals(rank, t.getTaxonomicRank());
		assertEquals(step, t.getStep());
		assertEquals(parentId, t.getParentId());
		return t;
	}

	private TaxonVernacularName testInsertAndLoadVernacularName(Taxon taxon1, String name, String lang, String variety, int step) {
		// Insert
		TaxonVernacularName tvn = new TaxonVernacularName();
		tvn.setVernacularName(name);
		tvn.setLanguageCode(lang);
		tvn.setLanguageVariety(variety);
		tvn.setTaxonId(taxon1.getId());
		tvn.setStep(step);
		taxonVernacularNameDAO.insert(tvn);
		
		// Confirm saved
		tvn = taxonVernacularNameDAO.load(tvn.getId());
		assertNotNull(tvn);
		assertEquals(taxon1.getId(), tvn.getTaxonId());
		assertEquals(name, tvn.getVernacularName());
		assertEquals(lang, tvn.getLanguageCode());
		assertEquals(variety, tvn.getLanguageVariety());
		assertEquals(step, tvn.getStep());
		return tvn;
	}

	private TaxonVernacularName testUpdateAndLoadVernacularName(TaxonVernacularName tvn, Taxon taxon1, String name, String lang, String variety, int step) {
		// Insert
		Integer id = tvn.getId();
		tvn.setVernacularName(name);
		tvn.setLanguageCode(lang);
		tvn.setLanguageVariety(variety);
		tvn.setTaxonId(taxon1.getId());
		tvn.setStep(step);
		taxonVernacularNameDAO.update(tvn);
		
		// Confirm saved
		tvn = taxonVernacularNameDAO.load(id);
		assertNotNull(tvn);
		assertEquals(id, tvn.getId());
		assertEquals(taxon1.getId(), tvn.getTaxonId());
		assertEquals(name, tvn.getVernacularName());
		assertEquals(lang, tvn.getLanguageCode());
		assertEquals(variety, tvn.getLanguageVariety());
		assertEquals(step, tvn.getStep());
		
		return tvn;
	}

	private void testDeleteAndLoadVernacularName(TaxonVernacularName t) {
		// Delete
		taxonVernacularNameDAO.delete(t.getId());
		
		// Confirm deleted
		t = taxonVernacularNameDAO.load(t.getId());
		assertNull(t);
	}


	private void testDeleteAndLoadTaxon(Taxon t) {
		// Delete
		taxonDao.delete(t.getId());
		
		// Confirm deleted
		t = taxonDao.load(t.getId());
		assertNull(t);
	}

	private void testDeleteAndLoadTaxonomy(Taxonomy t) {
		// Delete
		taxonomyDao.delete(t.getId());
		
		// Confirm deleted
		t = taxonomyDao.load(t.getId());
		assertNull(t);
	}
}
