package org.openforis.collect.designer.composer;

import org.openforis.collect.designer.viewmodel.SurveySelectVM;
import org.zkoss.bind.BindComposer;
import org.zkoss.bind.BindUtils;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zul.Listheader;

/**
 * 
 * @author S. Ricci
 *
 */
public class SurveySelectComposer extends BindComposer<Component> {

	private static final long serialVersionUID = 1L;
	private static final String LAST_MODIFIED_DATE_HEADER_ID = "surveysListLastModifiedDateHeader";

	@Override
	public void doAfterCompose(Component comp) throws Exception {
		super.doAfterCompose(comp);
		Selectors.wireEventListeners(comp, this);
		setGridDefaultSortDirection();
	}

	@Listen("onTimer=#surveysListUpdateTimer")
	public void onSurveysListUpdateTimeout(Event event) throws InterruptedException {
		BindUtils.postGlobalCommand(null, null, SurveySelectVM.UPDATE_SURVEY_LIST_COMMAND, null);
	}
	
	public void setGridDefaultSortDirection() {
		Component comp = this.getBinder().getView();
		Listheader lastModifiedDateHeader = (org.zkoss.zul.Listheader) comp.query("#" + LAST_MODIFIED_DATE_HEADER_ID);
		//reset sorting feedback
		for (Component header : lastModifiedDateHeader.getParent().getChildren()) {
			((Listheader) header).setSortDirection("natural");
		}
		//set descending sort direction on last modified date column header
		lastModifiedDateHeader.setSortDirection("descending");
	}
	
}