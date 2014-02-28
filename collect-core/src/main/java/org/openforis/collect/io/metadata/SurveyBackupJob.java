package org.openforis.collect.io.metadata;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.IOUtils;
import org.openforis.collect.io.metadata.samplingdesign.SamplingDesignFileColumn;
import org.openforis.collect.manager.SamplingDesignManager;
import org.openforis.collect.manager.SurveyManager;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.SamplingDesignItem;
import org.openforis.collect.model.SamplingDesignSummaries;
import org.openforis.collect.schedule.CollectJob;
import org.openforis.commons.io.csv.CsvWriter;
import org.openforis.schedule.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * 
 * @author S. Ricci
 *
 */
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class SurveyBackupJob extends CollectJob<SurveyBackupJob> {

	public static final String SURVEY_XML_FILE_NAME = "idml.xml";
	public static final String SAMPLING_DESIGN_FILE_NAME = "sampling_design.csv";
	
	private CollectSurvey survey;
	private File outputFile;
	private ZipOutputStream zipOutputStream;
	
	@Override
	public void init() {
		try {
			outputFile = File.createTempFile("collect", "survey_export.zip");
			zipOutputStream = new ZipOutputStream(new FileOutputStream(outputFile));
		} catch (IOException e) {
			throw new RuntimeException("Error creating output file for survey export", e);
		}
		IdmlExportTask idmlExportTask = createTask(IdmlExportTask.class);
		idmlExportTask.setSurvey(survey);
		idmlExportTask.setOutputStream(zipOutputStream);
		addTask(idmlExportTask);

		SamplingDesignExportTask samplingDesignExportTask = createTask(SamplingDesignExportTask.class);
		samplingDesignExportTask.setSurvey(survey);
		samplingDesignExportTask.setOutputStream(zipOutputStream);
		addTask(samplingDesignExportTask);

		super.init();
	}
	
	@Override
	protected void onBeforeCompleted() {
		super.onBeforeCompleted();
		IOUtils.closeQuietly(zipOutputStream);
	}
	
	@Override
	protected Task<SurveyBackupJob> nextTask() {
		Task<SurveyBackupJob> currentTask = getCurrentTask();
		try {
			//prepare new zip entry
			if ( currentTask != null ) {
				if ( currentTask instanceof IdmlExportTask || 
						currentTask instanceof SamplingDesignExportTask ) {
					zipOutputStream.closeEntry();
				}
			}
			Task<SurveyBackupJob> nextTask = super.nextTask();
			if ( nextTask instanceof IdmlExportTask ) {
				zipOutputStream.putNextEntry(new ZipEntry(SURVEY_XML_FILE_NAME));
			} else if ( nextTask instanceof SamplingDesignExportTask ) {
				zipOutputStream.putNextEntry(new ZipEntry(SAMPLING_DESIGN_FILE_NAME));
			}
			return nextTask;
		} catch ( IOException e ) {
			throw new RuntimeException("Error preparing next zip entry", e);
		}
	}
	
	public CollectSurvey getSurvey() {
		return survey;
	}

	public void setSurvey(CollectSurvey survey) {
		this.survey = survey;
	}
	
	public File getOutputFile() {
		return outputFile;
	}
	
	@Component
	@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
	public static class IdmlExportTask extends Task<SurveyBackupJob> {
		
		@Autowired
		private SurveyManager surveyManager;
		
		//parameters
		private CollectSurvey survey;
		private OutputStream outputStream;
		
		@Override
		protected void execute() throws Throwable {
			surveyManager.marshalSurvey(survey, outputStream, true, true, false);
		}
		
		public CollectSurvey getSurvey() {
			return survey;
		}
		
		public void setSurvey(CollectSurvey survey) {
			this.survey = survey;
		}

		public OutputStream getOutputStream() {
			return outputStream;
		}

		public void setOutputStream(OutputStream outputStream) {
			this.outputStream = outputStream;
		}
		
	}
	
	@Component
	@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
	public static class SamplingDesignExportTask extends Task<SurveyBackupJob> {
		
		@Autowired
		private SamplingDesignManager samplingDesignManager;

		//parameters
		private CollectSurvey survey;
		private OutputStream outputStream;

		@Override
		protected long countTotalItems() {
			Integer surveyId = survey.getId();
			int count = survey.isWork() ? 
					samplingDesignManager.countBySurveyWork(surveyId): 
					samplingDesignManager.countBySurvey(surveyId);
			return count;
		}
		
		@Override
		protected void execute() throws Throwable {
			if ( getTotalItems() > 0 ) {
				Integer surveyId = survey.getId();
				boolean work = survey.isWork();
				
				CsvWriter writer = new CsvWriter(outputStream);
				SamplingDesignSummaries summaries = work ? 
						samplingDesignManager.loadBySurveyWork(surveyId): 
							samplingDesignManager.loadBySurvey(surveyId);
						
				ArrayList<String> colNames = getHeaders();
				writer.writeHeaders(colNames.toArray(new String[0]));
				
				List<SamplingDesignItem> items = summaries.getRecords();
				for (SamplingDesignItem item : items) {
					writeSummary(writer, item);
					incrementItemsProcessed();
				}
				writer.flush();
			}
		}

		private ArrayList<String> getHeaders() {
			ArrayList<String> colNames = new ArrayList<String>();
			colNames.addAll(Arrays.asList(SamplingDesignFileColumn.LEVEL_COLUMN_NAMES));
			colNames.add(SamplingDesignFileColumn.X.getColumnName());
			colNames.add(SamplingDesignFileColumn.Y.getColumnName());
			colNames.add(SamplingDesignFileColumn.SRS_ID.getColumnName());
			return colNames;
		}

		protected void writeSummary(CsvWriter writer, SamplingDesignItem item) {
			List<String> lineValues = new ArrayList<String>();
			List<String> levelCodes = item.getLevelCodes();
			SamplingDesignFileColumn[] levelColumns = SamplingDesignFileColumn.LEVEL_COLUMNS;
			for (int level = 1; level <= levelColumns.length; level++) {
				String levelCode = level <= levelCodes.size() ? item.getLevelCode(level): "";
				lineValues.add(levelCode);
			}
			lineValues.add(item.getX().toString());
			lineValues.add(item.getY().toString());
			lineValues.add(item.getSrsId());
			writer.writeNext(lineValues.toArray(new String[0]));
		}

		public OutputStream getOutputStream() {
			return outputStream;
		}

		public void setOutputStream(OutputStream outputStream) {
			this.outputStream = outputStream;
		}

		public CollectSurvey getSurvey() {
			return survey;
		}

		public void setSurvey(CollectSurvey survey) {
			this.survey = survey;
		}
		
	}
}
