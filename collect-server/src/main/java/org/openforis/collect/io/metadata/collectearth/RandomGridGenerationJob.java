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

/**
 * @author S. Ricci
 *
 */
public class RandomGridGenerationJob extends Job {

	private SurveyManager surveyManager;
	// input
	private CollectSurvey survey;
	private File file;
	private float percentage;
	private String surveyFileName;
	private String newQualifier;

	@Override
	protected void buildTasks() throws Throwable {
		addTask(new RandomGridGenerationTask());
	}

	@Override
	protected void afterExecute() {
		super.afterExecute();
		File outputFile = ((RandomGridGenerationTask) getTasks().get(0)).outputFile;
		SurveyFile surveyFile = new SurveyFile(survey);
		surveyFile.setType(SurveyFileType.COLLECT_EARTH_GRID);
		surveyFile.setFilename(surveyFileName);
		surveyManager.addSurveyFile(surveyFile, outputFile);
		outputFile.delete();
	}
	
	public void setSurvey(CollectSurvey survey) {
		this.survey = survey;
	}
	
	public void setPercentage(float percentage) {
		this.percentage = percentage;
	}
	
	public void setSurveyFileName(String surveyFileName) {
		this.surveyFileName = surveyFileName;
	}

	public void setNewQualifier(String newQualifier) {
		this.newQualifier = newQualifier;
	}
	
	private class RandomGridGenerationTask extends Task {
		private static final String ID_COLUMN = "id";
		private File outputFile;

		@Override
		protected void execute() throws Throwable {
			Set<Integer> randomPlotIds = generateRandomPlotIds();

			outputFile = File.createTempFile("random_grid", ".csv");
			try (
					FileOutputStream outputStream = new FileOutputStream(outputFile);
					CsvWriter csvWriter = new CsvWriter(outputStream);
					CsvReader csvReader = new CsvReader(file);
			) {
				csvReader.readHeaders();
				List<String> headers = csvReader.getColumnNames();
				csvWriter.writeHeaders(headers);
				
				List<AttributeDefinition> qualifierAttributeDefinitions = survey.getSchema().getQualifierAttributeDefinitions();
				AttributeDefinition firstQualifierAttributeDefinition = qualifierAttributeDefinitions.isEmpty() ? null : qualifierAttributeDefinitions.get(0);
				String qualifierAttributeName = firstQualifierAttributeDefinition.getName();
				int qualifierColumnIndex = headers.indexOf(qualifierAttributeName);
				
				FlatRecord csvRecord = csvReader.nextRecord();
				while (csvRecord != null) {
					Integer plotId = csvRecord.getValue(ID_COLUMN, Integer.class);
					if (randomPlotIds.contains(plotId)) {
						Object[] values = csvRecord.toArray();
						Object[] valuesUpdated = ArrayUtils.clone(values);
						valuesUpdated[qualifierColumnIndex] = newQualifier;
						csvWriter.writeNext(valuesUpdated);
					}
					csvRecord = csvReader.nextRecord();
				}
			}
		}

		private Set<Integer> generateRandomPlotIds() throws FileNotFoundException, IOException {
			CsvReader csvReader = new CsvReader(file);
			csvReader.readHeaders();
			CsvLine csvLine = csvReader.readNextLine();
			List<Integer> plotIds = new ArrayList<>();
			while (csvLine != null) {
				Integer plotId = csvLine.getValue(ID_COLUMN, Integer.class);
				plotIds.add(plotId);
				csvLine = csvReader.readNextLine();
			}
			csvReader.close();
			List<Integer> randomPlotIds = RandomValuesGenerator.generateRandomSubset(plotIds, percentage);
			return new HashSet<>(randomPlotIds);
		}
		
	}

}
