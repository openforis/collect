/**
 * 
 */
package org.openforis.collect.io.metadata.species;

import java.io.File;

import org.openforis.collect.model.CollectSurvey;
import org.openforis.concurrency.Job;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * @author S. Ricci
 *
 */
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class SpeciesBackupImportJob extends Job {

	private File file;
	private CollectSurvey survey;
	private String taxonomyName;

	@Override
	protected void buildTasks() throws Throwable {
		SpeciesBackupImportTask task = createTask(SpeciesBackupImportTask.class);
		task.setFile(file);
		task.setSurvey(survey);
		task.setTaxonomyName(taxonomyName);
		task.setOverwriteAll(true);
		addTask(task);
	}
	
	public File getFile() {
		return file;
	}

	public void setFile(File file) {
		this.file = file;
	}

	public CollectSurvey getSurvey() {
		return survey;
	}

	public void setSurvey(CollectSurvey survey) {
		this.survey = survey;
	}

	public String getTaxonomyName() {
		return taxonomyName;
	}

	public void setTaxonomyName(String taxonomyName) {
		this.taxonomyName = taxonomyName;
	}
	
}
