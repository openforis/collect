/**
 * 
 */
package org.openforis.collect.designer.viewmodel;

import java.util.List;

import org.openforis.collect.manager.SurveyManager;
import org.openforis.collect.model.CollectSurvey;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zul.ListModel;
import org.zkoss.zul.ListModelList;

/**
 * 
 * @author S. Ricci
 *
 */
public class SurveySelectVM {
	
	@WireVariable
	private SurveyManager surveyManager;
	
	private CollectSurvey selectedSurvey;
	
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
