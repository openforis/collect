package org.openforis.collect.web.controller.designer;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class SurveyController {

	private static final String EDIT_SURVEY_VIEW = "editSurvey";

	@RequestMapping(value = "/surveys/edit/temp/{surveyId}", method = RequestMethod.GET)
	public ModelAndView editTemp(@PathVariable("surveyId") Integer surveyId, Model model) {
		model.addAttribute("temp_id", surveyId);
		return new ModelAndView(EDIT_SURVEY_VIEW);
	}
	
	@RequestMapping(value = "/surveys/edit/{surveyId}", method = RequestMethod.GET)
	public ModelAndView edit(@PathVariable("surveyId") Integer surveyId, Model model) {
		model.addAttribute("id", surveyId);
		return new ModelAndView(EDIT_SURVEY_VIEW);
	}
	
}
