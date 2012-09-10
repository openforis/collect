/**
 * 
 */
package org.openforis.collect.designer.viewmodel;

import java.util.List;

import org.openforis.collect.designer.session.SessionStatus;
import org.openforis.collect.manager.SurveyManager;
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
	
	private static final String SURVEY_EDIT_URL = "survey_edit.zul";
	private static final String SURVEY_SELECT_LANGUAGE_URL = "survey_select_language.zul";

	@WireVariable
	private SurveyManager surveyManager;
	
	private CollectSurvey selectedSurvey;
	
	@Command
	public void editSurvey() {
		SessionStatus sessionStatus = getSessionStatus();
		sessionStatus.setSurvey(selectedSurvey);
		Executions.sendRedirect(SURVEY_EDIT_URL);
	}

	@Command
	public void newSurvey() {
		SessionStatus sessionStatus = getSessionStatus();
		CollectSurvey survey = surveyManager.createSurvey();
		sessionStatus.setSurvey(survey);
		Executions.sendRedirect(SURVEY_SELECT_LANGUAGE_URL);
	}

	public ListModel<CollectSurvey> getSurveys() {
		List<CollectSurvey> surveys = surveyManager.getAll();
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
