/**
 * 
 */
package org.openforis.collect.manager;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.openforis.collect.CollectIntegrationTest;
import org.openforis.collect.manager.exception.SurveyValidationException;
import org.openforis.collect.metamodel.TaxonSummaries;
import org.openforis.collect.metamodel.TaxonSummary;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.CollectTaxonomy;
import org.openforis.collect.model.SamplingDesignItem;
import org.openforis.collect.model.SamplingDesignSummaries;
import org.openforis.collect.persistence.SurveyImportException;
import org.openforis.idm.metamodel.CodeList;
import org.openforis.idm.metamodel.CodeListItem;
import org.openforis.idm.metamodel.PersistedCodeListItem;
import org.openforis.idm.model.species.Taxon;
import org.openforis.idm.model.species.TaxonVernacularName;
import org.openforis.idm.model.species.Taxon.TaxonRank;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author S. Ricci
 *
 */
public class SurveyManagerIntegrationTest extends CollectIntegrationTest {

	private CollectSurvey survey;

	@Autowired
	private SurveyManager surveyManager;
	@Autowired
	private CodeListManager codeListManager;
	@Autowired
	private SpeciesManager speciesManager;
	@Autowired
	private SamplingDesignManager samplingDesignManager;
	
	@Before
	public void init() throws SurveyImportException, SurveyValidationException {
		InputStream is = ClassLoader.getSystemResourceAsStream("test.idm.xml");
		survey = surveyManager.importModel(is, "archenland1", false);
	}
	
	@Test
	public void createTemporarySurveyFromPublishedTest() {
		CollectSurvey surveyWork = surveyManager.createTemporarySurveyFromPublished(survey.getUri());
		assertTrue(surveyWork.isTemporary());
		{
			CodeList list = survey.getCodeList("admin_unit");
			List<CodeListItem> rootItems = codeListManager.loadRootItems(list);
			assertEquals(8, rootItems.size());
			List<CodeListItem> childItems = codeListManager.loadChildItems(rootItems.get(0));
			assertEquals(3, childItems.size());
		}
		{
			CodeList list = surveyWork.getCodeList("admin_unit");
			List<CodeListItem> rootItems = codeListManager.loadRootItems(list);
			assertEquals(8, rootItems.size());
			List<CodeListItem> childItems = codeListManager.loadChildItems(rootItems.get(0));
			assertEquals(3, childItems.size());
		}
	}
	
	@Test
	public void publishSurveyTest() throws SurveyImportException {
		CollectSurvey surveyWork = surveyManager.createTemporarySurveyFromPublished(survey.getUri());
		assertEquals("Archenland NFI", surveyWork.getProjectName());
		
		surveyWork.setProjectName("en", "New Project Name");
		surveyManager.publish(surveyWork);
		
		CollectSurvey survey = surveyManager.getByUri(surveyWork.getUri());
		assertFalse(survey.isTemporary());
		assertEquals("New Project Name", survey.getProjectName("en"));
	}
	
	@Test
	public void publishSurveyCodeListsTest() throws SurveyImportException {
		CollectSurvey surveyWork = surveyManager.createTemporarySurveyFromPublished(survey.getUri());
		{
			//modify item in list
			CodeList list = surveyWork.getCodeList("admin_unit");
			PersistedCodeListItem item = codeListManager.loadRootItem(list, "001", null);
			assertEquals(Integer.valueOf(1), item.getSortOrder());
			item.setCode("001A");
			codeListManager.save(item);
		}
		surveyManager.publish(surveyWork);
		
		CollectSurvey publishedSurvey = surveyManager.getByUri(surveyWork.getUri());
		
		CodeList list = publishedSurvey.getCodeList("admin_unit");
		List<CodeListItem> rootItems = codeListManager.loadRootItems(list);
		assertEquals(8, rootItems.size());
		{
			PersistedCodeListItem item = codeListManager.loadRootItem(list, "001A", null);
			assertEquals(Integer.valueOf(1), item.getSortOrder());
		}
		{
			PersistedCodeListItem item = codeListManager.loadRootItem(list, "001", null);
			assertNull(item);
		}
	}
	
