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
import org.openforis.collect.model.CollectSurvey;
import org.openforis.idm.metamodel.CodeList;
import org.openforis.idm.metamodel.CodeListLevel;
import org.openforis.idm.metamodel.ExternalCodeListItem;
import org.openforis.idm.metamodel.ExternalCodeListProvider;
import org.openforis.idm.metamodel.SurveyContext;
import org.openforis.idm.metamodel.xml.IdmlParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.datasource.DataSourceUtils;

/**
 * 
 * @author S. Ricci
 *
 */
public class DatabaseExternalCodeListProviderIntegrationTest extends CollectIntegrationTest {
	
	private static final String TEST_CODE_TABLE_NAME = "ofc_testcodetable";
	private static final String TEST_CODE_LIST_NAME = "testCodeList";

	@Autowired
	private DataSource dataSource;
	
	private CollectSurvey survey;
	
	@Before
	public void before() throws IdmlParseException, IOException, SurveyImportException, SQLException, LiquibaseException {
		SQLiteDatabase database = new SQLiteDatabase();
		Connection connection = DataSourceUtils.getConnection(dataSource);
		database.setConnection(new JdbcConnection(connection));
		Liquibase liquibase = new Liquibase("org/openforis/collect/db/changelog/sqlite/db.changelog-createtestcodetable.xml", new ClassLoaderResourceAccessor(), database);
		liquibase.update(null);
		survey = loadSurvey();
		CodeList testCodeList = survey.createCodeList();
		testCodeList.setName(TEST_CODE_LIST_NAME);
		testCodeList.setLookupTable(TEST_CODE_TABLE_NAME);
		CodeListLevel level = new CodeListLevel();
		level.setName("level1");
		testCodeList.addLevel(level);
		level = new CodeListLevel();
		level.setName("level2");
		testCodeList.addLevel(level);
		level = new CodeListLevel();
		level.setName("level3");
		testCodeList.addLevel(level);
		survey.addCodeList(testCodeList);
		surveyManager.importModel(survey);
	}

	@Test
	public void getFirstLevelItemsTest() throws SQLException {
		SurveyContext context = survey.getContext();
		ExternalCodeListProvider provider = context.getExternalCodeListProvider();
		CodeList list = survey.getCodeList(TEST_CODE_LIST_NAME);
		List<ExternalCodeListItem> childItems = provider.getChildItems(list);
		assertEquals(2, childItems.size());
		{
			ExternalCodeListItem item = childItems.get(0);
			assertEquals("001", item.getCode());
			assertEquals("Code 1", item.getLabel("en"));
		}
		{
			ExternalCodeListItem item = childItems.get(1);
			assertEquals("002", item.getCode());
			assertEquals("Code 2", item.getLabel("en"));
		}
	}

	@Test
	public void getChildItemsTest() throws SQLException {
		SurveyContext context = survey.getContext();
		ExternalCodeListProvider provider = context.getExternalCodeListProvider();
		CodeList list = survey.getCodeList(TEST_CODE_LIST_NAME);
		List<ExternalCodeListItem> firstLevelItems = provider.getChildItems(list);
		ExternalCodeListItem parent = firstLevelItems.get(0);
		List<ExternalCodeListItem> childItems = provider.getChildItems(parent);
		assertEquals(2, childItems.size());
		{
			ExternalCodeListItem item = childItems.get(0);
			assertEquals("011", item.getCode());
			assertEquals("Code 1-1", item.getLabel("en"));
		}
		{
			ExternalCodeListItem item = childItems.get(1);
			assertEquals("012", item.getCode());
			assertEquals("Code 1-2", item.getLabel("en"));
		}
	}
	
	@Test
	public void getParentItemTest() throws SQLException {
		SurveyContext context = survey.getContext();
		ExternalCodeListProvider provider = context.getExternalCodeListProvider();
		CodeList list = survey.getCodeList(TEST_CODE_LIST_NAME);
		List<ExternalCodeListItem> firstLevelItems = provider.getChildItems(list);
		{
			ExternalCodeListItem item = firstLevelItems.get(0);
			ExternalCodeListItem parentItem = provider.getParentItem(item);
			assertNull(parentItem);
		}
		{
			ExternalCodeListItem firstLevelItem = firstLevelItems.get(0);
			List<ExternalCodeListItem> childItems = provider.getChildItems(firstLevelItem);
			assertEquals(2, childItems.size());
			ExternalCodeListItem secondLevelItem = childItems.get(0);
			ExternalCodeListItem parentItem = provider.getParentItem(secondLevelItem);
			assertEquals(parentItem, firstLevelItem);
		}
	}
	
}
