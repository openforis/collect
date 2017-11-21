package org.openforis.collect.web.controller;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.validation.Valid;

import org.openforis.collect.manager.SurveyManager;
import org.openforis.collect.manager.SurveyObjectsGenerator;
import org.openforis.collect.manager.UserGroupManager;
import org.openforis.collect.manager.UserManager;
import org.openforis.collect.manager.exception.SurveyValidationException;
import org.openforis.collect.metamodel.SimpleSurveyCreationParameters;
import org.openforis.collect.metamodel.SurveyTarget;
import org.openforis.collect.metamodel.ui.UIOptions;
import org.openforis.collect.metamodel.ui.UITab;
import org.openforis.collect.metamodel.ui.UITabSet;
import org.openforis.collect.metamodel.view.SurveyView;
import org.openforis.collect.metamodel.view.SurveyViewGenerator;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.SurveySummary;
import org.openforis.collect.model.User;
import org.openforis.collect.model.UserGroup;
import org.openforis.collect.persistence.SurveyImportException;
import org.openforis.collect.persistence.SurveyStoreException;
import org.openforis.collect.web.controller.SurveyController.SurveyCreationParameters.TemplateType;
import org.openforis.collect.web.validator.SimpleSurveyCreationParametersValidator;
import org.openforis.collect.web.validator.SurveyCreationParametersValidator;
import org.openforis.commons.web.Response;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.Schema;
import org.openforis.idm.metamodel.xml.IdmlParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 
 * @author S. Ricci
 *
 */
@Controller
@RequestMapping("/api/survey")
public class SurveyController extends BasicController {

	private static final String IDM_TEMPLATE_FILE_NAME_FORMAT = "/org/openforis/collect/designer/templates/%s.idm.xml";
	public static final String DEFAULT_ROOT_ENTITY_NAME = "change_it_to_your_sampling_unit";
	public static final String DEFAULT_MAIN_TAB_LABEL = "Change it to your main tab label";

	@Autowired
	private SurveyManager surveyManager;
	@Autowired
	private UserManager userManager;
	@Autowired
	private UserGroupManager userGroupManager;
	@Autowired
	private SurveyCreationParametersValidator surveyCreationParametersValidator;
	@Autowired
	private SimpleSurveyCreationParametersValidator simpleSurveyCreationParametersValidator;
	
	@InitBinder
	protected void initBinder(WebDataBinder binder) {
		Object target = binder.getTarget();
		if (target != null) {
			if (target.getClass().isAssignableFrom(SurveyCreationParameters.class)) {
				binder.setValidator(surveyCreationParametersValidator);
			} else if (target.getClass().isAssignableFrom(SimpleSurveyCreationParameters.class)) {
				binder.setValidator(simpleSurveyCreationParametersValidator);
			}
		}
	}
	
	@RequestMapping(method=GET)
	public @ResponseBody
	List<?> loadSurveys(
			@RequestParam(value="userId", required=false) Integer userId,
			@RequestParam(value="groupId", required=false) Integer groupId,
			@RequestParam(value="full", required=false) boolean fullSurveys,
			@RequestParam(value="includeCodeListValues", required=false) boolean includeCodeListValues,
			@RequestParam(value="includeTemporary", required=false) boolean includeTemporary) throws Exception {
		String languageCode = Locale.ENGLISH.getLanguage();
		Set<UserGroup> groups = getAvailableUserGrups(userId, groupId);
		List<SurveySummary> summaries = includeTemporary ? surveyManager.loadCombinedSummaries(languageCode, true, groups, null)
				: surveyManager.getSurveySummaries(languageCode, groups);
		
		List<Object> views = new ArrayList<Object>();
		for (SurveySummary surveySummary : summaries) {
			if (fullSurveys) {
				CollectSurvey survey = surveyManager.getOrLoadSurveyById(surveySummary.getId());
				views.add(generateView(survey, includeCodeListValues));
			} else {
				views.add(surveySummary);
			}
		}
		return views;
	}

	@RequestMapping(value="{id}", method=GET)
	public @ResponseBody
	SurveyView loadSurvey(@PathVariable int id, 
			@RequestParam(value="includeCodeListValues", required=false, defaultValue="true") boolean includeCodeListValues) 
			throws Exception {
		CollectSurvey survey = surveyManager.getOrLoadSurveyById(id);
		return generateView(survey, includeCodeListValues);
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
	
	@RequestMapping(value="validatecreation", method=POST)
	public @ResponseBody Response validateSurveyCreationParameters(@Valid SurveyCreationParameters params, BindingResult result) {
		List<ObjectError> errors = result.getAllErrors();
		Response response = new Response();
		if (! errors.isEmpty()) {
			response.setErrorStatus();
			response.addObject("errors", errors);
		}
		return response;
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
	
	@RequestMapping(value="publish/{id}", method=POST)
	public @ResponseBody SurveyView publishSurvey(@PathVariable int id) throws SurveyImportException {
		CollectSurvey survey = surveyManager.getOrLoadSurveyById(id);
		surveyManager.publish(survey);
		return generateView(survey, false);
	}
	
	@RequestMapping(value="close/{id}", method=POST)
	public @ResponseBody SurveyView closeSurvey(@PathVariable int id) throws SurveyImportException {
		CollectSurvey survey = surveyManager.getOrLoadSurveyById(id);
		surveyManager.close(survey);
		return generateView(survey, false);
	}
	
	@RequestMapping(value="archive/{id}", method=POST)
	public @ResponseBody SurveyView archiveSurvey(@PathVariable int id) throws SurveyImportException {
		CollectSurvey survey = surveyManager.getOrLoadSurveyById(id);
		surveyManager.archive(survey);
		return generateView(survey, false);
	}
	
	@RequestMapping(value="changeusergroup/{id}", method=POST)
	public @ResponseBody SurveyView changeSurveyUserGroup(@PathVariable int id, @RequestParam int userGroupId) throws SurveyStoreException {
		CollectSurvey survey = surveyManager.getOrLoadSurveyById(id);
		survey.setUserGroupId(userGroupId);
		surveyManager.save(survey);
		return generateView(survey, false);
	}
	
	private SurveyView generateView(CollectSurvey survey, boolean includeCodeListValues) {
		if (survey == null) {
			return null;
		}
		SurveyViewGenerator viewGenerator = new SurveyViewGenerator(Locale.ENGLISH.getLanguage());
		viewGenerator.setIncludeCodeListValues(includeCodeListValues);
		SurveyView view = viewGenerator.generateView(survey);
		return view;
	}
	
	private Set<UserGroup> getAvailableUserGrups(Integer userId, Integer groupId) {
		if (groupId != null) {
			UserGroup group = userGroupManager.loadById(groupId);
			Set<UserGroup> groups = Collections.singleton(group);
			return groups;
		} else if (userId != null) {
			User availableToUser = userId == null ? null : userManager.loadById(userId);
			List<UserGroup> groups = userGroupManager.findByUser(availableToUser);
			return new HashSet<UserGroup>(groups);
		} else {
			return null;
		}
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
