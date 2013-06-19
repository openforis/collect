package org.openforis.collect.manager;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openforis.collect.CollectIntegrationTest;
import org.openforis.collect.manager.codelistimport.CodeListImportProcess;
import org.openforis.collect.manager.codelistimport.CodeListImportStatus;
import org.openforis.collect.manager.referencedataimport.ParsingError;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.persistence.SurveyImportException;
import org.openforis.idm.metamodel.CodeList;
import org.openforis.idm.metamodel.CodeList.CodeScope;
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
public class CodeListImportProcessIntegrationTest extends CollectIntegrationTest {

	private static final String VALID_TEST_CSV = "code-list-test.csv";
	private static final String INVALID_TEST_CSV = "code-list-invalid-test.csv";
	private static final String INVALID_SCHEME_SCOPE_TEST_CSV = "code-list-invalid-scheme-scope-test.csv";
	private static final String LANG = "en";
	private static final String TEST_CODE_LIST_NAME = "test";

	@Autowired
	private SurveyManager surveyManager;
	@Autowired
	private CodeListManager codeListManager;
	
	private CollectSurvey survey;
	
	@Before
	public void init() throws IdmlParseException, IOException, SurveyImportException {
		survey = loadSurvey();
		surveyManager.saveSurveyWork(survey);
	}
	
	public CodeListImportProcess importCSVFile(String fileName, CodeList codeList, CodeScope codeScope) throws Exception {
		File file = getTestFile(fileName);
		CodeListImportProcess process = new CodeListImportProcess(codeListManager, codeList, codeScope, LANG, file, true);
		process.call();
		return process;
	}
	
	@Test
	public void testImport() throws Exception {
		CodeList codeList = survey.createCodeList();
		codeList.setName(TEST_CODE_LIST_NAME);
		survey.addCodeList(codeList);
		CodeListImportProcess process = importCSVFile(VALID_TEST_CSV, codeList, CodeScope.LOCAL);
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
	}
	
	@Test
	public void testDuplicateValues() throws Exception {
		CodeList codeList = survey.createCodeList();
		codeList.setName(TEST_CODE_LIST_NAME);
		survey.addCodeList(codeList);
		CodeListImportProcess process = importCSVFile(INVALID_TEST_CSV, codeList, CodeScope.LOCAL);
		CodeListImportStatus status = process.getStatus();
		assertTrue(status.isError());
		List<ParsingError> errors = status.getErrors();
		assertTrue(containsError(errors, 4, "region_code"));
		assertTrue(containsError(errors, 4, "district_code"));
		assertTrue(containsError(errors, 7, "district_code"));
	}
	
	@Test
	public void testDuplicateValuesSchemeScope() throws Exception {
		CodeList codeList = survey.createCodeList();
		codeList.setName(TEST_CODE_LIST_NAME);
		survey.addCodeList(codeList);
		CodeListImportProcess process = importCSVFile(INVALID_SCHEME_SCOPE_TEST_CSV, codeList, CodeScope.SCHEME);
		CodeListImportStatus status = process.getStatus();
		assertTrue(status.isError());
		List<ParsingError> errors = status.getErrors();
		assertTrue(containsError(errors, 4, "region_code"));
		assertTrue(containsError(errors, 5, "district_code"));
		assertTrue(containsError(errors, 6, "district_code"));
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
