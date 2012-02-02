package org.openforis.collect.persistence;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;
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

	@Test
	public void testCRUD() throws Exception  {
		Taxonomy t = testInsertAndLoad();
		
		testUpdateAndLoad(t);
		
		taxonomyDao.delete(t.getId());
		
	}
	
	private Taxonomy testInsertAndLoad() {
		Taxonomy t = new Taxonomy();
		t.setName("trees");
		taxonomyDao.insert(t);

		t = taxonomyDao.load("trees");
		assertEquals("trees", t.getName());
		return t;
	}

	private void testUpdateAndLoad(Taxonomy t) {
		t.setName("bamboo");
		taxonomyDao.update(t);

		t = taxonomyDao.load("bamboo");
		assertEquals("bamboo", t.getName());
	}
}
