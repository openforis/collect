/**
 * 
 */
package org.openforis.collect.designer.viewmodel;

import java.io.IOException;
import java.util.List;

import org.openforis.collect.designer.session.SessionStatus;
import org.openforis.collect.designer.util.Resources.Page;
import org.openforis.collect.manager.SurveyWorkManager;
import org.openforis.collect.model.CollectSurvey;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zul.ListModel;
import org.zkoss.zul.ListModelList;

/**
 * 
 * @author S. Ricci
 *
 */
public class SurveySelectVM extends BaseVM {
	
	@WireVariable
	private SurveyWorkManager surveyWorkManager;
	
	private CollectSurvey selectedSurvey;
	
	@Command
	public void editSurvey() throws IOException {
		SessionStatus sessionStatus = getSessionStatus();
		sessionStatus.setSurvey(selectedSurvey);
		sessionStatus.setCurrentLanguageCode(null);
		Executions.sendRedirect(Page.SURVEY_EDIT.getLocation());
	}

	@Command
	public void newSurvey() throws IOException {
		CollectSurvey survey = surveyWorkManager.createSurvey();
		SessionStatus sessionStatus = getSessionStatus();
		sessionStatus.setSurvey(survey);
		sessionStatus.setCurrentLanguageCode(null);
		Executions.sendRedirect(Page.SURVEY_EDIT.getLocation());
	}

	public ListModel<CollectSurvey> getSurveysWork() {
		List<CollectSurvey> surveys = surveyWorkManager.getAll();
		return new ListModelList<CollectSurvey>(surveys);
	}

	public CollectSurvey getSelectedSurvey() {
		return selectedSurvey;
	}

	@NotifyChange({"surveySelected", "editingDisabled"})
	public void setSelectedSurvey(CollectSurvey selectedSurvey) {
		this.selectedSurvey = selectedSurvey;
	}
	
	public boolean isSurveySelected() {
		return this.selectedSurvey != null;
	}
	
	public boolean isEditingDisabled() {
		return this.selectedSurvey == null;
	}
	
	
}
