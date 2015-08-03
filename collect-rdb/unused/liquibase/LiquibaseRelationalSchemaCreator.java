package org.openforis.collect.relational.liquibase;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.sql.Connection;

import javax.xml.parsers.ParserConfigurationException;

import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseConnection;
import liquibase.database.DatabaseFactory;
import liquibase.database.core.PostgresDatabase;
import liquibase.database.core.SQLiteDatabase;
import liquibase.database.jvm.JdbcConnection;
import liquibase.diff.Diff;
import liquibase.diff.DiffResult;
import liquibase.exception.DatabaseException;
import liquibase.exception.LiquibaseException;
import liquibase.resource.FileSystemResourceAccessor;
import liquibase.snapshot.DatabaseSnapshot;

import org.openforis.collect.relational.CollectRdbException;
import org.openforis.collect.relational.RelationalSchemaCreator;
import org.openforis.collect.relational.model.RelationalSchema;

/**
 * 
 * @author G. Miceli
 *
 */
public class LiquibaseRelationalSchemaCreator implements RelationalSchemaCreator {
	
	@Override
	public void createRelationalSchema(RelationalSchema schema, Connection targetConn) throws CollectRdbException {
		PrintStream ps = null;
		try {
			LiquidbaseDatabaseSnapshotBuilder snapshotGen = new LiquidbaseDatabaseSnapshotBuilder();
			
			Database rdb = getDatabaseImplementation(targetConn);
			
			boolean dbSupportsFKs = rdb instanceof SQLiteDatabase ? false : true;
			DatabaseSnapshot generatedSnapshot = snapshotGen.createSnapshot(schema, dbSupportsFKs);
			
			String targetSchema = schema.getName();
			rdb.setDefaultSchemaName(targetSchema);
			DatabaseSnapshot emptyDbSnapshot = new DatabaseSnapshot(rdb, targetSchema);
			
			// Generate change set
			Diff diff = new Diff(generatedSnapshot, emptyDbSnapshot);
			DiffResult diffResult = diff.compare();
			
			File tmpFile = File.createTempFile("collect-schemagen", ".xml");
			ps = new PrintStream(new FileOutputStream(tmpFile));
			
			diffResult.setChangeSetAuthor("collect3");
			diffResult.setChangeSetContext("schemagen");
			System.out.println("Writing change log to "+tmpFile.getAbsolutePath());
			diffResult.printChangeLog(ps, rdb);
			ps.flush();
			
			// Execute change set
			Liquibase liq = new Liquibase(tmpFile.getName(), new FileSystemResourceAccessor(tmpFile.getParent()), rdb);
			liq.update("schemagen");
		} catch (LiquibaseException e) {
			throw new CollectRdbException("Failed to update schema", e);
		} catch (IOException e) {
			throw new CollectRdbException("Failed to create temp db changelog file", e);
		} catch (ParserConfigurationException e) {
			throw new CollectRdbException("Failed to write temp db changelog file", e);
		} finally {
			if ( ps != null ) {
				ps.close();
			}
		}		
	}

	private Database getDatabaseImplementation(Connection targetConn) throws DatabaseException {
		DatabaseConnection dbconn = new JdbcConnection(targetConn);

		DatabaseFactory dbFactory = DatabaseFactory.getInstance();
		Database rdb = dbFactory.findCorrectDatabaseImplementation(dbconn);
		
		if( rdb instanceof PostgresDatabase){
			rdb = new CollectPostgresDatabase(dbconn);
		}
		return rdb;
	}

}
