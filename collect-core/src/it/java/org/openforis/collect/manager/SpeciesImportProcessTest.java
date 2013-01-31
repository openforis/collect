package org.openforis.collect.manager;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.openforis.idm.model.species.Taxon.TaxonRank.FAMILY;
import static org.openforis.idm.model.species.Taxon.TaxonRank.GENUS;
import static org.openforis.idm.model.species.Taxon.TaxonRank.SPECIES;
import static org.openforis.idm.model.species.Taxon.TaxonRank.SUBSPECIES;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openforis.collect.persistence.TaxonDao;
import org.openforis.collect.persistence.TaxonVernacularNameDao;
import org.openforis.collect.persistence.TaxonomyDao;
import org.openforis.idm.model.TaxonOccurrence;
import org.openforis.idm.model.species.Taxon;
import org.openforis.idm.model.species.TaxonVernacularName;
import org.openforis.idm.model.species.Taxonomy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author S. Ricci
 */
@RunWith( SpringJUnit4ClassRunner.class )
@ContextConfiguration( locations = {"classpath:test-context.xml"} )
@TransactionConfiguration(defaultRollback=true)
@Transactional
public class SpeciesImportProcessTest {

	private static final String TEST_TAXONOMY_NAME = "it_tree";
	
	@Autowired
	private SpeciesManager speciesManager;
	@Autowired
	private TaxonomyDao taxonomyDao;
	@Autowired
	private TaxonDao taxonDao;
	@Autowired
	private TaxonVernacularNameDao taxonVernacularNameDao;
	
	@Before
	public void importCSVFile() throws Exception {
		File file = getCSVTestFile();
		String taxonomyName = TEST_TAXONOMY_NAME;
		SpeciesImportProcess process = new SpeciesImportProcess(speciesManager, taxonomyName, file);
		process.call();
		assertEquals(SpeciesImportProcess.Step.COMPLETE, process.getStep());
	}
	
	@Test
	public void testSpeciesImport() {
		String code = "AFZ/QUA";
		TaxonOccurrence occurrence = findByCode(code);
		TaxonOccurrence expected = new TaxonOccurrence(8, code, "Afzelia quanzensis");
		assertEquals(expected, occurrence);

		String code2 = "ALB/GLA";
		TaxonOccurrence occurrence2 = findByCode(code2);
		TaxonOccurrence expected2 = new TaxonOccurrence(11, code2, "Albizia glaberrima");
		assertEquals(expected2, occurrence2);
		
		String code3 = "ALB/SCH/amaniensis";
		TaxonOccurrence occurrence3 = findByCode(code3);
		TaxonOccurrence expected3 = new TaxonOccurrence(12, code3, "Albizia schimperiana var. amaniensis");
		assertEquals(expected3, occurrence3);
	}

	@Test
	public void testVernacularNamesImport() {
		List<TaxonOccurrence> occurrences = speciesManager.findByVernacularName(TEST_TAXONOMY_NAME, "Mbamba", 10);
		assertNotNull(occurrences);
		assertEquals(1, occurrences.size());
		TaxonOccurrence stored = occurrences.get(0);
		TaxonOccurrence expected = new TaxonOccurrence(8, "AFZ/QUA", "Afzelia quanzensis", "Mbambakofi", "swh", null);
		assertEquals(expected, stored);
		
		List<TaxonOccurrence> occurrences2 = speciesManager.findByVernacularName(TEST_TAXONOMY_NAME, "Mshai-mamba", 10);
		assertNotNull(occurrences2);
		assertEquals(1, occurrences2.size());
		TaxonOccurrence stored2 = occurrences2.get(0);
		TaxonOccurrence expected2 = new TaxonOccurrence(10, "ALB/ADI", "Albizia adianthifolia", "Mshai-mamba", "ksb", null);
		assertEquals(expected2, stored2);
		
		Taxon taxon = findTaxonByCode("ALB/ADI");
		Integer taxonId = taxon.getSystemId();
		List<TaxonVernacularName> vernacularNames = taxonVernacularNameDao.findByTaxon(taxonId);
		assertTrue(contains(vernacularNames, "ksb", "Mchao"));
		assertTrue(contains(vernacularNames, "ksb", "Mkengemshaa"));
		assertTrue(contains(vernacularNames, "ksb", "Msai"));
		assertTrue(contains(vernacularNames, "ksb", "Mshai"));
		assertTrue(contains(vernacularNames, "ksb", "Mshai-mamba"));
		
		assertFalse(contains(vernacularNames, "eng", "Mahogany"));
		assertFalse(contains(vernacularNames, "ksb", "Mahogany"));
	}

	protected boolean contains(List<TaxonVernacularName> taxonVernacularNames, String langCode, String vernacularName) {
		for (TaxonVernacularName taxonVernacularName : taxonVernacularNames) {
			if (vernacularName.equals(taxonVernacularName.getVernacularName()) && 
					langCode.equals(taxonVernacularName.getLanguageCode())) {
				return true;
			}
		}
		return false;
	}
	
	@Test
	public void testHierarchyImport() {
		Taxon subSpecies = findTaxonByCode("ALB/SCH/amaniensis");
		assertNotNull(subSpecies);
		assertEquals(SUBSPECIES, subSpecies.getTaxonRank());
		
		Integer speciesId = subSpecies.getParentId();
		assertNotNull(speciesId);
		Taxon species = taxonDao.loadById(speciesId);
		assertNotNull(species);
		assertNull(species.getCode());
		assertEquals(SPECIES, species.getTaxonRank());
		assertEquals("Albizia schimperiana", species.getScientificName());
		
		Integer genusId = species.getParentId();
		assertNotNull(genusId);
		Taxon genus = taxonDao.loadById(genusId);
		assertNotNull(genus);
		assertNull(genus.getCode());
		assertEquals(GENUS, genus.getTaxonRank());
		assertEquals("Albizia", genus.getScientificName());
		
		Integer familyId = genus.getParentId();
		assertNotNull(familyId);
		Taxon family = taxonDao.loadById(familyId);
		assertNotNull(family);
		assertNull(family.getParentId());
		assertEquals(FAMILY, family.getTaxonRank());
		assertEquals("Fabaceae", family.getScientificName());
	}

	protected Taxon findTaxonByCode(String code) {
		Taxonomy taxonomy = taxonomyDao.load(TEST_TAXONOMY_NAME);
		List<Taxon> results = taxonDao.findByCode(taxonomy.getId(), code, 10);
		assertNotNull(results);
		assertEquals(1, results.size());
		Taxon taxon = results.get(0);
		return taxon;
	}
	
	protected TaxonOccurrence findByCode(String code) {
		List<TaxonOccurrence> occurrences = speciesManager.findByCode(TEST_TAXONOMY_NAME, code, 10);
		assertNotNull(occurrences);
		assertEquals(1, occurrences.size());
		TaxonOccurrence occurrence = occurrences.get(0);
		return occurrence;
	}
	

	protected File getCSVTestFile() throws URISyntaxException {
		URL fileUrl = ClassLoader.getSystemResource("test-species.csv");
		File file = new File(fileUrl.toURI());
		return file;
	}
}
