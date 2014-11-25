package org.openforis.collect.datacleansing;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.openforis.idm.testfixture.NodeBuilder.attribute;
import static org.openforis.idm.testfixture.NodeBuilder.entity;
import static org.openforis.idm.testfixture.RecordBuilder.record;

import java.io.IOException;
import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.openforis.collect.CollectIntegrationTest;
import org.openforis.collect.manager.RecordManager;
import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.CollectRecord.Step;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.persistence.SurveyImportException;
import org.openforis.idm.metamodel.AttributeDefinition;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.xml.IdmlParseException;
import org.openforis.idm.model.Attribute;
import org.openforis.idm.model.Node;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * @author S. Ricci
 *
 */
public class QueryExecutorIntegrationTest extends CollectIntegrationTest {

	@Autowired
	private RecordManager recordManager;
	@Autowired
	private QueryExecutor queryExecutor;
	
	private CollectSurvey survey;
	
	@Before
	public void init() throws IdmlParseException, IOException, SurveyImportException {
		survey = importModel();
		initRecords();
 	}
	
	@Test
	public void testSimpleQuery() {
		//select region from tree where dbh > 20
		Query query = new Query(survey);
		EntityDefinition treeDef = (EntityDefinition) survey.getSchema().getDefinitionByPath("/cluster/plot/tree");
		AttributeDefinition dbhDef = (AttributeDefinition) survey.getSchema().getDefinitionByPath("/cluster/plot/tree/dbh");
		query.setEntityDefinition(treeDef);
		query.setAttributeDefinition(dbhDef);
		query.setStep(Step.ENTRY);
		query.setConditions("dbh > 20");
		
		QueryResultIterator it = queryExecutor.execute(query);
		assertTrue(it.hasNext());
		
		//first result
		Node<?> node = it.next();
		assertTrue(node instanceof Attribute);
		CollectRecord record = (CollectRecord) node.getRecord();
		assertEquals(Arrays.asList("10_117"), record.getRootEntityKeyValues());
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
			).build(survey, "cluster");
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
			).build(survey, "cluster");
			recordManager.save(record);
		}
	}
	
}
