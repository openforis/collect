package org.openforis.collect.persistence;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;
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
 * @author S. Ricci
 * @author E. Wibowo
 */
@RunWith( SpringJUnit4ClassRunner.class )
@ContextConfiguration( locations = {"classpath:test-context.xml"} )
@TransactionConfiguration(defaultRollback=true)
@Transactional
public class SpeciesDaoIntegrationTest {
//	private final Log log = LogFactory.getLog(ModelDaoIntegrationTest.class);

	@Autowired
	private TaxonomyDao taxonomyDao;

	@Autowired
	private TaxonDao taxonDao;

	@Autowired
	private TaxonVernacularNameDao taxonVernacularNameDao;

	private void testFindCode(String match, int maxResults, int expectedResults) {
		// Create taxonomy
		Taxonomy taxonomy1 = testInsertAndLoadTaxonomy("it_bamboo");
		testUpdateAndLoadTaxonomy(taxonomy1, "it_trees");
		Taxon family1 = testInsertAndLoadTaxon(taxonomy1, -1, "JUGLANDACAE","Juglandaceae", "family", 9, null);
		Taxon genus1 = testInsertAndLoadTaxon(taxonomy1, -2, "JUG", "Juglans sp.", "genus", 9, family1);
		testInsertAndLoadTaxon(taxonomy1, -3, "JUG/REG", "Juglans regia", "species", 9, genus1);

		List<Taxon> results = taxonDao.findByCode(taxonomy1.getId(), match, maxResults);
		assertEquals(expectedResults, results.size());
		match = match.toUpperCase();
		for (Taxon taxon : results) {
			String code = taxon.getCode();
			code = (code == null) ? "" : code.toUpperCase();
			assertTrue(code.startsWith(match));
		}
	}
	
	@Test
	public void testFindCode() throws Exception {
		testFindCode("JUG", 100, 3);
	}
	
	@Test
	public void testFindUnknownCode() throws Exception {
		testFindCode("XXX", 100, 0);
	}

	@Test
	public void testFindCodeCaseInsensitive() throws Exception {
		testFindCode("jug", 100, 3);
	}

	@Test
	public void testFindCodeMaxRecords() throws Exception {
		testFindCode("jug", 1, 1);
	}

	@Test
	public void testFindSpecificCode() throws Exception {
		testFindCode("jug/reg", 100, 1);
	}
	
	private void testFindScientificName(String match, int maxResults, int expectedResults) {
		// Create taxonomy
		Taxonomy taxonomy1 = testInsertAndLoadTaxonomy("it_bamboo");
		testUpdateAndLoadTaxonomy(taxonomy1, "it_trees");
		Taxon family1 = testInsertAndLoadTaxon(taxonomy1, -1, "JUGLANDACAE","Juglandaceae", "family", 9, null);
		Taxon genus1 = testInsertAndLoadTaxon(taxonomy1, -2, "JUG", "Juglans sp.", "genus", 9, family1);
		testInsertAndLoadTaxon(taxonomy1, -3, "JUG/REG", "Juglans regia", "species", 9, genus1);
		
		List<Taxon> results = taxonDao.findByScientificName(taxonomy1.getId(), match, maxResults);
		assertEquals(expectedResults, results.size());
		match = match.toUpperCase();
		for (Taxon taxon : results) {
			String name = taxon.getScientificName();
			name = name.toUpperCase();
			assertTrue(name.startsWith(match));
		}
	}

	@Test
	public void testFindScientificName() throws Exception {
		testFindScientificName("jugl", 100, 3);
	}

	@Test
	public void testFindSpecificScientificName() throws Exception {
		testFindScientificName("juglans regia", 100, 1);
	}


	private void testFindVernacularName(String match, int maxResults, int expectedResults) {
		// Create taxonomy
		Taxonomy taxonomy1 = testInsertAndLoadTaxonomy("it_bamboo");
		testUpdateAndLoadTaxonomy(taxonomy1, "it_trees");
		Taxon family1 = testInsertAndLoadTaxon(taxonomy1, -1, "JUGLANDACAE","Juglandaceae", "family", 9, null);
		Taxon genus1 = testInsertAndLoadTaxon(taxonomy1, -2, "JUG", "Juglans sp.", "genus", 9, family1);
		Taxon species1 = testInsertAndLoadTaxon(taxonomy1, -3, "JUG/REG", "Juglans regia", "species", 9, genus1);
		testInsertAndLoadVernacularName(family1, "Walnut family", "eng", "", 9);
		testInsertAndLoadVernacularName(genus1, "Walnut", "eng", "", 0);
		testInsertAndLoadVernacularName(genus1, "Noce", "ita", "", 9);
		testInsertAndLoadVernacularName(species1, "Noce bianco", "ita", "", 9);
		testInsertAndLoadVernacularName(species1, "Persian walnut", "eng", "", 9);
		testInsertAndLoadVernacularName(species1, "Орех грецкий", "rus", "", 9);
		
		List<TaxonVernacularName> results = taxonVernacularNameDao.findByVernacularName(taxonomy1.getId(), match, maxResults);
		assertEquals(expectedResults, results.size());
		match = match.toUpperCase();
		for (TaxonVernacularName tvn : results) {
			String name = tvn.getVernacularName();
			name = name.toUpperCase();
			assertTrue(name.contains(match));
		}
	}

