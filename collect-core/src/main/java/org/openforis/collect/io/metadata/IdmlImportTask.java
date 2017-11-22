/**
 * 
 */
package org.openforis.collect.io.metadata;

import java.io.File;

import org.openforis.collect.manager.SurveyManager;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.SurveySummary;
import org.openforis.collect.model.User;
import org.openforis.collect.persistence.SurveyImportException;
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
	
	//input
	private transient File file;
	private boolean importInPublishedSurvey;
	private String surveyUri;
	private String surveyName;
	private boolean validate;
	private User activeUser;
	
	//output
	private transient CollectSurvey survey;

	public IdmlImportTask() {
		this.validate = false;
	}
	
	protected void execute() throws Throwable {
		SurveySummary oldSurveySummary = surveyManager.loadSummaryByUri(surveyUri);
		if ( oldSurveySummary == null ) {
			//new survey
			if ( importInPublishedSurvey ) {
				survey = surveyManager.importModel(file, surveyName, validate);
			} else {
				survey = surveyManager.importTemporaryModel(file, surveyName, validate);
			}
		} else if ( importInPublishedSurvey ) {
			//survey already exists
			if ( oldSurveySummary.getPublishedId() != null ) {
				//published survey exists, update it
				survey = surveyManager.updateModel(file, validate);
			} else {
				//work survey exists, cannot import survey as published
				throw new SurveyImportException(String.format("Cannot import as published survey - " +
						"survey work already exists for this uri (%s) delete it before proceed", surveyUri));
			}
		} else if ( oldSurveySummary.isTemporary() ) {
			//survey work already exists, update it
			survey = surveyManager.updateTemporaryModel(file, validate);
		} else {
			//duplicates published survey into work and update it with packaged file
			survey = surveyManager.importInPublishedTemporaryModel(surveyUri, file, validate, activeUser);
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
	
	public boolean isImportInPublishedSurvey() {
		return importInPublishedSurvey;
	}
	
	public void setImportInPublishedSurvey(boolean importInPublishedSurvey) {
		this.importInPublishedSurvey = importInPublishedSurvey;
	}
	
	public boolean isValidate() {
		return validate;
	}
	
	public void setValidate(boolean validate) {
		this.validate = validate;
	}
	
	public void setActiveUser(User activeUser) {
		this.activeUser = activeUser;
	}
	
	public CollectSurvey getSurvey() {
		return survey;
	}

}