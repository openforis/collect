package org.openforis.collect.web.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.openforis.collect.manager.RecordManager;
import org.openforis.collect.manager.SurveyManager;
import org.openforis.collect.metamodel.SurveyViewGenerator;
import org.openforis.collect.metamodel.SurveyViewGenerator.SurveyView;
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
public class SurveyController {
	
	private static final String EDIT_SURVEY_VIEW = "editSurvey";

	@Autowired
	private RecordManager recordManager;
	@Autowired
	private SurveyManager surveyManager;

	@RequestMapping(value = "summaries.json", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody
	List<SurveySummary> loadSummaries(@RequestParam(value="include_record_ids", required=false) boolean includeRecordIds) throws Exception {
		List<SurveySummary> surveys = surveyManager.getSurveySummaries(Locale.ENGLISH.getLanguage());
		return surveys;
	}

	@RequestMapping(value = "{id}.json", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody
	SurveyView loadSurvey(@PathVariable int id) throws Exception {
		CollectSurvey survey = surveyManager.getById(id);
		SurveyViewGenerator viewGenerator = new SurveyViewGenerator(Locale.ENGLISH);
		SurveyView view = viewGenerator.generateView(survey);
		return view;
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
	
	@RequestMapping(value = "temp/{surveyId}/edit.htm", method = RequestMethod.GET)
	public ModelAndView editTemp(@PathVariable("surveyId") Integer surveyId, Model model) {
		model.addAttribute("temp_id", surveyId);
		return new ModelAndView(EDIT_SURVEY_VIEW);
	}
	
	@RequestMapping(value = "{surveyId}/edit.htm", method = RequestMethod.GET)
	public ModelAndView edit(@PathVariable("surveyId") Integer surveyId, Model model) {
		model.addAttribute("id", surveyId);
		return new ModelAndView(EDIT_SURVEY_VIEW);
	}
	

}