	@Test
	public void testFindVernacularName() throws Exception {
		testFindVernacularName("walnut", 100, 3);
	}

	@Test
	public void testFindVernacularNameMaxResults() throws Exception {
		testFindVernacularName("walnut", 3, 3);
	}

	@Test
	public void testFindUnicode() throws Exception {
		testFindVernacularName("Орех", 100, 1);
	}

	@Test
	public void testFindUnicodeCaseInsensitive() throws Exception {
		testFindVernacularName("орех", 100, 1);
	}

	@Test
	public void testCRUD() throws Exception  {
		// Create taxonomy
		Taxonomy taxonomy1 = testInsertAndLoadTaxonomy("it_bamboo");
		testUpdateAndLoadTaxonomy(taxonomy1, "it_trees");
		
		// Create taxa
		Stack<Taxon> taxa = new Stack<Taxon>();
		taxa.push(testInsertAndLoadTaxon(taxonomy1, -1, "JUG","Juglandaceaex", "familyx", 9, null));
		taxa.push(testInsertAndLoadTaxon(taxonomy1, -2, "JUG2", "sJuglans sp.", "sadgenus", 0, null));
		taxa.push(testUpdateAndLoadTaxon(taxa.pop(), "Juglans sp.", "genus", 9, taxa.get(0)));
		taxa.push(testInsertAndLoadTaxon(taxonomy1, -4, "JUG3", "Juglans regia", "species", 9, taxa.get(1)));
		
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
		Taxonomy t1 = new Taxonomy();
		t1.setName(name);
		taxonomyDao.insert(t1);
		Taxonomy t = t1;

		// Confirm saved
		Taxonomy t2 = taxonomyDao.loadById(t.getId());
		assertEquals(t.getId(), t2.getId());
		assertEquals(t.getName(), t2.getName());
		return t2;
	}

	private void testUpdateAndLoadTaxonomy(Taxonomy t, String newName) {
		// Update
		Integer id = t.getId();
		t.setName(newName);
		taxonomyDao.update(t);
		
		// Confirm saved
		t = taxonomyDao.loadById(id);
		assertEquals(newName, t.getName());
	}
	
	private Taxon testInsertAndLoadTaxon(Taxonomy taxonomy, int taxonId, String code, String scientificName, String rank, int step, Taxon parent) {
		Integer parentId = parent == null ? null : parent.getSystemId();
		
		// Insert
		Taxon t = new Taxon();
		t.setTaxonId(taxonId);
		t.setCode(code);
		t.setScientificName(scientificName);
		t.setTaxonomicRank(rank);
		t.setStep(step);
		t.setTaxonomyId(taxonomy.getId());
		t.setParentId(parentId);
		taxonDao.insert(t);
		
		// Confirm saved
		t = taxonDao.loadById(t.getSystemId());
		assertNotNull(t);
		assertEquals((Integer) taxonId, t.getTaxonId());
		assertEquals(scientificName, t.getScientificName());
		assertEquals(rank, t.getTaxonomicRank());
		assertEquals(taxonomy.getId(), t.getTaxonomyId());
		assertEquals(step, t.getStep());
		assertEquals(parentId, t.getParentId());
		return t;
	}

	
	private Taxon testUpdateAndLoadTaxon(Taxon t, String scientificName, String rank, int step, Taxon parent) {
		Integer parentId = parent == null ? null : parent.getSystemId();
		
		// Insert
		t.setScientificName(scientificName);
		t.setTaxonomicRank(rank);
		t.setStep(step);
		t.setParentId(parentId);
		taxonDao.update(t);
		
		// Confirm saved
		t = taxonDao.loadById(t.getSystemId());
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
		tvn.setTaxonSystemId(taxon1.getSystemId());
		tvn.setStep(step);
		taxonVernacularNameDao.insert(tvn);
		
		// Confirm saved
		tvn = taxonVernacularNameDao.loadById(tvn.getId());
		assertNotNull(tvn);
		assertEquals(taxon1.getSystemId(), tvn.getTaxonSystemId());
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
		tvn.setTaxonSystemId(taxon1.getSystemId());
		tvn.setStep(step);
		taxonVernacularNameDao.update(tvn);
		
		// Confirm saved
		tvn = taxonVernacularNameDao.loadById(id);
		assertNotNull(tvn);
		assertEquals(id, tvn.getId());
		assertEquals(taxon1.getSystemId(), tvn.getTaxonSystemId());
		assertEquals(name, tvn.getVernacularName());
		assertEquals(lang, tvn.getLanguageCode());
		assertEquals(variety, tvn.getLanguageVariety());
		assertEquals(step, tvn.getStep());
		
		return tvn;
	}

