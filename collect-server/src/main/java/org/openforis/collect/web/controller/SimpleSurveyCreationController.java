package org.openforis.collect.web.controller;

import static org.springframework.web.bind.annotation.RequestMethod.POST;

import java.util.Locale;

import org.openforis.collect.manager.SamplingDesignManager;
import org.openforis.collect.manager.SurveyManager;
import org.openforis.collect.manager.UserGroupManager;
import org.openforis.collect.metamodel.SimpleSurveyCreationParameters;
import org.openforis.collect.metamodel.SurveyCreator;
import org.openforis.collect.metamodel.view.SurveyView;
import org.openforis.collect.metamodel.view.SurveyViewGenerator;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.web.validator.SimpleSurveyCreationParametersValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/api/survey")
public class SimpleSurveyCreationController {

	@Autowired
	private SurveyManager surveyManager;
	@Autowired
	private SamplingDesignManager samplingDesignManager;
	@Autowired
	private UserGroupManager userGroupManager;
	
	@Autowired
	private SimpleSurveyCreationParametersValidator validator;
	
	@InitBinder
	protected void initBinder(WebDataBinder binder) {
		binder.setValidator(validator);
	}

	@Transactional
	@RequestMapping(value="simple", method=POST)
	public @ResponseBody
	SurveyView createSimpleSurvey(@RequestBody SimpleSurveyCreationParameters parameters, BindingResult bindingResult) throws Exception {
		SurveyCreator surveyCreator = new SurveyCreator(surveyManager, samplingDesignManager, userGroupManager);
		CollectSurvey survey = surveyCreator.generateSimpleSurvey(parameters);
		return generateView(survey, true);
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
}
