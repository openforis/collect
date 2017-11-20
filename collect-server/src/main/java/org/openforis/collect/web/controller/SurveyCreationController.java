package org.openforis.collect.web.controller;

import static org.springframework.web.bind.annotation.RequestMethod.POST;

import java.io.InputStream;
import java.util.Locale;

import javax.validation.Valid;

import org.openforis.collect.manager.SurveyManager;
import org.openforis.collect.manager.SurveyObjectsGenerator;
import org.openforis.collect.manager.UserGroupManager;
import org.openforis.collect.manager.exception.SurveyValidationException;
import org.openforis.collect.metamodel.SurveyTarget;
import org.openforis.collect.metamodel.ui.UIOptions;
import org.openforis.collect.metamodel.ui.UITab;
import org.openforis.collect.metamodel.ui.UITabSet;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.SurveySummary;
import org.openforis.collect.model.UserGroup;
import org.openforis.collect.web.controller.SurveyCreationController.SurveyCreationParameters.TemplateType;
import org.openforis.collect.web.validator.SurveyCreationParametersValidator;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.Schema;
import org.openforis.idm.metamodel.xml.IdmlParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/api/survey")
public class SurveyCreationController {
	
	private static final String IDM_TEMPLATE_FILE_NAME_FORMAT = "/org/openforis/collect/designer/templates/%s.idm.xml";
	public static final String DEFAULT_ROOT_ENTITY_NAME = "change_it_to_your_sampling_unit";
	public static final String DEFAULT_MAIN_TAB_LABEL = "Change it to your main tab label";

	@Autowired
	private SurveyManager surveyManager;
	@Autowired
	private UserGroupManager userGroupManager;
	
	@Autowired
	private SurveyCreationParametersValidator validator;
	
	@InitBinder
	protected void initBinder(WebDataBinder binder) {
		binder.setValidator(validator);
	}

	@Transactional
	@RequestMapping(method=POST)
	public @ResponseBody
	SurveySummary createSurvey(@Valid SurveyCreationParameters params, BindingResult bindingResult) throws Exception {
		CollectSurvey survey;
		switch (params.getTemplateType()) {
		case BLANK:
			survey = createEmptySurvey(params.getName(), params.getDefaultLanguageCode());
			break;
		default:
			survey = createNewSurveyFromTemplate(params.getName(), params.getDefaultLanguageCode(), params.getTemplateType());
		}
		UserGroup userGroup = userGroupManager.loadById(params.getUserGroupId());
		survey.setUserGroupId(userGroup.getId());
		surveyManager.save(survey);
		
		return SurveySummary.createFromSurvey(survey);
	}
	
	private CollectSurvey createNewSurveyFromTemplate(String name, String langCode, TemplateType templateType)
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

	private CollectSurvey createEmptySurvey(String name, String langCode) {
		//create empty survey
		CollectSurvey survey = surveyManager.createTemporarySurvey(name, langCode);
		//add default root entity
		Schema schema = survey.getSchema();
		EntityDefinition rootEntity = schema.createEntityDefinition();
		rootEntity.setMultiple(true);
		rootEntity.setName(DEFAULT_ROOT_ENTITY_NAME);
		schema.addRootEntityDefinition(rootEntity);
		//create root tab set
		UIOptions uiOptions = survey.getUIOptions();
		UITabSet rootTabSet = uiOptions.createRootTabSet((EntityDefinition) rootEntity);
		UITab mainTab = uiOptions.getMainTab(rootTabSet);
		mainTab.setLabel(langCode, DEFAULT_MAIN_TAB_LABEL);
		
		SurveyObjectsGenerator surveyObjectsGenerator = new SurveyObjectsGenerator();
		surveyObjectsGenerator.addPredefinedObjects(survey);
		
		return survey;
	}

	public static class SurveyCreationParameters {
		
		public enum TemplateType {
			BLANK,
			BIOPHYSICAL, 
			COLLECT_EARTH,
			COLLECT_EARTH_IPCC,
			//SOCIOECONOMIC, 
		}

		private String name;
		private TemplateType templateType;
		private String defaultLanguageCode;
		private int userGroupId;

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public TemplateType getTemplateType() {
			return templateType;
		}

		public void setTemplateType(TemplateType templateType) {
			this.templateType = templateType;
		}

		public String getDefaultLanguageCode() {
			return defaultLanguageCode;
		}
		
		public void setDefaultLanguageCode(String defaultLanguageCode) {
			this.defaultLanguageCode = defaultLanguageCode;
		}
		
		public int getUserGroupId() {
			return userGroupId;
		}
		
		public void setUserGroupId(int userGroupId) {
			this.userGroupId = userGroupId;
		}
	}
}
