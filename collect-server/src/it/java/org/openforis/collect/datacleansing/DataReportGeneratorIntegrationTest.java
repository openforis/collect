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
import org.openforis.collect.datacleansing.DataQuery.ErrorSeverity;
import org.openforis.collect.datacleansing.manager.DataQueryGroupManager;
import org.openforis.collect.datacleansing.manager.DataQueryManager;
import org.openforis.collect.datacleansing.manager.DataQueryTypeManager;
import org.openforis.collect.datacleansing.manager.DataReportManager;
import org.openforis.collect.manager.RecordManager;
import org.openforis.collect.manager.exception.SurveyValidationException;
import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.CollectRecord.Step;
import org.openforis.collect.model.RecordUpdater;
import org.openforis.collect.persistence.SurveyImportException;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.NumberAttributeDefinition;
import org.openforis.idm.metamodel.Unit;
import org.openforis.idm.metamodel.xml.IdmlParseException;
import org.openforis.idm.model.RealValue;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * @author A. Modragon
 * @author S. Ricci
 *
 */
public class DataReportGeneratorIntegrationTest extends DataCleansingIntegrationTest {

	@Autowired
	private DataQueryManager dataQueryManager;
	@Autowired
	private DataQueryTypeManager dataQueryTypeManager;
	@Autowired
	private DataQueryGroupManager dataQueryGroupManager;
	@Autowired
	private DataReportManager dataReportManager;
	@Autowired
	private RecordManager recordManager;
	@Autowired
	private CollectJobManager jobManager;
	
	private DataQueryType invalidAttributeErrorType;
	private RecordUpdater updater;
	
	@Override
	public void init() throws SurveyImportException, IdmlParseException, SurveyValidationException {
		super.init();
		updater = new RecordUpdater();
		initRecords();
		initDataQueryTypes();
	}

	private void initDataQueryTypes() {
		invalidAttributeErrorType = new DataQueryType(survey);
		invalidAttributeErrorType.setCode("invalid");
		invalidAttributeErrorType.setLabel("Invalid attribute");
		dataQueryTypeManager.save(invalidAttributeErrorType, adminUser);
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
			.type(invalidAttributeErrorType)
			.severity(ErrorSeverity.ERROR)
			.build();
		
		dataQueryManager.save(query, adminUser);
		
		DataQueryGroup queryGroup = new DataQueryGroup(survey);
		queryGroup.setTitle("Simple query group");
		queryGroup.addQuery(query);
		
		dataQueryGroupManager.save(queryGroup, adminUser);
		
		DataReportGeneratorJob job = jobManager.createJob(DataReportGeneratorJob.class);
		job.setQueryGroup(queryGroup);
		job.setRecordStep(Step.ENTRY);
		job.setActiveUser(adminUser);
		jobManager.start(job, false);
		DataReport report = job.getReport();
		
		DataReport reloadedReport = dataReportManager.loadById(survey, report.getId());
		
		List<DataReportItem> items = dataReportManager.loadItems(reloadedReport, 0, 100);
		
		assertFalse(items.isEmpty());
		
		assertEquals(1, items.size());
		
		DataReportItem item = items.get(0);
		CollectRecord record = recordManager.load(survey, item.getRecordId());
		assertEquals(Arrays.asList("10_117"), record.getRootEntityKeyValues());
		Unit defaultUnit = dbhDef.getDefaultUnit();
		Integer unitId = defaultUnit == null ? null : defaultUnit.getId();
		assertEquals(new RealValue(30.0d, unitId), item.extractAttributeValue());
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
