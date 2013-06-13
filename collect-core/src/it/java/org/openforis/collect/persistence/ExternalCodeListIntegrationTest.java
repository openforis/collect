package org.openforis.collect.persistence;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import liquibase.Liquibase;
import liquibase.database.core.SQLiteDatabase;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.LiquibaseException;
import liquibase.resource.ClassLoaderResourceAccessor;

import org.junit.Before;
import org.junit.Test;
import org.openforis.collect.CollectIntegrationTest;
import org.openforis.collect.manager.CodeListManager;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.idm.metamodel.CodeList;
import org.openforis.idm.metamodel.CodeListLevel;
import org.openforis.idm.metamodel.ExternalCodeListItem;
import org.openforis.idm.metamodel.xml.IdmlParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.datasource.DataSourceUtils;

/**
 * 
 * @author S. Ricci
 *
 */
public class ExternalCodeListIntegrationTest extends CollectIntegrationTest {
	
	private static final String EN_LANG_CODE = "en";
	private static final String LIQUIBASE_CHANGELOG = "org/openforis/collect/db/changelog/sqlite/db.changelog-createtestcodetable.xml";
	private static final String TEST_HIERARCHICAL_CODE_TABLE_NAME = "ofc_hierarchicalcodetable";
	private static final String TEST_HIERARCHICAL_CODE_LIST_NAME = "hierarchicalTestCodeList";
	private static final String TEST_FLAT_CODE_TABLE_NAME = "ofc_flatcodetable";
	private static final String TEST_FLAT_CODE_LIST_NAME = "flatTestCodeList";

	@Autowired
	private DataSource dataSource;
	
	private CollectSurvey survey;
	
	@Autowired
	private CodeListManager codeListManager;
	
	private CodeList hierarchicalList;
	private CodeList flatList;
	
	@Before
	public void before() throws IdmlParseException, IOException, SurveyImportException, SQLException, LiquibaseException {
		SQLiteDatabase database = new SQLiteDatabase();
		Connection connection = DataSourceUtils.getConnection(dataSource);
		database.setConnection(new JdbcConnection(connection));
		Liquibase liquibase = new Liquibase(LIQUIBASE_CHANGELOG, new ClassLoaderResourceAccessor(), database);
		liquibase.update(null);
		survey = loadSurvey();
		createHierarchicalTestList();
		createFlatTestList();
		surveyManager.importModel(survey);
	}

	protected void createFlatTestList() {
		flatList = survey.createCodeList();
		flatList.setName(TEST_FLAT_CODE_LIST_NAME);
		flatList.setLookupTable(TEST_FLAT_CODE_TABLE_NAME);
		survey.addCodeList(flatList);
	}
	
	protected void createHierarchicalTestList() {
		hierarchicalList = survey.createCodeList();
		hierarchicalList.setName(TEST_HIERARCHICAL_CODE_LIST_NAME);
		hierarchicalList.setLookupTable(TEST_HIERARCHICAL_CODE_TABLE_NAME);
		CodeListLevel level = new CodeListLevel();
		level.setName("level1");
		hierarchicalList.addLevel(level);
		level = new CodeListLevel();
		level.setName("level2");
		hierarchicalList.addLevel(level);
		level = new CodeListLevel();
		level.setName("level3");
		hierarchicalList.addLevel(level);
		survey.addCodeList(hierarchicalList);
	}

	@Test
	public void getFlatListItemsTest() throws SQLException {
		List<ExternalCodeListItem> childItems = codeListManager.loadRootItems(flatList);
		assertEquals(3, childItems.size());
		{
			ExternalCodeListItem item = childItems.get(0);
			assertEquals("001", item.getCode());
			assertEquals("Code 1", item.getLabel(EN_LANG_CODE));
		}
		{
			ExternalCodeListItem item = childItems.get(1);
			assertEquals("002", item.getCode());
			assertEquals("Code 2", item.getLabel(EN_LANG_CODE));
		}
		{
			ExternalCodeListItem item = childItems.get(2);
			assertEquals("003", item.getCode());
			assertEquals("Code 3", item.getLabel(EN_LANG_CODE));
		}
	}

	@Test
	public void getNestedChildItemTest() throws SQLException {
		List<ExternalCodeListItem> rootItems = codeListManager.loadRootItems(hierarchicalList);
		ExternalCodeListItem parent = rootItems.get(0);
		List<ExternalCodeListItem> children = codeListManager.loadChildItems(parent);
		assertEquals(2, children.size());
		ExternalCodeListItem child = children.get(1);
		assertEquals("012", child.getCode());
	}
	
	@Test
	public void getFirstLevelItemsTest() throws SQLException {
		List<ExternalCodeListItem> childItems = codeListManager.loadRootItems(hierarchicalList);
		assertEquals(2, childItems.size());
		{
			ExternalCodeListItem item = childItems.get(0);
			assertEquals("001", item.getCode());
			assertEquals("Code 1", item.getLabel(EN_LANG_CODE));
		}
		{
			ExternalCodeListItem item = childItems.get(1);
			assertEquals("002", item.getCode());
			assertEquals("Code 2", item.getLabel(EN_LANG_CODE));
		}
	}

	@Test
	public void getChildItemsTest() throws SQLException {
		List<ExternalCodeListItem> firstLevelItems = codeListManager.loadRootItems(hierarchicalList);
		ExternalCodeListItem parent = firstLevelItems.get(0);
		List<ExternalCodeListItem> childItems = codeListManager.loadChildItems(parent);
		assertEquals(2, childItems.size());
		{
			ExternalCodeListItem item = childItems.get(0);
			assertEquals("011", item.getCode());
			assertEquals("Code 1-1", item.getLabel(EN_LANG_CODE));
		}
		{
			ExternalCodeListItem item = childItems.get(1);
			assertEquals("012", item.getCode());
			assertEquals("Code 1-2", item.getLabel(EN_LANG_CODE));
		}
	}
	
	@Test
	public void getParentItemTest() throws SQLException {
		List<ExternalCodeListItem> firstLevelItems = codeListManager.loadRootItems(hierarchicalList);
		{
			ExternalCodeListItem item = firstLevelItems.get(0);
			ExternalCodeListItem parentItem = codeListManager.loadExternalParentItem(item);
			assertNull(parentItem);
		}
		{
			ExternalCodeListItem firstLevelItem = firstLevelItems.get(0);
			List<ExternalCodeListItem> childItems = codeListManager.loadChildItems(firstLevelItem);
			assertEquals(2, childItems.size());
			ExternalCodeListItem secondLevelItem = childItems.get(0);
			ExternalCodeListItem parentItem = codeListManager.loadExternalParentItem(secondLevelItem);
			assertEquals(parentItem, firstLevelItem);
		}
	}
	
}
