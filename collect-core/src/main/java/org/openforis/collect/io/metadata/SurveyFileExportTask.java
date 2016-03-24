package org.openforis.collect.io.metadata;

import java.io.IOException;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.IOUtils;
import org.openforis.collect.io.SurveyBackupJob;
import org.openforis.collect.manager.SurveyManager;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.SurveyFile;
import org.openforis.concurrency.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class SurveyFileExportTask extends Task {

	@Autowired
	private SurveyManager surveyManager;

	// input
	private CollectSurvey survey;
	private ZipOutputStream zipOutputStream;

	@Override
	protected long countTotalItems() {
		List<SurveyFile> surveyFiles = surveyManager.loadSurveyFileSummaries(survey);
		return surveyFiles.size();
	}

	@Override
	protected void execute() throws Throwable {
		List<SurveyFile> surveyFiles = surveyManager.loadSurveyFileSummaries(survey);
		for (SurveyFile surveyFile : surveyFiles) {
			exportSurveyFile(surveyFile);
			incrementProcessedItems();
		}
	}

	private void exportSurveyFile(SurveyFile surveyFile) {
		try {
			byte[] content = surveyManager.loadSurveyFileContent(surveyFile);
			ZipEntry entry = new ZipEntry(String.format("%s/%s/%s", SurveyBackupJob.SURVEY_FILES_FOLDER,
					surveyFile.getType().getCode(), surveyFile.getFilename()));
			zipOutputStream.putNextEntry(entry);
			IOUtils.write(content, zipOutputStream);
			zipOutputStream.closeEntry();
		} catch (IOException e) {
			throw new RuntimeException(
					String.format("Error exporting survey file with name: %s", surveyFile.getFilename()), e);
		}
	}
	
	public void setSurvey(CollectSurvey survey) {
		this.survey = survey;
	}
	
	public void setZipOutputStream(ZipOutputStream zipOutputStream) {
		this.zipOutputStream = zipOutputStream;
	}
}
