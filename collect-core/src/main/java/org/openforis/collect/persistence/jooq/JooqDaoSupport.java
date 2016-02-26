package org.openforis.collect.persistence.jooq;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jooq.exception.DataAccessException;

/**
 * @author G. Miceli
 * @author M. Togna
 * @author S. Ricci
 */
public abstract class JooqDaoSupport {
	protected final Log log = LogFactory.getLog(getClass());

	private static final String CONSTRAINT_VIOLATION_CODE = "23";
	private static final String CONSTRAINT_VIOLATION_MESSAGE = "constraint violation";
	
	private CollectDSLContext dsl;
	   
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
	
	// TODO Move to MappingJooqFactory
	protected static Timestamp toTimestamp(Date date) {
		if ( date == null ) {
			return null;
		} else {
			return new Timestamp(date.getTime());
		}
	}

	protected CollectDSLContext dsl() {
		return dsl;
	}
	
	public void setDsl(CollectDSLContext dsl) {
		this.dsl = dsl;
	}
	
}
