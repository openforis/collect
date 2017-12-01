/**
 * 
 */
package org.openforis.collect.io;

import java.io.File;

import org.openforis.collect.io.metadata.IdmlImportTask;
import org.openforis.collect.io.metadata.IdmlUnmarshallTask;
import org.openforis.collect.manager.SurveyManager;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.User;
import org.openforis.collect.model.UserGroup;
import org.openforis.concurrency.Job;
import org.openforis.concurrency.Worker;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author S. Ricci
 *
 */
public abstract class AbstractSurveyRestoreJob extends Job {

	@Autowired
	protected SurveyManager surveyManager;
	
	//input
	/**
	 * XML file containing the survey idml
	 */
	protected File file;
	/**
	 * Optional when updating already existing survey.
	 */
	protected String surveyName;
	/**
	 * Optional: it must be the same as the one in the packaged XML file
	 */
	protected String surveyUri;
	/**
	 * Restores the survey into a published survey.
	 * It creates a new one with the specified survey name if it does not exist.
	 */
	protected boolean restoreIntoPublishedSurvey;
	/**
	 * If true, validates the XML file to import against schema.
	 */
	protected boolean validateSurvey;
	/**
	 * User group to be assigned to the new survey
	 */
	protected UserGroup userGroup;
	/**
	 * Active user restoring the survey
	 */
	protected User activeUser;

	//output
	protected CollectSurvey survey;

	@Override
	protected void onTaskCompleted(Worker task) {
		super.onTaskCompleted(task);
		if ( task instanceof IdmlUnmarshallTask ) {
			CollectSurvey s = ((IdmlUnmarshallTask) task).getSurvey();
			this.surveyUri = s.getUri();
		} else if ( task instanceof IdmlImportTask ) {
			IdmlImportTask t = (IdmlImportTask) task;
			//get output survey and set it into job instance instance variable
			this.survey = t.getSurvey();
		}
	}
	
	public SurveyManager getSurveyManager() {
		return surveyManager;
	}
	
	public void setSurveyManager(SurveyManager surveyManager) {
		this.surveyManager = surveyManager;
	}
	
	public File getFile() {
		return file;
	}

	public void setFile(File file) {
		this.file = file;
	}

	public String getSurveyName() {
		return surveyName;
	}

	public void setSurveyName(String surveyName) {
		this.surveyName = surveyName;
	}
	
	public String getSurveyUri() {
		return surveyUri;
	}
	
	public void setSurveyUri(String surveyUri) {
		this.surveyUri = surveyUri;
	}

	public boolean isRestoreIntoPublishedSurvey() {
		return restoreIntoPublishedSurvey;
	}
	
	public void setRestoreIntoPublishedSurvey(boolean restoreIntoPublishedSurvey) {
		this.restoreIntoPublishedSurvey = restoreIntoPublishedSurvey;
	}
	
	public boolean isValidateSurvey() {
		return validateSurvey;
	}
	
	public void setValidateSurvey(boolean validateSurvey) {
		this.validateSurvey = validateSurvey;
	}
	
	public CollectSurvey getSurvey() {
		return survey;
	}

	public void setUserGroup(UserGroup userGroup) {
		this.userGroup = userGroup;
	}
	
	public void setActiveUser(User activeUser) {
		this.activeUser = activeUser;
	}
	
}
