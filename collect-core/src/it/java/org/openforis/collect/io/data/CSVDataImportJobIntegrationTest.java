package org.openforis.collect.io.data;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.GregorianCalendar;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.openforis.collect.CollectIntegrationTest;
import org.openforis.collect.concurrency.CollectJobManager;
import org.openforis.collect.io.data.CSVDataImportJob.CSVDataImportInput;
import org.openforis.collect.io.data.CSVDataImportJob.DataParsingError;
import org.openforis.collect.io.data.csv.CSVDataImportSettings;
import org.openforis.collect.io.metadata.parsing.ParsingError;
import org.openforis.collect.io.metadata.parsing.ParsingError.ErrorType;
import org.openforis.collect.manager.RecordManager;
import org.openforis.collect.manager.SurveyManager;
import org.openforis.collect.manager.exception.SurveyValidationException;
import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.CollectRecord.Step;
import org.openforis.collect.model.CollectRecordSummary;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.RecordFilter;
import org.openforis.collect.persistence.RecordDao;
import org.openforis.collect.persistence.SurveyImportException;
import org.openforis.concurrency.Worker.Status;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.xml.IdmlParseException;
import org.openforis.idm.model.Code;
import org.openforis.idm.model.CodeAttribute;
import org.openforis.idm.model.Date;
import org.openforis.idm.model.DateAttribute;
import org.openforis.idm.model.Entity;
import org.openforis.idm.model.EntityBuilder;
import org.openforis.idm.model.IntegerAttribute;
import org.openforis.idm.model.RealAttribute;
import org.openforis.idm.model.RealValue;
import org.openforis.idm.model.TextAttribute;
import org.openforis.idm.model.Time;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author S. Ricci
 */
public class CSVDataImportJobIntegrationTest extends CollectIntegrationTest {

	private static final String VALID_TEST_CSV = "data-import-test.csv";
	private static final String VALID_NESTED_ENTITY_TEST_CSV = "data-import-nested-entity-test.csv";
	private static final String VALID_SINGLE_ENTITY_TEST_CSV = "data-import-single-entity-test.csv";
	private static final String VALID_ENTITY_POSITION_TEST_CSV = "data-import-entity-position-test.csv";
	private static final String INVALID_HEADER_TEST_CSV = "data-import-invalid-header-test.csv";
	private static final String MISSING_REQUIRED_COLUMNS_TEST_CSV ="data-import-missing-required-columns-test.csv";
	private static final String MISSING_RECORD_TEST_CSV ="data-import-missing-record-test.csv";
	private static final String MISSING_PARENT_ENTITY_TEST_CSV ="data-import-missing-parent-entity-test.csv";
	private static final String INVALID_VALUES_TEST_CSV = "data-import-invalid-values-test.csv";
	
	@Autowired
	private SurveyManager surveyManager;
	@Autowired
	private RecordDao recordDao;
	@Autowired
	private RecordManager recordManager;
	
	private CollectSurvey survey;

	private Integer meterUnitId;
	@SuppressWarnings("unused")
	private Integer centimeterUnitId;
	private Integer kilometerUnitId;
	
	@Autowired
	private CollectJobManager jobManager;
	
	@SuppressWarnings("deprecation")
	@Before
	public void init() throws IdmlParseException, IOException, SurveyImportException, SurveyValidationException {
		survey = loadSurvey();
		survey.setTemporary(false);
		surveyManager.importModel(survey);
		meterUnitId = survey.getUnit("m").getId();
		centimeterUnitId = survey.getUnit("cm").getId();
		kilometerUnitId = survey.getUnit("km").getId();
	}
	
	public CSVDataImportJob importCSVFile(String fileName, int parentEntityDefinitionId) throws Exception {
		return importCSVFile(fileName, parentEntityDefinitionId, true);
	}
	
	public CSVDataImportJob importCSVFile(String fileName, int parentEntityDefinitionId, boolean transactional) throws Exception {
		return importCSVFile(fileName, parentEntityDefinitionId, transactional, false, null);
	}

