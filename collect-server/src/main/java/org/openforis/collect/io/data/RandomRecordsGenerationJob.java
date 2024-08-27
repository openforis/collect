/**
 * 
 */
package org.openforis.collect.io.data;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.openforis.collect.manager.RandomValuesGenerator;
import org.openforis.collect.manager.RecordManager;
import org.openforis.collect.manager.SurveyManager;
import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.CollectRecordSummary;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.RecordFilter;
import org.openforis.collect.model.RecordUpdater;
import org.openforis.collect.model.SurveyFile;
import org.openforis.collect.model.SurveyFile.SurveyFileType;
import org.openforis.commons.io.csv.CsvReader;
import org.openforis.commons.io.csv.CsvWriter;
import org.openforis.commons.io.flat.FlatRecord;
import org.openforis.concurrency.Job;
import org.openforis.concurrency.Task;
import org.openforis.idm.metamodel.AttributeDefinition;
import org.openforis.idm.model.Attribute;
import org.openforis.idm.model.Entity;
import org.openforis.idm.model.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * @author S. Ricci
 *
 */
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class RandomRecordsGenerationJob extends Job {

	private SurveyManager surveyManager;
	private RecordManager recordManager;
	// input
	private CollectSurvey survey;
	private File file;
	private double percentage;
	private String outputGridSurveyFileName;
	private String oldMeasurement;
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
			surveyFile.setFilename(outputGridSurveyFileName);
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

	public void setOutputGridSurveyFileName(String outputGridSurveyFileName) {
		this.outputGridSurveyFileName = outputGridSurveyFileName;
	}

	public void setOldMeasurement(String oldMeasurement) {
		this.oldMeasurement = oldMeasurement;
	}

	public void setNewMeasurement(String newMeasurement) {
		this.newMeasurement = newMeasurement;
	}

	private class RandomGridGenerationTask extends Task {
		private static final String ID_COLUMN = "id";
		private File outputFile;

		@Override
		protected void execute() throws Throwable {
			RecordUpdater recordUpdater = new RecordUpdater();
			AttributeDefinition measurementKeyDef = survey.getFirstMeasurementKeyDef();
			RandomRecordKeysGenerationResult keyValuesGenerationResult = generateRandomKeyValues();
			Set<String> randomPlotIds = keyValuesGenerationResult.keyValues;
			setTotalItems(randomPlotIds.size());

			for (String plotId : randomPlotIds) {
				int recordId = keyValuesGenerationResult.recordIdByKeyValue.get(plotId);
				CollectRecord record = recordManager.load(survey, recordId);
				record.setId(null);
				Entity rootEntity = record.getRootEntity();
				Attribute<?, Value> measurementKeyAttr = rootEntity.getChild(measurementKeyDef);
				Value newMeasurementValue = measurementKeyDef.createValue(newMeasurement);
				recordUpdater.updateAttribute(measurementKeyAttr, newMeasurementValue);
//				recordManager.save(record);
				incrementProcessedItems();
			}
			generateGridFile(randomPlotIds);
		}

		private void generateGridFile(Set<String> randomPlotIds)
				throws IOException, FileNotFoundException, UnsupportedEncodingException {
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
					if (!isRunning())
						break;

					String plotId = csvRecord.getValue(ID_COLUMN, String.class);
					if (randomPlotIds.contains(plotId)) {
						Object[] values = csvRecord.toArray();
						Object[] valuesUpdated = ArrayUtils.clone(values);
						valuesUpdated[measurementColumnIndex] = newMeasurement;
						csvWriter.writeNext(valuesUpdated);
					}
					csvRecord = csvReader.nextRecord();
				}
			} finally {
				IOUtils.closeQuietly(csvReader);
				IOUtils.closeQuietly(csvWriter);
				IOUtils.closeQuietly(outputStream);
			}
		}

		private RandomRecordKeysGenerationResult generateRandomKeyValues() throws FileNotFoundException, IOException {
			List<String> keyValues = new ArrayList<>();
			Map<String, Integer> recordIdByKeyValue = new HashMap<>();
			RecordFilter filter = new RecordFilter(survey);
			List<CollectRecordSummary> oldRecordsSummaries = recordManager.loadSummaries(filter);
			for (CollectRecordSummary recordSummary : oldRecordsSummaries) {
				List<String> recordKeyValues = recordSummary.getRootEntityKeyValues();
				String measurementValue = recordKeyValues.get(1);
				if (oldMeasurement.equals(measurementValue)) {
					String keyValue = recordKeyValues.get(0);
					keyValues.add(keyValue);
					recordIdByKeyValue.put(keyValue, recordSummary.getId());
				}
			}
			List<String> randomKeyValues = RandomValuesGenerator.generateRandomSubset(keyValues, percentage);
			RandomRecordKeysGenerationResult result = new RandomRecordKeysGenerationResult();
			result.keyValues = new HashSet<>(randomKeyValues);
			result.recordIdByKeyValue = recordIdByKeyValue;
			return result;
		}
	}

	private static class RandomRecordKeysGenerationResult {
		Set<String> keyValues;
		Map<String, Integer> recordIdByKeyValue;
	}

}
