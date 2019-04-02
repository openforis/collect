package org.openforis.collect.web.service;

import java.io.InputStream;
import java.util.Locale;

import org.openforis.collect.manager.SurveyManager;
import org.openforis.collect.manager.SurveyObjectsGenerator;
import org.openforis.collect.manager.UserGroupManager;
import org.openforis.collect.manager.exception.SurveyValidationException;
import org.openforis.collect.metamodel.SurveyTarget;
import org.openforis.collect.metamodel.ui.UIOptions;
import org.openforis.collect.metamodel.ui.UITab;
import org.openforis.collect.metamodel.ui.UITabSet;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.UserGroup;
import org.openforis.collect.persistence.SurveyStoreException;
import org.openforis.collect.web.controller.SurveyController.SurveyCreationParameters;
import org.openforis.collect.web.controller.SurveyController.SurveyCreationParameters.TemplateType;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.Schema;
import org.openforis.idm.metamodel.xml.IdmlParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SurveyService {
	
	private static final String IDM_TEMPLATE_FILE_NAME_FORMAT = "/org/openforis/collect/designer/templates/%s.idm.xml";
	public static final String DEFAULT_ROOT_ENTITY_NAME = "change_it_to_your_sampling_unit";
	public static final String DEFAULT_MAIN_TAB_LABEL = "Change it to your main tab label";
	
	@Autowired
	private SurveyManager surveyManager;
	@Autowired
	private UserGroupManager userGroupManager;
	
	public CollectSurvey createNewSurvey(SurveyCreationParameters params) throws SurveyStoreException, IdmlParseException, SurveyValidationException {
		CollectSurvey survey = createEmptySurvey(params);
		UserGroup userGroup = userGroupManager.loadById(params.getUserGroupId());
		survey.setUserGroupId(userGroup.getId());
		surveyManager.save(survey);
		return survey;
	}

	private CollectSurvey createEmptySurvey(SurveyCreationParameters params) throws IdmlParseException, SurveyValidationException {
		CollectSurvey survey;
		switch (params.getTemplateType()) {
		case BLANK:
			survey = createEmptySurvey(params.getName(), params.getDefaultLanguageCode());
			break;
		default:
			survey = createNewSurveyFromTemplate(params.getName(), params.getDefaultLanguageCode(), params.getTemplateType());
		}
		return survey;
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
	

}
