package org.openforis.collect.designer.viewmodel;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.openforis.collect.designer.form.validator.BaseValidator;
import org.openforis.collect.designer.model.LabelledItem;
import org.openforis.collect.designer.model.LabelledItem.LabelComparator;
import org.openforis.collect.designer.session.SessionStatus;
import org.openforis.collect.designer.util.Resources.Page;
import org.openforis.collect.manager.SurveyManager;
import org.openforis.collect.manager.exception.SurveyValidationException;
import org.openforis.collect.metamodel.ui.UIOptions;
import org.openforis.collect.metamodel.ui.UITab;
import org.openforis.collect.metamodel.ui.UITabSet;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.SurveySummary;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.Languages;
import org.openforis.idm.metamodel.Languages.Standard;
import org.openforis.idm.metamodel.Schema;
import org.openforis.idm.metamodel.xml.IdmlParseException;
import org.zkoss.bind.ValidationContext;
import org.zkoss.bind.Validator;
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
public class NewSurveyParametersPopUpVM extends BaseVM {

	private enum TemplateType {
		BLANK,
		IPCC
		//BIOPHYSICAL, 
		//SOCIOECONOMIC, 
	}

	protected static final String SURVEY_NAME_FIELD = "name";;
	protected static final String DUPLICATE_NAME_MESSAGE_KEY = "survey.validation.error.duplicate_name";
	
	@WireVariable
	private SurveyManager surveyManager;
	
	private Map<String, Object> form;
	
	private BindingListModelListModel<LabelledItem> templateModel;
	private BindingListModelListModel<LabelledItem> languageModel;

	private Validator nameValidator;
	
	@Init
	public void init() {
		form = new HashMap<String, Object>();
		nameValidator = new BaseValidator() {
			@Override
			public void validate(ValidationContext ctx) {
				if ( validateRequired(ctx, SURVEY_NAME_FIELD) && validateInternalName(ctx, SURVEY_NAME_FIELD) ) {
					validateNameUniqueness(ctx);
				}
			}
			
			private boolean validateNameUniqueness(ValidationContext ctx) {
				String name = getValue(ctx, SURVEY_NAME_FIELD);
				SurveySummary existingSurveySummary = loadExistingSurveySummaryByName(ctx, name);
				if ( existingSurveySummary != null ) {
					this.addInvalidMessage(ctx, SURVEY_NAME_FIELD, Labels.getLabel(DUPLICATE_NAME_MESSAGE_KEY));
					return false;
				} else {
					return true;
				}
			}
			
			private SurveySummary loadExistingSurveySummaryByName(ValidationContext ctx, String name) {
				NewSurveyParametersPopUpVM vm = (NewSurveyParametersPopUpVM) getVM(ctx);
				SurveySummary summary = vm.surveyManager.loadSummaryByName(name);
				return summary;
			}

		};
		initLanguageModel();
		initTemplatesModel();
	}

	private void initTemplatesModel() {
		List<LabelledItem> templates = new ArrayList<LabelledItem>();
		for (TemplateType templateType : TemplateType.values()) {
			String name = templateType.name();
			templates.add(new LabelledItem(name, Labels.getLabel("survey.template.type." + name.toLowerCase())));
		}
		templateModel = new BindingListModelListModel<LabelledItem>(new ListModelList<LabelledItem>(templates));
		templateModel.setMultiple(false);
		LabelledItem defaultTemplate = LabelledItem.getByCode(templates, TemplateType.BLANK.name());
		form.put("template", defaultTemplate);
	}

	private void initLanguageModel() {
		List<LabelledItem> languages = new ArrayList<LabelledItem>();
		List<String> codes = Languages.getCodes(Standard.ISO_639_1);
		for (String code : codes) {
			LabelledItem item = new LabelledItem(code, Labels.getLabel(code));
			languages.add(item);
		}
		Collections.sort(languages, new LabelComparator());
		languageModel = new BindingListModelListModel<LabelledItem>(new ListModelList<LabelledItem>(languages));
		LabelledItem defaultLanguage = LabelledItem.getByCode(languages, Locale.ENGLISH.getLanguage());
		form.put("language", defaultLanguage);
	}
	
	public BindingListModelListModel<LabelledItem> getTemplateModel() {
		return templateModel;
	}
	
	@Command
	public void ok() throws IdmlParseException, SurveyValidationException {
		String name = (String) form.get("name");
		String langCode = ((LabelledItem) form.get("language")).getCode();
		String templateCode = ((LabelledItem) form.get("template")).getCode();

		CollectSurvey survey;
		if ( templateCode.equals(TemplateType.BLANK.name())) {
			survey = createEmptySurvey(name, langCode);
		} else {
			survey = createNewSurveyFromTemplate(name, langCode, templateCode);
		}
		//put survey in session and redirect into survey edit page
		SessionStatus sessionStatus = getSessionStatus();
		sessionStatus.setSurvey(survey);
		sessionStatus.setCurrentLanguageCode(survey.getDefaultLanguage());
		Executions.sendRedirect(Page.SURVEY_EDIT.getLocation());
	}

	protected CollectSurvey createNewSurveyFromTemplate(String name, String langCode, String templateCode)
			throws IdmlParseException, SurveyValidationException {
		String templateFileName = "/org/openforis/collect/designer/templates/" + templateCode.toLowerCase() + ".idm.xml";
		InputStream surveyFileIs = this.getClass().getResourceAsStream(templateFileName);
		CollectSurvey survey = surveyManager.unmarshalSurvey(surveyFileIs, false, true);
		survey.setName(name);
		survey.setWork(true);
		survey.setUri(surveyManager.generateSurveyUri(name));
		survey.setDefaultLanguage(langCode);
		return survey;
	}

	protected CollectSurvey createEmptySurvey(String name, String langCode) {
		//create empty survey
		CollectSurvey survey = surveyManager.createSurveyWork(name, langCode);
		//add default root entity
		Schema schema = survey.getSchema();
		EntityDefinition rootEntity = schema.createEntityDefinition();
		rootEntity.setName(SchemaVM.DEFAULT_ROOT_ENTITY_NAME);
		schema.addRootEntityDefinition(rootEntity);
		//create root tab set
		UIOptions uiOptions = survey.getUIOptions();
		UITabSet rootTabSet = uiOptions.createRootTabSet((EntityDefinition) rootEntity);
		UITab mainTab = uiOptions.getMainTab(rootTabSet);
		mainTab.setLabel(langCode, SchemaVM.DEFAULT_MAIN_TAB_LABEL);
		return survey;
	}
	
	public Validator getNameValidator() {
		return nameValidator;
	}

	public BindingListModelListModel<LabelledItem> getLanguageModel() {
		return languageModel;
	}

	public Map<String, Object> getForm() {
		return form;
	}
	
	public void setForm(Map<String, Object> form) {
		this.form = form;
	}
}
