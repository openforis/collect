package org.openforis.collect.web.controller;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.client.utils.URIBuilder;
import org.openforis.collect.ProxyContext;
import org.openforis.collect.manager.MessageSource;
import org.openforis.collect.manager.RandomRecordGenerator;
import org.openforis.collect.manager.RecordManager;
import org.openforis.collect.manager.SurveyManager;
import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.CollectRecord.Step;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.proxy.RecordProxy;
import org.openforis.collect.persistence.RecordPersistenceException;
import org.openforis.idm.metamodel.SurveyContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
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
public class RecordController extends BasicController implements Serializable {

	private static final long serialVersionUID = 1L;

	// private static Log LOG = LogFactory.getLog(DataController.class);

	@Autowired
	private RecordManager recordManager;
	@Autowired
	private SurveyManager surveyManager;
	@Autowired
	private SurveyContext surveyContext;
	@Autowired
	private MessageSource messageSource;
	@Autowired
	private RandomRecordGenerator randomRecordGenerator;
	
	@RequestMapping(value = "survey/{surveyId}/data/records/{recordId}/binary_data.json", method=GET)
	public @ResponseBody
	Map<String, Object> loadData(
			@PathVariable int surveyId,
			@PathVariable int recordId,
			@RequestParam(value="step") Integer stepNumber) throws Exception {
		stepNumber = getStepNumberOrDefault(stepNumber);
		CollectSurvey survey = surveyManager.getById(surveyId);
		byte[] data = recordManager.loadBinaryData(survey, recordId, Step.valueOf(stepNumber));
		byte[] encoded = Base64.encodeBase64(data);
		String result = new String(encoded);
		
		Map<String,Object> map = new HashMap<String, Object>();
		map.put("data", result);
		
 		return map;
	}

	@RequestMapping(value = "/survey/{surveyId}/data/records/count.json", method=GET)
	public @ResponseBody
	int getCount(@PathVariable int surveyId,
			@RequestParam(value="rootEntityDefinitionId", required=false) Integer rootEntityDefinitionId,
			@RequestParam(value="step", required=false) Integer stepNumber) throws Exception {
		stepNumber = getStepNumberOrDefault(stepNumber);
		CollectSurvey survey = surveyManager.getById(surveyId);
		if (rootEntityDefinitionId == null) {
			rootEntityDefinitionId = survey.getSchema().getFirstRootEntityDefinition().getId();
		}
		int count = recordManager.countRecords(survey, rootEntityDefinitionId, stepNumber);
		return count;
	}

	@RequestMapping(value = "survey/{surveyId}/data/records/{recordId}/edit.htm", method=GET)
	public ModelAndView editRecord(@PathVariable int surveyId, @PathVariable int recordId ) throws Exception {
		URIBuilder uriBuilder = new URIBuilder("redirect:/index.htm");
		uriBuilder.addParameter("edit", "true");
		uriBuilder.addParameter("surveyId", Integer.toString(surveyId));
		uriBuilder.addParameter("recordId", Integer.toString(recordId));
		String url = uriBuilder.toString();
		//String url = String.format("redirect:/index.htm?edit=true&surveyId=%d&recordId=%d", surveyId, recordId);
		return new ModelAndView(url);
	}
	
	@RequestMapping(value = "survey/{surveyId}/data/records/{recordId}/content.json", method=GET, produces=APPLICATION_JSON_VALUE)
	public @ResponseBody
	RecordProxy loadRecord(
			@PathVariable int surveyId, 
			@PathVariable int recordId,
			@RequestParam(value="step", required=false) Integer stepNumber) throws RecordPersistenceException {
		stepNumber = getStepNumberOrDefault(stepNumber);
		CollectSurvey survey = surveyManager.getById(surveyId);
		CollectRecord record = recordManager.load(survey, recordId, Step.valueOf(stepNumber));
		return toProxy(record);
	}

	@Transactional
	@RequestMapping(value = "survey/{surveyId}/data/records/random.json", method=POST, consumes=APPLICATION_JSON_VALUE)
	public @ResponseBody
	RecordProxy createRandomRecord(@PathVariable int surveyId, @RequestBody RandomRecordGenerationParameters params) throws RecordPersistenceException {
		CollectRecord record = randomRecordGenerator.generate(surveyId, params.getUserId());
		return toProxy(record);
	}
	
	private RecordProxy toProxy(CollectRecord record) {
		String defaultLanguage = record.getSurvey().getDefaultLanguage();
		Locale locale = new Locale(defaultLanguage);
		ProxyContext context = new ProxyContext(locale, messageSource, surveyContext);
		return new RecordProxy(record, context);
	}
	
	private Integer getStepNumberOrDefault(Integer stepNumber) {
		if (stepNumber == null) {
			stepNumber = Step.ENTRY.getStepNumber();
		}
		return stepNumber;
	}
	
	static class RandomRecordGenerationParameters {
		
		private int userId;

		public int getUserId() {
			return userId;
		}

		public void setUserId(int userId) {
			this.userId = userId;
		}
		
	}

}
