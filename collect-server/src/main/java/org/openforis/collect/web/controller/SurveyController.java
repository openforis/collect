package org.openforis.collect.web.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.openforis.collect.manager.RecordManager;
import org.openforis.collect.manager.SurveyManager;
import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.SurveySummary;
import org.openforis.idm.metamodel.EntityDefinition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

/**
 * 
 * @author S. Ricci
 *
 */
@Controller
public class SurveyController {
	
	private static final String EDIT_SURVEY_VIEW = "editSurvey";

	@Autowired
	private RecordManager recordManager;
	@Autowired
	private SurveyManager surveyManager;

	@RequestMapping(value = "/surveys/summaries.json", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody
	List<Map<String, Object>> loadSummaries() throws Exception {
		List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
		List<SurveySummary> surveys = surveyManager.getSurveySummaries(Locale.ENGLISH.getLanguage());
		for (SurveySummary s : surveys) {
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("id", s.getId());
			map.put("uri", s.getUri());
			map.put("record_ids", getRecordIds(s));
			result.add(map);
		}
		return result;
	}

	private List<Integer> getRecordIds(SurveySummary s) {
		List<Integer> recordIds = new ArrayList<Integer>();
		CollectSurvey survey = surveyManager.getById(s.getId());
		List<EntityDefinition> rootEntities = survey.getSchema().getRootEntityDefinitions();
		EntityDefinition rootEntity = rootEntities.get(0);
		String rootEntityName = rootEntity.getName();
		List<CollectRecord> summaries = recordManager.loadSummaries(survey, rootEntityName);
		for (CollectRecord r : summaries) {
			recordIds.add(r.getId());
		}
		return recordIds;
	}
	
	@RequestMapping(value = "/survey/temp/{surveyId}/edit.htm", method = RequestMethod.GET)
	public ModelAndView editTemp(@PathVariable("surveyId") Integer surveyId, Model model) {
		model.addAttribute("temp_id", surveyId);
		return new ModelAndView(EDIT_SURVEY_VIEW);
	}
	
	@RequestMapping(value = "/survey/{surveyId}/edit.htm", method = RequestMethod.GET)
	public ModelAndView edit(@PathVariable("surveyId") Integer surveyId, Model model) {
		model.addAttribute("id", surveyId);
		return new ModelAndView(EDIT_SURVEY_VIEW);
	}
	

}
