package org.openforis.idm.db;

import java.io.IOException;
import java.io.Writer;
import java.math.BigInteger;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.List;

import org.openforis.idm.metamodel.Survey;

import liquibase.change.Change;
import liquibase.changelog.ChangeSet;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.changelog.RanChangeSet;
import liquibase.changelog.ChangeSet.ExecType;
import liquibase.changelog.ChangeSet.RunStatus;
import liquibase.database.Database;
import liquibase.database.DatabaseConnection;
import liquibase.database.structure.DatabaseObject;
import liquibase.exception.DatabaseException;
import liquibase.exception.DatabaseHistoryException;
import liquibase.exception.DateParseException;
import liquibase.exception.LiquibaseException;
import liquibase.exception.RollbackImpossibleException;
import liquibase.exception.StatementNotSupportedOnDatabaseException;
import liquibase.exception.UnsupportedChangeException;
import liquibase.sql.visitor.SqlVisitor;
import liquibase.statement.DatabaseFunction;
import liquibase.statement.SqlStatement;

public class IdmDatabase implements Database {

	
	private Survey survey;

	public IdmDatabase(Survey survey) {
		this.survey = survey;
	}
	
	public Survey getSurvey() {
		return survey;
	}

	@Override
	public DatabaseObject[] getContainingObjects() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getPriority() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean isCorrectDatabaseImplementation(DatabaseConnection conn)
			throws DatabaseException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String getDefaultDriver(String url) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DatabaseConnection getConnection() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setConnection(DatabaseConnection conn) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean requiresUsername() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean requiresPassword() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean getAutoCommitMode() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean supportsDDLInTransaction() {
		return false;
	}

	@Override
	public String getDatabaseProductName() {
		return "IDM";
	}

	@Override
	public String getDatabaseProductVersion() throws DatabaseException {
		return "3.0";
	}

	@Override
	public int getDatabaseMajorVersion() throws DatabaseException {
		return 3;
	}

	@Override
	public int getDatabaseMinorVersion() throws DatabaseException {
		return 0;
	}

