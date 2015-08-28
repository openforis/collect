package org.openforis.collect.datacleansing;

import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.Test;
import org.openforis.collect.concurrency.CollectJobManager;
import org.openforis.collect.datacleansing.io.DataCleansingImportTask;
import org.openforis.collect.datacleansing.manager.DataCleansingMetadataManager;
import org.openforis.collect.utils.Dates;
import org.openforis.commons.lang.DeepComparable;
import org.openforis.concurrency.Job;
import org.openforis.concurrency.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

/**
 * 
 * @author S. Ricci
 *
 */
public class CleansingMetadataIntegrationTest extends DataCleansingIntegrationTest {

	@Autowired
	private ApplicationContext applicationContext;
	@Autowired
	private CollectJobManager jobManager;
	@Autowired
	private DataCleansingMetadataManager dataCleansingMetadataManager;
	
	@Test
	public void importTest() {
		DataCleansingMetadata metadata = importMetadataTestFile("cleansing-metadata-test.json");
		DataCleansingMetadata expectedMetadata = createTestMetadata();
		assertDeepEquals(expectedMetadata, metadata);
	}

	@Test
	public void roundTripTest() {
		DataCleansingMetadata metadata = createTestMetadata();
		dataCleansingMetadataManager.saveMetadata(survey, metadata);
		
		DataCleansingMetadata reloadedMetadata = dataCleansingMetadataManager.loadMetadata(survey);
		assertDeepEquals(metadata, reloadedMetadata);
	}

	private DataCleansingMetadata importMetadataTestFile(final String testFileName) {
		Job job = new Job() {
			protected void buildTasks() throws Throwable {
				File file = getSystemResourceFile(testFileName);
				DataCleansingImportTask importTask = applicationContext.getBean(DataCleansingImportTask.class);
				importTask.setInputFile(file);
				importTask.setSurvey(survey);
				addTask((Task) importTask);
			}
		};
		jobManager.start(job, false);
		DataCleansingMetadata metadata = dataCleansingMetadataManager.loadMetadata(survey);
		return metadata;
	}

	private DataCleansingMetadata createTestMetadata() {
		DataQueryBuilder dataQueryBuilder1 = dataQuery()
				.uuid("98beaf86-7a03-4593-9171-a6643e8cb451")
				.entityId(727)
				.attributeId(747)
				.title("Empty cluster remarks")
				.description("Query 1 description")
				.conditions("idm:blank($this)")
				.creationDate(Dates.parseDateTime("2015-08-26T11:57:53.312+02:00"))
				.modifiedDate(Dates.parseDateTime("2015-08-27T17:28:35.889+02:00"));

		DataQueryBuilder dataQueryBuilder2 = dataQuery()
				.uuid("2e697d32-6ce5-4a06-b174-7873f1702702")
				.entityId(748)
				.attributeId(813)
				.title("Empty plot remarks")
				.description("Query 2 description")
				.conditions("idm:blank($this)")
				.creationDate(Dates.parseDateTime("2015-08-26T11:58:32.031+02:00"))
				.modifiedDate(Dates.parseDateTime("2015-08-27T17:28:35.089+02:00"));
			
		DataErrorTypeBuilder errorTypeBuilder1 = dataErrorType()
				.uuid("28ddb51f-8499-4dea-be9c-0c08d93918ff")
				.code("E1")
				.label("Error 1")
				.description("Error 1 description")
				.creationDate(Dates.parseDateTime("2015-08-27T17:40:01.741+02:00"))
				.modifiedDate(Dates.parseDateTime("2015-08-27T18:45:01.741+02:00"));
		
		DataErrorTypeBuilder errorTypeBuilder2 = dataErrorType()
				.uuid("b1349656-6115-4087-8bf4-6f49b4004809")
				.code("E2")
				.label("Error 2")
				.description("Error 2 description")
				.creationDate(Dates.parseDateTime("2015-08-27T17:40:01.741+02:00"))
				.modifiedDate(Dates.parseDateTime("2015-08-27T18:45:01.741+02:00"));
		
		DataCleansingStepBuilder cleansingStepBuilder1 = dataCleansingStep()
				.uuid("02fd4fb7-505c-4b0b-8868-1c6adef3c4ac")
				.title("Update empty sampling unit notes with \"NA\"")
				.description("Calculation step 1 description")
				.query(dataQueryBuilder1.build())
				.fixExpression("\"NA\"")
				.creationDate(Dates.parseDateTime("2015-08-27T15:25:01.928+02:00"))
				.modifiedDate(Dates.parseDateTime("2015-08-27T17:28:35.896+02:00"));
			
		DataCleansingStepBuilder cleansingStepBuilder2 = dataCleansingStep()
				.uuid("fb279de6-fa19-4f55-841d-8d8d700a68f7")
				.title("Update empty plot notes with \"NA\"")
				.description("Calculation step 2 description")
				.query(dataQueryBuilder2.build())
				.fixExpression("\"NA\"")
				.creationDate(Dates.parseDateTime("2015-08-27T17:29:24.325+02:00"))
				.modifiedDate(Dates.parseDateTime("2015-08-27T17:30:23.519+02:00"));
		
		DataCleansingMetadata expectedMetadata = metadata(
			dataQueryBuilder1
			, dataQueryBuilder2
			, errorTypeBuilder1
			, errorTypeBuilder2
			, dataErrorQuery()
				.uuid("89b9682f-9267-41da-80e2-f8a5444f3cae")
				.type(errorTypeBuilder1.build())
				.query(dataQueryBuilder1.build())
				.creationDate(Dates.parseDateTime("2015-08-27T17:40:01.741+02:00"))
				.modifiedDate(Dates.parseDateTime("2015-08-27T18:45:01.741+02:00"))
			, cleansingStepBuilder1
			, cleansingStepBuilder2
			, dataCleansingChain()
				.uuid("ff917a6a-b9dd-429d-b187-8205cd280238")
			    .title("Replace empty values")
			    .description("Chain 1 description")
			    .creationDate(Dates.parseDateTime("2015-08-26T12:15:00.936+02:00"))
			    .modifiedDate(Dates.parseDateTime("2015-08-26T12:15:00.936+02:00"))
			    .step(cleansingStepBuilder1.build())
			    .step(cleansingStepBuilder2.build())
			).build();
		
		return expectedMetadata;
	}

	private void assertDeepEquals(DeepComparable obj1, DeepComparable obj2) {
		assertTrue(obj1.deepEquals(obj2));
	}
	
	
}
