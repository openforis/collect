package org.openforis.collect.persistence.jooq;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jooq.exception.DataAccessException;
import org.springframework.jdbc.core.support.JdbcDaoSupport;

/**
 * @author G. Miceli
 * @author M. Togna
 * @author S. Ricci
 */
public abstract class JooqDaoSupport extends JdbcDaoSupport {
	private final Log log = LogFactory.getLog(getClass());

	private static final String CONSTRAINT_VIOLATION_CODE = "23";
	private static final String CONSTRAINT_VIOLATION_MESSAGE = "constraint violation";
	   
	public static boolean isConstraintViolation(DataAccessException e) {
		Throwable cause = e.getCause();
		if (! (cause instanceof SQLException)) {
			return false;
		}
		String sqlState = ((SQLException) cause).getSQLState();
		if (sqlState == null) {
			return StringUtils.containsIgnoreCase(cause.getMessage(), CONSTRAINT_VIOLATION_MESSAGE);
		} else {
			return sqlState.startsWith(CONSTRAINT_VIOLATION_CODE);
		}
	}
	
	protected Log getLog() {
		return log;
	}
	
	protected CollectDSLContext dsl() {
		Connection connection = getConnection();
		return new CollectDSLContext(connection);
	}

	// TODO Move to MappingJooqFactory
	protected static Timestamp toTimestamp(Date date) {
		if ( date == null ) {
			return null;
		} else {
			return new Timestamp(date.getTime());
		}
	}
}