	public CSVDataImportJob importCSVFile(String fileName, int parentEntityDefinitionId, boolean transactional, 
			boolean insertNewRecords, String newRecordVersionName) throws Exception {
		return importCSVFile(fileName, parentEntityDefinitionId, transactional, insertNewRecords, newRecordVersionName, true);
	}
	
	public CSVDataImportJob importCSVFile(String fileName, int parentEntityDefinitionId, boolean transactional, 
			boolean insertNewRecords, String newRecordVersionName, boolean createAncestorEntities) throws Exception {
		CSVDataImportSettings settings = new CSVDataImportSettings();
		settings.setInsertNewRecords(insertNewRecords);
		settings.setNewRecordVersionName(newRecordVersionName);
		settings.setCreateAncestorEntities(createAncestorEntities);
		return importCSVFile(fileName, parentEntityDefinitionId, transactional, settings);
	}
	
	public CSVDataImportJob importCSVFile(String fileName, int parentEntityDefinitionId, boolean transactional, CSVDataImportSettings settings) throws Exception {
		File file = getTestFile(fileName);
		CSVDataImportJob job = jobManager.createJob(transactional ? TransactionalCSVDataImportJob.BEAN_NAME: 
			CSVDataImportJob.BEAN_NAME, CSVDataImportJob.class);
		job.setInput(new CSVDataImportInput(file, survey, Step.values(), parentEntityDefinitionId, settings));
		jobManager.start(job, false);
		return job;
	}
	
	@Test
	public void validTest() throws Exception {
		{
			CollectRecord record = createTestRecord(survey, "10_111");
			recordDao.insert(record);
			assertEquals(Integer.valueOf(0), record.getErrors());
		}
		{
			CollectRecord record = createTestRecord(survey, "10_114");
			recordDao.insert(record);
		}
		EntityDefinition clusterDefn = survey.getSchema().getRootEntityDefinition("cluster");
		
		CSVDataImportJob process = importCSVFile(VALID_TEST_CSV, clusterDefn.getId());
		assertTrue(process.isCompleted());
		assertTrue(process.getParsingErrors().isEmpty());
		{
			CollectRecord reloadedRecord = loadRecord("10_111");
			assertEquals(Integer.valueOf(1), reloadedRecord.getErrors());
			Entity cluster = reloadedRecord.getRootEntity();
			RealAttribute plotDirection = (RealAttribute) cluster.getChild("plot_direction");
			RealValue plotDirectionVal = plotDirection.getValue();
			assertEquals(Double.valueOf(50d), plotDirectionVal.getValue());
			RealAttribute plotDistance = (RealAttribute) cluster.getChild("plot_distance");
			RealValue plotDistanceVal = plotDistance.getValue();
			assertEquals(Double.valueOf(200d), plotDistanceVal.getValue());
			assertEquals(meterUnitId, plotDistanceVal.getUnitId());
			TextAttribute gpsModel = (TextAttribute) cluster.getChild("gps_model");
			assertEquals("GPS MAP 62S", gpsModel.getValue().getValue());
			
			assertEquals(2, cluster.getCount("map_sheet"));
			{
				TextAttribute mapSheet = (TextAttribute) cluster.getChild("map_sheet", 0);
				assertEquals("new map sheet 1", mapSheet.getValue().getValue());
			}
			{
				TextAttribute mapSheet = (TextAttribute) cluster.getChild("map_sheet", 1);
				assertEquals("new map sheet 2", mapSheet.getValue().getValue());
			}
		}
		{
			CollectRecord reloadedRecord = loadRecord("10_114");
			Entity cluster = reloadedRecord.getRootEntity();
			RealAttribute plotDirection = (RealAttribute) cluster.getChild("plot_direction");
			RealValue plotDirectionVal = plotDirection.getValue();
			assertEquals(Double.valueOf(40d), plotDirectionVal.getValue());
			RealAttribute plotDistance = (RealAttribute) cluster.getChild("plot_distance");
			RealValue plotDistanceVal = plotDistance.getValue();
			assertEquals(Double.valueOf(0.3d), plotDistanceVal.getValue());
			assertEquals(kilometerUnitId, plotDistanceVal.getUnitId());
			TextAttribute gpsModel = (TextAttribute) cluster.getChild("gps_model");
			assertEquals("GPS MAP 62S", gpsModel.getValue().getValue());
		}
	}

