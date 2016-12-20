package org.openforis.collect.web.controller;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.openforis.collect.manager.RecordManager;
import org.openforis.collect.manager.SamplingDesignManager;
import org.openforis.collect.manager.SurveyManager;
import org.openforis.collect.metamodel.SurveyCreator;
import org.openforis.collect.metamodel.SurveyViewGenerator;
import org.openforis.collect.metamodel.SurveyViewGenerator.SurveyView;
import org.openforis.collect.metamodel.samplingdesign.SingleAttributeSurveyCreationParameters;
import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.SurveySummary;
import org.openforis.collect.web.validator.SimpleSurveyParametersValidator;
import org.openforis.idm.metamodel.EntityDefinition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
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
@RequestMapping("/surveys/")
public class SurveyController extends BasicController {

	private static final String EDIT_SURVEY_VIEW = "editSurvey";

	@Autowired
	private SimpleSurveyParametersValidator validator;
	
	@InitBinder
	protected void initBinder(WebDataBinder binder) {
		binder.setValidator(validator);
	}
	
	@Autowired
	private RecordManager recordManager;
	@Autowired
	private SurveyManager surveyManager;
	@Autowired
	private SamplingDesignManager samplingDesignManager;

	@RequestMapping(value = "summaries-by-user.json", method=GET, produces=APPLICATION_JSON_VALUE)
	public @ResponseBody
	List<SurveySummary> loadSummariesByUser(@RequestParam(required=false) int userID) {
		//TODO
		return null;
		//TODO add institution, imagery, boundaries
	}

	@RequestMapping(value = "summaries.json", method=GET, produces=APPLICATION_JSON_VALUE)
	public @ResponseBody
	List<SurveySummary> loadSummaries(
			@RequestParam(required=false) boolean includeTemporary,
			@RequestParam(required=false) boolean includeRecordIds) throws Exception {
		String language = Locale.ENGLISH.getLanguage();
		if (includeTemporary) {
			return surveyManager.loadCombinedSummaries(language, true);
		} else {
			return surveyManager.getSurveySummaries(language);
		}
	}
	
	@RequestMapping(value = "{id}.json", method=GET, produces=APPLICATION_JSON_VALUE)
	public @ResponseBody
	SurveyView loadSurvey(@PathVariable int id, 
			@RequestParam(value="include-code-lists", required=false, defaultValue="true") boolean includeCodeLists) 
			throws Exception {
		CollectSurvey survey = surveyManager.getOrLoadSurveyById(id);
		return generateView(survey, includeCodeLists);
	}

	@RequestMapping(value = "temp/{surveyId}/edit.htm", method=GET)
	public ModelAndView editTemp(@PathVariable("surveyId") Integer surveyId, Model model) {
		model.addAttribute("temp_id", surveyId);
		return new ModelAndView(EDIT_SURVEY_VIEW);
	}
	
	@RequestMapping(value = "{surveyId}/edit.htm", method=GET)
	public ModelAndView edit(@PathVariable("surveyId") Integer surveyId, Model model) {
		model.addAttribute("id", surveyId);
		return new ModelAndView(EDIT_SURVEY_VIEW);
	}
	
	@Transactional
	@RequestMapping(value = "create-single-attribute-survey.json", method=POST, produces=APPLICATION_JSON_VALUE)
	public @ResponseBody
	SurveyView createSingleAttributeSurvey(@Validated SingleAttributeSurveyCreationParameters parameters, BindingResult result) throws Exception {
		SurveyCreator surveyCreator = new SurveyCreator(surveyManager, samplingDesignManager);
		CollectSurvey survey = surveyCreator.generateAndPublishSurvey(parameters);
		return generateView(survey);
	}

	protected List<Integer> getRecordIds(SurveySummary s) {
		List<Integer> recordIds = new ArrayList<Integer>();
		CollectSurvey survey = surveyManager.getById(s.getId());
		List<EntityDefinition> rootEntities = survey.getSchema().getRootEntityDefinitions();
		EntityDefinition rootEntity = rootEntities.get(0);
		String rootEntityName = rootEntity.getName();
		List<CollectRecord> recordSummaries = recordManager.loadSummaries(survey, rootEntityName);
		for (CollectRecord r : recordSummaries) {
			recordIds.add(r.getId());
		}
		return recordIds;
	}

	private SurveyView generateView(CollectSurvey survey) {
		return generateView(survey, false);
	}
	
	private SurveyView generateView(CollectSurvey survey, boolean includeCodeLists) {
		SurveyViewGenerator viewGenerator = new SurveyViewGenerator(Locale.ENGLISH);
		viewGenerator.setIncludeCodeLists(includeCodeLists);
		SurveyView view = viewGenerator.generateView(survey);
		return view;
	}
}
