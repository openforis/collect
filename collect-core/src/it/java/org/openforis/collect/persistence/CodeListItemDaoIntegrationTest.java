/**
 * 
 */
package org.openforis.collect.persistence;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.openforis.collect.CollectIntegrationTest;
import org.openforis.collect.manager.exception.SurveyValidationException;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.idm.metamodel.CodeList;
import org.openforis.idm.metamodel.PersistedCodeListItem;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author S. Ricci
 *
 */
public class CodeListItemDaoIntegrationTest extends CollectIntegrationTest {

	private static final String IDM_TEST_XML = "test.idm.xml";
	private static final String EN_LANG_CODE = "en";
	
	@Autowired
	private CodeListItemDao codeListItemDao;
	
	private CollectSurvey survey;
	
	@Before
	public void init() throws SurveyImportException, SurveyValidationException {
		InputStream is = ClassLoader.getSystemResourceAsStream(IDM_TEST_XML);
		survey = surveyManager.importModel(is, "archenland1", false);
	}
	
	@Test
	public void persistedFlatListTest() {
		CodeList list = survey.getCodeList("measurement");
		List<PersistedCodeListItem> rootItems = codeListItemDao.loadRootItems(list);
		assertEquals(3, rootItems.size());
		{
			PersistedCodeListItem item = rootItems.get(0);
			assertEquals("P", item.getCode());
			assertEquals(Integer.valueOf(1), item.getSortOrder());
			assertEquals("Planned", item.getLabel(EN_LANG_CODE));
			assertEquals("Planned as part of original sampling design", item.getDescription(EN_LANG_CODE));
		}
	}
	
	@Test
	public void persistedHierarchicalListTest() {
		CodeList list = survey.getCodeList("admin_unit");
		List<PersistedCodeListItem> rootItems = codeListItemDao.loadRootItems(list);
		assertEquals(8, rootItems.size());
		{
			PersistedCodeListItem item = rootItems.get(2);
			assertEquals(22, item.getId());
			assertEquals("003", item.getCode());
			assertEquals(Integer.valueOf(3), item.getSortOrder());
			assertEquals("Colin", item.getLabel(EN_LANG_CODE));
			boolean hasChildItems = codeListItemDao.hasChildItems(list, item.getSystemId());
			assertTrue(hasChildItems);
			{
				PersistedCodeListItem child = (PersistedCodeListItem) codeListItemDao.loadItem(list, item.getSystemId(), "002", null);
				assertNotNull(child);
				assertEquals(24, child.getId());
				assertEquals(Integer.valueOf(2), child.getSortOrder());
				assertEquals("Muddy Banks", child.getLabel(EN_LANG_CODE));
				boolean hasChildItems2 = codeListItemDao.hasChildItems(list, child.getSystemId());
				assertFalse(hasChildItems2);
			}
		}
	}
	
	@Test
	public void moveItemInPersistedListTest() {
		CodeList list = survey.getCodeList("admin_unit");
		List<PersistedCodeListItem> items = codeListItemDao.loadRootItems(list);
		List<String> codes = getCodes(items);
		assertEquals(Arrays.asList("001", "002", "003", "004", "005", "006", "007", "008"), codes);
		
		PersistedCodeListItem item = items.get(2);
		assertEquals(Integer.valueOf(3), item.getSortOrder());
		
		codeListItemDao.shiftItem(item, 0);
		
		List<PersistedCodeListItem> items2 = codeListItemDao.loadRootItems(list);
		List<String> codes2 = getCodes(items2);
		assertEquals(Arrays.asList("003", "001", "002", "004", "005", "006", "007", "008"), codes2);
		
		PersistedCodeListItem item2 = items2.get(0);
		assertEquals(Integer.valueOf(1), item2.getSortOrder());

		codeListItemDao.shiftItem(item2, 4);
		
		List<PersistedCodeListItem> items3 = codeListItemDao.loadRootItems(list);
		List<String> codes3 = getCodes(items3);
		assertEquals(Arrays.asList("001", "002", "004", "005", "003", "006", "007", "008"), codes3);
		
		PersistedCodeListItem item3 = items3.get(4);
		assertEquals(Integer.valueOf(5), item3.getSortOrder());
		
		codeListItemDao.shiftItem(item3, 8);
		
		List<PersistedCodeListItem> items4 = codeListItemDao.loadRootItems(list);
		List<String> codes4 = getCodes(items4);
		assertEquals(Arrays.asList("001", "002", "004", "005", "006", "007", "008", "003"), codes4);
		
		PersistedCodeListItem item4 = items4.get(7);
		assertEquals(Integer.valueOf(8), item4.getSortOrder());
	}

	protected List<String> getCodes(List<PersistedCodeListItem> items) {
		List<String> codes = new ArrayList<String>();
		for (PersistedCodeListItem item : items) {
			codes.add(item.getCode());
		}
		return codes;
	}
	/*
	@Test
	public void importHugeCodeList() {
		CodeList list = survey.createCodeList();
		list.setName("huge_list");
		int size = 100000;
		List<PersistedCodeListItem> items = generateItems(list, size);
		{
			System.out.println("----- Batch import -----");
			System.out.println("Start importing " + size + " records");
			long start = System.currentTimeMillis();
			codeListItemDao.insert(items);
			long end = System.currentTimeMillis();
			System.out.println("End. Batch insert took: " + (end - start) + " millis.");
		}
		codeListItemDao.deleteByCodeList(list);
		{
			System.out.println("----- Simple Batch import -----");
			System.out.println("Start importing " + size + " records");
			long start = System.currentTimeMillis();
			codeListItemDao.simpleBatchInsert(items);
			long end = System.currentTimeMillis();
			System.out.println("End. Batch insert took: " + (end - start) + " millis.");
		}
		codeListItemDao.deleteByCodeList(list);
		{
			System.out.println("----- Sequencial insert import -----");
			System.out.println("Start importing " + size + " records");
			long start = System.currentTimeMillis();
			for (PersistedCodeListItem item : items) {
				codeListItemDao.insert(item);
			}
			long end = System.currentTimeMillis();
			System.out.println("End. Sequencial insert took: " + (end - start) + " millis.");
		}
	}

	private List<PersistedCodeListItem> generateItems(CodeList codeList, int size) {
		int lastId = 10000;
		Integer parentId = null;
		return generateItems(codeList, size, lastId, parentId);
	}

	protected List<PersistedCodeListItem> generateItems(CodeList codeList,
			int size, int lastId, Integer parentId) {
		List<PersistedCodeListItem> result = new ArrayList<PersistedCodeListItem>(size);
		int sortOrder = 1;
		for(int i=0; i < size; i++) {
			PersistedCodeListItem item = new PersistedCodeListItem(codeList);
			item.setSystemId(++lastId);
			item.setParentId(parentId);
			item.setCode(String.valueOf(i));
			item.setLabel("en", "Code " + i);
			item.setSortOrder(sortOrder ++);
			result.add(item);
			double childrenSize = Math.min(size - i - 1, 10);
			int intChildrenSize = new Double(childrenSize).intValue();
			if ( childrenSize > 0 ) {
				List<PersistedCodeListItem> children = generateItems(codeList, intChildrenSize, lastId, item.getSystemId());
				result.addAll(children);
				lastId += intChildrenSize;
				i+=intChildrenSize;
			}
		}
		return result;
	}
	*/
}
