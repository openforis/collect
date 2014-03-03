/**
 * 
 */
package org.openforis.collect.io;

import java.io.File;

import org.openforis.collect.manager.SurveyManager;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.concurrency.Task;
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
public class IdmlImportTask extends Task {

	@Autowired
	private SurveyManager surveyManager;
	
	//parameters
	private transient File file;
	private String publishedSurveyUri;
	private boolean updatingExistingSurvey;
	private boolean updatingPublishedSurvey;
	private String name;

	//output
	private transient CollectSurvey survey;
	
	protected void execute() throws Throwable {
		if ( updatingExistingSurvey ) {
			if ( updatingPublishedSurvey ) {
				survey = surveyManager.importInPublishedWorkModel(publishedSurveyUri, file, false);
			} else {
				survey = surveyManager.updateModel(file, false);
			}
		} else {
			survey = surveyManager.importWorkModel(file, name, false);
		}
	}

	public CollectSurvey getSurvey() {
		return survey;
	}

	public File getFile() {
		return file;
	}

	public void setFile(File file) {
		this.file = file;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isUpdatingExistingSurvey() {
		return updatingExistingSurvey;
	}

	public void setUpdatingExistingSurvey(boolean updatingExistingSurvey) {
		this.updatingExistingSurvey = updatingExistingSurvey;
	}

	public boolean isUpdatingPublishedSurvey() {
		return updatingPublishedSurvey;
	}

	public void setUpdatingPublishedSurvey(boolean updatingPublishedSurvey) {
		this.updatingPublishedSurvey = updatingPublishedSurvey;
	}

	public String getPublishedSurveyUri() {
		return publishedSurveyUri;
	}

	public void setPublishedSurveyUri(String publishedSurveyUri) {
		this.publishedSurveyUri = publishedSurveyUri;
	}
	
}