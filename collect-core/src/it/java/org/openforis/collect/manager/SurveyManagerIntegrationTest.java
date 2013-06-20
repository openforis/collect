/**
 * 
 */
package org.openforis.collect.manager;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.InputStream;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openforis.collect.manager.exception.SurveyValidationException;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.persistence.SurveyImportException;
import org.openforis.idm.metamodel.CodeList;
import org.openforis.idm.metamodel.CodeListItem;
import org.openforis.idm.metamodel.PersistedCodeListItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author S. Ricci
 *
 */
@RunWith( SpringJUnit4ClassRunner.class )
@ContextConfiguration( locations = {"classpath:test-context.xml"} )
@TransactionConfiguration(defaultRollback=true)
@Transactional
public class SurveyManagerIntegrationTest  {

	private CollectSurvey survey;

	@Autowired
	private SurveyManager surveyManager;
	@Autowired
	private CodeListManager codeListManager;
	
	@Before
	public void init() throws SurveyImportException, SurveyValidationException {
		InputStream is = ClassLoader.getSystemResourceAsStream("test.idm.xml");
		survey = surveyManager.importModel(is, "archenland1", false);
	}
	
	@Test
	public void duplicatePublishedSurveyTest() {
		CollectSurvey surveyWork = surveyManager.duplicatePublishedSurveyForEdit(survey.getUri());
		assertTrue(surveyWork.isWork());
		{
			CodeList list = survey.getCodeList("admin_unit");
			List<CodeListItem> rootItems = codeListManager.loadRootItems(list);
			assertEquals(8, rootItems.size());
			List<CodeListItem> childItems = codeListManager.loadChildItems(rootItems.get(0));
			assertEquals(3, childItems.size());
		}
		{
			CodeList list = surveyWork.getCodeList("admin_unit");
			List<CodeListItem> rootItems = codeListManager.loadRootItems(list);
			assertEquals(8, rootItems.size());
			List<CodeListItem> childItems = codeListManager.loadChildItems(rootItems.get(0));
			assertEquals(3, childItems.size());
		}
	}
	
	@Test
	public void publishSurveyTest() throws SurveyImportException {
		CollectSurvey surveyWork = surveyManager.duplicatePublishedSurveyForEdit(survey.getUri());
		assertEquals("Archenland NFI", surveyWork.getProjectName(null));
		
		surveyWork.setProjectName(null, "New Project Name");
		surveyManager.publish(surveyWork);
		
		CollectSurvey survey = surveyManager.getByUri(surveyWork.getUri());
		assertFalse(survey.isWork());
		assertEquals("New Project Name", survey.getProjectName(null));
	}
	
	@Test
	public void publishSurveyCodeListsTest() throws SurveyImportException {
		CollectSurvey surveyWork = surveyManager.duplicatePublishedSurveyForEdit(survey.getUri());
		{
			//modify item in list
			CodeList list = surveyWork.getCodeList("admin_unit");
			PersistedCodeListItem item = codeListManager.loadRootItem(list, "001", null);
			assertEquals(Integer.valueOf(1), item.getSortOrder());
			item.setCode("001A");
			codeListManager.save(item);
		}
		surveyManager.publish(surveyWork);
		
		CollectSurvey publishedSurvey = surveyManager.getByUri(surveyWork.getUri());
		
		CodeList list = publishedSurvey.getCodeList("admin_unit");
		List<CodeListItem> rootItems = codeListManager.loadRootItems(list);
		assertEquals(8, rootItems.size());
		{
			PersistedCodeListItem item = codeListManager.loadRootItem(list, "001A", null);
			assertEquals(Integer.valueOf(1), item.getSortOrder());
		}
		{
			PersistedCodeListItem item = codeListManager.loadRootItem(list, "001", null);
			assertNull(item);
		}
	}
}
