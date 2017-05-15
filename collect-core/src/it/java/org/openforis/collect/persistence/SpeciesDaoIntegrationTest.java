package org.openforis.collect.persistence;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Stack;

import org.junit.Test;
import org.openforis.collect.CollectIntegrationTest;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.CollectTaxonomy;
import org.openforis.idm.model.species.Taxon;
import org.openforis.idm.model.species.Taxon.TaxonRank;
import org.openforis.idm.model.species.TaxonVernacularName;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author G. Miceli
 * @author S. Ricci
 * @author E. Wibowo
 */
public class SpeciesDaoIntegrationTest extends CollectIntegrationTest {
//	private final Log log = LogFactory.getLog(ModelDaoIntegrationTest.class);

	@Autowired
	private TaxonomyDao taxonomyDao;

	@Autowired
	private TaxonDao taxonDao;

	@Autowired
	private TaxonVernacularNameDao taxonVernacularNameDao;

	private void testFindCode(String match, int maxResults, int expectedResults) {
		// Create taxonomy
		CollectTaxonomy taxonomy1 = testInsertAndLoadTaxonomy("it_bamboo");
		testUpdateAndLoadTaxonomy(taxonomy1, "it_trees");
		Taxon family1 = testInsertAndLoadTaxon(taxonomy1, -1, "JUGLANDACAE","Juglandaceae", TaxonRank.FAMILY, 9, null);
		Taxon genus1 = testInsertAndLoadTaxon(taxonomy1, -2, "JUG", "Juglans sp.", TaxonRank.GENUS, 9, family1);
		testInsertAndLoadTaxon(taxonomy1, -3, "JUG/REG", "Juglans regia", TaxonRank.SPECIES, 9, genus1);

		List<Taxon> results = taxonDao.findByCode(taxonomy1, match, maxResults);
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
		CollectTaxonomy taxonomy1 = testInsertAndLoadTaxonomy("it_bamboo");
		testUpdateAndLoadTaxonomy(taxonomy1, "it_trees");
		Taxon family1 = testInsertAndLoadTaxon(taxonomy1, -1, "JUGLANDACAE","Juglandaceae", TaxonRank.FAMILY, 9, null);
		Taxon genus1 = testInsertAndLoadTaxon(taxonomy1, -2, "JUG", "Juglans sp.",TaxonRank.GENUS, 9, family1);
		testInsertAndLoadTaxon(taxonomy1, -3, "JUG/REG", "Juglans regia", TaxonRank.SPECIES, 9, genus1);
		
		List<Taxon> results = taxonDao.findByScientificName(taxonomy1, match, maxResults);
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
		CollectTaxonomy taxonomy1 = testInsertAndLoadTaxonomy("it_bamboo");
		testUpdateAndLoadTaxonomy(taxonomy1, "it_trees");
		Taxon family1 = testInsertAndLoadTaxon(taxonomy1, -1, "JUGLANDACAE","Juglandaceae", TaxonRank.FAMILY, 9, null);
		Taxon genus1 = testInsertAndLoadTaxon(taxonomy1, -2, "JUG", "Juglans sp.", TaxonRank.GENUS, 9, family1);
		Taxon species1 = testInsertAndLoadTaxon(taxonomy1, -3, "JUG/REG", "Juglans regia", TaxonRank.SPECIES, 9, genus1);
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

	//Disabled because SQLite does not provide Unicode case support by default
	//TODO implement it
//	@Test
//	public void testFindUnicode() throws Exception {
//		testFindVernacularName("Орех", 100, 1);
//	}
//
//	@Test
//	public void testFindUnicodeCaseInsensitive() throws Exception {
//		testFindVernacularName("орех", 100, 1);
//	}

	@Test
	public void testCRUD() throws Exception  {
		// Create taxonomy
		CollectTaxonomy taxonomy1 = testInsertAndLoadTaxonomy("it_bamboo");
		testUpdateAndLoadTaxonomy(taxonomy1, "it_trees");
		
		// Create taxa
		Stack<Taxon> taxa = new Stack<Taxon>();
		taxa.push(testInsertAndLoadTaxon(taxonomy1, -1, "JUG","Juglandaceaex", TaxonRank.FAMILY, 9, null));
		taxa.push(testInsertAndLoadTaxon(taxonomy1, -2, "JUG2", "sJuglans sp.", TaxonRank.GENUS, 0, null));
		taxa.push(testUpdateAndLoadTaxon(taxonomy1, taxa.pop(), "Juglans sp.", TaxonRank.GENUS, 9, taxa.get(0)));
		taxa.push(testInsertAndLoadTaxon(taxonomy1, -4, "JUG3", "Juglans regia", TaxonRank.SPECIES, 9, taxa.get(1)));
		
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
	
	@Test
	public void testDeleteTaxonByTaxonomy() {
		CollectSurvey survey = createAndStoreSurvey();
		
		// Create taxonomy
		CollectTaxonomy t = new CollectTaxonomy();
		t.setSurvey(survey);
		
		t.setName("it_trees");
		taxonomyDao.insert(t);
		
		CollectTaxonomy t2 = new CollectTaxonomy();
		t2.setSurvey(survey);
		t2.setName("it_bamboos");
		taxonomyDao.insert(t2);
		
		Taxon family1 = testInsertAndLoadTaxon(t, -1, "JUGLANDACAE", "Juglandaceae", TaxonRank.FAMILY, 9, null);
		Taxon genus1 = testInsertAndLoadTaxon(t, -2, "JUG", "Juglans sp.",TaxonRank.GENUS, 9, family1);
		testInsertAndLoadTaxon(t, -3, "JUG/REG", "Juglans regia", TaxonRank.SPECIES, 9, genus1);
		
		Taxon family2 = testInsertAndLoadTaxon(t2, -1, "JUGLANDACAE", "Juglandaceae", TaxonRank.FAMILY, 9, null);
		Taxon genus2 = testInsertAndLoadTaxon(t2, -2, "JUG", "Juglans sp.",TaxonRank.GENUS, 9, family2);
		testInsertAndLoadTaxon(t2, -3, "JUG/REG", "Juglans regia", TaxonRank.SPECIES, 9, genus2);
		
		//verify taxon records present
		List<Taxon> results = taxonDao.findByCode(t, "%", 10);
		assertEquals(3, results.size());
		taxonDao.deleteByTaxonomy(t);
		
		//verify all taxon records deleted for taxonomy 1
		List<Taxon> results2 = taxonDao.findByCode(t, "%", 10);
		assertTrue(results2 == null || results2.size() == 0);
		
		//verify all taxon records NOT deleted for taxonomy 2
		List<Taxon> results3 = taxonDao.findByCode(t2, "%", 10);
		assertEquals(3, results3.size());
	}

	private CollectTaxonomy testInsertAndLoadTaxonomy(String name) {
		CollectSurvey survey = createAndStoreSurvey();
		
		// Insert
		CollectTaxonomy t1 = new CollectTaxonomy();
		t1.setName(name);
		t1.setSurvey(survey);
		taxonomyDao.insert(t1);
		CollectTaxonomy t = t1;

		// Confirm saved
		CollectTaxonomy t2 = taxonomyDao.loadById(survey, t.getId());
		assertEquals(t.getId(), t2.getId());
		assertEquals(t.getName(), t2.getName());
		return t2;
	}

	private void testUpdateAndLoadTaxonomy(CollectTaxonomy t, String newName) {
		// Update
		Integer id = t.getId();
		t.setName(newName);
		taxonomyDao.update(t);
		
		// Confirm saved
		t = taxonomyDao.loadById(t.getSurvey(), id);
		assertEquals(newName, t.getName());
	}
	
	private Taxon testInsertAndLoadTaxon(CollectTaxonomy taxonomy, int taxonId, String code, String scientificName, TaxonRank rank, int step, Taxon parent) {
		Integer parentId = parent == null ? null : parent.getSystemId();
		
		// Insert
		Taxon t = new Taxon();
		t.setTaxonId(taxonId);
		t.setCode(code);
		t.setScientificName(scientificName);
		t.setTaxonRank(rank);
		t.setStep(step);
		t.setTaxonomyId(taxonomy.getId());
		t.setParentId(parentId);
		taxonDao.insert(t);
		
		// Confirm saved
		t = taxonDao.loadById((CollectTaxonomy) taxonomy, t.getSystemId());
		assertNotNull(t);
		assertEquals((Integer) taxonId, t.getTaxonId());
		assertEquals(scientificName, t.getScientificName());
		assertEquals(rank, t.getTaxonRank());
		assertEquals(taxonomy.getId(), t.getTaxonomyId());
		assertEquals(step, t.getStep());
		assertEquals(parentId, t.getParentId());
		return t;
	}

	
	private Taxon testUpdateAndLoadTaxon(CollectTaxonomy taxonomy, Taxon t, String scientificName, TaxonRank rank, int step, Taxon parent) {
		Integer parentId = parent == null ? null : parent.getSystemId();
		
		// Insert
		t.setScientificName(scientificName);
		t.setTaxonRank(rank);
		t.setStep(step);
		t.setParentId(parentId);
		taxonDao.update(t);
		
		// Confirm saved
		t = taxonDao.loadById(taxonomy, t.getSystemId());
		assertNotNull(t);
		assertEquals(scientificName, t.getScientificName());
		assertEquals(rank, t.getTaxonRank());
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
		taxonDao.delete(t);
		
		// Confirm deleted
		t = taxonDao.loadById((CollectTaxonomy) t.getTaxonomy(), t.getSystemId());
		assertNull(t);
	}

	private void testDeleteAndLoadTaxonomy(CollectTaxonomy t) {
		// Delete
		taxonomyDao.delete(t);
		
		// Confirm deleted
		t = taxonomyDao.loadById(t.getSurvey(), t.getId());
		assertNull(t);
	}

	protected CollectSurvey createAndStoreSurvey() {
		CollectSurvey survey = createSurvey();
		survey.setName("survey_test");
		survey.setUri("http://www.openforis.org/idm/species_dao_it");
		try {
			surveyManager.importModel(survey);
		} catch (SurveyImportException e) {
			throw new RuntimeException(e);
		}
		return survey;
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
