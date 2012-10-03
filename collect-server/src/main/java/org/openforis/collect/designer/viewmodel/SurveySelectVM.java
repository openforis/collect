/**
 * 
 */
package org.openforis.collect.designer.viewmodel;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openforis.collect.designer.model.SurveyWorkSummary;
import org.openforis.collect.designer.session.SessionStatus;
import org.openforis.collect.designer.util.Resources.Page;
import org.openforis.collect.manager.SurveyManager;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.SurveySummary;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.DependsOn;
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
	private SurveyManager surveyManager;
	
	private SurveyWorkSummary selectedSurvey;
	
	@Command
	public void editSurvey() throws IOException {
		String uri = selectedSurvey.getUri();
		CollectSurvey surveyWork;
		if ( selectedSurvey.isPublished() ) {
			surveyWork = surveyManager.loadPublishedSurveyForEdit(uri);
		} else {
			surveyWork = surveyManager.loadSurveyWork(selectedSurvey.getId());
		}
		SessionStatus sessionStatus = getSessionStatus();
		sessionStatus.setSurvey(surveyWork);
		sessionStatus.setCurrentLanguageCode(null);
		Executions.sendRedirect(Page.SURVEY_EDIT.getLocation());
	}

	@Command
	public void newSurvey() throws IOException {
		CollectSurvey survey = surveyManager.createSurveyWork();
		SessionStatus sessionStatus = getSessionStatus();
		sessionStatus.setSurvey(survey);
		sessionStatus.setCurrentLanguageCode(null);
		Executions.sendRedirect(Page.SURVEY_EDIT.getLocation());
	}

	public ListModel<SurveyWorkSummary> getSurveySummaries() {
		List<SurveySummary> surveySummaries = surveyManager.getSurveySummaries(null);
		List<SurveySummary> surveyWorkSummaries = surveyManager.getSurveyWorkSummaries();
		List<SurveyWorkSummary> result = new ArrayList<SurveyWorkSummary>();
		Map<String, SurveyWorkSummary> workingSummariesByUri = new HashMap<String, SurveyWorkSummary>();
		for (SurveySummary summary : surveyWorkSummaries) {
			SurveyWorkSummary summaryWork = new SurveyWorkSummary(summary.getId(), summary.getName(), summary.getUri(), false, true);
			result.add(summaryWork);
			workingSummariesByUri.put(summary.getUri(), summaryWork);
		}
		for (SurveySummary summary : surveySummaries) {
			SurveyWorkSummary summaryWork;
			summaryWork = workingSummariesByUri.get(summary.getUri());
			if ( summaryWork == null ) {
				summaryWork = new SurveyWorkSummary(summary.getId(), summary.getName(), summary.getUri(), true, false);
				result.add(summaryWork);
			} else {
				summaryWork.setPublished(true);
			}
		}
		return new ListModelList<SurveyWorkSummary>(result);
	}

	public SurveyWorkSummary getSelectedSurvey() {
		return selectedSurvey;
	}

	public void setSelectedSurvey(SurveyWorkSummary selectedSurvey) {
		this.selectedSurvey = selectedSurvey;
	}
	
	@DependsOn("selectedSurvey")
	public boolean isSurveySelected() {
		return this.selectedSurvey != null;
	}
	
	@DependsOn("surveySelected")
	public boolean isEditingDisabled() {
		return this.selectedSurvey == null;
	}
	
	
}
