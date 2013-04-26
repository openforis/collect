package org.openforis.collect.relational.liquibase;

import java.util.List;
import java.util.Set;

import liquibase.database.Database;
import liquibase.database.structure.Column;
import liquibase.database.structure.ForeignKey;
import liquibase.database.structure.PrimaryKey;
import liquibase.database.structure.Table;
import liquibase.diff.DiffStatusListener;
import liquibase.exception.DatabaseException;
import liquibase.snapshot.DatabaseSnapshot;
import liquibase.snapshot.DatabaseSnapshotGenerator;

import org.openforis.collect.relational.model.DataPrimaryKeyColumn;
import org.openforis.collect.relational.model.ReferentialConstraint;
import org.openforis.collect.relational.model.RelationalSchema;
import org.openforis.collect.relational.model.RelationalSchemaGenerator;
import org.openforis.collect.relational.model.SchemaGenerationException;
import org.openforis.idm.metamodel.Survey;

/**
 * @author G. Miceli
 */
public class CollectDatabaseSnapshotGenerator implements DatabaseSnapshotGenerator {

	private RelationalSchemaGenerator relationalSchemaGenerator;

	public CollectDatabaseSnapshotGenerator(RelationalSchemaGenerator relationalSchemaGenerator) {
		this.relationalSchemaGenerator = relationalSchemaGenerator;
	}

	@Override
	public boolean supports(Database database) {
		return database instanceof CollectDatabase;
	}

	@Override
	public int getPriority(Database database) {
		return Integer.MAX_VALUE;
	}

	@Override
	public DatabaseSnapshot createSnapshot(Database database, String surveyUri, Set<DiffStatusListener> listeners)
			throws DatabaseException {
		CollectDatabase db = (CollectDatabase) database;
		Survey survey = db.getSurvey(surveyUri);
		if ( survey == null ) {
			throw new DatabaseException("Survey not found for "+surveyUri);
		}
		try {
			String schemaName = "collect_rdb";  // TODO make configurable
			RelationalSchema schema = relationalSchemaGenerator.generateSchema(survey, schemaName);
			DatabaseSnapshot snapshot = new DatabaseSnapshot(database, surveyUri);

			createCodeListTables(snapshot, schema);
			createDataTables(snapshot, schema);
			// TODO
			
			return snapshot;
		} catch (SchemaGenerationException e) {
			throw new DatabaseException(e);
		}
	}

	private void createCodeListTables(DatabaseSnapshot snapshot, RelationalSchema schema) {
		// TODO Auto-generated method stub
		
	}

	private void createDataTables(DatabaseSnapshot snapshot, RelationalSchema schema) {
		// Create table
		for (org.openforis.collect.relational.model.Table<?> itable : schema.getTables()) {
			Table ltable = new Table(itable.getName());
			ltable.setDatabase(snapshot.getDatabase());
			ltable.setSchema(schema.getName());
			ltable.setRawSchemaName(schema.getName());
			// Create columns
			for (org.openforis.collect.relational.model.Column<?> icolumn : itable.getColumns()) {
				Column lcolumn = new Column();
				lcolumn.setTable(ltable);
				lcolumn.setName(icolumn.getName());
				lcolumn.setNullable(icolumn.isNullable());
				lcolumn.setDataType(icolumn.getType());
				if ( icolumn.getLength() != null ) {
					lcolumn.setColumnSize(icolumn.getLength());
				}
				// Set PK
				if ( icolumn instanceof DataPrimaryKeyColumn ) {
					lcolumn.setPrimaryKey(true);
					lcolumn.setUnique(true);
					// Add PK constraint
					PrimaryKey lpk = new PrimaryKey();
					lpk.setTable(ltable);
					lpk.setName(itable.getPrimaryKeyConstraint().getName());
					lpk.getColumnNamesAsList().add(lcolumn.getName());
					snapshot.getPrimaryKeys().add(lpk);
				}
				// Add column
				ltable.getColumns().add(lcolumn);
			}
			// Add FKs
			List<ReferentialConstraint> ifks = itable.getReferentialContraints();
			for (ReferentialConstraint ifk : ifks) {
				ForeignKey lfk = new ForeignKey();
				lfk.setName(ifk.getName());
				// TODO create FKs
			}
			// Add table
			snapshot.getTables().add(ltable);
		}
	}

