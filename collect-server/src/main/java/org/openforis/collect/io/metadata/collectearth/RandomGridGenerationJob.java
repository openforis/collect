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
import org.openforis.concurrency.Worker;

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

	@Override
	protected void buildTasks() throws Throwable {
		addTask(new RandomGridGenerationTask());
	}

	@Override
	protected void initializeTask(Worker task) {
		RandomGridGenerationTask t = (RandomGridGenerationTask) task;
		t.setPercentage(percentage);
		super.initializeTask(t);
	}

	@Override
	protected void afterExecute() {
		super.afterExecute();
		File outputFile = ((RandomGridGenerationTask) getTasks().get(0)).outputFile;
		SurveyFile surveyFile = new SurveyFile(survey);
		surveyFile.setType(SurveyFileType.COLLECT_EARTH_GRID);
		surveyFile.setFilename(surveyFileName);
		surveyManager.addSurveyFile(surveyFile, outputFile);
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

	private class RandomGridGenerationTask extends Task {
		private static final String ID_COLUMN = "id";
		private float percentage;
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
				FlatRecord csvRecord = csvReader.nextRecord();
				while (csvRecord != null) {
					Integer plotId = csvRecord.getValue(ID_COLUMN, Integer.class);
					if (randomPlotIds.contains(plotId)) {
						csvWriter.writeNext(csvRecord);
					}
					csvRecord = csvReader.nextRecord();
				}
			}
		}

		private Set<Integer> generateRandomPlotIds() throws FileNotFoundException, IOException {
			CsvReader csvReader = new CsvReader(file);
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
		
		public void setPercentage(float percentage) {
			this.percentage = percentage;
		}
	}

}
