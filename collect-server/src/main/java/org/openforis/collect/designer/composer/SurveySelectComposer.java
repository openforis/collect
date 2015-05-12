package org.openforis.collect.designer.composer;

import org.openforis.collect.designer.viewmodel.SurveySelectVM;
import org.zkoss.bind.BindComposer;
import org.zkoss.bind.BindUtils;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.Listen;

/**
 * 
 * @author S. Ricci
 *
 */
public class SurveySelectComposer extends BindComposer<Component> {

	private static final long serialVersionUID = 1L;

	@Override
	public void doAfterCompose(Component comp) throws Exception {
		super.doAfterCompose(comp);
		Selectors.wireEventListeners(comp, this);
	}
	
	@Listen("onTimer=#surveysListUpdateTimer")
	public void onSurveysListUpdateTimeout(Event event) throws InterruptedException {
		BindUtils.postGlobalCommand(null, null, SurveySelectVM.UPDATE_SURVEY_LIST_COMMAND, null);
	}
	
}