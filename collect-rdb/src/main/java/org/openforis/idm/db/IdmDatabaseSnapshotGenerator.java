package org.openforis.idm.db;

import java.util.List;
import java.util.Set;

import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.structure.Column;
import liquibase.database.structure.ForeignKey;
import liquibase.database.structure.Table;
import liquibase.diff.DiffStatusListener;
import liquibase.exception.DatabaseException;
import liquibase.snapshot.DatabaseSnapshot;
import liquibase.snapshot.DatabaseSnapshotGenerator;
import liquibase.snapshot.DatabaseSnapshotGeneratorFactory;

public class IdmDatabaseSnapshotGenerator implements DatabaseSnapshotGenerator {
	
	private String tablePrefix; 

	public String getTablePrefix() {
		return tablePrefix;
	}
	
	public void setTablePrefix(String tablePrefix) {
		this.tablePrefix = tablePrefix;
	}
	
	@Override
	public boolean supports(Database database) {
		return database instanceof IdmDatabase;
	}

	@Override
	public int getPriority(Database database) {
		return Integer.MAX_VALUE;
	}

	@Override
	public DatabaseSnapshot createSnapshot(Database database, String surveyUri,
			Set<DiffStatusListener> listeners) throws DatabaseException {
		
//		IdmDatabase db = (IdmDatabase) database;		
		DatabaseSnapshot snapshot = new DatabaseSnapshot(database, surveyUri);
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Table getDatabaseChangeLogTable(Database database)
			throws DatabaseException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Table getDatabaseChangeLogLockTable(Database database)
			throws DatabaseException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Table getTable(String schemaName, String tableName, Database database)
			throws DatabaseException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Column getColumn(String schemaName, String tableName,
			String columnName, Database database) throws DatabaseException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ForeignKey getForeignKeyByForeignKeyTable(String schemaName,
			String tableName, String fkName, Database database)
			throws DatabaseException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<ForeignKey> getForeignKeys(String schemaName, String tableName,
			Database database) throws DatabaseException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean hasIndex(String schemaName, String tableName,
			String indexName, Database database, String columnNames)
			throws DatabaseException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean hasDatabaseChangeLogTable(Database database) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean hasDatabaseChangeLogLockTable(Database database) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean hasTable(String schemaName, String tableName,
			Database database) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean hasView(String schemaName, String viewName, Database database) {
		// TODO Auto-generated method stub
		return false;
	}
	
	public static void main(String[] args) throws DatabaseException {
		DatabaseFactory dbFactory = DatabaseFactory.getInstance();
		IdmDatabase db = new IdmDatabase();
		dbFactory.register(db);
		
		DatabaseSnapshotGeneratorFactory snapshotFactory = DatabaseSnapshotGeneratorFactory.getInstance();
		IdmDatabaseSnapshotGenerator generator = new IdmDatabaseSnapshotGenerator();
		generator.setTablePrefix("anfi");
		snapshotFactory.register(generator);
		
		DatabaseSnapshot snapshot = snapshotFactory.createSnapshot(db, "http://....", null);
		
	}
}
