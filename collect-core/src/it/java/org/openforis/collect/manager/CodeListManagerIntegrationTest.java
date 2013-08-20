/**
 * 
 */
package org.openforis.collect.manager;

import static org.junit.Assert.assertEquals;

import java.io.InputStream;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.openforis.collect.CollectIntegrationTest;
import org.openforis.collect.manager.exception.SurveyValidationException;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.persistence.SurveyImportException;
import org.openforis.idm.metamodel.CodeList;
import org.openforis.idm.metamodel.CodeListItem;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author S. Ricci
 *
 */
public class CodeListManagerIntegrationTest extends CollectIntegrationTest {
	
	private static final String IDM_TEST_XML = "test.idm.xml";
	
	@Autowired
	private CodeListManager codeListManager;
	
	private CollectSurvey survey;
	
	@Before
	public void init() throws SurveyImportException, SurveyValidationException {
		InputStream is = ClassLoader.getSystemResourceAsStream(IDM_TEST_XML);
		survey = surveyManager.importModel(is, "archenland1", false);
	}
	
	@Test
	public void loadItemsByLevelTest() {
		CodeList list = survey.getCodeList("admin_unit");
		List<CodeListItem> rootItems = codeListManager.loadItems(list, 1);
		assertEquals(8, rootItems.size());
		{
			CodeListItem item = rootItems.get(1);
			assertEquals("002", item.getCode());
		}
		List<CodeListItem> secondLevelItems = codeListManager.loadItems(list, 2);
		assertEquals(23, secondLevelItems.size());
		{
			CodeListItem item = secondLevelItems.get(1);
			assertEquals("002", item.getCode());
		}
		
	}
	
}