	private void testDeleteAndLoadVernacularName(TaxonVernacularName t) {
		// Delete
		taxonVernacularNameDao.delete(t.getId());
		
		// Confirm deleted
		t = taxonVernacularNameDao.loadById(t.getId());
		assertNull(t);
	}


	private void testDeleteAndLoadTaxon(Taxon t) {
		// Delete
		taxonDao.delete(t.getSystemId());
		
		// Confirm deleted
		t = taxonDao.loadById(t.getSystemId());
		assertNull(t);
	}

	private void testDeleteAndLoadTaxonomy(Taxonomy t) {
		// Delete
		taxonomyDao.delete(t.getId());
		
		// Confirm deleted
		t = taxonomyDao.loadById(t.getId());
		assertNull(t);
	}
	/*
	private TaxonVernacularName testInsertAndLoadVernacularNameWithQualifier(TaxonVernacularName tvn, Taxon taxon1, String name, String lang, String variety, int step, String qualifer1) {
		// Insert
		Integer id = tvn.getId();
		tvn.setVernacularName(name);
		tvn.setLanguageCode(lang);
		tvn.setLanguageVariety(variety);
		tvn.setTaxonSystemId(taxon1.getSystemId());
		tvn.setStep(step);
		taxonVernacularNameDao.update(tvn);
		
		// Confirm saved
		tvn = taxonVernacularNameDao.loadById(id);
		assertNotNull(tvn);
		assertEquals(id, tvn.getId());
		assertEquals(taxon1.getSystemId(), tvn.getTaxonSystemId());
		assertEquals(name, tvn.getVernacularName());
		assertEquals(lang, tvn.getLanguageCode());
		assertEquals(variety, tvn.getLanguageVariety());
		assertEquals(step, tvn.getStep());
		
		return tvn;
	}

	private TaxonVernacularName testUpdateAndLoadVernacularNameWithQualifier(TaxonVernacularName tvn, Taxon taxon1, String name, String lang, String variety, int step, String qualifer1) {
		// Insert
		Integer id = tvn.getId();
		tvn.setVernacularName(name);
		tvn.setLanguageCode(lang);
		tvn.setLanguageVariety(variety);
		tvn.setTaxonSystemId(taxon1.getSystemId());
		tvn.setStep(step);
		taxonVernacularNameDao.update(tvn);
		
		// Confirm saved
		tvn = taxonVernacularNameDao.loadById(id);
		assertNotNull(tvn);
		assertEquals(id, tvn.getId());
		assertEquals(taxon1.getSystemId(), tvn.getTaxonSystemId());
		assertEquals(name, tvn.getVernacularName());
		assertEquals(lang, tvn.getLanguageCode());
		assertEquals(variety, tvn.getLanguageVariety());
		assertEquals(step, tvn.getStep());
		
		return tvn;
	}	
	
	private TaxonVernacularName testInsertAndLoadVernacularNameWithQualifier(Taxon taxon1, String name, String lang, String variety, int step, String qualifier1) {
		// Insert
		TaxonVernacularName tvn = new TaxonVernacularName();
		tvn.setVernacularName(name);
		tvn.setLanguageCode(lang);
		tvn.setLanguageVariety(variety);
		tvn.setTaxonSystemId(taxon1.getSystemId());
		tvn.setStep(step);
		List<String> q = new ArrayList<String>();
		q.add(qualifier1);
		tvn.setQualifiers(q);
		taxonVernacularNameDao.insert(tvn);
		
		// Confirm saved
		tvn = taxonVernacularNameDao.loadById(tvn.getId());
		assertNotNull(tvn);
		assertEquals(taxon1.getSystemId(), tvn.getTaxonSystemId());
		assertEquals(name, tvn.getVernacularName());
		assertEquals(lang, tvn.getLanguageCode());
		assertEquals(variety, tvn.getLanguageVariety());
		assertEquals(1, tvn.getQualifiers().size());		
		assertEquals(qualifier1, tvn.getQualifiers().get(0));
		assertEquals(step, tvn.getStep());
		return tvn;
	}
	
	public void findVernacularNameWithQualifer(String match, int maxResults, int expectedResults)
	{
		// Create taxonomy
		Taxonomy taxonomy1 = testInsertAndLoadTaxonomy("it_bamboo");
		testUpdateAndLoadTaxonomy(taxonomy1, "it_trees");
		
		// From IDNFI NFICODE=604		CODE=0340736052431	FAMILY=Dipterocarpaceae	GENUS=Shorea	SPECIES=S. leprosula Miq.
		Taxon family1 = testInsertAndLoadTaxon(taxonomy1, 1, "DIP","Dipterocarpaceae", "family", 9, null);
		Taxon genus1 = testInsertAndLoadTaxon(taxonomy1, 2, "SHO", "Shorea.", "genus", 9, family1);
		Taxon species1 = testInsertAndLoadTaxon(taxonomy1, 3, "LEP", "S. leprosula Miq.", "species", 9, genus1);
		
		testInsertAndLoadVernacularNameWithQualifier(species1, "Meranti", "id", "", 9,"21"); // Kalimantan Timur, East Borneo
		
		List<TaxonVernacularName> results = taxonVernacularNameDao.findByVernacularName(match, maxResults);
		assertEquals(expectedResults, results.size());
		match = match.toUpperCase();
		for (TaxonVernacularName tvn : results) {
			String name = tvn.getVernacularName();
			name = name.toUpperCase();
			assertTrue(name.contains(match));
		}
	}
	
	@Test
	public void testFindVernacularNameWithQualifer() throws Exception {
		findVernacularNameWithQualifer("Meranti", 100, 1);
	}
	
	public void findVernacularNameBasedOnQualifier1(String match, HashMap<TableField, String> qualifiers, int maxResults, int expectedResults)
	{
		// Create taxonomy
		Taxonomy taxonomy1 = testInsertAndLoadTaxonomy("it_bamboo");
		testUpdateAndLoadTaxonomy(taxonomy1, "it_trees");
		
		// From IDNFI NFICODE=604		CODE=0340736052431	FAMILY=Dipterocarpaceae	GENUS=Shorea	SPECIES=S. leprosula Miq.
		Taxon family1 = testInsertAndLoadTaxon(taxonomy1, 1, "DIP","Dipterocarpaceae", "family", 9, null);
		Taxon genus1 = testInsertAndLoadTaxon(taxonomy1, 2, "SHO", "Shorea.", "genus", 9, family1);
		Taxon species1 = testInsertAndLoadTaxon(taxonomy1, 3, "LEP", "S. leprosula Miq.", "species", 9, genus1);
		
		testInsertAndLoadVernacularNameWithQualifier(species1, "Meranti", "id", "", 9,"21"); // Kalimantan Timur, East Borneo
		testInsertAndLoadVernacularNameWithQualifier(species1, "Meranti bunga", "id", "", 9,"21"); // Kalimantan Timur, East Borneo
		testInsertAndLoadVernacularNameWithQualifier(species1, "Meranti putih", "id", "", 9,"21"); // Kalimantan Timur, East Borneo
		
		List<TaxonVernacularName> results = taxonVernacularNameDao.findByVernacularName(match, qualifiers, maxResults);//already using qualifer1 as the criteria
		assertEquals(expectedResults, results.size());
		match = match.toUpperCase();
		for (TaxonVernacularName tvn : results) {
			String name = tvn.getVernacularName();
			name = name.toUpperCase();
			assertTrue(name.contains(match));
		}
	}
	
	@Test
	public void testFindVernacularNameBasedOnQualifier1_Exist()
	{
		HashMap<TableField,String> qualifiers = new HashMap();
		qualifiers.put(OFC_TAXON_VERNACULAR_NAME.QUALIFIER1, "21");
		findVernacularNameBasedOnQualifier1("Meranti", qualifiers,100, 3);
	}
	
	@Test
	public void testFindVernacularNameBasedOnQualifier1_NonExist()
	{
		HashMap<TableField,String> qualifiers = new HashMap();
		qualifiers.put(OFC_TAXON_VERNACULAR_NAME.QUALIFIER1, "21");
		findVernacularNameBasedOnQualifier1("Nyatoh", qualifiers,100, 0);
	}
*/	
	

	
}
