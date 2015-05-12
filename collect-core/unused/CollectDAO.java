package org.openforis.collect.persistence;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jooq.SQLDialect;
import org.jooq.impl.Factory;

/**
 * @author G. Miceli
 * @author M. Togna
 */
public abstract class CollectDao {
	private final Log log = LogFactory.getLog(getClass());

	private DataSource dataSource;

	public DataSource getDataSource() {
		return dataSource;
	}

	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}
	/*
	protected <T> T call(Callback<T> callback) throws SQLException {
		if ( dataSource==null ) {
			throw new IllegalStateException("Data source not set");
		}
		Connection conn = null;
		try {
			conn = dataSource.getConnection();
			conn.setAutoCommit(false);
			// TODO Real dialect from config file
			Factory jooqFactory = new Factory(conn, SQLDialect.POSTGRES);
			T result = callback.execute(jooqFactory);
			conn.commit();
			return result;
		} catch (RuntimeException t) {
			if ( conn!=null && !conn.isClosed() ) {
				conn.rollback();
			}
			throw t;
		} catch (SQLException ex) {
			if ( conn!=null && !conn.isClosed() ) {
				conn.rollback();
			}
			throw ex;
		} finally {
			if ( conn != null ) {		
				conn.close();
			} 
			conn = null;
		}
	}
	 */
	protected class Transaction {
		private Connection conn = null;

		protected Transaction() {
		}

		protected void begin() {
			if ( dataSource==null ) {
				throw new IllegalStateException("Data source not set");
			}
			try {
				conn = dataSource.getConnection();
				conn.setAutoCommit(false);
			} catch (SQLException e) {
				throw new RuntimeException("Could not create database connection", e);
			}
		}

		/**
		 * Automatically rolls back the transaction on failure  
		 * @param callback
		 * @return
		 */
		protected <T> T call(JooqCallback<T> callback)  {
			// TODO Read dialect from config file
			try {
				Factory jooqFactory = new Factory(conn, SQLDialect.POSTGRES);
				T result = callback.execute(jooqFactory);
				return result;
			} catch (RuntimeException e) {
				rollback();
				throw e;
			}
		}
		
		protected void commit() {
			try {
				conn.commit();
			} catch (SQLException e) {
				log.error("Commit failed", e);
			}
		}
		
		protected void rollback() {
			try {
				if ( conn!=null && !conn.isClosed() ) {
					conn.rollback();
				}
			} catch (SQLException e) {
				log.warn("Rollback failed", e);
			}
		}

		protected void end() {
			try {
				if ( conn!=null && !conn.isClosed() ) {
					conn.close();
				}
			} catch (SQLException e) {
				log.warn("Could not close connection", e);
			}
		}
	}
	
	protected interface JooqCallback<T> {
		abstract T execute(Factory jooqFactory);
				
	}
	
	protected Log getLog() {
		return log;
	}
}
