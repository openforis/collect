package org.openforis.idm.db;

import java.util.List;
import java.util.Set;

import javax.xml.namespace.QName;

import liquibase.database.structure.Table;
import liquibase.exception.DatabaseException;
import liquibase.snapshot.DatabaseSnapshot;

import org.openforis.idm.metamodel.CodeList;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.metamodel.Schema;
import org.openforis.idm.metamodel.Survey;
import org.openforis.idm.transform2.Transformation;

/**
 * 
 * @author G. Miceli
 *
 * TODO Refactor IdmDataTableBuilder
 * TODO Refactor IdmCodeListTableBuilder
 */
public class IdmDatabaseSnapshotBuilder {

	private DatabaseSnapshot snapshot;
	private Set<Table> tables;
	private IdmDatabase database;
	private String surveyUri;
	private Survey survey;
	private Schema schema;
	private String tablePrefix; 
	private String codeTableSuffix;
	private String dataTableSuffix;
	
	private static final String RDB_NAMESPACE = "http://www.openforis.org/collect/3.0/rdb";
	public static final QName TABLE_NAME_QNAME = new QName(RDB_NAMESPACE, "table");
	public static final QName COLUMN_NAME_QNAME = new QName(RDB_NAMESPACE, "column");

	public IdmDatabaseSnapshotBuilder() {
	}

	public String getTablePrefix() {
		return tablePrefix;
	}
	
	public void setTablePrefix(String tablePrefix) {
		this.tablePrefix = tablePrefix;
	}
	
	public void setDatabase(IdmDatabase database) {
		this.database = database;
		this.survey = database.getSurvey();
		this.surveyUri = survey.getUri(); 
		this.schema = survey.getSchema();
	}

	public IdmDatabase getDatabase() {
		return database;
	}
	
	private void createDataTables() throws DatabaseException {
		RelationalTransformer rt = database.getRelationalTransformer();
		List<Transformation> xforms = rt.getTransformations();
		for (Transformation xform : xforms) {
			createDataTable(xform);
		}
//		List<EntityDefinition> rootDefns = schema.getRootEntityDefinitions();
//		for (EntityDefinition defn : rootDefns) {
//			createDataTable(defn);
//		}
	}
	
	private void createDataTable(Transformation xform) throws DatabaseException {
		Set<Table> tables = snapshot.getTables();
//		EntityDefinition defn = (EntityDefinition) xform.getNodeDefinition();
		IdmDataTableBuilder dtb = new IdmDataTableBuilder(xform);
		dtb.setTablePrefix(tablePrefix);
		dtb.setTableSuffix(dataTableSuffix);
		Table table = dtb.toTable();
		
		if ( tables.contains(table) ) {					
			throw new DatabaseException("Duplicate table name '"+table.getName()+"' for "+xform.getRowAxis());
		}
		
		tables.add(table);
	}

//	private void createDataTable(EntityDefinition defn)  throws DatabaseException {
//		Set<Table> tables = snapshot.getTables();
//		
//		// Tail recursion so that leaf tables are created first; required for relations
//		List<NodeDefinition> childDefns = ((EntityDefinition) defn).getChildDefinitions();
//		for (NodeDefinition child : childDefns) {
//			if ( child instanceof EntityDefinition ) {			
//				createDataTable((EntityDefinition) child);				
//			}
//		}
//
//		IdmDataTableBuilder dtb = new IdmDataTableBuilder(defn);
//		dtb.setTablePrefix(tablePrefix);
//		dtb.setTableSuffix(dataTableSuffix);
//		Table table = dtb.toTable();
//		
//		if ( tables.contains(table) ) {					
//			throw new DatabaseException("Duplicate table name '"+table.getName()+"' for "+defn.getPath());
//		}
//		
//		tables.add(table);
//	}

	// TODO hierarchical code lists as multiple tables
	private void createCodeListTables() throws DatabaseException {
		List<CodeList> lists = survey.getCodeLists();
		for (CodeList list : lists) {
			IdmCodeListTableBuilder tb = new IdmCodeListTableBuilder();
			tb.setList(list);
			tb.setTablePrefix(tablePrefix);
			tb.setTableSuffix(codeTableSuffix);
			Table table = tb.toTable();
			if ( tables.contains(table) ) {					
				throw new DatabaseException("Duplicate table name '"+table.getName()+"' for list "+list.getName());
			}
			tables.add(table);
		}
	}

	public String getCodeTableSuffix() {
		return codeTableSuffix;
	}

	public void setCodeTableSuffix(String codeTableSuffix) {
		this.codeTableSuffix = codeTableSuffix;
	}

	public String getDataTableSuffix() {
		return dataTableSuffix;
	}

	public void setDataTableSuffix(String dataTableSuffix) {
		this.dataTableSuffix = dataTableSuffix;
	}

	public DatabaseSnapshot toSnapshot() throws DatabaseException {
		this.snapshot = new DatabaseSnapshot(database, surveyUri);
		this.tables = snapshot.getTables();
		
		createDataTables();
		createCodeListTables();

		return snapshot;
	}
}