	@Test
	public void validEntityPositionTest() throws Exception {
		{
			CollectRecord record = createTestRecord(survey, "10_111");
			recordDao.insert(record);
		}
		{
			CollectRecord record = createTestRecord(survey, "10_114");
			recordDao.insert(record);
		}
		EntityDefinition clusterDefn = survey.getSchema().getRootEntityDefinition("cluster");
		EntityDefinition timeStudyDefn = clusterDefn.getChildDefinition("time_study", EntityDefinition.class);
		
		CSVDataImportJob process = importCSVFile(VALID_ENTITY_POSITION_TEST_CSV, timeStudyDefn.getId());
		assertEquals(Status.COMPLETED, process.getStatus());
		assertTrue(process.getParsingErrors().isEmpty());
		{
			CollectRecord reloadedRecord = loadRecord("10_111");
			Entity cluster = reloadedRecord.getRootEntity();
			{
				Entity timeStudy = (Entity) cluster.getChild("time_study", 0);
				DateAttribute date = (DateAttribute) timeStudy.getChild("date");
				Date dateVal = date.getValue();
				assertEquals(Integer.valueOf(2013), dateVal.getYear());
				assertEquals(Integer.valueOf(2), dateVal.getMonth());
				assertEquals(Integer.valueOf(24), dateVal.getDay());
			}
			{
				Entity timeStudy = (Entity) cluster.getChild("time_study", 1);
				DateAttribute date = (DateAttribute) timeStudy.getChild("date");
				Date dateVal = date.getValue();
				assertEquals(Integer.valueOf(2013), dateVal.getYear());
				assertEquals(Integer.valueOf(3), dateVal.getMonth());
				assertEquals(Integer.valueOf(15), dateVal.getDay());
			}
		}
	}

	@Test
	public void nestedEntityTest() throws Exception {
		CollectRecord record = createTestRecord(survey, "10_114");
		recordDao.insert(record);
		EntityDefinition clusterDefn = survey.getSchema().getRootEntityDefinition("cluster");
		EntityDefinition plotDefn = (EntityDefinition) clusterDefn.getChildDefinition("plot");
		CSVDataImportJob process = importCSVFile(VALID_NESTED_ENTITY_TEST_CSV, plotDefn.getId());
		assertTrue(process.isCompleted());
		assertTrue(process.getParsingErrors().isEmpty());
		
		CollectRecord reloadedRecord = recordDao.load(survey, record.getId(), Step.ENTRY);
		Entity reloadedCluster = reloadedRecord.getRootEntity();
		{
			Entity plot = reloadedCluster.findChildEntitiesByKeys("plot", "1", "A").get(0);
			CodeAttribute landUse = (CodeAttribute) plot.getChild("land_use");
			assertEquals("2", landUse.getValue().getCode());
		}
		{
			Entity plot = reloadedCluster.findChildEntitiesByKeys("plot", "2", "B").get(0);
			CodeAttribute landUse = (CodeAttribute) plot.getChild("land_use");
			assertEquals("3", landUse.getValue().getCode());
		}
	}
	
