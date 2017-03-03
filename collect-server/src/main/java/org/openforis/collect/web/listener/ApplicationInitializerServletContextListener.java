package org.openforis.collect.web.listener;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jooq.impl.DataSourceConnectionProvider;
import org.openforis.collect.persistence.DbInitializer;
import org.openforis.collect.persistence.DbUtils;

public class ApplicationInitializerServletContextListener implements ServletContextListener {

	private final Log LOG = LogFactory.getLog(ApplicationInitializerServletContextListener.class);

	@Override
	public void contextInitialized(ServletContextEvent sce) {
		LOG.info("========Open Foris Collect - Starting initialization ==========");
		initDB();
		LOG.info("========Open Foris Collect - Initialized ======================");
	}

	protected void initDB() {
		LOG.info("========Open Foris Collect - Starting DB initialization ========");
		DataSourceConnectionProvider connectionProvider = new DataSourceConnectionProvider(DbUtils.getDataSource());
		new DbInitializer(connectionProvider).start();
		LOG.info("========Open Foris Collect - DB Initialized ====================");
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
	}

}