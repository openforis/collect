package org.openforis.collect.io.metadata;

import static org.openforis.collect.io.SurveyBackupJob.SURVEY_FILES_FOLDER;
import static org.openforis.collect.io.SurveyBackupJob.ZIP_FOLDER_SEPARATOR;

import java.io.File;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FilenameUtils;
import org.openforis.collect.io.BackupFileExtractor;
import org.openforis.collect.manager.SurveyManager;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.SurveyFile;
import org.openforis.collect.model.SurveyFile.SurveyFileType;
import org.openforis.concurrency.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class SurveyFilesImportTask extends Task {

	@Autowired
	private SurveyManager surveyManager;

	// input
	private transient CollectSurvey survey;
	private transient BackupFileExtractor backupFileExtractor;

	@Override
	protected long countTotalItems() {
		int total = 0;
		List<String> types = backupFileExtractor.listEntriesInPath(SURVEY_FILES_FOLDER);
		for (String typeCode : types) {
			total += backupFileExtractor.countEntriesInPath(determineSurveyFilesPath(typeCode));
		}
		return total;
	}

	@Override
	protected void execute() throws Throwable {
		surveyManager.deleteSurveyFiles(survey);
		Set<String> types = backupFileExtractor.listDirectoriesInPath(SURVEY_FILES_FOLDER);
		for (String typeCode : types) {
			SurveyFileType type = SurveyFileType.fromCode(typeCode);
			String surveyFilesPath = determineSurveyFilesPath(typeCode);
			List<String> entryNames = backupFileExtractor.listEntriesInPath(surveyFilesPath);
			for (String entryName : entryNames) {
				File file = backupFileExtractor.extract(entryName);
				String fileName = FilenameUtils.getName(entryName);
				SurveyFile surveyFile = new SurveyFile(survey);
				surveyFile.setFilename(fileName);
				surveyFile.setType(type);
				surveyManager.addSurveyFile(surveyFile, file);
			}
		}
	}

	private String determineSurveyFilesPath(String typeCode) {
		return SURVEY_FILES_FOLDER + ZIP_FOLDER_SEPARATOR + typeCode;
	}
	
	public void setSurvey(CollectSurvey survey) {
		this.survey = survey;
	}
	
	public void setBackupFileExtractor(BackupFileExtractor backupFileExtractor) {
		this.backupFileExtractor = backupFileExtractor;
	}

}
