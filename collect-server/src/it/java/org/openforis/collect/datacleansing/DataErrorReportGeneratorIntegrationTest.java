package org.openforis.collect.datacleansing;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.openforis.idm.testfixture.NodeBuilder.attribute;
import static org.openforis.idm.testfixture.NodeBuilder.entity;
import static org.openforis.idm.testfixture.RecordBuilder.record;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.openforis.collect.CollectIntegrationTest;
import org.openforis.collect.datacleansing.manager.DataErrorQueryManager;
import org.openforis.collect.datacleansing.manager.DataErrorReportManager;
import org.openforis.collect.datacleansing.manager.DataErrorTypeManager;
import org.openforis.collect.manager.RecordManager;
import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.CollectRecord.Step;
import org.openforis.collect.model.CollectSurvey;
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
public class DataErrorReportGeneratorIntegrationTest extends CollectIntegrationTest {

	@Autowired
	private DataErrorReportGenerator reportGenerator;
	@Autowired
	private DataErrorTypeManager dataErrorTypeManager;
	@Autowired
	private DataErrorQueryManager dataErrorQueryManager;
	@Autowired
	private DataErrorReportManager dataErrorReportManager;
	@Autowired
	private RecordManager recordManager;
	
	private CollectSurvey survey;
	private DataErrorType invalidAttributeErrorType;
	private RecordUpdater updater;
	
	@Before
	public void init() throws SurveyImportException, IdmlParseException {
		updater = new RecordUpdater();
		survey = importModel();
		initRecords();
		invalidAttributeErrorType = new DataErrorType(survey);
		invalidAttributeErrorType.setCode("invalid");
		invalidAttributeErrorType.setLabel("Invalid attribute");
		dataErrorTypeManager.save(invalidAttributeErrorType);
	}
	
	@Test
	public void testSimpleQuery() {
		DataErrorQuery query = new DataErrorQuery(survey);
		query.setType(invalidAttributeErrorType);
		EntityDefinition treeDef = (EntityDefinition) survey.getSchema().getDefinitionByPath("/cluster/plot/tree");
		NumberAttributeDefinition dbhDef = (NumberAttributeDefinition) survey.getSchema().getDefinitionByPath("/cluster/plot/tree/dbh");
		query.setTitle("Find trees with invalid DBH");
		query.setEntityDefinition(treeDef);
		query.setAttributeDefinition(dbhDef);
		query.setConditions("dbh > 20");
		
		dataErrorQueryManager.save(query);
		
		DataErrorReport report = reportGenerator.generate(query, Step.ENTRY);
		
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
