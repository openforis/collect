package org.openforis.collect.web.controller;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.openforis.collect.manager.SurveyManager;
import org.openforis.collect.manager.UserGroupManager;
import org.openforis.collect.manager.UserManager;
import org.openforis.collect.metamodel.view.SurveyView;
import org.openforis.collect.metamodel.view.SurveyViewGenerator;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.SurveySummary;
import org.openforis.collect.model.User;
import org.openforis.collect.model.UserGroup;
import org.openforis.collect.persistence.SurveyImportException;
import org.openforis.collect.persistence.SurveyStoreException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
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

	@Autowired
	private SurveyManager surveyManager;
	@Autowired
	private UserManager userManager;
	@Autowired
	private UserGroupManager userGroupManager;
	
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
}
