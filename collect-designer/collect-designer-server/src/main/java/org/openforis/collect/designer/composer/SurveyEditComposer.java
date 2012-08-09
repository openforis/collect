/**
 * 
 */
package org.openforis.collect.designer.composer;

import org.openforis.collect.manager.SurveyManager;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.persistence.SurveyImportException;
import org.zkoss.bind.BindComposer;
import org.zkoss.bind.annotation.Command;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.select.annotation.VariableResolver;
import org.zkoss.zk.ui.select.annotation.WireVariable;

/**
 * @author S. Ricci
 *
 */
@VariableResolver(org.zkoss.zkplus.spring.DelegatingVariableResolver.class)
public class SurveyEditComposer extends BindComposer<Component> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	@WireVariable
	private SurveyManager surveyManager;
	
	private CollectSurvey survey;
	
	public CollectSurvey getSurvey() {
		if ( survey == null ) {
			survey = surveyManager.createSurvey();
		}
		return survey;
	}
	
	public void setSurvey(CollectSurvey survey) {
		this.survey = survey;
	}
	
	@Command
    public void save() throws SurveyImportException {
		surveyManager.importModel(survey);
    }
	
}
