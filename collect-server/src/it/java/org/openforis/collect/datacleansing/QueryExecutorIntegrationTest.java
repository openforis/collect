package org.openforis.collect.datacleansing;

import static org.junit.Assert.*;
import static org.openforis.idm.testfixture.NodeBuilder.attribute;
import static org.openforis.idm.testfixture.NodeBuilder.entity;
import static org.openforis.idm.testfixture.RecordBuilder.record;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.openforis.collect.CollectIntegrationTest;
import org.openforis.collect.manager.RecordManager;
import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.persistence.SurveyImportException;
import org.openforis.idm.metamodel.xml.IdmlParseException;
import org.openforis.idm.model.Attribute;
import org.openforis.idm.model.Node;
import org.openforis.idm.testfixture.RecordBuilder;
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
		Object query = null;
		QueryResultIterator it = queryExecutor.execute(query);
		assertTrue(it.hasNext());
		while (it.hasNext()) {
			Node<?> node = it.next();
			assertTrue(node instanceof Attribute);
		}
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
