/**
 * 
 */
package org.openforis.collect.designer.composer;

import java.util.List;

import org.openforis.collect.manager.SurveyManager;
import org.openforis.collect.model.CollectSurvey;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.VariableResolver;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zul.ListModel;
import org.zkoss.zul.ListModelList;

/**
 * @author S. Ricci
 *
 */
@VariableResolver(org.zkoss.zkplus.spring.DelegatingVariableResolver.class)
public class SurveySelectComposer extends SelectorComposer<Component> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
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

	public void setSelectedSurvey(CollectSurvey selectedSurvey) {
		this.selectedSurvey = selectedSurvey;
	}
	
}
