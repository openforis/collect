package org.openforis.collect.manager;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;
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
import org.openforis.collect.relational.model.BooleanVarcharColumn;
import org.openforis.collect.relational.model.CodeListTable;
import org.openforis.collect.relational.model.CodeValueFKColumn;
import org.openforis.collect.relational.model.Column;
import org.openforis.collect.relational.model.DataColumn;
import org.openforis.collect.relational.model.RelationalSchema;
import org.openforis.collect.relational.model.RelationalSchemaConfig;
import org.openforis.collect.relational.model.RelationalSchemaGenerator;
import org.openforis.collect.relational.model.Table;
import org.openforis.idm.metamodel.AttributeDefinition;
import org.openforis.idm.metamodel.CodeList;
import org.openforis.idm.metamodel.FieldDefinition;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.metamodel.NodeLabel.Type;
import org.openforis.idm.metamodel.NumericAttributeDefinition;
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
	@Qualifier("rdbDataSource")
	private DataSource rdbDataSource;
	
	public void export(String surveyName, String rootEntityName, Step step,
			String targetSchemaName) throws CollectRdbException {
		export(surveyName, rootEntityName, step, targetSchemaName, RelationalSchemaConfig.createDefault());
	}
	
	public void export(String surveyName, String rootEntityName, Step step,
			String targetSchemaName, RelationalSchemaConfig config) throws CollectRdbException {
		Connection targetConn = DataSourceUtils.getConnection(rdbDataSource);
		export(surveyName, rootEntityName, step, targetSchemaName, targetConn, config);
	}
	
	public void export(String surveyName, String rootEntityName, Step step,
			String targetSchemaName, Connection targetConn, RelationalSchemaConfig config) throws CollectRdbException {
		CollectSurvey survey = surveyManager.get(surveyName);
		
		// Generate relational model
		RelationalSchemaGenerator rsg = new RelationalSchemaGenerator(config);
		RelationalSchema targetSchema = rsg.generateSchema(survey, targetSchemaName);
		targetSchema.print(System.out);
//		createTargetDBSchema(targetSchema, targetConn);
		
		// Insert data
//		List<CollectRecord> summaries = recordDao.loadSummaries(survey, rootEntityName, step);
//		int total = summaries.size();
//		if ( LOG.isInfoEnabled() ) {
//			LOG.info("Total records: " + total);
//		}
//		insertRecords(survey, summaries, step, targetSchema, targetConn);
//		if ( LOG.isInfoEnabled() ) {
//			LOG.info("\nAll records exported");
//		}
		
		printMdxSchema(targetSchema);
	}

	private void printMdxSchema(RelationalSchema targetSchema) {
		List<Table<?>> tables = targetSchema.getTables();
		for (Table<?> table : tables) {
			if ( table.getName().equals("household") || table.equals("informant") ) {
				System.out.println(table.getName()+"-------------------------------");
				List<Column<?>> cols = table.getColumns();
				// Dimensions
				for (Column<?> column : cols) {
					if ( column instanceof CodeValueFKColumn ) {
						CodeValueFKColumn fkcol = (CodeValueFKColumn) column;
						printCodeDimension(fkcol);
					} else if ( column instanceof BooleanVarcharColumn ){
						BooleanVarcharColumn col = (BooleanVarcharColumn) column;
						printBooleanDimension(col);
					} 
				}
				// Measures
				for (Column<?> column : cols) {
					if ( column instanceof DataColumn && ( column.getType() == Types.INTEGER || column.getType() == Types.FLOAT ) ) {
						NodeDefinition defn = ((DataColumn) column).getNodeDefinition();
						if ( defn instanceof FieldDefinition ) {
							AttributeDefinition attr = ((FieldDefinition) defn).getAttributeDefinition();
							if ( attr instanceof NumericAttributeDefinition ) {
								DataColumn col = (DataColumn) column;
								printMeasures(col, attr);							
							}
						}
						 
					}
				}
			}
		}
	}

	private void printMeasures(DataColumn col, AttributeDefinition defn) {
		String caption = defn.getLabel(Type.INSTANCE, "");
		caption = caption == null ? defn.getLabel(Type.HEADING, "") : caption;
		
		printMeasure(col, defn, "sum", "Total "+caption);
		printMeasure(col, defn, "avg", "Avg. "+caption);
		printMeasure(col, defn, "min", "Min. "+caption);
		printMeasure(col, defn, "max", "Max. "+caption);
	}

	private void printMeasure(DataColumn col, AttributeDefinition defn, String agg, String caption) {
		System.out.println(
		"<Measure name=\""+agg+defn.getName()+"\" column=\""+col.getName()+
			"\" datatype=\"Numeric\" formatString=\"#,###.##\" aggregator=\""+agg+"\" caption=\""+caption+"\" visible=\"true\">"+
		"</Measure>"
		);
	}

	private void printBooleanDimension(BooleanVarcharColumn col) {
		FieldDefinition<Boolean> fld = col.getFieldDefinition();
		AttributeDefinition defn = fld.getAttributeDefinition();
		String caption = defn.getLabel(Type.INSTANCE, "");
		caption = caption == null ? defn.getLabel(Type.HEADING, "") : caption;
		System.out.println(
				          " <Dimension type=\"StandardDimension\" visible=\"true\" highCardinality=\"false\" name=\""+ 
				        		  defn.getName()+ "\" caption=\""+caption+"\">\n"
						+ "		<Hierarchy name=\""+ defn.getName() + "\" visible=\"true\" hasAll=\"true\">\n"
						+ "			<Level name=\""+ defn.getName()+ "\" visible=\"true\" caption=\""+caption+"\"\n"
						+ "				column=\""+col.getName()+"\" uniqueMembers=\"false\" levelType=\"Regular\" hideMemberIf=\"Never\">\n"
						+ "			</Level>\n"
						+ "		</Hierarchy>\n"
						+ "	</Dimension>");
	}

	private void printCodeDimension(CodeValueFKColumn fkcol) {
		NodeDefinition defn = fkcol.getNodeDefinition();
		CodeListTable codeListTable = fkcol.getCodeListTable();
		CodeList codeList = codeListTable.getCodeList();
		String caption = defn.getLabel(Type.INSTANCE, "");
		caption = caption == null ? defn.getLabel(Type.HEADING, "") : caption;
		System.out.println(
				          " <Dimension type=\"StandardDimension\" visible=\"true\" highCardinality=\"false\" name=\""+ 
				        		  codeList.getName()+ "\" caption=\""+caption+"\" foreignKey=\""+fkcol.getName()+"\">\n"
						+ "		<Hierarchy name=\""+ codeList.getName() + "\" visible=\"true\" hasAll=\"true\">\n"
						+ "			<Table name=\""+codeListTable.getName()+"\" schema=\"naforma1_se\">\n"
						+ "			</Table>\n"
						+ "			<Level name=\""+ codeList.getName()+ "\" visible=\"true\" caption=\""+caption+"\"\n"
						+ "				table=\""+codeListTable.getName()+"\" column=\""+codeListTable.getColumns().get(0).getName()+"\" nameColumn=\""+codeListTable.getBaseName()+"_label_en\" uniqueMembers=\"false\" levelType=\"Regular\" hideMemberIf=\"Never\">\n"
						+ "			</Level>\n"
						+ "		</Hierarchy>\n"
						+ "	</Dimension>");
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
	
	public static void main(String[] args) throws CollectRdbException {
		ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("application-context.xml");
		CollectRDBPublisher publisher = ctx.getBean(CollectRDBPublisher.class);
		RelationalSchemaConfig config = RelationalSchemaConfig.createDefault();
//		config.setDefaultCode(null);
		publisher.export(
				"naforma1",
				"cluster",
				Step.ANALYSIS,
				"naforma1_se",
				config);
//		DriverManager.getConnection("jdbc:postgresql://localhost:5433/archenland1", "postgres","postgres")); 
	}
	
}
