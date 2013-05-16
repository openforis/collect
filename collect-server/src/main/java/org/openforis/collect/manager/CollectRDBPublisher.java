package org.openforis.collect.manager;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.CollectRecord.Step;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.persistence.RecordDao;
import org.openforis.collect.persistence.jooq.DialectAwareJooqFactory;
import org.openforis.collect.relational.CollectRdbException;
import org.openforis.collect.relational.DatabaseExporter;
import org.openforis.collect.relational.RelationalSchemaCreator;
import org.openforis.collect.relational.jooq.JooqDatabaseExporter;
import org.openforis.collect.relational.liquibase.LiquibaseRelationalSchemaCreator;
import org.openforis.collect.relational.model.RelationalSchema;
import org.openforis.collect.relational.model.RelationalSchemaConfig;
import org.openforis.collect.relational.model.RelationalSchemaGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.transaction.annotation.Transactional;

/**
 * 
 * @author S. Ricci
 *
 */
@Transactional
public class CollectRDBPublisher {
	
	protected static Log LOG = LogFactory.getLog(CollectRDBPublisher.class);

	@Autowired
	private SurveyManager surveyManager;
	@Autowired
	private RecordDao recordDao;
	@Autowired
	@Qualifier("dataSource")
	private DataSource dataSource;
	@Autowired(required=false)
	@Qualifier("rdbDataSource")
	private DataSource rdbDataSource;
	
	public void export(String surveyName, String rootEntityName, Step step,
			String targetSchemaName) throws CollectRdbException {
		export(surveyName, rootEntityName, step, targetSchemaName, RelationalSchemaConfig.createDefault());
	}
	
	public void export(String surveyName, String rootEntityName, Step step,
			String targetSchemaName, RelationalSchemaConfig config) throws CollectRdbException {
		Connection targetConn = getTargetConnection();
		export(surveyName, rootEntityName, step, targetSchemaName, targetConn, config);
	}

	public void export(String surveyName, String rootEntityName, Step step,
			String targetSchemaName, Connection targetConn, RelationalSchemaConfig config) throws CollectRdbException {
		CollectSurvey survey = surveyManager.get(surveyName);
		
		// Generate relational model
		RelationalSchemaGenerator rsg = new RelationalSchemaGenerator(config);
		RelationalSchema targetSchema = rsg.generateSchema(survey, targetSchemaName);
		
		createTargetDBSchema(targetSchema, targetConn);
		
		// Insert data
		List<CollectRecord> summaries = recordDao.loadSummaries(survey, rootEntityName, step);
		int total = summaries.size();
		if ( LOG.isInfoEnabled() ) {
			LOG.info("Total records: " + total);
		}
		insertRecords(survey, summaries, step, targetSchema, targetConn);
		if ( LOG.isInfoEnabled() ) {
			LOG.info("\nAll records exported");
		}
	}

	protected void createTargetDBSchema(RelationalSchema targetSchema, Connection targetConn)
			throws CollectRdbException {
		RelationalSchemaCreator relationalSchemaCreator = new LiquibaseRelationalSchemaCreator();
		relationalSchemaCreator.createRelationalSchema(targetSchema, targetConn);
	}
	
	@Transactional("rdbTransactionManager")
	protected void insertRecords(CollectSurvey survey, List<CollectRecord> summaries, Step step, 
			RelationalSchema targetSchema, Connection targetConn) throws CollectRdbException {
		int count = 0;
		DatabaseExporter databaseExporter = new JooqDatabaseExporter(new DialectAwareJooqFactory(targetConn));
		databaseExporter.insertReferenceData(targetSchema);
		for (CollectRecord summary : summaries) {
			if ( LOG.isInfoEnabled() ) {
				LOG.info("Exporting record #" + (++count) + " id: " + summary.getId());
			}
			CollectRecord record = recordDao.load(survey, summary.getId(), step.getStepNumber());
			databaseExporter.insertData(targetSchema, record);
		}
		try {
			targetConn.commit();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	protected Connection getTargetConnection() {
		DataSource targetDataSource = rdbDataSource == null ? dataSource: rdbDataSource;
		Connection targetConn = DataSourceUtils.getConnection(targetDataSource);
		return targetConn;
	}
	
	public static void main(String[] args) throws CollectRdbException {
		ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("application-context.xml");
		CollectRDBPublisher publisher = ctx.getBean(CollectRDBPublisher.class);
		RelationalSchemaConfig config = RelationalSchemaConfig.createDefault();
//		config.setDefaultCode(null);
		publisher.export(
				"naforma1",
				"cluster",
				Step.ANALYSIS,
				"naforma1",
				config);
//		DriverManager.getConnection("jdbc:postgresql://localhost:5433/archenland1", "postgres","postgres")); 
	}
	
}
