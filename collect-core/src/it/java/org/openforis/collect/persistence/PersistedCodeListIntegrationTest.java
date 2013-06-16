/**
 * 
 */
package org.openforis.collect.persistence;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.InputStream;
import java.util.List;

import org.junit.Test;
import org.openforis.collect.CollectIntegrationTest;
import org.openforis.collect.manager.CodeListManager;
import org.openforis.collect.manager.exception.SurveyValidationException;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.idm.metamodel.CodeList;
import org.openforis.idm.metamodel.PersistedCodeListItem;
import org.openforis.idm.metamodel.xml.IdmlParseException;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author S. Ricci
 *
 */
public class PersistedCodeListIntegrationTest extends CollectIntegrationTest {

	private static final String IDM_TEST_XML = "test.idm.xml";
	private static final String EN_LANG_CODE = "en";
	
	@Autowired
	private CodeListManager codeListManager;
	
	@Test
	public void importFromXMLTest() throws IdmlParseException,
			SurveyImportException, SurveyValidationException {
		InputStream is = ClassLoader.getSystemResourceAsStream(IDM_TEST_XML);
		CollectSurvey survey = surveyManager.updateModel(is, false);
		{
			CodeList list = survey.getCodeList("measurement");
			List<PersistedCodeListItem> rootItems = codeListManager.loadRootItems(list);
			assertEquals(3, rootItems.size());
			{
				PersistedCodeListItem item = rootItems.get(0);
				assertEquals("P", item.getCode());
				assertEquals("Planned", item.getLabel(EN_LANG_CODE));
				assertEquals("Planned as part of original sampling design", item.getDescription(EN_LANG_CODE));
			}
		}
		{
			CodeList list = survey.getCodeList("admin_unit");
			List<PersistedCodeListItem> rootItems = codeListManager.loadRootItems(list);
			assertEquals(8, rootItems.size());
			{
				PersistedCodeListItem item = rootItems.get(2);
				assertEquals(22, item.getId());
				assertEquals("003", item.getCode());
				assertEquals("Colin", item.getLabel(EN_LANG_CODE));
				PersistedCodeListItem child = (PersistedCodeListItem) codeListManager.loadChildItem(item, "002");
				assertNotNull(child);
				assertEquals(24, child.getId());
				assertEquals("Muddy Banks", child.getLabel(EN_LANG_CODE));
			}
		}
	}
}
