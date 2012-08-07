/**
 * 
 */
package org.openforis.collect.designer.composer;

import java.util.List;

import org.openforis.collect.manager.SurveyManager;
import org.openforis.collect.model.CollectSurvey;
import org.springframework.beans.factory.annotation.Autowired;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.VariableResolver;
import org.zkoss.zul.ListModel;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Window;

/**
 * @author S. Ricci
 *
 */
@VariableResolver(org.zkoss.zkplus.spring.DelegatingVariableResolver.class)
public class SurveySelectComposer extends SelectorComposer<Window> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	@Autowired
	private SurveyManager surveyManager;
	
	public ListModel<CollectSurvey> getSurveys() {
		List<CollectSurvey> surveys = surveyManager.getAll();
		return new ListModelList<CollectSurvey>(surveys);
	}
	
}
