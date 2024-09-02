/**
 * 
 */
package org.openforis.collect.io.metadata.collectearth;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.openforis.collect.manager.RandomValuesGenerator;
import org.openforis.collect.manager.SurveyManager;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.SurveyFile;
import org.openforis.collect.model.SurveyFile.SurveyFileType;
import org.openforis.commons.io.csv.CsvLine;
import org.openforis.commons.io.csv.CsvReader;
import org.openforis.commons.io.csv.CsvWriter;
import org.openforis.commons.io.flat.FlatRecord;
import org.openforis.concurrency.Job;
import org.openforis.concurrency.Task;
import org.openforis.idm.metamodel.AttributeDefinition;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * @author S. Ricci
 *
 */
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class RandomGridGenerationJob extends Job {

	private SurveyManager surveyManager;
	// input
	private CollectSurvey survey;
	private File file;
	private double percentage;
	private String surveyFileName;
	private String newMeasurement;

	@Override
	protected void buildTasks() throws Throwable {
		addTask(new RandomGridGenerationTask());
	}

	@Override
	protected void validateInput() throws Throwable {
		super.validateInput();
		AttributeDefinition measurementKeyDef = survey.getFirstMeasurementKeyDef();
		if (measurementKeyDef == null) {
			throw new Exception("Expected at least one measurement attribute");
		}
	}

	@Override
	protected void afterExecute() {
		super.afterExecute();
		if (isCompleted()) {
			File outputFile = ((RandomGridGenerationTask) getTasks().get(0)).outputFile;
			SurveyFile surveyFile = new SurveyFile(survey);
			surveyFile.setType(SurveyFileType.COLLECT_EARTH_GRID);
			surveyFile.setFilename(surveyFileName);
			surveyManager.addSurveyFile(surveyFile, outputFile);
			outputFile.delete();
		}
	}

	public void setSurveyManager(SurveyManager surveyManager) {
		this.surveyManager = surveyManager;
	}
	
	public void setSurvey(CollectSurvey survey) {
		this.survey = survey;
	}
	
	public void setFile(File file) {
		this.file = file;
	}

	public void setPercentage(double percentage) {
		this.percentage = percentage;
	}

	public void setSurveyFileName(String surveyFileName) {
		this.surveyFileName = surveyFileName;
	}

	public void setNewMeasurement(String newMeasurement) {
		this.newMeasurement = newMeasurement;
	}

	private class RandomGridGenerationTask extends Task {
		private static final String ID_COLUMN = "id";
		private File outputFile;

		@Override
		protected void execute() throws Throwable {
			Set<String> randomPlotIds = generateRandomPlotIds();
			int totalRandomPlotIds = randomPlotIds.size();
			if (totalRandomPlotIds == 0) {
				throw new Error("Random grid cannot be generated: no records found (check the percentage value)");
			}
			setTotalItems(totalRandomPlotIds);

			outputFile = File.createTempFile("random_grid", ".csv");
			FileOutputStream outputStream = null;
			CsvWriter csvWriter = null;
			CsvReader csvReader = null;
			try {
				outputStream = new FileOutputStream(outputFile);
				csvWriter = new CsvWriter(outputStream);
				csvReader = new CsvReader(file);
				csvReader.readHeaders();
				List<String> headers = csvReader.getColumnNames();
				csvWriter.writeHeaders(headers);

				AttributeDefinition measurementKeyDef = survey.getFirstMeasurementKeyDef();
				String measurementAttributeName = measurementKeyDef.getName();
				int measurementColumnIndex = headers.indexOf(measurementAttributeName);

				FlatRecord csvRecord = csvReader.nextRecord();
				while (csvRecord != null) {
					if (!isRunning()) break;
					
					String plotId = csvRecord.getValue(ID_COLUMN, String.class);
					if (randomPlotIds.contains(plotId)) {
						Object[] values = csvRecord.toArray();
						Object[] valuesUpdated = ArrayUtils.clone(values);
						valuesUpdated[measurementColumnIndex] = newMeasurement;
						csvWriter.writeNext(valuesUpdated);
						incrementProcessedItems();
					}
					csvRecord = csvReader.nextRecord();
				}
			} finally {
				IOUtils.closeQuietly(csvReader);
				IOUtils.closeQuietly(csvWriter);
				IOUtils.closeQuietly(outputStream);
			}
		}

		private Set<String> generateRandomPlotIds() throws FileNotFoundException, IOException {
			List<String> plotIds = new ArrayList<>();
			try (CsvReader csvReader = new CsvReader(file);) {
				csvReader.readHeaders();
				CsvLine csvLine = csvReader.readNextLine();
				while (csvLine != null) {
					String plotId = csvLine.getValue(ID_COLUMN, String.class);
					plotIds.add(plotId);
					csvLine = csvReader.readNextLine();
				}
			}
			List<String> randomPlotIds = RandomValuesGenerator.generateRandomSubset(plotIds, percentage);
			return new HashSet<>(randomPlotIds);
		}

	}

}
