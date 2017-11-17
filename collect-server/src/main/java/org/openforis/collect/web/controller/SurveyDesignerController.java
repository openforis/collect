package org.openforis.collect.web.controller;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("/surveys")
public class SurveyDesignerController {

	private static final String EDIT_SURVEY_VIEW = "editSurvey";

	@RequestMapping(value="{surveyId}/edit.htm", method=GET)
	public ModelAndView edit(@PathVariable("surveyId") Integer surveyId, Model model) {
		model.addAttribute("id", surveyId);
		return new ModelAndView(EDIT_SURVEY_VIEW);
	}
}
