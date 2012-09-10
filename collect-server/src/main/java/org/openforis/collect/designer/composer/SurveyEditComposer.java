/**
 * 
 */
package org.openforis.collect.designer.composer;

import org.openforis.collect.manager.SurveyManager;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.persistence.SurveyImportException;
import org.openforis.idm.metamodel.Languages;
import org.zkoss.bind.BindComposer;
import org.zkoss.bind.annotation.Command;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.select.annotation.VariableResolver;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zul.ListModel;
import org.zkoss.zul.ListModelList;

/**
 * @author S. Ricci
 *
 */
@VariableResolver(org.zkoss.zkplus.spring.DelegatingVariableResolver.class)
public class SurveyEditComposer<T extends Component> extends BindComposer<T> {

	private static final long serialVersionUID = 1L;
	
	@WireVariable
	private SurveyManager surveyManager;
	
	@Command
    public void save() throws SurveyImportException {
		CollectSurvey survey = (CollectSurvey) getViewModel();
		surveyManager.importModel(survey);
    }
	
	public ListModel<String> getLanguageCodes() {
		return new ListModelList<String>(Languages.LANGUAGE_CODES);
	}
	
}
