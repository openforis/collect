package org.openforis.collect.relational.sql;

import java.io.StringWriter;
import java.io.Writer;
import java.sql.Connection;
import java.sql.Statement;

import org.jooq.SQLDialect;
import org.openforis.collect.persistence.jooq.CollectDSLContext;
import org.openforis.collect.persistence.utils.DBUtils;
import org.openforis.collect.relational.RelationalSchemaCreator;
import org.openforis.collect.relational.model.RelationalSchema;
import org.openforis.collect.relational.print.RDBPrintJob.RdbDialect;

public class SQLRelationalSchemaCreator implements RelationalSchemaCreator {

	@Override
	public void createRelationalSchema(RelationalSchema schema, Connection conn) {
		CollectDSLContext dsl = new CollectDSLContext(conn);
		RdbDialect rdbDialect = getRdbDialect(dsl);
		Writer writer = new StringWriter();
		SqlSchemaWriter schemaWriter = new SqlSchemaWriter(writer, schema, rdbDialect);
		Statement stmt = null;
		try {
			schemaWriter.write();
			String sql = writer.toString();
			stmt = conn.createStatement();
	        stmt.executeUpdate(sql);
		} catch (Throwable e) {
			throw new RuntimeException(String.format("Error generating schema on db for rdb schema %s", schema.getName()), e);
		} finally {
			DBUtils.closeQuietly(stmt);
		}
	}

	private RdbDialect getRdbDialect(CollectDSLContext dsl) {
		SQLDialect dialect = dsl.getDialect();
		RdbDialect rdbDialect;
		switch(dialect) {
		case SQLITE:
			rdbDialect = RdbDialect.SQLITE;
			break;
		default:
			rdbDialect = RdbDialect.STANDARD;
		}
		return rdbDialect;
	}
	
	@Override
	public void addConstraints(RelationalSchema schema, Connection conn) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void addIndexes(RelationalSchema schema, Connection conn) {
		throw new UnsupportedOperationException();
	}
	
}
