package org.openforis.collect.manager;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.openforis.collect.model.CollectRecord.Step;
import org.openforis.collect.relational.CollectRdbException;
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
public class CollectRDBPublisherIntegrationTest {

	@Autowired
	private CollectRDBPublisher rdbPublisher;
	
	@Test
	public void test() throws CollectRdbException {
		/*
		rdbPublisher.export(
				"naforma1",
				"cluster",
				Step.ANALYSIS,
				"naforma1");
		*/
//		DriverManager.getConnection("jdbc:postgresql://localhost:5433/archenland1", "postgres","postgres")); 
	}
	
}
