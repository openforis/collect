/**
 * 
 */
package org.openforis.collect.io.metadata;

import java.io.File;

import org.openforis.collect.manager.SurveyManager;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.concurrency.Task;
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

	private SurveyManager surveyManager;
	
	//parameters
	private transient File file;
	private String publishedSurveyUri;
	private boolean newSurvey;
	private boolean updatePublishedSurvey;
	private String name;

	//output
	private transient CollectSurvey survey;
	
	protected void execute() throws Throwable {
		if ( newSurvey ) {
			//import packaged survey into a new work survey
			survey = surveyManager.importWorkModel(file, name, false);
		} else if ( updatePublishedSurvey ) {
			//duplicate published survey into work and update it with packaged file
			survey = surveyManager.importInPublishedWorkModel(publishedSurveyUri, file, false);
		} else {
			//update "temporary/work" survey
			survey = surveyManager.updateModel(file, false);
		}
	}

	public SurveyManager getSurveyManager() {
		return surveyManager;
	}
	
	public void setSurveyManager(SurveyManager surveyManager) {
		this.surveyManager = surveyManager;
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
		return newSurvey;
	}

	public void setNewSurvey(boolean newSurvey) {
		this.newSurvey = newSurvey;
	}

	public boolean isUpdatePublishedSurvey() {
		return updatePublishedSurvey;
	}

	public void setUpdatePublishedSurvey(boolean updatePublishedSurvey) {
		this.updatePublishedSurvey = updatePublishedSurvey;
	}

	public String getPublishedSurveyUri() {
		return publishedSurveyUri;
	}

	public void setPublishedSurveyUri(String publishedSurveyUri) {
		this.publishedSurveyUri = publishedSurveyUri;
	}
	
}