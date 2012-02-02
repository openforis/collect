package org.openforis.collect.persistence;

import static org.junit.Assert.*;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.openforis.collect.model.species.Taxon;
import org.openforis.collect.model.species.Taxonomy;
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
@TransactionConfiguration(defaultRollback=false)
@Transactional
public class SpeciesDAOIntegrationTest {
//	private final Log log = LogFactory.getLog(ModelDAOIntegrationTest.class);

	@Autowired
	protected TaxonomyDAO taxonomyDao;

	@Autowired
	protected TaxonDAO taxonDao;

	@Test
	public void testCRUD() throws Exception  {
		// Create taxonomy
		Taxonomy taxonomy1 = testInsertAndLoadTaxonomy("bamboo");
		testUpdateAndLoadTaxonomy(taxonomy1, "trees");
		
		// Create taxa
		Taxon taxon1 = testInsertAndLoadTaxon(taxonomy1, "Juglandaceae", "family", null);
		Taxon taxon2 = testInsertAndLoadTaxon(taxonomy1, "Juglans sp.", "genus", taxon1.getId());
		Taxon taxon3 = testInsertAndLoadTaxon(taxonomy1, "Juglans regia", "species", taxon2.getId());
		
		// Remove taxonomy
//		testDeleteAndLoadTaxonomy(taxonomy1);
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
	
	private Taxon testInsertAndLoadTaxon(Taxonomy taxonomy, String scientificName, String rank, Integer parentId) {
		// Insert
		Taxon t = new Taxon();
		t.setScientificName(scientificName);
		t.setTaxonomicRank(rank);
		t.setStep(9);
		t.setTaxonomyId(taxonomy.getId());
		t.setParentId(parentId);
		taxonDao.insert(t);
		
		// Confirm saved
		t = taxonDao.load(t.getId());
		assertEquals(scientificName, t.getScientificName());
		assertEquals(rank, t.getTaxonomicRank());
		assertEquals(taxonomy.getId(), t.getTaxonomyId());
		assertEquals(9, t.getStep());
		assertEquals(parentId, t.getParentId());
		return t;
	}

	private void testDeleteAndLoadTaxonomy(Taxonomy t) {
		// Delete
		taxonomyDao.delete(t.getId());
		
		// Confirm deleted
		t = taxonomyDao.load(t.getId());
		assertNull(t);
	}
}
