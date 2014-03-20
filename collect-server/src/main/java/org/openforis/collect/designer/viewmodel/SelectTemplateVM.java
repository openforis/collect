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
import org.openforis.collect.metamodel.ui.UIOptions;
import org.openforis.collect.metamodel.ui.UITab;
import org.openforis.collect.metamodel.ui.UITabSet;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.Schema;
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
				survey = createEmptySurvey();
			} else {
				survey = createNewSurveyFromTemplate(templateCode);
			}
			//put survey in session and redirect into survey edit page
			SessionStatus sessionStatus = getSessionStatus();
			sessionStatus.setSurvey(survey);
			sessionStatus.setCurrentLanguageCode(survey.getDefaultLanguage());
			Executions.sendRedirect(Page.SURVEY_EDIT.getLocation());
		} else {
			MessageUtil.showWarning("survey.template.error.select_type");
		}
	}

	protected CollectSurvey createNewSurveyFromTemplate(String templateCode)
			throws IdmlParseException, SurveyValidationException {
		String templateFileName = "/org/openforis/collect/designer/templates/" + templateCode.toLowerCase() + ".idm.xml";
		InputStream surveyFileIs = this.getClass().getResourceAsStream(templateFileName);
		CollectSurvey survey = surveyManager.unmarshalSurvey(surveyFileIs, false, true);
		survey.setWork(true);
		survey.setUri(surveyManager.generateRandomSurveyUri());
		return survey;
	}

	protected CollectSurvey createEmptySurvey() {
		String defaultLanguge = "en";
		//create empty survey
		CollectSurvey survey = surveyManager.createSurveyWork();
		//add default language
		survey.addLanguage(defaultLanguge);
		//add default root entity
		Schema schema = survey.getSchema();
		EntityDefinition rootEntity = schema.createEntityDefinition();
		rootEntity.setName(SchemaVM.DEFAULT_ROOT_ENTITY_NAME);
		schema.addRootEntityDefinition(rootEntity);
		//create root tab set
		UIOptions uiOptions = survey.getUIOptions();
		UITabSet rootTabSet = uiOptions.createRootTabSet((EntityDefinition) rootEntity);
		UITab mainTab = uiOptions.getMainTab(rootTabSet);
		mainTab.setLabel(defaultLanguge, SchemaVM.DEFAULT_MAIN_TAB_LABEL);
		return survey;
	}

}
