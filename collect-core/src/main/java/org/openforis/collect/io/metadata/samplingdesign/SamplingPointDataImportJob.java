/**
 * 
 */
package org.openforis.collect.io.metadata.samplingdesign;

import java.io.File;
import java.util.Collections;
import java.util.List;

import org.openforis.collect.io.metadata.parsing.ParsingError;
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
public class SamplingPointDataImportJob extends Job {

	private File file;
	private CollectSurvey survey;

	@Override
	protected void buildTasks() throws Throwable {
		SamplingDesignImportTask task = createTask(SamplingDesignImportTask.class);
		task.setFile(file);
		task.setSurvey(survey);
		task.setOverwriteAll(true);
		addTask(task);
	}
	
	public List<ParsingError> getErrors() {
		if (getTasks().isEmpty()) {
			return Collections.emptyList();
		}
		return ((SamplingDesignImportTask) getTasks().get(0)).getErrors();
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

}
