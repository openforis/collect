package org.openforis.collect.web.controller;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.PATCH;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.openforis.collect.manager.SamplingDesignManager;
import org.openforis.collect.manager.SurveyManager;
import org.openforis.collect.manager.UserGroupManager;
import org.openforis.collect.manager.UserManager;
import org.openforis.collect.metamodel.SimpleSurveyCreationParameters;
import org.openforis.collect.metamodel.SurveyCreator;
import org.openforis.collect.metamodel.view.SurveyView;
import org.openforis.collect.metamodel.view.SurveyViewGenerator;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.SurveySummary;
import org.openforis.collect.model.User;
import org.openforis.collect.model.UserGroup;
import org.openforis.collect.persistence.SurveyImportException;
import org.openforis.collect.web.validator.SimpleSurveyParametersValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

/**
 * 
 * @author S. Ricci
 *
 */
@Controller
@RequestMapping("/api/survey")
public class SurveyController extends BasicController {

	private static final String EDIT_SURVEY_VIEW = "editSurvey";

	@Autowired
	private SimpleSurveyParametersValidator validator;
	@Autowired
	private SurveyManager surveyManager;
	@Autowired
	private SamplingDesignManager samplingDesignManager;
	@Autowired
	private UserManager userManager;
	@Autowired
	private UserGroupManager userGroupManager;
	
	@InitBinder
	protected void initBinder(WebDataBinder binder) {
		binder.setValidator(validator);
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
		List<SurveySummary> summaries = surveyManager.getSurveySummaries(languageCode, groups);
		
		if (fullSurveys) {
			List<SurveyView> views = new ArrayList<SurveyView>();
			for (SurveySummary surveySummary : summaries) {
				CollectSurvey survey = surveyManager.getById(surveySummary.getId());
				views.add(generateView(survey, includeCodeListValues));
			}
			return views;
		} else if (includeTemporary) {
			return surveyManager.loadCombinedSummaries(languageCode, true); //TODO fix it, filter by user
		} else {
			return summaries;
		}
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
	@RequestMapping(value="simple", method=POST)
	public @ResponseBody
	SurveyView createSimpleSurvey(@RequestBody SimpleSurveyCreationParameters parameters, BindingResult bindingResult) throws Exception {
		SurveyCreator surveyCreator = new SurveyCreator(surveyManager, samplingDesignManager, userGroupManager);
		CollectSurvey survey = surveyCreator.generateSimpleSurvey(parameters);
		return generateView(survey, true);
	}

	@RequestMapping(value="publish/{id}", method=PATCH)
	public @ResponseBody SurveyView publishSurvey(@PathVariable int id) throws SurveyImportException {
		CollectSurvey survey = surveyManager.getOrLoadSurveyById(id);
		surveyManager.publish(survey);
		return generateView(survey, false);
	}
	
	@RequestMapping(value="close/{id}", method=PATCH)
	public @ResponseBody SurveyView closeSurvey(@PathVariable int id) throws SurveyImportException {
		CollectSurvey survey = surveyManager.getOrLoadSurveyById(id);
		surveyManager.close(survey);
		return generateView(survey, false);
	}
	
	@RequestMapping(value="archive/{id}", method=PATCH)
	public @ResponseBody SurveyView archiveSurvey(@PathVariable int id) throws SurveyImportException {
		CollectSurvey survey = surveyManager.getOrLoadSurveyById(id);
		surveyManager.archive(survey);
		return generateView(survey, false);
	}
	
	@RequestMapping(value="temp/{surveyId}/edit.htm", method=GET)
	public ModelAndView editTemp(@PathVariable("surveyId") Integer surveyId, Model model) {
		model.addAttribute("temp_id", surveyId);
		return new ModelAndView(EDIT_SURVEY_VIEW);
	}
	
	@RequestMapping(value="{surveyId}/edit.htm", method=GET)
	public ModelAndView edit(@PathVariable("surveyId") Integer surveyId, Model model) {
		model.addAttribute("id", surveyId);
		return new ModelAndView(EDIT_SURVEY_VIEW);
	}
	
	private SurveyView generateView(CollectSurvey survey, boolean includeCodeListValues) {
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
}
