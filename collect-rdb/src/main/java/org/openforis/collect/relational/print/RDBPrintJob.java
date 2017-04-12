package org.openforis.collect.relational.print;

import java.io.File;
import java.io.FileWriter;
import java.io.Writer;

import org.apache.commons.io.IOUtils;
import org.openforis.collect.manager.RecordManager;
import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.RecordFilter;
import org.openforis.collect.relational.data.DataExtractorFactory;
import org.openforis.collect.relational.data.internal.CodeTableDataExtractor;
import org.openforis.collect.relational.model.CodeTable;
import org.openforis.collect.relational.model.DataTable;
import org.openforis.collect.relational.model.RelationalSchema;
import org.openforis.collect.relational.model.RelationalSchemaGenerator;
import org.openforis.commons.collection.Visitor;
import org.openforis.concurrency.Job;
import org.openforis.concurrency.Task;
import org.openforis.concurrency.Worker;

/**
 * 
 * @author S. Ricci
 *
 */
public class RDBPrintJob extends Job {

	public enum RdbDialect {
		STANDARD, SQLITE
	}

	//input
	private CollectSurvey survey;
	private String targetSchemaName;
	private boolean includeData;
	private RecordManager recordManager;
	private RecordFilter recordFilter;
	private RdbDialect dialect;
	private String dateTimeFormat;
	private boolean includeForeignKeysInCreateTable;
	
	//output
	private File outputFile;

	private transient RelationalSchema schema;
	private transient Writer writer;

	public RDBPrintJob() {
		this.includeForeignKeysInCreateTable = true;
	}
	
	@Override
	protected void createInternalVariables() throws Throwable {
		super.createInternalVariables();
		outputFile = File.createTempFile("rdb", ".sql");
		writer = new FileWriter(outputFile);
	}
	
	@Override
	protected void buildTasks() throws Throwable {
		addTask(new SchemaGenerationTask());
		addTask(new RDBSchemaPrintTask());
		addTask(new ReferenceDataPrintTask());
		if ( includeData ) {
			addTask(new RecordDataPrintTask());
		}
	}
	
	@Override
	protected void initializeTask(Worker task) {
		if ( task instanceof SchemaGenerationTask ) {
			((SchemaGenerationTask) task).setSurvey(survey);
			((SchemaGenerationTask) task).setTargetSchemaName(targetSchemaName);
		} else if(task instanceof RDBPrintTask) {
			RDBPrintTask t = (RDBPrintTask) task;
			t.setWriter(writer);
			t.setSchema(schema);
			t.setDialect(dialect);
			t.setDateTimeFormat(dateTimeFormat);
			
			if(task instanceof RDBSchemaPrintTask) {
				((RDBSchemaPrintTask) task).setIncludeForeignKeysInCreateTable(includeForeignKeysInCreateTable);
			} else if(task instanceof RecordDataPrintTask) {
				RecordDataPrintTask dataPrintTask = (RecordDataPrintTask) task;
				dataPrintTask.setRecordManager(recordManager);
				dataPrintTask.setRecordFilter(recordFilter);
			}
		}
		super.initializeTask(task);
	}
	
	@Override
	protected void onTaskCompleted(Worker task) {
		if ( task instanceof SchemaGenerationTask ) {
			this.schema = ((SchemaGenerationTask) task).getSchema();
		}
		super.onTaskCompleted(task);
	}
	
	@Override
	protected void onEnd() {
		super.onEnd();
		IOUtils.closeQuietly(writer);
	}
	
	public CollectSurvey getSurvey() {
		return survey;
	}
	
	public void setSurvey(CollectSurvey survey) {
		this.survey = survey;
	}
	
	public void setIncludeData(boolean includeData) {
		this.includeData = includeData;
	}
	
	public void setRecordManager(RecordManager recordManager) {
		this.recordManager = recordManager;
	}
	
	public void setRecordFilter(RecordFilter recordFilter) {
		this.recordFilter = recordFilter;
	}
	
	public void setWriter(Writer writer) {
		this.writer = writer;
	}
	
	public void setTargetSchemaName(String targetSchemaName) {
		this.targetSchemaName = targetSchemaName;
	}
	
	public void setDialect(RdbDialect dialect) {
		this.dialect = dialect;
	}
	
	public void setDateTimeFormat(String dateTimeFormat) {
		this.dateTimeFormat = dateTimeFormat;
	}
	
	public void setIncludeForeignKeysInCreateTable(boolean includeForeignKeysInCreateTable) {
		this.includeForeignKeysInCreateTable = includeForeignKeysInCreateTable;
	}
	
	public File getOutputFile() {
		return outputFile;
	}
	
	private static class SchemaGenerationTask extends Task {

		//input
		private CollectSurvey survey;
		private String targetSchemaName;
		
		//output
		private RelationalSchema schema;
		
		@Override
		protected void execute() throws Throwable {
			RelationalSchemaGenerator schemaGenerator = new RelationalSchemaGenerator();
			RelationalSchema schema = schemaGenerator.generateSchema(survey, targetSchemaName);
			this.schema = schema;
		}

		public void setSurvey(CollectSurvey survey) {
			this.survey = survey;
		}
		
		public void setTargetSchemaName(String targetSchemaName) {
			this.targetSchemaName = targetSchemaName;
		}
		
		public RelationalSchema getSchema() {
			return schema;
		}
		
	}
	
	public static class ReferenceDataPrintTask extends RDBPrintTask {
		
		@Override
		protected long countTotalItems() {
			long total = 0;
			for (CodeTable codeTable : schema.getCodeListTables()) {
				CodeTableDataExtractor extractor = DataExtractorFactory.getExtractor(codeTable);
				total += extractor.getTotal();
			}
			return total;
		}
		
		@Override
		protected void execute() throws Throwable {
			for (CodeTable codeTable : schema.getCodeListTables()) {
				if(!isRunning()) {
					return;
				}
				CodeTableDataExtractor extractor = DataExtractorFactory.getExtractor(codeTable);
				setProcessedItems(getProcessedItems() + extractor.getTotal());
			}
		}
		
	}

	public static class RecordDataPrintTask extends RDBPrintTask {
		
		private RecordManager recordManager;
		private RecordFilter recordFilter;

		@Override
		protected long countTotalItems() {
			return recordManager.countRecords(recordFilter);
		}

		@Override
		protected void execute() throws Throwable {
			recordManager.visitSummaries(recordFilter, null, new Visitor<CollectRecord>() {
				public void visit(CollectRecord summary) {
					CollectRecord record = recordManager.load((CollectSurvey) summary.getSurvey(), summary.getId(), summary.getStep());
					if (record != null) {
						for (DataTable table : schema.getDataTables()) {
							if(!isRunning()) {
								return;
							}
							try {
								writeBatchInsert(table, DataExtractorFactory.getRecordDataExtractor(table, record));
							} catch(Exception e) {
								throw new RuntimeException(e);
							}
						}
					}
					incrementProcessedItems();
				}
			});
		}
		
		public void setRecordManager(RecordManager recordManager) {
			this.recordManager = recordManager;
		}
		
		public void setRecordFilter(RecordFilter recordFilter) {
			this.recordFilter = recordFilter;
		}
	}
	
}