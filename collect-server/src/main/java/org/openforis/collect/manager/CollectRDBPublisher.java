package org.openforis.collect.manager;

import java.sql.Connection;
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
import org.openforis.collect.relational.model.RelationalSchemaGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.transaction.annotation.Transactional;

/**
 * 
 * @author S. Ricci
 *
 */
public class CollectRDBPublisher {
	
	protected static Log LOG = LogFactory.getLog(CollectRDBPublisher.class);

	@Autowired
	private SurveyManager surveyManager;
	@Autowired
	private RecordDao recordDao;
	@Autowired
	private DataSource rdbDataSource;
	
	public void export(String surveyName, String rootEntityName, Step step,
			String targetSchemaName) throws CollectRdbException {
		CollectSurvey survey = surveyManager.get(surveyName);
		
		// Generate relational model
		RelationalSchemaGenerator rsg = new RelationalSchemaGenerator();
		RelationalSchema targetSchema = rsg.generateSchema(survey, targetSchemaName);
		
		createTargetDBSchema(targetSchema);
		
		// Insert data
		List<CollectRecord> summaries = recordDao.loadSummaries(survey, rootEntityName, step);
		int total = summaries.size();
		if ( LOG.isInfoEnabled() ) {
			LOG.info("Total records: " + total);
		}
		insertRecords(survey, summaries, step, targetSchema);
		if ( LOG.isInfoEnabled() ) {
			LOG.info("\nAll records exported");
		}
	}

	protected void createTargetDBSchema(RelationalSchema targetSchema)
			throws CollectRdbException {
		Connection rdbTargetConn = DataSourceUtils.getConnection(rdbDataSource);
		RelationalSchemaCreator relationalSchemaCreator = new LiquibaseRelationalSchemaCreator();
		relationalSchemaCreator.createRelationalSchema(targetSchema, rdbTargetConn);
	}
	
	@Transactional("rdbTransactionManager")
	protected void insertRecords(CollectSurvey survey, List<CollectRecord> summaries, Step step, 
			RelationalSchema targetSchema) throws CollectRdbException {
		int count = 0;
		Connection rdbTargetConn = DataSourceUtils.getConnection(rdbDataSource);
		DatabaseExporter databaseExporter = new JooqDatabaseExporter(new DialectAwareJooqFactory(rdbTargetConn));
		for (CollectRecord summary : summaries) {
			if ( LOG.isInfoEnabled() ) {
				LOG.info("Exporting record " + (++count));
			}
			CollectRecord record = recordDao.load(survey, summary.getId(), step.getStepNumber());
			databaseExporter.insertData(targetSchema, record);
		}
//		try {
//			rdbTargetConn.commit();
//		} catch (SQLException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
	}

}
