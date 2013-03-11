package org.openforis.collect.manager;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openforis.collect.CollectIntegrationTest;
import org.openforis.collect.manager.codeListImport.CodeListImportProcess;
import org.openforis.collect.manager.codeListImport.CodeListImportStatus;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.persistence.SurveyImportException;
import org.openforis.idm.metamodel.CodeList;
import org.openforis.idm.metamodel.CodeListItem;
import org.openforis.idm.metamodel.xml.IdmlParseException;
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
public class CodeListImportProcessTest extends CollectIntegrationTest {

	private static final String VALID_TEST_CSV = "test-code-list.csv";
	//private static final String INVALID_TEST_CSV = "test-code-list.csv";
	private static final String LANG = "en";
	private static final String TEST_CODE_LIST_NAME = "test";

	@Autowired
	private SurveyManager surveyManager;
	
	private CollectSurvey survey;
	
	@Before
	public void init() throws IdmlParseException, IOException, SurveyImportException {
		survey = loadSurvey();
		surveyManager.saveSurveyWork(survey);
	}
	
	public CodeListImportProcess importCSVFile(String fileName) throws Exception {
		File file = getTestFile(fileName);
		CodeList codeList = survey.createCodeList();
		codeList.setName(TEST_CODE_LIST_NAME);
		CodeListImportProcess process = new CodeListImportProcess(codeList, LANG, file, true);
		survey.addCodeList(codeList);
		process.call();
		return process;
	}
	
	@Test
	public void testImport() throws Exception {
		CodeListImportProcess process = importCSVFile(VALID_TEST_CSV);
		CodeListImportStatus status = process.getStatus();
		assertTrue(status.isComplete());
		assertTrue(status.getSkippedRows().isEmpty());
		assertEquals(5, status.getProcessed());
		CodeList codeList = survey.getCodeList(TEST_CODE_LIST_NAME);
		List<CodeListItem> items = codeList.getItems();
		assertEquals(2, items.size());
		{
			CodeListItem item = codeList.getItem("001");
			assertNotNull(item);
			assertEquals("Dodoma", item.getLabel(LANG));
			List<CodeListItem> childItems = item.getChildItems();
			assertEquals(2, childItems.size());
			CodeListItem childItem = childItems.get(0);
			assertEquals("001", childItem.getCode());
			assertEquals("Kondoa", childItem.getLabel(LANG));
			childItem = childItems.get(1);
			assertEquals("002", childItem.getCode());
			assertEquals("Mpwapwa", childItem.getLabel(LANG));
		}
		{
			CodeListItem item = codeList.getItem("002");
			assertNotNull(item);
			assertEquals("Arusha", item.getLabel(LANG));
			List<CodeListItem> childItems = item.getChildItems();
			assertEquals(2, childItems.size());
			CodeListItem childItem = childItems.get(0);
			assertEquals("001", childItem.getCode());
			assertEquals("Monduli", childItem.getLabel(LANG));
			childItem = childItems.get(1);
			assertEquals("002", childItem.getCode());
			assertEquals("Arumeru", childItem.getLabel(LANG));
		}
	}
	
	protected File getTestFile(String fileName) throws URISyntaxException {
		URL fileUrl = ClassLoader.getSystemResource(fileName);
		File file = new File(fileUrl.toURI());
		return file;
	}
}