	@Test
	public void publishSurveyTaxonomyTest() throws SurveyImportException {
		insertTestTaxonomy();
		CollectSurvey temporarySurvey = surveyManager.createTemporarySurveyFromPublished(survey.getUri());
		{
			CollectTaxonomy taxonomy = speciesManager.loadTaxonomyByName(temporarySurvey, "tree");
			assertNotNull(taxonomy);
			TaxonSummaries summaries = speciesManager.loadFullTaxonSummariesOld(taxonomy);
			assertEquals(1, summaries.getTotalCount());
			List<TaxonSummary> taxonSummaryList = summaries.getItems();
			{
				TaxonSummary taxonSummary = taxonSummaryList.get(0);
				assertEquals("Albizia glaberrima", taxonSummary.getScientificName());
				
				List<String> vernacularLanguages = taxonSummary.getVernacularLanguages();
				assertEquals(Arrays.asList("swh"), vernacularLanguages);
				
				List<String> vernacularNames = taxonSummary.getVernacularNames("swh");
				assertEquals(Arrays.asList("Mgerenge", "Mchani"), vernacularNames);
			}
		}
	}
	
	@Test
	public void duplicateSurveySamplingDesignForEditTest() {
		insertTestSamplingDesign();
		CollectSurvey surveyWork = surveyManager.createTemporarySurveyFromPublished(survey.getUri());
		SamplingDesignSummaries summaries = samplingDesignManager.loadBySurvey(surveyWork.getId());
		List<SamplingDesignItem> records = summaries.getRecords();
		assertEquals(3, records.size());
		{
			SamplingDesignItem item = records.get(0);
			assertEquals(Arrays.asList("7_81"), item.getLevelCodes());
			assertEquals("EPSG:21035", item.getSrsId());
			assertEquals(Double.valueOf(792200d), item.getX());
			assertEquals(Double.valueOf(9484420d), item.getY());
		}
		{
			SamplingDesignItem item = records.get(1);
			assertEquals(Arrays.asList("7_81", "2"), item.getLevelCodes());
			assertEquals("EPSG:21035", item.getSrsId());
			assertEquals(Double.valueOf(792200d), item.getX());
			assertEquals(Double.valueOf(9484420d), item.getY());
		}
		{
			SamplingDesignItem item = records.get(2);
			assertEquals(Arrays.asList("7_81", "3"), item.getLevelCodes());
			assertEquals("EPSG:21035", item.getSrsId());
			assertEquals(Double.valueOf(792200d), item.getX());
			assertEquals(Double.valueOf(9484670d), item.getY());
		}
	}
	
	private void insertTestSamplingDesign() {
		{
			SamplingDesignItem item = new SamplingDesignItem();
			item.setSurveyId(survey.getId());
			item.setLevelCodes(Arrays.asList("7_81"));
			item.setSrsId("EPSG:21035");
			item.setX(792200d);
			item.setY(9484420d);
			samplingDesignManager.save(item);
		}
		{
			SamplingDesignItem item = new SamplingDesignItem();
			item.setSurveyId(survey.getId());
			item.setLevelCodes(Arrays.asList("7_81", "2"));
			item.setSrsId("EPSG:21035");
			item.setX(792200d);
			item.setY(9484420d);
			samplingDesignManager.save(item);
		}
		{
			SamplingDesignItem item = new SamplingDesignItem();
			item.setSurveyId(survey.getId());
			item.setLevelCodes(Arrays.asList("7_81", "3"));
			item.setSrsId("EPSG:21035");
			item.setX(792200d);
			item.setY(9484670d);
			samplingDesignManager.save(item);
		}
	}

	private void insertTestTaxonomy() {
		CollectTaxonomy taxonomy = new CollectTaxonomy();
		taxonomy.setName("tree");
		taxonomy.setSurvey(survey);
		speciesManager.save(taxonomy);
		Taxon taxon = new Taxon();
		taxon.setTaxonomyId(taxonomy.getId());
		taxon.setCode("ALB/GLA");
		taxon.setScientificName("Albizia glaberrima");
		taxon.setTaxonRank(TaxonRank.GENUS);
		speciesManager.save(taxon);
		{
			TaxonVernacularName vernacularName = new TaxonVernacularName();
			vernacularName.setTaxonSystemId(taxon.getSystemId());
			vernacularName.setVernacularName("Mgerenge");
			vernacularName.setLanguageCode("swh");
			speciesManager.save(vernacularName);
		}
		{
			TaxonVernacularName vernacularName = new TaxonVernacularName();
			vernacularName.setTaxonSystemId(taxon.getSystemId());
			vernacularName.setVernacularName("Mchani");
			vernacularName.setLanguageCode("swh");
			speciesManager.save(vernacularName);
		}
	}
}
