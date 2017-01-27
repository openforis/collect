package org.openforis.collect.designer.viewmodel;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.openforis.collect.designer.form.validator.SurveyNameValidator;
import org.openforis.collect.designer.model.LabelledItem;
import org.openforis.collect.designer.model.LabelledItem.LabelComparator;
import org.openforis.collect.designer.session.SessionStatus;
import org.openforis.collect.designer.util.Resources.Page;
import org.openforis.collect.manager.SurveyManager;
import org.openforis.collect.manager.SurveyObjectsGenerator;
import org.openforis.collect.manager.exception.SurveyValidationException;
import org.openforis.collect.metamodel.SurveyTarget;
import org.openforis.collect.metamodel.ui.UIOptions;
import org.openforis.collect.metamodel.ui.UITab;
import org.openforis.collect.metamodel.ui.UITabSet;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.Institution;
import org.openforis.collect.persistence.SurveyStoreException;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.Languages;
import org.openforis.idm.metamodel.Languages.Standard;
import org.openforis.idm.metamodel.Schema;
import org.openforis.idm.metamodel.xml.IdmlParseException;
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

	private static final String IDM_TEMPLATE_FILE_NAME_FORMAT = "/org/openforis/collect/designer/templates/%s.idm.xml";
	private static final String SURVEY_NAME_FIELD = "name";
	private static final String TEMPLATE_FIELD_NAME = "template";
	private static final String LANGUAGE_FIELD_NAME = "language";
	private static final String INSTITUTION_FIELD_NAME = "institution";


	private enum TemplateType {
		BLANK,
		BIOPHYSICAL, 
		COLLECT_EARTH,
		COLLECT_EARTH_IPCC,
		//SOCIOECONOMIC, 
	}

	@WireVariable 
	private SurveyManager surveyManager;
	
	private Map<String, Object> form;
	
	private BindingListModelListModel<LabelledItem> templateModel;
	private BindingListModelListModel<LabelledItem> languageModel;

	private Validator nameValidator;
	
	@Init(superclass=false)
	public void init() {
		super.init();
		form = new HashMap<String, Object>();
		nameValidator = new SurveyNameValidator(surveyManager, SURVEY_NAME_FIELD, true);
		initLanguageModel();
		initTemplatesModel();
		form.put(INSTITUTION_FIELD_NAME, getDefaultPublicInstitutionItem());
	}

	private void initTemplatesModel() {
		List<LabelledItem> templates = new ArrayList<LabelledItem>();
		for (TemplateType templateType : TemplateType.values()) {
			String name = templateType.name();
			templates.add(new LabelledItem(name, Labels.getLabel("survey.template.type." + name.toLowerCase(Locale.ENGLISH))));
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
	public void ok() throws IdmlParseException, SurveyValidationException, SurveyStoreException {
		String name = (String) form.get(SURVEY_NAME_FIELD);
		String langCode = ((LabelledItem) form.get(LANGUAGE_FIELD_NAME)).getCode();
		String templateCode = ((LabelledItem) form.get(TEMPLATE_FIELD_NAME)).getCode();
		TemplateType templateType = TemplateType.valueOf(templateCode);
		String institutionName = ((LabelledItem) form.get(INSTITUTION_FIELD_NAME)).getCode();
		
		CollectSurvey survey;
		switch (templateType) {
		case BLANK:
			survey = createEmptySurvey(name, langCode);
			break;
		default:
			survey = createNewSurveyFromTemplate(name, langCode, templateType);
		}
		Institution institution = institutionManager.findByName(institutionName);
		survey.setInstitutionId(institution.getId());
		surveyManager.save(survey);
		//put survey in session and redirect into survey edit page
		SessionStatus sessionStatus = getSessionStatus();
		sessionStatus.setSurvey(survey);
		sessionStatus.setCurrentLanguageCode(survey.getDefaultLanguage());
		Executions.sendRedirect(Page.SURVEY_EDIT.getLocation());
	}

	protected CollectSurvey createNewSurveyFromTemplate(String name, String langCode, TemplateType templateType)
			throws IdmlParseException, SurveyValidationException {
		String templateFileName = String.format(IDM_TEMPLATE_FILE_NAME_FORMAT, templateType.name().toLowerCase(Locale.ENGLISH));
		InputStream surveyFileIs = this.getClass().getResourceAsStream(templateFileName);
		CollectSurvey survey = surveyManager.unmarshalSurvey(surveyFileIs, false, true);
		survey.setName(name);
		survey.setTemporary(true);
		survey.setUri(surveyManager.generateSurveyUri(name));
		survey.setDefaultLanguage(langCode);
		SurveyTarget target;
		switch (templateType) {
		case COLLECT_EARTH:
		case COLLECT_EARTH_IPCC:
			target = SurveyTarget.COLLECT_EARTH;
			break;
		default:
			target = SurveyTarget.COLLECT_DESKTOP;
		}
		survey.setTarget(target);
		
		if ( survey.getSamplingDesignCodeList() == null ) {
			survey.addSamplingDesignCodeList();
		}
		return survey;
	}

	protected CollectSurvey createEmptySurvey(String name, String langCode) {
		//create empty survey
		CollectSurvey survey = surveyManager.createTemporarySurvey(name, langCode);
		//add default root entity
		Schema schema = survey.getSchema();
		EntityDefinition rootEntity = schema.createEntityDefinition();
		rootEntity.setMultiple(true);
		rootEntity.setName(SchemaVM.DEFAULT_ROOT_ENTITY_NAME);
		schema.addRootEntityDefinition(rootEntity);
		//create root tab set
		UIOptions uiOptions = survey.getUIOptions();
		UITabSet rootTabSet = uiOptions.createRootTabSet((EntityDefinition) rootEntity);
		UITab mainTab = uiOptions.getMainTab(rootTabSet);
		mainTab.setLabel(langCode, SchemaVM.DEFAULT_MAIN_TAB_LABEL);
		
		SurveyObjectsGenerator surveyObjectsGenerator = new SurveyObjectsGenerator();
		surveyObjectsGenerator.addPredefinedObjects(survey);
		
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
