package org.openforis.collect.designer.viewmodel;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.openforis.collect.designer.model.LabelledItem;
import org.openforis.collect.designer.session.SessionStatus;
import org.openforis.collect.designer.util.MessageUtil;
import org.openforis.collect.designer.util.Resources.Page;
import org.openforis.collect.manager.SurveyManager;
import org.openforis.collect.manager.exception.SurveyValidationException;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.idm.metamodel.xml.IdmlParseException;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.Init;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zkplus.databind.BindingListModelListModel;
import org.zkoss.zul.ListModelList;

/**
 * 
 * @author S. Ricci
 *
 */
public class SelectTemplateVM extends BaseVM {

	private enum TemplateType {
		//BIOPHYSICAL, 
		//SOCIOECONOMIC, 
		IPCC, BLANK
	};
	
	@WireVariable
	private SurveyManager surveyManager;
	
	private List<LabelledItem> templates;
	private BindingListModelListModel<LabelledItem> templatesModel;
	
	@Init
	public void init() {
		templates = new ArrayList<LabelledItem>();
		for (TemplateType templateType : TemplateType.values()) {
			String name = templateType.name();
			templates.add(new LabelledItem(name, Labels.getLabel("survey.template.type." + name.toLowerCase())));
		}
		templatesModel = new BindingListModelListModel<LabelledItem>(new ListModelList<LabelledItem>(templates));
		templatesModel.setMultiple(false);
	}
	
	public BindingListModelListModel<LabelledItem> getTemplatesModel() {
		return templatesModel;
	}
	
	public String getSelectedTemplateCode() {
		Set<LabelledItem> selectedTemplates = templatesModel.getSelection();
		if ( selectedTemplates != null ) {
			for (LabelledItem item : selectedTemplates) {
				return item.getCode();
			}
		}
		return null;
	}
	
	@Command
	public void ok() throws IdmlParseException, SurveyValidationException {
		String templateCode = getSelectedTemplateCode();
		if ( templateCode != null ) {
			CollectSurvey survey;
			if ( templateCode.equals(TemplateType.BLANK.name())) {
				//create empty survey
				survey = surveyManager.createSurveyWork();
			} else {
				//create survey from template
				String templateFileName = "/org/openforis/collect/designer/templates/" + templateCode.toLowerCase() + ".idm.xml";
				InputStream surveyFileIs = this.getClass().getResourceAsStream(templateFileName);
				survey = surveyManager.unmarshalSurvey(surveyFileIs, false, true);
				survey.setWork(true);
				survey.setUri(surveyManager.generateRandomSurveyUri());
			}
			//put survey in session and redirect into survey edit page
			SessionStatus sessionStatus = getSessionStatus();
			sessionStatus.setSurvey(survey);
			sessionStatus.setCurrentLanguageCode(null);
			Executions.sendRedirect(Page.SURVEY_EDIT.getLocation());
		} else {
			MessageUtil.showWarning("survey.template.error.select_type");
		}
	}

}
