package org.openforis.idm.db;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.List;
import java.util.Set;

import liquibase.database.Database;
import liquibase.database.DatabaseConnection;
import liquibase.database.DatabaseFactory;
import liquibase.database.core.PostgresDatabase;
import liquibase.database.jvm.JdbcConnection;
import liquibase.database.structure.Column;
import liquibase.database.structure.ForeignKey;
import liquibase.database.structure.Table;
import liquibase.diff.Diff;
import liquibase.diff.DiffResult;
import liquibase.diff.DiffStatusListener;
import liquibase.exception.DatabaseException;
import liquibase.snapshot.DatabaseSnapshot;
import liquibase.snapshot.DatabaseSnapshotGenerator;
import liquibase.snapshot.DatabaseSnapshotGeneratorFactory;

import org.openforis.idm.metamodel.DefaultSurveyContext;
import org.openforis.idm.metamodel.Survey;
import org.openforis.idm.metamodel.SurveyContext;
import org.openforis.idm.metamodel.xml.IdmlParseException;
import org.openforis.idm.metamodel.xml.SurveyIdmlBinder;

public class IdmDatabaseSnapshotGenerator implements DatabaseSnapshotGenerator {
	
	private static final String RDB_NAMESPACE = "http://www.openforis.org/collect/3.0/rdb";	
	private IdmDatabaseSnapshotBuilder builder;
	
	public IdmDatabaseSnapshotGenerator() {
		this.builder = new IdmDatabaseSnapshotBuilder();
	}
	
	public String getTablePrefix() {
		return builder.getTablePrefix();
	}
	
	public void setTablePrefix(String tablePrefix) {
		builder.setTablePrefix(tablePrefix);
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
		
		builder.setDatabase((IdmDatabase) database);
		
		return builder.toSnapshot();
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
	
	private static Survey loadSurvey() throws IdmlParseException, FileNotFoundException {
//		InputStream is = new FileInputStream("/home/gino/workspace/faofin/tz/naforma-idm/tanzania-naforma.idm.xml");
//		SurveyContext ctx = new DefaultSurveyContext();
//		SurveyIdmlBinder binder = new SurveyIdmlBinder(ctx);
//		return binder.unmarshal(is);
		return null; // TODO
	}

	public static void main(String[] args) {
		try { 
			Survey survey = loadSurvey();
			IdmDatabase idmDb = new IdmDatabase(survey);
//			dbFactory.register(idmDb);

			
			
			// Register snapshot generator for IDM "databases"
			DatabaseFactory dbFactory = DatabaseFactory.getInstance();
			DatabaseSnapshotGeneratorFactory snapshotFactory = DatabaseSnapshotGeneratorFactory.getInstance();
			IdmDatabaseSnapshotGenerator generator = new IdmDatabaseSnapshotGenerator();
			generator.setTablePrefix("tz_");
			generator.setCodeListTableSuffix("_code");
			snapshotFactory.register(generator);
			
			Connection conn = DriverManager.getConnection("jdbc:postgresql://localhost:5432/naforma1","postgres","postgres");
			DatabaseConnection dbconn = new JdbcConnection(conn);
			PostgresDatabase rdb = (PostgresDatabase) dbFactory.findCorrectDatabaseImplementation(dbconn);
	//		DatabaseSnapshot snapshot = snapshotFactory.createSnapshot(idmDb, "http://....", null);
	//		Liquibase liq = new Liquibase("changes.log", new FileSystemResourceAccessor("/home/gino/temp/ofc"), idmDb);			
			Diff diff = new Diff(idmDb, rdb);
			DiffResult diffResult = diff.compare();
			diffResult.printChangeLog(System.out, rdb);
//			diffResult.printResult(System.out);
//			System.out.println(diffResult);
	//		liq.update("changes");
		} catch ( Exception e ) {
			e.printStackTrace();
		}
	}

	public void setCodeListTableSuffix(String codeTableSuffix) {
		builder.setCodeTableSuffix(codeTableSuffix);
	}
	
	public String getCodeTableSuffix() {
		return builder.getCodeTableSuffix();
	}
}
