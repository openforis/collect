package org.openforis.collect.manager;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.openforis.collect.CollectIntegrationTest;
import org.openforis.collect.io.metadata.parsing.ParsingError;
import org.openforis.collect.manager.codelistimport.CodeListImportProcess;
import org.openforis.collect.manager.codelistimport.CodeListImportStatus;
import org.openforis.collect.manager.exception.SurveyValidationException;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.persistence.SurveyStoreException;
import org.openforis.idm.metamodel.CodeList;
import org.openforis.idm.metamodel.CodeListItem;
import org.openforis.idm.metamodel.xml.IdmlParseException;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author S. Ricci
 */
public class CodeListImportProcessIntegrationTest extends CollectIntegrationTest {

	private static final String VALID_TEST_CSV = "code-list-test.csv";
	private static final String VALID_MULTILANG_TEST_CSV = "code-list-multi-lang-test.csv";
	private static final String INVALID_TEST_CSV = "code-list-invalid-test.csv";
	private static final String LANG = "en";
	private static final String TEST_CODE_LIST_NAME = "test";

	@Autowired
	private SurveyManager surveyManager;
	@Autowired
	private CodeListManager codeListManager;
	
	private CollectSurvey survey;
	
	@Before
	public void init() throws IdmlParseException, IOException, SurveyStoreException, SurveyValidationException {
		survey = loadSurvey();
		surveyManager.save(survey);
	}
	
	public CodeListImportProcess importCSVFile(String fileName, CodeList codeList) throws Exception {
		File file = getTestFile(fileName);
		CodeListImportProcess process = new CodeListImportProcess(codeListManager, codeList, LANG, file, true);
		process.call();
		return process;
	}
	
	@Test
	public void testImport() throws Exception {
		CodeList codeList = survey.createCodeList();
		codeList.setName(TEST_CODE_LIST_NAME);
		survey.addCodeList(codeList);
		CodeListImportProcess process = importCSVFile(VALID_TEST_CSV, codeList);
		CodeListImportStatus status = process.getStatus();
		assertTrue(status.isComplete());
		assertTrue(status.getSkippedRows().isEmpty());
		assertEquals(6, status.getProcessed());
		List<CodeListItem> items = codeListManager.loadRootItems(codeList);
		assertEquals(3, items.size());
		{
			CodeListItem item = codeListManager.loadRootItem(codeList, "001", null);
			assertNotNull(item);
			assertEquals("Dodoma", item.getLabel(LANG));
			List<CodeListItem> childItems = codeListManager.loadChildItems(item);
			assertEquals(2, childItems.size());
			CodeListItem childItem = childItems.get(0);
			assertEquals("001", childItem.getCode());
			assertEquals("Kondoa", childItem.getLabel(LANG));
			childItem = childItems.get(1);
			assertEquals("002", childItem.getCode());
			assertEquals("Mpwapwa", childItem.getLabel(LANG));
		}
		{
			CodeListItem item = codeListManager.loadRootItem(codeList, "002", null);
			assertNotNull(item);
			assertEquals("Arusha", item.getLabel(LANG));
			List<CodeListItem> childItems = codeListManager.loadChildItems(item);
			assertEquals(2, childItems.size());
			CodeListItem childItem = childItems.get(0);
			assertEquals("001", childItem.getCode());
			assertEquals("Monduli", childItem.getLabel(LANG));
			childItem = childItems.get(1);
			assertEquals("002", childItem.getCode());
			assertEquals("Arumeru", childItem.getLabel(LANG));
		}
		{
			CodeListItem item = codeListManager.loadRootItem(codeList, "003", null);
			assertNotNull(item);
		}
	}
	
	@Test
	public void testMultiLangImport() throws Exception {
		survey.addLanguage("es");
		CodeList codeList = survey.createCodeList();
		codeList.setName(TEST_CODE_LIST_NAME);
		survey.addCodeList(codeList);
		CodeListImportProcess process = importCSVFile(VALID_MULTILANG_TEST_CSV, codeList);
		CodeListImportStatus status = process.getStatus();
		assertTrue(status.isComplete());
		assertTrue(status.getSkippedRows().isEmpty());
		assertEquals(5, status.getProcessed());
		List<CodeListItem> items = codeListManager.loadRootItems(codeList);
		assertEquals(2, items.size());
		{
			CodeListItem item = codeListManager.loadRootItem(codeList, "001", null);
			assertNotNull(item);
			assertEquals("Dodoma", item.getLabel(LANG));
			assertEquals("Dodoma ES", item.getLabel("es"));
			List<CodeListItem> childItems = codeListManager.loadChildItems(item);
			assertEquals(2, childItems.size());
			CodeListItem childItem = childItems.get(0);
			assertEquals("001", childItem.getCode());
			assertEquals("Kondoa", childItem.getLabel(LANG));
			childItem = childItems.get(1);
			assertEquals("002", childItem.getCode());
			assertEquals("Mpwapwa", childItem.getLabel(LANG));
			assertEquals("Mpwapwa ES", childItem.getLabel("es"));
		}
		{
			CodeListItem item = codeListManager.loadRootItem(codeList, "002", null);
			assertNotNull(item);
			assertEquals("Arusha", item.getLabel(LANG));
			List<CodeListItem> childItems = codeListManager.loadChildItems(item);
			assertEquals(2, childItems.size());
			CodeListItem childItem = childItems.get(0);
			assertEquals("001", childItem.getCode());
			assertEquals("Monduli", childItem.getLabel(LANG));
			assertNull(childItem.getLabel("es"));
			childItem = childItems.get(1);
			assertEquals("002", childItem.getCode());
			assertEquals("Arumeru", childItem.getLabel(LANG));
		}
	}
	
	@Test
	public void testDuplicateValues() throws Exception {
		CodeList codeList = survey.createCodeList();
		codeList.setName(TEST_CODE_LIST_NAME);
		survey.addCodeList(codeList);
		CodeListImportProcess process = importCSVFile(INVALID_TEST_CSV, codeList);
		CodeListImportStatus status = process.getStatus();
		assertTrue(status.isError());
		List<ParsingError> errors = status.getErrors();
		assertTrue(containsError(errors, 4, "region_label_en")); //different label
		assertTrue(containsError(errors, 4, "district_code"));
		assertTrue(containsError(errors, 7, "district_code"));
	}
	
	protected boolean containsError(List<ParsingError> errors, long row,
			String column) {
		for (ParsingError error : errors) {
			if ( error.getRow() == row && Arrays.asList(error.getColumns()).contains(column) ) {
				return true;
			}
		}
		return false;
	}
	
	protected File getTestFile(String fileName) throws URISyntaxException {
		URL fileUrl = ClassLoader.getSystemResource(fileName);
		File file = new File(fileUrl.toURI());
		return file;
	}
}