	@Override
	public String getTypeName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getDefaultCatalogName() throws DatabaseException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getDefaultSchemaName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getLiquibaseSchemaName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setDefaultSchemaName(String schemaName)
			throws DatabaseException {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean supportsInitiallyDeferrableColumns() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean supportsSequences() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean supportsDropTableCascadeConstraints() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean supportsAutoIncrement() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String getDateLiteral(String isoDate) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getCurrentDateTimeFunction() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setCurrentDateTimeFunction(String function) {
		// TODO Auto-generated method stub

	}

	@Override
	public String getLineComment() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getAutoIncrementClause(BigInteger startWith,
			BigInteger incrementBy) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getDatabaseChangeLogTableName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getDatabaseChangeLogLockTableName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setDatabaseChangeLogTableName(String tableName) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setDatabaseChangeLogLockTableName(String tableName) {
		// TODO Auto-generated method stub

	}

	@Override
	public String getConcatSql(String... values) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean hasDatabaseChangeLogTable() throws DatabaseException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setCanCacheLiquibaseTableInfo(boolean canCacheLiquibaseTableInfo) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean hasDatabaseChangeLogLockTable() throws DatabaseException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void checkDatabaseChangeLogTable(
			boolean updateExistingNullChecksums,
			DatabaseChangeLog databaseChangeLog, String[] contexts)
			throws DatabaseException {
		// TODO Auto-generated method stub

	}

	@Override
	public void checkDatabaseChangeLogLockTable() throws DatabaseException {
		// TODO Auto-generated method stub

	}

	@Override
	public void dropDatabaseObjects(String schema) throws DatabaseException {
		// TODO Auto-generated method stub

	}

	@Override
	public void tag(String tagString) throws DatabaseException {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean doesTagExist(String tag) throws DatabaseException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isSystemTable(String catalogName, String schemaName,
			String tableName) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isLiquibaseTable(String tableName) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean shouldQuoteValue(String value) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean supportsTablespaces() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String getViewDefinition(String schemaName, String name)
			throws DatabaseException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isSystemView(String catalogName, String schemaName,
			String name) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String getTimeLiteral(Time time) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getDateTimeLiteral(Timestamp timeStamp) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getDateLiteral(Date defaultDateValue) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getDateLiteral(java.util.Date defaultDateValue) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String escapeTableName(String schemaName, String tableName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String escapeIndexName(String schemaName, String indexName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String escapeDatabaseObject(String objectName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String escapeColumnName(String schemaName, String tableName,
			String columnName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String escapeColumnNameList(String columnNames) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String convertRequestedSchemaToSchema(String requestedSchema)
			throws DatabaseException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String convertRequestedSchemaToCatalog(String requestedSchema)
			throws DatabaseException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean supportsSchemas() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String generatePrimaryKeyName(String tableName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String escapeSequenceName(String schemaName, String sequenceName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String escapeViewName(String schemaName, String viewName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public RunStatus getRunStatus(ChangeSet changeSet)
			throws DatabaseException, DatabaseHistoryException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public RanChangeSet getRanChangeSet(ChangeSet changeSet)
			throws DatabaseException, DatabaseHistoryException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void markChangeSetExecStatus(ChangeSet changeSet, ExecType execType)
			throws DatabaseException {
		// TODO Auto-generated method stub

	}

	@Override
	public List<RanChangeSet> getRanChangeSetList() throws DatabaseException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public java.util.Date getRanDate(ChangeSet changeSet)
			throws DatabaseException, DatabaseHistoryException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void removeRanStatus(ChangeSet changeSet) throws DatabaseException {
		// TODO Auto-generated method stub

	}

	@Override
	public void commit() throws DatabaseException {
		// TODO Auto-generated method stub

	}

	@Override
	public void rollback() throws DatabaseException {
		// TODO Auto-generated method stub

	}

	@Override
	public String escapeStringForDatabase(String string) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void close() throws DatabaseException {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean supportsRestrictForeignKeys() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String escapeConstraintName(String constraintName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isAutoCommit() throws DatabaseException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setAutoCommit(boolean b) throws DatabaseException {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isLocalDatabase() throws DatabaseException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void executeStatements(Change change, DatabaseChangeLog changeLog,
			List<SqlVisitor> sqlVisitors) throws LiquibaseException,
			UnsupportedChangeException {
		// TODO Auto-generated method stub

	}

	@Override
	public void execute(SqlStatement[] statements, List<SqlVisitor> sqlVisitors)
			throws LiquibaseException {
		// TODO Auto-generated method stub

	}

	@Override
	public void saveStatements(Change change, List<SqlVisitor> sqlVisitors,
			Writer writer) throws IOException, UnsupportedChangeException,
			StatementNotSupportedOnDatabaseException, LiquibaseException {
		// TODO Auto-generated method stub

	}

	@Override
	public void executeRollbackStatements(Change change,
			List<SqlVisitor> sqlVisitors) throws LiquibaseException,
			UnsupportedChangeException, RollbackImpossibleException {
		// TODO Auto-generated method stub

	}

	@Override
	public void saveRollbackStatement(Change change,
			List<SqlVisitor> sqlVisitors, Writer writer) throws IOException,
			UnsupportedChangeException, RollbackImpossibleException,
			StatementNotSupportedOnDatabaseException, LiquibaseException {
		// TODO Auto-generated method stub

	}

	@Override
	public int getNextChangeSetSequenceValue() throws LiquibaseException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public java.util.Date parseDate(String dateAsString)
			throws DateParseException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<DatabaseFunction> getDatabaseFunctions() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void reset() {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean supportsForeignKeyDisable() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean disableForeignKeyChecks() throws DatabaseException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void enableForeignKeyChecks() throws DatabaseException {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isReservedWord(String string) {
		// TODO Auto-generated method stub
		return false;
	}

}
