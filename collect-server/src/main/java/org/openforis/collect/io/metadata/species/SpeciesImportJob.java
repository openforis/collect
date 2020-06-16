package org.openforis.collect.io.metadata.species;

import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE;

import java.io.File;
import java.util.Collections;
import java.util.List;

import org.openforis.collect.io.metadata.parsing.ParsingError;
import org.openforis.collect.io.parsing.CSVFileOptions;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.concurrency.Job;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope(SCOPE_PROTOTYPE)
public class SpeciesImportJob extends Job {
	
	private File file;
	private CollectSurvey survey;
	private int taxonomyId;
	private CSVFileOptions csvFileOptions;
	private boolean overwriteAll = true;

	@Override
	protected void buildTasks() throws Throwable {
		SpeciesImportTask task = createTask(SpeciesImportTask.class);
		task.setFile(file);
		task.setSurvey(survey);
		task.setTaxonomyId(taxonomyId);
		task.setCsvFileOptions(csvFileOptions);
		task.setOverwriteAll(overwriteAll);
		addTask(task);
	}

	public List<ParsingError> getErrors() {
		if (getTasks().isEmpty()) {
			return Collections.emptyList();
		}
		return ((SpeciesImportTask) getTasks().get(0)).getErrors();
	}

	public void setFile(File file) {
		this.file = file;
	}

	public void setSurvey(CollectSurvey survey) {
		this.survey = survey;
	}

	public void setTaxonomyId(int taxonomyId) {
		this.taxonomyId = taxonomyId;
	}

	public void setCsvFileOptions(CSVFileOptions csvFileOptions) {
		this.csvFileOptions = csvFileOptions;
	}

	public void setOverwriteAll(boolean overwriteAll) {
		this.overwriteAll = overwriteAll;
	}
}
