package org.openforis.collect.datacleansing;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.openforis.idm.testfixture.NodeBuilder.attribute;
import static org.openforis.idm.testfixture.NodeBuilder.entity;
import static org.openforis.idm.testfixture.RecordBuilder.record;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.openforis.collect.concurrency.CollectJobManager;
import org.openforis.collect.datacleansing.DataErrorQuery.Severity;
import org.openforis.collect.datacleansing.manager.DataErrorQueryGroupManager;
import org.openforis.collect.datacleansing.manager.DataErrorQueryManager;
import org.openforis.collect.datacleansing.manager.DataErrorReportManager;
import org.openforis.collect.datacleansing.manager.DataErrorTypeManager;
import org.openforis.collect.datacleansing.manager.DataQueryManager;
import org.openforis.collect.manager.RecordManager;
import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.CollectRecord.Step;
import org.openforis.collect.model.RecordUpdater;
import org.openforis.collect.persistence.SurveyImportException;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.NumberAttributeDefinition;
import org.openforis.idm.metamodel.xml.IdmlParseException;
import org.openforis.idm.model.RealValue;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * @author A. Modragon
 * @author S. Ricci
 *
 */
public class DataErrorReportGeneratorIntegrationTest extends DataCleansingIntegrationTest {

	@Autowired
	private DataQueryManager dataQueryManager;
	@Autowired
	private DataErrorTypeManager dataErrorTypeManager;
	@Autowired
	private DataErrorQueryManager dataErrorQueryManager;
	@Autowired
	private DataErrorQueryGroupManager dataErrorQueryGroupManager;
	@Autowired
	private DataErrorReportManager dataErrorReportManager;
	@Autowired
	private RecordManager recordManager;
	@Autowired
	private CollectJobManager jobManager;
	
	private DataErrorType invalidAttributeErrorType;
	private RecordUpdater updater;
	
	@Override
	public void init() throws SurveyImportException, IdmlParseException {
		super.init();
		updater = new RecordUpdater();
		initRecords();
		initDataErrorTypes();
	}

	private void initDataErrorTypes() {
		invalidAttributeErrorType = new DataErrorType(survey);
		invalidAttributeErrorType.setCode("invalid");
		invalidAttributeErrorType.setLabel("Invalid attribute");
		dataErrorTypeManager.save(invalidAttributeErrorType);
	}
	
	@Test
	public void testSimpleErrorReport() {
		EntityDefinition treeDef = (EntityDefinition) survey.getSchema().getDefinitionByPath("/cluster/plot/tree");
		NumberAttributeDefinition dbhDef = (NumberAttributeDefinition) survey.getSchema().getDefinitionByPath("/cluster/plot/tree/dbh");
		
		DataQuery query = dataQuery()
			.title("Find trees with invalid DBH")
			.entity(treeDef)
			.attribute(dbhDef)
			.conditions("dbh > 20")
			.build();
		
		dataQueryManager.save(query);
		
		DataErrorQuery dataErrorQuery = dataErrorQuery()
			.type(invalidAttributeErrorType)
			.query(query)
			.severity(Severity.ERROR)
			.build();
		
		dataErrorQueryManager.save(dataErrorQuery);
		
		DataErrorQueryGroup queryGroup = new DataErrorQueryGroup(survey);
		queryGroup.setTitle("Simple query group");
		queryGroup.addQuery(dataErrorQuery);
		
		dataErrorQueryGroupManager.save(queryGroup);
		
		DataErrorReportGeneratorJob job = jobManager.createJob(DataErrorReportGeneratorJob.class);
		job.setErrorQueryGroup(queryGroup);
		job.setRecordStep(Step.ENTRY);
		jobManager.start(job, false);
		DataErrorReport report = job.getReport();
		
		DataErrorReport reloadedReport = dataErrorReportManager.loadById(survey, report.getId());
		
		List<DataErrorReportItem> items = dataErrorReportManager.loadItems(reloadedReport, 0, 100);
		
		assertFalse(items.isEmpty());
		
		assertEquals(1, items.size());
		
		DataErrorReportItem item = items.get(0);
		CollectRecord record = recordManager.load(survey, item.getRecordId());
		assertEquals(Arrays.asList("10_117"), record.getRootEntityKeyValues());
		assertEquals(new RealValue(30.0d, dbhDef.getDefaultUnit()), item.extractAttributeValue());
	}

	private void initRecords() {
		{
			CollectRecord record = (CollectRecord) record(
				attribute("id", "10_114"),
				attribute("region", "001"),
				attribute("district", "002"),
				entity("plot",
					attribute("no", "1"),
					entity("tree",
						attribute("tree_no", "1"),
						attribute("dbh", "10")
					),
					entity("tree",
						attribute("tree_no", "2"),
						attribute("dbh", "20")
					)
				)
			).build(survey, "cluster", "2.0");
			updater.initializeRecord(record);
			recordManager.save(record);
		}
		{
			CollectRecord record = (CollectRecord) record(
				attribute("id", "10_117"),
				attribute("region", "002"),
				attribute("district", "003"),
				entity("plot",
					attribute("no", "1"),
					entity("tree",
						attribute("tree_no", "1"),
						attribute("dbh", "20")
					),
					entity("tree",
						attribute("tree_no", "2"),
						attribute("dbh", "30")
					)
				)
			).build(survey, "cluster", "2.0");
			updater.initializeRecord(record);
			recordManager.save(record);
		}
	}
}
