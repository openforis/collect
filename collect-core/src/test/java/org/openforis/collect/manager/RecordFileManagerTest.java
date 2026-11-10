package org.openforis.collect.manager;

import static org.junit.Assert.assertEquals;

import java.io.File;

import org.junit.Before;
import org.junit.Test;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.CollectSurveyContext;
import org.openforis.idm.metamodel.FileAttributeDefinition;

public class RecordFileManagerTest {

	private RecordFileManager manager;
	private FileAttributeDefinition defn;

	@Before
	public void init() {
		manager = new RecordFileManager();
		manager.storageDirectory = new File("/tmp/collect-upload");
		CollectSurvey survey = (CollectSurvey) new CollectSurveyContext().createSurvey();
		survey.setId(1);
		defn = survey.getSchema().createFileAttributeDefinition();
	}

	@Test
	public void testPlainFileName() {
		File file = manager.getRepositoryFile(defn, "a2b1c3d4-0000-1111-2222-333344445555.jpg");
		assertEquals(new File(manager.storageDirectory, "1/" + defn.getId() + "/a2b1c3d4-0000-1111-2222-333344445555.jpg"), file);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testPathTraversalRejected() {
		manager.getRepositoryFile(defn, "../../../../etc/passwd");
	}

	@Test(expected = IllegalArgumentException.class)
	public void testParentDirectoryRejected() {
		manager.getRepositoryFileAbsolutePath(defn, "..");
	}
}