	@Test
	public void validDeleteExistingEntitiesTest() throws Exception {
		CollectRecord record = createTestRecord(survey, "10_114");
		recordDao.insert(record);
		EntityDefinition clusterDefn = survey.getSchema().getRootEntityDefinition("cluster");
		EntityDefinition plotDefn = (EntityDefinition) clusterDefn.getChildDefinition("plot");
		
		assertEquals(3, record.getRootEntity().getCount(plotDefn));
		
		CSVDataImportSettings settings = new CSVDataImportSettings();
		settings.setDeleteExistingEntities(true);
		CSVDataImportJob process = importCSVFile(VALID_NESTED_ENTITY_TEST_CSV, plotDefn.getId(), true, settings);
		assertTrue(process.isCompleted());
		assertTrue(process.getParsingErrors().isEmpty());
		
		CollectRecord reloadedRecord = recordDao.load(survey, record.getId(), Step.ENTRY);
		
		assertEquals(2, reloadedRecord.getRootEntity().getCount(plotDefn));
		
		Entity reloadedCluster = reloadedRecord.getRootEntity();
		{
			Entity plot = reloadedCluster.findChildEntitiesByKeys("plot", "1", "A").get(0);
			CodeAttribute landUse = (CodeAttribute) plot.getChild("land_use");
			assertEquals("2", landUse.getValue().getCode());
		}
		{
			Entity plot = reloadedCluster.findChildEntitiesByKeys("plot", "2", "B").get(0);
			CodeAttribute landUse = (CodeAttribute) plot.getChild("land_use");
			assertEquals("3", landUse.getValue().getCode());
		}
	}
	
	@Test
	public void singleEntityTest() throws Exception {
		{
			CollectRecord record = createTestRecord(survey, "10_114");
			recordDao.insert(record);
		}
		EntityDefinition clusterDefn = survey.getSchema().getRootEntityDefinition("cluster");
		EntityDefinition plotDefn = (EntityDefinition) clusterDefn.getChildDefinition("plot");
		CSVDataImportJob process = importCSVFile(VALID_SINGLE_ENTITY_TEST_CSV, plotDefn.getId());
		assertTrue(process.isCompleted());
		assertTrue(process.getParsingErrors().isEmpty());
		{
			CollectRecord reloadedRecord = loadRecord("10_114");
			Entity reloadedCluster = reloadedRecord.getRootEntity();
			{
				Entity plot = reloadedCluster.findChildEntitiesByKeys("plot", "1", "A").get(0);
				Entity timeStudy = (Entity) plot.getChild("time_study");
				DateAttribute date = (DateAttribute) timeStudy.getChild("date");
				assertEquals(new Date(2012, 2, 15), date.getValue());
			}
			{
				Entity plot = reloadedCluster.findChildEntitiesByKeys("plot", "2", "B").get(0);
				Entity timeStudy = (Entity) plot.getChild("time_study");
				DateAttribute date = (DateAttribute) timeStudy.getChild("date");
				assertEquals(new Date(2013, 5, 18), date.getValue());
			}
		}
	}
	
//	@Test
	//TODO transactional process not working only in test spring context
	public void missingRecordTest() throws Exception {
		{
			CollectRecord record = createTestRecord(survey, "10_111");
			recordDao.insert(record);
		}
		{
			CollectRecord record = createTestRecord(survey, "10_114");
			recordDao.insert(record);
		}
		EntityDefinition clusterDefn = survey.getSchema().getRootEntityDefinition("cluster");
		CSVDataImportJob process = importCSVFile(MISSING_RECORD_TEST_CSV, clusterDefn.getId());

		assertTrue(process.isFailed());
		assertEquals(1, process.getParsingErrors().size());
		{
			ParsingError error = process.getParsingErrors().get(0);
			assertEquals(ErrorType.INVALID_VALUE, error.getErrorType());
			assertEquals(4, error.getRow());
			assertTrue(Arrays.equals(new String[]{"id"}, error.getColumns()));
		}
		
		//verify that the transaction is rolled back properly
		{
			CollectRecord reloadedRecord = loadRecord("10_111");
			Entity cluster = reloadedRecord.getRootEntity();
			RealAttribute plotDistance = (RealAttribute) cluster.getChild("plot_distance");
			RealValue plotDistanceVal = plotDistance.getValue();
			assertEquals(Double.valueOf(100d), plotDistanceVal.getValue());
			assertEquals(meterUnitId, plotDistanceVal.getUnitId());
		}
		{
			CollectRecord reloadedRecord = loadRecord("10_114");
			Entity cluster = reloadedRecord.getRootEntity();
			RealAttribute plotDistance = (RealAttribute) cluster.getChild("plot_distance");
			RealValue plotDistanceVal = plotDistance.getValue();
			assertEquals(Double.valueOf(100d), plotDistanceVal.getValue());
			assertEquals(meterUnitId, plotDistanceVal.getUnitId());
		}
	}
	
