package org.openforis.collect.web.controller;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;
import org.openforis.collect.manager.RecordManager;
import org.openforis.collect.manager.SurveyManager;
import org.openforis.collect.model.CollectRecord.Step;
import org.openforis.collect.model.CollectSurvey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

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

	@RequestMapping(value = "/survey/{survey_id}/record/{record_id}/step/{step}/data.json", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody
	Map<String, Object> loadData(@PathVariable(value="survey_id") int surveyId,
			@PathVariable(value="record_id") int recordId,
			@PathVariable(value="step") int stepNumber) throws Exception {
		CollectSurvey survey = surveyManager.getById(surveyId);
		byte[] data = recordManager.loadBinaryData(survey, recordId, Step.valueOf(stepNumber));
		byte[] encoded = Base64.encodeBase64(data);
		String result = new String(encoded);
		
		Map<String,Object> map = new HashMap<String, Object>();
		map.put("data", result);
		
 		return map;
	}

	
	
}
