package org.openforis.collect.io.metadata.species;

import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE;

import org.openforis.collect.io.metadata.ReferenceDataImportSimpleJob;
import org.openforis.collect.io.metadata.parsing.ParsingError;
import org.openforis.collect.io.parsing.CSVFileOptions;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope(SCOPE_PROTOTYPE)
public class SpeciesImportJob extends ReferenceDataImportSimpleJob<ParsingError, SpeciesImportTask> {
	
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
