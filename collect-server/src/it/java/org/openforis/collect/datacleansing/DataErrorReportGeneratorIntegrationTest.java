package org.openforis.collect.datacleansing;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.openforis.collect.CollectIntegrationTest;
import org.openforis.collect.manager.RecordManager;
import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.CollectRecord.Step;
import org.openforis.idm.metamodel.AttributeDefinition;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.model.Attribute;
import org.openforis.idm.model.Node;
import org.springframework.beans.factory.annotation.Autowired;

public class DataErrorReportGeneratorIntegrationTest extends CollectIntegrationTest {

	private CollectSurvey survey;
	@Autowired
	private DataQueryExecutor queryExecutor;
	@Autowired
	private RecordManager recordManager;
	
	public void testSimpleQuery() {
		//select region from tree where dbh > 20
		DataQuery query = new DataQuery(survey);
		EntityDefinition treeDef = (EntityDefinition) survey.getSchema().getDefinitionByPath("/cluster/plot/tree");
		AttributeDefinition dbhDef = (AttributeDefinition) survey.getSchema().getDefinitionByPath("/cluster/plot/tree/dbh");
		query.setEntityDefinition(treeDef);
		query.setAttributeDefinition(dbhDef);
		query.setStep(Step.ENTRY);
		query.setConditions("dbh > 20");
		
		DataQueryResultIterator it = queryExecutor.execute(query);
		assertTrue(it.hasNext());
		
		//first result
		Node<?> node = it.next();
		assertTrue(node instanceof Attribute);
		CollectRecord record = (CollectRecord) node.getRecord();
		assertEquals(Arrays.asList("10_117"), record.getRootEntityKeyValues());
	}
	
}
