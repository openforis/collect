package org.openforis.collect.manager;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.CollectSurveyContext;
import org.openforis.idm.metamodel.FileAttributeDefinition;
import org.openforis.idm.model.FileAttribute;

public class RecordFileManagerTest {

	private static final String UNIQUE_FILE_NAME = "a2b1c3d4-0000-1111-2222-333344445555.jpg";

	@Rule
	public TemporaryFolder tempFolder = new TemporaryFolder();

	private RecordFileManager manager;
	private FileAttributeDefinition defn;

	@Before
	public void init() throws IOException {
		manager = new RecordFileManager();
		manager.storageDirectory = tempFolder.newFolder("upload");
		CollectSurvey survey = (CollectSurvey) new CollectSurveyContext().createSurvey();
		survey.setId(1);
		defn = survey.getSchema().createFileAttributeDefinition();
	}

	@Test
	public void testPlainFileName() {
		File expected = new File(manager.storageDirectory, "1/" + defn.getId() + "/" + UNIQUE_FILE_NAME);
		assertEquals(expected, manager.getRepositoryFile(defn, UNIQUE_FILE_NAME));
		assertEquals(expected.getAbsolutePath(), manager.getRepositoryFileAbsolutePath(defn, UNIQUE_FILE_NAME));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testPathTraversalRejected() {
		manager.getRepositoryFile(defn, "../../../../etc/passwd");
	}

	@Test(expected = IllegalArgumentException.class)
	public void testParentDirectoryRejected() {
		manager.getRepositoryFileAbsolutePath(defn, "..");
	}

	@Test
	public void testMoveFileKeepsUniqueFileName() throws IOException {
		org.openforis.idm.model.File value = manager.moveFileIntoRepository(new FileAttribute(defn),
				tempFolder.newFile("restored.jpg"), UNIQUE_FILE_NAME, false);
		assertEquals(UNIQUE_FILE_NAME, value.getFilename());
	}

	@Test
	public void testMoveFileReplacesTraversalFileName() throws IOException {
		org.openforis.idm.model.File value = manager.moveFileIntoRepository(new FileAttribute(defn),
				tempFolder.newFile("restored.jpg"), "../../" + UNIQUE_FILE_NAME, false);
		assertNotEquals(UNIQUE_FILE_NAME, value.getFilename());
		assertTrue(manager.getRepositoryFile(defn, value.getFilename()).exists());
	}
}
