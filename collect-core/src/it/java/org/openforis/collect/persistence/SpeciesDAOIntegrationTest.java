package org.openforis.collect.persistence;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.openforis.collect.model.species.Taxon;
import org.openforis.collect.model.species.Taxonomy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

@RunWith( SpringJUnit4ClassRunner.class )
@ContextConfiguration( locations = {"classpath:test-context.xml"} )
@TransactionConfiguration(defaultRollback=false)
@Transactional
public class SpeciesDAOIntegrationTest {
//	private final Log log = LogFactory.getLog(ModelDAOIntegrationTest.class);

	@Autowired
	protected TaxonomyDAO taxonomyDao;

	@Test
	public void testCRUD() throws Exception  {
		insertTaxonomy();
	}

	private void insertTaxonomy() throws Exception {
		Taxonomy t = new Taxonomy();
		t.setName("trees");
		taxonomyDao.insert(t);
		t.setName("bamboo");
		taxonomyDao.update(t);
	}

	private void insertTaxon() throws Exception {
		Taxon t = new Taxon();
		t.setScientificName("Juglandaceae");
		t.setTaxonomicRank("family");
		t.setStep(9);
	}
	
	
}
