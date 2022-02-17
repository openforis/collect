package org.openforis.collect.persistence.jooq;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jooq.Batch;
import org.jooq.Query;
import org.jooq.exception.DataAccessException;

/**
 * @author G. Miceli
 * @author M. Togna
 * @author S. Ricci
 */
public abstract class JooqDaoSupport {
	protected final Logger log = LogManager.getLogger(getClass());

	private static final String CONSTRAINT_VIOLATION_CODE = "23";
	private static final String CONSTRAINT_VIOLATION_MESSAGE = "constraint violation";
	private static final int SQLITE_VALUE_MAX_SIZE = 400000; // 400KB

	protected CollectDSLContext dsl;
	   
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
	
	// TODO Move to MappingJooqFactory
	protected static Timestamp toTimestamp(Date date) {
		if ( date == null ) {
			return null;
		} else {
			return new Timestamp(date.getTime());
		}
	}

	private boolean queryExceedsMaxSizeForBatch(Query query) {
		if (!dsl().isSQLite()) return false;
		
		List<Object> bindValues = query.getBindValues();
		for (Object bindValue : bindValues) {
			if (bindValue != null && 
					(bindValue instanceof String && 
						((String) bindValue).length() > SQLITE_VALUE_MAX_SIZE
							||
					bindValue.getClass().isArray() && bindValue instanceof byte[] && 
						((byte[]) bindValue).length > SQLITE_VALUE_MAX_SIZE)
					) {
				return true;
			}
		}
		return false;
	}
	
	public void executeInBatch(List<Query> queries) {
		List<Query> queriesToBeExecutedSequentially = new ArrayList<Query>();
		List<Query> queriesToBeExecutedInBatch = new ArrayList<Query>();
		for (Query query : queries) {
			if (queryExceedsMaxSizeForBatch(query)) {
				queriesToBeExecutedSequentially.add(query);
			} else {
				queriesToBeExecutedInBatch.add(query);
			}
		}
		if (!queriesToBeExecutedSequentially.isEmpty()) {
			for (Query query : queriesToBeExecutedSequentially) {
				dsl().execute(query);
			}
		}
		if (!queriesToBeExecutedInBatch.isEmpty()) {
			Batch batch = dsl().batch(queriesToBeExecutedInBatch);
			batch.execute();
		}
	}
	
	protected Logger getLog() {
		return log;
	}
	
	protected CollectDSLContext dsl() {
		return dsl;
	}
	
	public void setDsl(CollectDSLContext dsl) {
		this.dsl = dsl;
	}
	
	public static class CollectStoreQuery {
		
		private Query internalQuery;

		public CollectStoreQuery(Query internalQuery) {
			super();
			this.internalQuery = internalQuery;
		}
		
		public Query getInternalQuery() {
			return internalQuery;
		}
		
		@Override
		public String toString() {
			return internalQuery.toString();
		}
	}
	
	public static class CollectStoreQueryBuffer {
		
		private static final int DEFAULT_BATCH_SIZE = 100;
		
		private int bufferSize;
		private List<CollectStoreQuery> buffer;
		
		public CollectStoreQueryBuffer() {
			this(DEFAULT_BATCH_SIZE);
		}
		
		public CollectStoreQueryBuffer(int size) {
			this.bufferSize = size;
			this.buffer = new ArrayList<CollectStoreQuery>(size);
		}
		
		public void append(CollectStoreQuery query) {
			buffer.add(query);
			if (buffer.size() == bufferSize) {
				flush();
			}
		}

		public void appendAll(List<CollectStoreQuery> queries) {
			for (CollectStoreQuery query : queries) {
				append(query);
			}
		}
		
		public int size() {
			return buffer.size();
		}
		
		public List<CollectStoreQuery> flush() {
			List<CollectStoreQuery> current = new ArrayList<CollectStoreQuery>(buffer);
			buffer.clear();
			return current;
		}

	}
}
