package org.jooq.impl;

import java.sql.Connection;

import javax.sql.DataSource;

public class SingleConnectionDataSourceConnectionProvider extends DataSourceConnectionProvider {

	public SingleConnectionDataSourceConnectionProvider(DataSource dataSource) {
		super(dataSource);
	}

	@Override
	public void release(Connection connection) {
	}

}