	@Test
	public void newRecordsTest() throws Exception {
		EntityDefinition clusterDefn = survey.getSchema().getRootEntityDefinition("cluster");
		CSVDataImportJob process = importCSVFile(VALID_TEST_CSV, clusterDefn.getId(), true, true, "2.0");

		assertTrue(process.isCompleted());
		assertTrue(process.getParsingErrors().isEmpty());
		{
			CollectRecord reloadedRecord = loadRecord("10_111");
			Entity cluster = reloadedRecord.getRootEntity();
			RealAttribute plotDistance = (RealAttribute) cluster.getChild("plot_distance");
			RealValue plotDistanceVal = plotDistance.getValue();
			assertEquals(Double.valueOf(200d), plotDistanceVal.getValue());
			assertEquals(meterUnitId, plotDistanceVal.getUnitId());
		}
		{
			CollectRecord reloadedRecord = loadRecord("10_114");
			Entity cluster = reloadedRecord.getRootEntity();
			RealAttribute plotDistance = (RealAttribute) cluster.getChild("plot_distance");
			RealValue plotDistanceVal = plotDistance.getValue();
			assertEquals(Double.valueOf(0.3d), plotDistanceVal.getValue());
			assertEquals(kilometerUnitId, plotDistanceVal.getUnitId());
		}
	}
	
	@Test
	public void createMissingParentEntityTest() throws Exception {
		{
			CollectRecord record = createTestRecord(survey, "10_111");
			recordDao.insert(record);
		}
		{
			CollectRecord record = createTestRecord(survey, "10_114");
			recordDao.insert(record);
		}
		EntityDefinition clusterDefn = survey.getSchema().getRootEntityDefinition("cluster");
		EntityDefinition plotDefn = (EntityDefinition) clusterDefn.getChildDefinition("plot");
		EntityDefinition treeDefn = (EntityDefinition) plotDefn.getChildDefinition("tree");
		CSVDataImportJob process = importCSVFile(MISSING_PARENT_ENTITY_TEST_CSV, treeDefn.getId(), true, false, null, true);
		assertTrue(process.isCompleted());
		assertTrue(process.getParsingErrors().isEmpty());
		{
			CollectRecord reloadedRecord = loadRecord("10_111");
			Entity cluster = reloadedRecord.getRootEntity();
			Entity plot = (Entity) cluster.getChild("plot", 0);
			assertEquals(3, plot.getCount("tree"));
			Entity tree = (Entity) plot.getChild("tree", 2);
			IntegerAttribute treeNo = (IntegerAttribute) tree.getChild("tree_no");
			assertEquals(Integer.valueOf(2), treeNo.getValue().getValue());
			IntegerAttribute stemNo = (IntegerAttribute) tree.getChild("stem_no");
			assertEquals(Integer.valueOf(2), stemNo.getValue().getValue());
			RealAttribute dbh = (RealAttribute) tree.getChild("dbh");
			assertEquals(Double.valueOf(200), dbh.getValue().getValue());
		}
	}
	
	@Test
	public void missingRequiredColumnsTest() throws Exception {
		EntityDefinition clusterDefn = survey.getSchema().getRootEntityDefinition("cluster");
		EntityDefinition plotDefn = (EntityDefinition) clusterDefn.getChildDefinition("plot");
		CSVDataImportJob process = importCSVFile(MISSING_REQUIRED_COLUMNS_TEST_CSV, plotDefn.getId());
		assertFalse(process.isCompleted());
		assertTrue(process.isFailed());
		
		List<DataParsingError> errors = process.getParsingErrors();
		assertEquals(1, errors.size());
		ParsingError headerError = errors.get(0);
		assertEquals(ErrorType.MISSING_REQUIRED_COLUMNS, headerError.getErrorType());
	}
	
