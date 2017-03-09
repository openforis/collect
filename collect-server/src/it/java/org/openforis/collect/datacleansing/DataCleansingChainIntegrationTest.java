package org.openforis.collect.datacleansing;

import static org.junit.Assert.assertEquals;
import static org.openforis.idm.testfixture.NodeBuilder.attribute;
import static org.openforis.idm.testfixture.NodeBuilder.entity;
import static org.openforis.idm.testfixture.RecordBuilder.record;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.openforis.collect.CollectIntegrationTest;
import org.openforis.collect.concurrency.CollectJobManager;
import org.openforis.collect.datacleansing.DataCleansingStepValue.UpdateType;
import org.openforis.collect.datacleansing.DataQueryExecutorJob.DataQueryExecutorJobInput;
import org.openforis.collect.datacleansing.manager.DataCleansingChainManager;
import org.openforis.collect.datacleansing.manager.DataCleansingStepManager;
import org.openforis.collect.datacleansing.manager.DataQueryManager;
import org.openforis.collect.manager.RecordManager;
import org.openforis.collect.manager.exception.SurveyValidationException;
import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.CollectRecord.Step;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.NodeProcessor;
import org.openforis.collect.model.RecordUpdater;
import org.openforis.collect.persistence.SurveyImportException;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.NumberAttributeDefinition;
import org.openforis.idm.metamodel.TaxonAttributeDefinition;
import org.openforis.idm.metamodel.xml.IdmlParseException;
import org.openforis.idm.model.Node;
import org.openforis.idm.model.TaxonOccurrence;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * @author S. Ricci
 *
 */
public class DataCleansingChainIntegrationTest extends CollectIntegrationTest {

	@Autowired
	private DataCleansingChainManager chainManager;
	@Autowired
	private DataCleansingStepManager stepManager;
	@Autowired
	private DataQueryManager dataQueryManager;
	@Autowired
	private RecordManager recordManager;
	@Autowired
	private CollectJobManager jobManager;
	
	private RecordUpdater updater;
	
	private CollectSurvey survey;
	
	@Before
	public void init() throws SurveyImportException, IdmlParseException, SurveyValidationException {
		updater = new RecordUpdater();
		survey = importModel();
		initRecords();
	}

	@Test
	public void testSimpleChain() {
		DataCleansingChain chain = new DataCleansingChain(survey);
		chain.setTitle("Test chain");
		chain.setDescription("This is just a test");
		
		DataQuery query = new DataQuery(survey);
		EntityDefinition treeDef = (EntityDefinition) survey.getSchema().getDefinitionByPath("/cluster/plot/tree");
		NumberAttributeDefinition dbhDef = (NumberAttributeDefinition) survey.getSchema().getDefinitionByPath("/cluster/plot/tree/dbh");
		query.setTitle("Find trees with invalid DBH");
		query.setEntityDefinition(treeDef);
		query.setAttributeDefinition(dbhDef);
		query.setConditions("dbh > 20");
		dataQueryManager.save(query);
		
		int initialCount = countResults(query);
		assertEquals(1, initialCount);
		
		DataCleansingStep step = new DataCleansingStep(survey);
		step.setTitle("Step 1");
		step.setDescription("This is the step 1");
		step.setQuery(query);
		DataCleansingStepValue updateValue = new DataCleansingStepValue();
		updateValue.setUpdateType(UpdateType.ATTRIBUTE);
		updateValue.setFixExpression("20"); //set dbh = 20
		step.addUpdateValue(updateValue);
		stepManager.save(step);
		
		chain.addStep(step);
		
		chainManager.save(chain);
		
		DataCleansingChainExecutorJob job = jobManager.createJob(DataCleansingChainExecutorJob.class);
		job.setSurvey(survey);
		job.setChain(chain);
		job.setRecordStep(Step.ENTRY);
		jobManager.start(job, false);
		
		int finalCount = countResults(query);
		assertEquals(0, finalCount);
	}

	@Test
	public void testUpdateFieldChain() {
		DataCleansingChain chain = new DataCleansingChain(survey);
		chain.setTitle("Test chain");
		chain.setDescription("This is just a test");
		
		DataQuery query = new DataQuery(survey);
		EntityDefinition treeDef = (EntityDefinition) survey.getSchema().getDefinitionByPath("/cluster/plot/tree");
		TaxonAttributeDefinition speciesDef = (TaxonAttributeDefinition) survey.getSchema().getDefinitionByPath("/cluster/plot/tree/species");
		query.setTitle("Find trees with unlisted species");
		query.setEntityDefinition(treeDef);
		query.setAttributeDefinition(speciesDef);
		query.setConditions("species/@code = 'UNL'");
		dataQueryManager.save(query);
		
		int initialCount = countResults(query);
		assertEquals(1, initialCount);
		
		DataCleansingStep step = new DataCleansingStep(survey);
		step.setTitle("Step 1");
		step.setDescription("This is the step 1");
		step.setQuery(query);
		{
			DataCleansingStepValue updateValue = new DataCleansingStepValue();
			updateValue.setUpdateType(UpdateType.FIELD);
			updateValue.setCondition("idm:blank(species/@code)");
			updateValue.setFieldFixExpressions(Arrays.asList("'UNL'", "'Will not be applied'"));
			step.addUpdateValue(updateValue);
		}
		{
			DataCleansingStepValue updateValue = new DataCleansingStepValue();
			updateValue.setUpdateType(UpdateType.FIELD);
			updateValue.setFieldFixExpressions(Arrays.asList("'PIN'", "'Pinus Sp.'"));
			step.addUpdateValue(updateValue);
		}	
		stepManager.save(step);
		
		chain.addStep(step);
		
		chainManager.save(chain);
		
		DataCleansingChainExecutorJob job = jobManager.createJob(DataCleansingChainExecutorJob.class);
		job.setSurvey(survey);
		job.setChain(chain);
		job.setRecordStep(Step.ENTRY);
		jobManager.start(job, false);
		
		int finalCount = countResults(query);
		assertEquals(0, finalCount);
	}

	private int countResults(DataQuery query) {
		final List<Node<?>> nodes = new ArrayList<Node<?>>();
		DataQueryExecutorJob job = jobManager.createJob(DataQueryExecutorJob.class);
		DataQueryExecutorJobInput dataQueryExecutorJobInput = new DataQueryExecutorJobInput(query, Step.ENTRY, new NodeProcessor() {
			public void process(Node<?> node) throws Exception {
				nodes.add(node);
			}
		});
		job.setInput(dataQueryExecutorJobInput);
		jobManager.start(job, false);
		return nodes.size();
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
			record.setCreationDate(new Date());
			record.setModifiedDate(new Date());
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
						attribute("species", new TaxonOccurrence("UNL", "Unlisted species")),
						attribute("dbh", "20")
					),
					entity("tree",
						attribute("tree_no", "2"),
						attribute("species", new TaxonOccurrence("ACA", "Acacia Sp.")),
						attribute("dbh", "30")
					)
				)
			).build(survey, "cluster", "2.0");
			record.setCreationDate(new Date());
			record.setModifiedDate(new Date());
			updater.initializeRecord(record);
			recordManager.save(record);
		}
	}
}