	@Override
	public Table getDatabaseChangeLogTable(Database database) throws DatabaseException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Table getDatabaseChangeLogLockTable(Database database) throws DatabaseException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Table getTable(String schemaName, String tableName, Database database) throws DatabaseException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Column getColumn(String schemaName, String tableName, String columnName, Database database)
			throws DatabaseException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ForeignKey getForeignKeyByForeignKeyTable(String schemaName, String tableName, String fkName,
			Database database) throws DatabaseException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<ForeignKey> getForeignKeys(String schemaName, String tableName, Database database)
			throws DatabaseException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean hasIndex(String schemaName, String tableName, String indexName, Database database, String columnNames)
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
	public boolean hasTable(String schemaName, String tableName, Database database) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean hasView(String schemaName, String viewName, Database database) {
		// TODO Auto-generated method stub
		return false;
	}

//	private static CollectSurvey loadSurvey() throws IdmlParseException, FileNotFoundException {
////		InputStream is = ClassLoader.getSystemResourceAsStream("test.idm.xml");
////		InputStream is = new FileInputStream("/home/gino/workspace/of/idm/idm-test/src/main/resources/test.idm.xml");
//		InputStream is = new FileInputStream("D:/data/workspace/idm/idm-test/src/main/resources/test.idm.xml");
////		InputStream is = new FileInputStream("/home/gino/workspace/faofin/tz/naforma-idm/tanzania-naforma.idm.xml");
//		CollectSurveyContext ctx = new CollectSurveyContext(new ExpressionFactory(), null, null);
//		CollectSurveyIdmlBinder binder = new CollectSurveyIdmlBinder(ctx);
//		return (CollectSurvey) binder.unmarshal(is);
//	}
//
//
//	public static void main(String[] args) {
//		try { 
//			ClassLoader survey = loadSurvey();
//			
//			IdmDatabase idmDb = Mockito.mock(IdmDatabase.class);
//			
////			dbFactory.register(idmDb);
//
//			
//			
//			// Register snapshot generator for IDM "databases"
//			DatabaseFactory dbFactory = DatabaseFactory.getInstance();
//			DatabaseSnapshotGeneratorFactory snapshotFactory = DatabaseSnapshotGeneratorFactory.getInstance();
//			IdmDatabaseSnapshotGenerator generator = new IdmDatabaseSnapshotGenerator();
//			generator.setTablePrefix("tz_");
//			generator.setCodeListTableSuffix("_code");
//			snapshotFactory.register(generator);
//			Set<DiffStatusListener> listeners = Collections.emptySet();
//			DatabaseSnapshot snapshot = snapshotFactory.createSnapshot(idmDb, "", listeners);
//			System.out.println(snapshot.toString());
////			SqlGeneratorFactory.getInstance().generateSql(statement, database)
//			
//			Connection conn = DriverManager.getConnection("jdbc:postgresql://localhost:5432/archenland1","postgres","postgres");
//			DatabaseConnection dbconn = new JdbcConnection(conn);
//			PostgresDatabase rdb = (PostgresDatabase) dbFactory.findCorrectDatabaseImplementation(dbconn);
//	//		DatabaseSnapshot snapshot = snapshotFactory.createSnapshot(idmDb, "http://....", null);
//	//		Liquibase liq = new Liquibase("changes.log", new FileSystemResourceAccessor("/home/gino/temp/ofc"), idmDb);			
//			Diff diff = new Diff(idmDb, rdb);
//			DiffResult diffResult = diff.compare();
//			diffResult.setChangeSetAuthor("collect-rdb-3.1-SNAPSHOT");
////			diffResult.printChangeLog(System.out, rdb);
////			diffResult.printResult(System.out);
////			System.out.println(diffResult);
//	//		liq.update("changes");
//		} catch ( Exception e ) {
//			e.printStackTrace();
//		}
//	}

}