	@Test
	public void invalidHeaderTest() throws Exception {
		EntityDefinition clusterDefn = survey.getSchema().getRootEntityDefinition("cluster");
		EntityDefinition plotDefn = (EntityDefinition) clusterDefn.getChildDefinition("plot");
		CSVDataImportJob process = importCSVFile(INVALID_HEADER_TEST_CSV, plotDefn.getId());
		assertFalse(process.isCompleted());
		assertTrue(process.isFailed());
		
		List<DataParsingError> errors = process.getParsingErrors();
		assertEquals(1, errors.size());
		ParsingError headerError = errors.get(0);
		assertEquals(ErrorType.WRONG_COLUMN_NAME, headerError.getErrorType());
		assertTrue(Arrays.equals(new String[]{"land_usage"}, headerError.getColumns()));
	}
	
	@Test
	public void invalidValuesTest() throws Exception {
		{
			CollectRecord record = createTestRecord(survey, "10_111");
			recordDao.insert(record);
		}
		{
			CollectRecord record = createTestRecord(survey, "10_114");
			recordDao.insert(record);
		}
		EntityDefinition clusterDefn = survey.getSchema().getRootEntityDefinition("cluster");
		CSVDataImportJob process = importCSVFile(INVALID_VALUES_TEST_CSV, clusterDefn.getId());
		assertFalse(process.isCompleted());
		assertTrue(process.isFailed());
		List<DataParsingError> errors = process.getParsingErrors();
		assertEquals(4, errors.size());
		{
			ParsingError error = errors.get(0);
			assertEquals(2, error.getRow());
			assertEquals(ErrorType.INVALID_VALUE, error.getErrorType());
			assertTrue(Arrays.equals(new String[]{"vehicle_location_srs"}, error.getColumns()));
		}
		{
			ParsingError error = errors.get(1);
			assertEquals(4, error.getRow());
			assertEquals(ErrorType.INVALID_VALUE, error.getErrorType());
			assertTrue(Arrays.equals(new String[]{"vehicle_location_x"}, error.getColumns()));
		}
		{
			ParsingError error = errors.get(2);
			assertEquals(4, error.getRow());
			assertEquals(ErrorType.INVALID_VALUE, error.getErrorType());
			assertTrue(Arrays.equals(new String[]{"plot_distance"}, error.getColumns()));
		}
		{
			ParsingError error = errors.get(3);
			assertEquals(5, error.getRow());
			assertEquals(ErrorType.INVALID_VALUE, error.getErrorType());
			assertTrue(Arrays.equals(new String[]{"vehicle_location_y"}, error.getColumns()));
		}
	}
	
	private CollectRecord loadRecord(String key) {
		RecordFilter filter = new RecordFilter(survey);
		filter.setRootEntityId(survey.getSchema().getRootEntityDefinition("cluster").getId());
		filter.setKeyValues(Arrays.asList(key));
		List<CollectRecordSummary> summaries = recordDao.loadSummaries(filter);
		assertEquals(1, summaries.size());
		CollectRecordSummary summary = summaries.get(0);
		CollectRecord reloadedRecord = recordManager.load(survey, summary.getId(), summary.getStep());
		return reloadedRecord;
	}
	
	protected boolean containsError(List<ParsingError> errors, long row,
			String column) {
		for (ParsingError error : errors) {
			if ( error.getRow() == row && Arrays.asList(error.getColumns()).contains(column) ) {
				return true;
			}
		}
		return false;
	}

