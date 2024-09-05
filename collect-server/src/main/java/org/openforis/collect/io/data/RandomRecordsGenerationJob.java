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

import org.apache.commons.compress.utils.FileNameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.openforis.collect.concurrency.SurveyLockingJob;
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
import org.openforis.collect.model.SurveySummary;
import org.openforis.collect.model.User;
import org.openforis.collect.utils.Files;
import org.openforis.commons.io.csv.CsvReader;
import org.openforis.commons.io.csv.CsvWriter;
import org.openforis.commons.io.flat.FlatRecord;
import org.openforis.concurrency.Task;
import org.openforis.idm.metamodel.AttributeDefinition;
import org.openforis.idm.model.Attribute;
import org.openforis.idm.model.Entity;
import org.openforis.idm.model.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * @author S. Ricci
 *
 */
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class RandomRecordsGenerationJob extends SurveyLockingJob {

	@Autowired
	private SurveyManager surveyManager;
	@Autowired
	private RecordManager recordManager;
	// input
	private User user;
	private double percentage;
	private String sourceGridSurveyFileName;
	private String oldMeasurement;
	private String newMeasurement;
	// temp variables
	private CollectSurvey tempSurvey;
	private String outputGridSurveyFileName;
	private Boolean countOnly;

	@Override
	protected void createInternalVariables() throws Throwable {
		super.createInternalVariables();
		String surveyUri = survey.getUri();
		SurveySummary surveySummary = surveyManager.loadSummaryByUri(surveyUri);
		if (!surveySummary.isTemporary()) {
			tempSurvey = surveyManager.createTemporarySurveyFromPublished(surveyUri, user);
		} else {
			Integer tempSurveyId = surveySummary.getId();
			tempSurvey = surveyManager.getOrLoadSurveyById(tempSurveyId);
		}
		outputGridSurveyFileName = generateOutputGridSurveyFileName();
		// validate output grid file name
		List<SurveyFile> surveyFileSummaries = surveyManager.loadSurveyFileSummaries(tempSurvey);
		for (SurveyFile surveyFile : surveyFileSummaries) {
			if (surveyFile.getFilename().equals(outputGridSurveyFileName)) {
				throw new Exception("Grid file with the same name already exists: " + outputGridSurveyFileName);
			}
		}
	}

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
		if (isCompleted() && !Boolean.TRUE.equals(countOnly)) {
			File outputFile = ((RandomGridGenerationTask) getTasks().get(0)).outputFile;
			SurveyFile surveyFile = new SurveyFile(tempSurvey);
			surveyFile.setType(SurveyFileType.COLLECT_EARTH_GRID);
			String outputGridSurveyFileName = generateOutputGridSurveyFileName();
			surveyFile.setFilename(outputGridSurveyFileName);
			surveyManager.addSurveyFile(surveyFile, outputFile);
			outputFile.delete();
		}
	}
	
	@Override
	protected Map<String, Object> prepareResult() {
		Map<String, Object> result = super.prepareResult();
		RandomGridGenerationTask task = (RandomGridGenerationTask) getTasks().get(0);
		result.put("recordsCount", task.getTotalItems());
		return result;
	}

	private String generateOutputGridSurveyFileName() {
		AttributeDefinition measurementKeyDef = survey.getFirstMeasurementKeyDef();
		String inputName = FileNameUtils.getBaseName(sourceGridSurveyFileName);
		return inputName + "_" + measurementKeyDef.getName() + "_" + newMeasurement + ".csv";
	}

	public void setSurveyManager(SurveyManager surveyManager) {
		this.surveyManager = surveyManager;
	}
	
	public void setUser(User user) {
		this.user = user;
	}

	public void setPercentage(double percentage) {
		this.percentage = percentage;
	}

	public void setSourceGridSurveyFileName(String sourceGridSurveyFileName) {
		this.sourceGridSurveyFileName = sourceGridSurveyFileName;
	}

	public void setOldMeasurement(String oldMeasurement) {
		this.oldMeasurement = oldMeasurement;
	}

	public void setNewMeasurement(String newMeasurement) {
		this.newMeasurement = newMeasurement;
	}
	
	public void setCountOnly(Boolean countOnly) {
		this.countOnly = countOnly;
	}

	private class RandomGridGenerationTask extends Task {
		private static final String ID_COLUMN = "id";
		private File outputFile;

		@Override
		protected void execute() throws Throwable {
			RandomRecordKeysGenerationResult keyValuesGenerationResult = generateRandomKeyValues();
			Set<String> randomPlotIds = keyValuesGenerationResult.keyValues;
			int totalItems = randomPlotIds.size();
			if (totalItems == 0) {
				throw new Error("No records to clone");
			}
			setTotalItems(totalItems);

			if (!Boolean.TRUE.equals(countOnly)) {
				RecordUpdater recordUpdater = new RecordUpdater();
				AttributeDefinition measurementKeyDef = survey.getFirstMeasurementKeyDef();
				for (String plotId : randomPlotIds) {
					int recordId = keyValuesGenerationResult.recordIdByKeyValue.get(plotId);
					CollectRecord record = recordManager.load(survey, recordId);
					record.setId(null);
					Entity rootEntity = record.getRootEntity();
					Attribute<?, Value> measurementKeyAttr = rootEntity.getChild(measurementKeyDef);
					Value newMeasurementValue = measurementKeyDef.createValue(newMeasurement);
					recordUpdater.updateAttribute(measurementKeyAttr, newMeasurementValue);
					recordManager.save(record);
					incrementProcessedItems();
				}
				generateGridFile(randomPlotIds);
			}
		}

		private void generateGridFile(Set<String> randomPlotIds)
				throws IOException, FileNotFoundException, UnsupportedEncodingException {
			SurveyFile sourceGridSurveyFile = loadSourceSurveyFile();
			byte[] sourceSurveyFileContent = surveyManager.loadSurveyFileContent(sourceGridSurveyFile);
			File sourceGridFile = Files.witeToTempFile(sourceSurveyFileContent, "temp_source_grid", ".csv");

			outputFile = File.createTempFile("random_grid", ".csv");
			FileOutputStream outputStream = null;
			CsvWriter csvWriter = null;
			CsvReader csvReader = null;
			try {
				outputStream = new FileOutputStream(outputFile);
				csvWriter = new CsvWriter(outputStream);
				csvReader = new CsvReader(sourceGridFile);
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

		private SurveyFile loadSourceSurveyFile() {
			List<SurveyFile> surveyFileSummaries = surveyManager.loadSurveyFileSummaries(tempSurvey);
			for (SurveyFile surveyFile : surveyFileSummaries) {
				if (surveyFile.getFilename().equals(sourceGridSurveyFileName)) {
					return surveyFile;
				}
			}
			return null;
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
