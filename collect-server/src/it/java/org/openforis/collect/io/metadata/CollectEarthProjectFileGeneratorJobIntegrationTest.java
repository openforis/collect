package org.openforis.collect.io.metadata;

import static org.junit.Assert.*;

import java.io.File;

import org.junit.Test;
import org.openforis.collect.CollectIntegrationTest;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.concurrency.JobManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * @author S. Ricci
 *
 */
public class CollectEarthProjectFileGeneratorJobIntegrationTest extends CollectIntegrationTest {

	@Autowired
	private JobManager springJobManager;
	
	@Test
	public void testFileGeneration() throws Exception  {
		CollectSurvey survey = loadSurvey();
		
		CollectEarthProjectFileCreatorJob job = springJobManager.createJob(CollectEarthProjectFileCreatorJob.class);
		job.setSurvey(survey);
		
		springJobManager.start(job, false);
		
		assertTrue(job.isCompleted());
		
		File outputFile = job.getOutputFile();
		assertNotNull(outputFile);
	}
	
}
