package org.openforis.collect.web.controller;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.client.utils.URIBuilder;
import org.openforis.collect.manager.RecordManager;
import org.openforis.collect.manager.RecordSessionManager;
import org.openforis.collect.manager.SamplingDesignManager;
import org.openforis.collect.manager.SurveyManager;
import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.CollectRecord.Step;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.RecordFilter;
import org.openforis.collect.model.SamplingDesignItem;
import org.openforis.collect.model.SamplingDesignSummaries;
import org.openforis.collect.model.User;
import org.openforis.collect.model.proxy.RecordProxy;
import org.openforis.collect.persistence.RecordPersistenceException;
import org.openforis.collect.web.session.SessionState;
import org.openforis.commons.collection.Visitor;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.model.Attribute;
import org.openforis.idm.model.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.ModelAndView;

/**
 * 
 * @author S. Ricci
 * 
 */
@Controller
@Scope(WebApplicationContext.SCOPE_SESSION)
public class RecordController extends BasicController implements Serializable {

	private static final long serialVersionUID = 1L;

	// private static Log LOG = LogFactory.getLog(DataController.class);

	@Autowired
	private RecordManager recordManager;
	@Autowired
	private SurveyManager surveyManager;
	@Autowired
	private RecordSessionManager sessionManager;
	@Autowired
	private SamplingDesignManager samplingDesignManager;
	
	@RequestMapping(value = "/surveys/{survey_id}/records/{record_id}/steps/{step}/binary_data.json", method=GET, produces=APPLICATION_JSON_VALUE)
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

	@RequestMapping(value = "/surveys/{survey_id}/records/count.json", method=GET, produces=APPLICATION_JSON_VALUE)
	public @ResponseBody
	int getCount(@PathVariable(value="survey_id") int surveyId,
			@RequestParam(value="rootEntityDefinitionId") int rootEntityDefinitionId,
			@RequestParam(value="step") int stepNumber) throws Exception {
		CollectSurvey survey = surveyManager.getById(surveyId);
		int count = recordManager.countRecords(survey, rootEntityDefinitionId, stepNumber);
		return count;
	}

	@RequestMapping(value = "/surveys/{survey_id}/records/{record_id}/edit.htm", method=GET)
	public ModelAndView editRecord(@PathVariable(value="survey_id") int surveyId,
			@PathVariable(value="record_id") int recordId ) throws Exception {
		URIBuilder uriBuilder = new URIBuilder("redirect:/index.htm");
		uriBuilder.addParameter("edit", "true");
		uriBuilder.addParameter("surveyId", Integer.toString(surveyId));
		uriBuilder.addParameter("recordId", Integer.toString(recordId));
		String url = uriBuilder.toString();
		//String url = String.format("redirect:/index.htm?edit=true&surveyId=%d&recordId=%d", surveyId, recordId);
		return new ModelAndView(url);
	}
	
	@RequestMapping(value = "/surveys/{survey_id}/records/{record_id}/steps/{step}/content.json", method=GET, produces=APPLICATION_JSON_VALUE)
	public @ResponseBody
	RecordProxy loadRecord(
			@PathVariable(value="survey_id") int surveyId, 
			@PathVariable(value="record_id") int recordId,
			@PathVariable(value="step") int stepNumber) throws RecordPersistenceException {
		CollectSurvey survey = surveyManager.getById(surveyId);
		SessionState sessionState = sessionManager.getSessionState();
		CollectRecord record = recordManager.checkout(survey, sessionState.getUser(), recordId, Step.valueOf(stepNumber), sessionState.getSessionId(), true);
		return new RecordProxy(record, sessionState.getLocale());
	}
	
	
	@RequestMapping(value = "/surveys/{survey_id}/records/create-random-record.json", method=GET, produces=APPLICATION_JSON_VALUE)
	public @ResponseBody
	RecordProxy createRandomRecord(@PathVariable(value="survey_id") int surveyId, @RequestParam int userID) throws RecordPersistenceException {
		CollectSurvey survey = surveyManager.getById(surveyId);
		Map<String, Integer> recordMeasurementsByKey = calculateRecordMeasurementsByKey(survey, userID);
		
		Integer minMeasurements = Collections.min(recordMeasurementsByKey.values());
		
		//do not consider measurements different from minimum measurement
		Iterator<Entry<String, Integer>> iterator = recordMeasurementsByKey.entrySet().iterator();
		while (iterator.hasNext()) {
			Entry<String, Integer> entry = iterator.next();
			if (entry.getValue() != minMeasurements) {
				iterator.remove();
			}
		}
		//randomly select one record key among the ones with minimum measurements
		ArrayList<String> recordKeys = new ArrayList<String>(recordMeasurementsByKey.keySet());
		int recordKeyIdx = new Double(Math.floor(Math.random() * recordKeys.size())).intValue();
		String recordKey = recordKeys.get(recordKeyIdx);
		
		SessionState sessionState = sessionManager.getSessionState();
		User user = sessionState.getUser();
		
		EntityDefinition rootEntityDef = survey.getSchema().getRootEntityDefinitions().get(0);
		String rootEntityName = rootEntityDef.getName();
		CollectRecord record = recordManager.create(survey, rootEntityName, user, null);
		
		Attribute<?,Value> keyAttribute = record.findNodeByPath(rootEntityDef.getPath());
		recordManager.updateAttribute(keyAttribute, keyAttribute.getDefinition().createValue(recordKey));
		
		return new RecordProxy(record, Locale.ENGLISH);
	}
	
	private Map<String, Integer> calculateRecordMeasurementsByKey(CollectSurvey survey, final int userID) {
		final Map<String, Integer> measurementsByRecordKey = new HashMap<String, Integer>();
		recordManager.visitSummaries(new RecordFilter(survey), null, new Visitor<CollectRecord>() {
			public void visit(CollectRecord summary) {
				if (summary.getCreatedBy().getId() != userID) {
					String key = summary.getRootEntityKeyValues().get(0);
					Integer measurements = measurementsByRecordKey.get(key);
					if (measurements == null) {
						measurements = 1;
					} else {
						measurements += 1;
					}
					measurementsByRecordKey.put(key, measurements);
				}
			}
		});
		
		Set<String> plannedRecordKeys = new HashSet<String>();
		SamplingDesignSummaries samplingPoints = samplingDesignManager.loadBySurvey(survey.getId(), 1);
		for (SamplingDesignItem item : samplingPoints.getRecords()) {
			String key = item.getLevelCode(1);
			Integer measurements = measurementsByRecordKey.get(key);
			if (measurements == null) {
				measurements = 0;
				plannedRecordKeys.add(key);
			}
		}
		return measurementsByRecordKey;
	}
}
