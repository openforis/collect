/**
 * 
 */
package org.openforis.collect.persistence;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.openforis.collect.manager.RecordManager;
import org.openforis.collect.manager.SurveyManager;
import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.persistence.xml.CollectIdmlBindingContext;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.Schema;
import org.openforis.idm.metamodel.xml.InvalidIdmlException;
import org.openforis.idm.metamodel.xml.SurveyUnmarshaller;
import org.openforis.idm.model.Entity;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author M. Togna
 * 
 */
//@RunWith(SpringJUnit4ClassRunner.class)
//@ContextConfiguration(locations = { "classpath:test-context.xml" })
//@TransactionConfiguration(defaultRollback = true)
//@Transactional
public class SurveyManagerIntegrationTest {
	private static final String SURVEY_NAME = "archenland1";

	protected CollectSurvey survey;
	protected EntityDefinition clusterEntityDefinition;
	protected Entity cluster;
	
	@Autowired
	private SurveyManager surveyManager;

	@Autowired
	private RecordManager recordManager;
	
//	@Before
	public void before() throws SurveyImportException, IOException, InvalidIdmlException {
		if (survey == null) {
			survey = importModel();
		}
		Schema schema = survey.getSchema();
		clusterEntityDefinition = schema.getRootEntityDefinition("cluster");
		CollectRecord record = new CollectRecord(recordManager, survey, "2.0");
		cluster = record.createRootEntity("cluster");
	}

//	@Test
//	public void testEmptyDependencies() {
//		ModelDependencies dependencies = surveyManager.getSurveyDependencies(SURVEY_NAME);
//		
//		EntityDefinition timeStudy = (EntityDefinition) clusterEntityDefinition.getChildDefinition("time_study");
//		NodeDefinition endTimeDefn = timeStudy.getChildDefinition("end_time");
//		Set<String> paths = dependencies.getDependantPaths(endTimeDefn);
//		Assert.assertTrue(paths.isEmpty());
//	}
//
//	@Test
//	public void testNonEmptyStartTime() {
//		ModelDependencies dependencies = surveyManager.getSurveyDependencies(SURVEY_NAME);
//		NodeDefinition endTimeDefn = ((EntityDefinition) clusterEntityDefinition.getChildDefinition("time_study")).getChildDefinition("start_time");
//		Set<String> paths = dependencies.getDependantPaths(endTimeDefn);
//		Assert.assertFalse(paths.isEmpty());
//		Assert.assertEquals(1, paths.size());
//		String path = paths.iterator().next();
//		Assert.assertEquals("parent()/end_time", path);
//	}
//
//	@Test
//	public void testRequiredDependency() {
//		ModelDependencies dependencies = surveyManager.getSurveyDependencies(SURVEY_NAME);
//		NodeDefinition definition = ((EntityDefinition) clusterEntityDefinition.getChildDefinition("plot")).getChildDefinition("share");
//		Set<String> paths = dependencies.getDependantPaths(definition);
//		Assert.assertFalse(paths.isEmpty());
//		Assert.assertEquals(1, paths.size());
//		String path = paths.iterator().next();
//		Assert.assertEquals("parent()/subplot", path);
//	}
//
//	@Test
//	public void testRelevantDependency() {
//		ModelDependencies dependencies = surveyManager.getSurveyDependencies(SURVEY_NAME);
//		NodeDefinition definition = ((EntityDefinition) clusterEntityDefinition.getChildDefinition("plot")).getChildDefinition("vegetation_type");
//		Set<String> paths = dependencies.getDependantPaths(definition);
//		Assert.assertFalse(paths.isEmpty());
//		Assert.assertEquals(15, paths.size());
//	}

	private CollectSurvey importModel() throws IOException, SurveyImportException, InvalidIdmlException {
		URL idm = ClassLoader.getSystemResource("test.idm.xml");
		InputStream is = idm.openStream();
		CollectIdmlBindingContext idmlBindingContext = new CollectIdmlBindingContext();
		SurveyUnmarshaller surveyUnmarshaller = idmlBindingContext.createSurveyUnmarshaller();
		CollectSurvey survey = (CollectSurvey) surveyUnmarshaller.unmarshal(is);
		surveyManager.importModel(survey);
		return survey;
	}
}