	private CollectRecord createTestRecord(CollectSurvey survey, String id) {
		CollectRecord record = new CollectRecord(survey, "2.0", "cluster");
		Entity cluster = record.getRootEntity();
		record.setCreationDate(new GregorianCalendar(2011, 11, 31, 23, 59).getTime());
		EntityBuilder.addValue(cluster, "id", new Code(id));
		EntityBuilder.addValue(cluster, "region", new Code("001"));
		EntityBuilder.addValue(cluster, "district", new Code("002"));
		EntityBuilder.addValue(cluster, "plot_distance", 100d, survey.getUnit(meterUnitId));
		EntityBuilder.addValue(cluster, "map_sheet", "map sheet 1");
		EntityBuilder.addValue(cluster, "map_sheet", "map sheet 2");
		EntityBuilder.addValue(cluster, "map_sheet", "map sheet 3");
		{
			Entity timeStudy = EntityBuilder.addEntity(cluster, "time_study");
			EntityBuilder.addValue(timeStudy, "date", new Date(2012, 1, 1));
			EntityBuilder.addValue(timeStudy, "start_time", new Time(9, 10));
			EntityBuilder.addValue(timeStudy, "end_time", new Time(12, 20));
		}
		{
			Entity timeStudy = EntityBuilder.addEntity(cluster, "time_study");
			EntityBuilder.addValue(timeStudy, "date", new Date(2012, 2, 20));
			EntityBuilder.addValue(timeStudy, "start_time", new Time(8, 15));
			EntityBuilder.addValue(timeStudy, "end_time", new Time(11, 10));
		}
		{
			Entity plot = EntityBuilder.addEntity(cluster, "plot");
			EntityBuilder.addValue(plot, "no", new Code("1"));
			EntityBuilder.addValue(plot, "subplot", "A");
			EntityBuilder.addValue(plot, "land_use", new Code("1"));
			{
				Entity tree = EntityBuilder.addEntity(plot, "tree");
				EntityBuilder.addValue(tree, "tree_no", 1);
				EntityBuilder.addValue(tree, "stem_no", 1);
				EntityBuilder.addValue(tree, "dbh", 10.5d);
			}
			{
				Entity tree = EntityBuilder.addEntity(plot, "tree");
				EntityBuilder.addValue(tree, "tree_no", 2);
				EntityBuilder.addValue(tree, "stem_no", 1);
				EntityBuilder.addValue(tree, "dbh", 20.5d);
			}
		}
		{
			Entity plot = EntityBuilder.addEntity(cluster, "plot");
			EntityBuilder.addValue(plot, "no", new Code("2"));
			EntityBuilder.addValue(plot, "subplot", "A");
			EntityBuilder.addValue(plot, "land_use", new Code("2"));
			{
				Entity tree = EntityBuilder.addEntity(plot, "tree");
				EntityBuilder.addValue(tree, "tree_no", 1);
				EntityBuilder.addValue(tree, "stem_no", 1);
				EntityBuilder.addValue(tree, "dbh", 10.5d);
			}
			{
				Entity tree = EntityBuilder.addEntity(plot, "tree");
				EntityBuilder.addValue(tree, "tree_no", 2);
				EntityBuilder.addValue(tree, "stem_no", 1);
				EntityBuilder.addValue(tree, "dbh", 20.5d);
			}
		}
		{
			Entity plot = EntityBuilder.addEntity(cluster, "plot");
			EntityBuilder.addValue(plot, "no", new Code("2"));
			EntityBuilder.addValue(plot, "subplot", "B");
			EntityBuilder.addValue(plot, "land_use", new Code("3"));
			{
				Entity tree = EntityBuilder.addEntity(plot, "tree");
				EntityBuilder.addValue(tree, "tree_no", 1);
				EntityBuilder.addValue(tree, "stem_no", 1);
				EntityBuilder.addValue(tree, "dbh", 10.5d);
			}
			{
				Entity tree = EntityBuilder.addEntity(plot, "tree");
				EntityBuilder.addValue(tree, "tree_no", 2);
				EntityBuilder.addValue(tree, "stem_no", 1);
				EntityBuilder.addValue(tree, "dbh", 20.5d);
			}
		}
		record.updateSummaryFields();
		recordManager.validate(record);
		return record;
	}

	protected File getTestFile(String fileName) throws URISyntaxException {
		URL fileUrl = ClassLoader.getSystemResource(fileName);
		File file = new File(fileUrl.toURI());
		return file;
	}

}
