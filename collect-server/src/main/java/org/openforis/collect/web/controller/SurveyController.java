package org.openforis.collect.web.controller;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

import java.util.List;
import java.util.Locale;

import org.openforis.collect.manager.SamplingDesignManager;
import org.openforis.collect.manager.SurveyManager;
import org.openforis.collect.metamodel.SimpleSurveyCreationParameters;
import org.openforis.collect.metamodel.SurveyCreator;
import org.openforis.collect.metamodel.SurveyView;
import org.openforis.collect.metamodel.SurveyViewGenerator;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.SurveySummary;
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
@RequestMapping("/survey/")
public class SurveyController extends BasicController {

	private static final String EDIT_SURVEY_VIEW = "editSurvey";

	@Autowired
	private SimpleSurveyParametersValidator validator;
	@Autowired
	private SurveyManager surveyManager;
	@Autowired
	private SamplingDesignManager samplingDesignManager;
	
	@InitBinder
	protected void initBinder(WebDataBinder binder) {
		binder.setValidator(validator);
	}

	@RequestMapping(value="summaries.json", method=GET)
	public @ResponseBody
	List<SurveySummary> loadSummaries(
			@RequestParam(value="include-temporary", required=false) boolean includeTemporary) throws Exception {
		String language = Locale.ENGLISH.getLanguage();
		if (includeTemporary) {
			return surveyManager.loadCombinedSummaries(language, true);
		} else {
			return surveyManager.getSurveySummaries(language);
		}
	}
	
	@RequestMapping(value = "/published/full-list.json", method=GET)
	public @ResponseBody
	List<SurveyView> loadFullList() throws Exception {
		Locale locale = Locale.ENGLISH;
		List<CollectSurvey> result = surveyManager.getAll();
		SurveyViewGenerator viewGenerator = new SurveyViewGenerator(locale);
		return viewGenerator.generateViews(result);
	}

	@RequestMapping(value="{id}.json", method=GET)
	public @ResponseBody
	SurveyView loadSurvey(@PathVariable int id, 
			@RequestParam(value="include-code-lists", required=false, defaultValue="true") boolean includeCodeLists) 
			throws Exception {
		CollectSurvey survey = surveyManager.getOrLoadSurveyById(id);
		return generateView(survey, includeCodeLists);
	}
	
	@Transactional
	@RequestMapping(value="simple", method=POST)
	public @ResponseBody
	SurveyView insertSurvey(@RequestBody SimpleSurveyCreationParameters parameters, BindingResult bindingResult) throws Exception {
		SurveyCreator surveyCreator = new SurveyCreator(surveyManager, samplingDesignManager);
		CollectSurvey survey = surveyCreator.generateAndPublishSurvey(parameters);
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
	
	private SurveyView generateView(CollectSurvey survey, boolean includeCodeLists) {
		SurveyViewGenerator viewGenerator = new SurveyViewGenerator(Locale.ENGLISH);
		viewGenerator.setIncludeCodeLists(includeCodeLists);
		SurveyView view = viewGenerator.generateView(survey);
		return view;
	}
}
