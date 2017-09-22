package org.openforis.collect.manager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.CollectRecordSummary;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.RecordFilter;
import org.openforis.collect.model.SamplingDesignItem;
import org.openforis.collect.model.SamplingDesignSummaries;
import org.openforis.collect.model.User;
import org.openforis.commons.collection.Visitor;
import org.openforis.idm.metamodel.AttributeDefinition;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class RandomRecordGenerator extends RecordGenerator {
	
	@Transactional
	public CollectRecord generate(int surveyId, NewRecordParameters parameters) {
		CollectSurvey survey = surveyManager.getById(surveyId);
		User user = userManager.loadById(parameters.getUserId());

		List<String> recordKey = provideRandomRecordKey(survey, user, parameters.isOnlyUnanalyzedSamplingPoints());
		if (recordKey == null) {
			return null;
		}
		return this.generate(surveyId, parameters, recordKey);
	}

	private List<String> provideRandomRecordKey(CollectSurvey survey, User user, boolean onlyUnanalyzed) {
		Map<List<String>, Integer> recordMeasurementsByKey = calculateRecordMeasurementsByKey(survey, user);
		
		if (recordMeasurementsByKey.isEmpty()) {
			throw new IllegalStateException(String.format("Sampling design data not defined for survey %s", survey.getName()));
		}
		int minMeasurements = onlyUnanalyzed ? 0 : Collections.min(recordMeasurementsByKey.values());
		return provideRandomRecordKey(recordMeasurementsByKey, minMeasurements);
	}

	private List<String> provideRandomRecordKey(Map<List<String>, Integer> recordMeasurementsByKey, int measurements) {
		//do not consider measurements different from specified number of measurement
		Map<List<String>, Integer> filteredRecordMeasurementsByKey = new HashMap<List<String>, Integer>(recordMeasurementsByKey);
		Iterator<Entry<List<String>, Integer>> iterator = filteredRecordMeasurementsByKey.entrySet().iterator();
		while (iterator.hasNext()) {
			Entry<List<String>, Integer> entry = iterator.next();
			if (entry.getValue() != measurements) {
				iterator.remove();
			}
		}
		if (filteredRecordMeasurementsByKey.isEmpty()) {
			return null;
		}
		//randomly select one record key among the ones with minimum measurements
		List<List<String>> recordKeys = new ArrayList<List<String>>(filteredRecordMeasurementsByKey.keySet());
		int recordKeyIdx = new Double(Math.floor(Math.random() * recordKeys.size())).intValue();
		List<String> recordKey = recordKeys.get(recordKeyIdx);
		List<String> result = new ArrayList<String>(recordKey);
		result.add(String.valueOf(measurements + 1));
		return result;
	}

	private Map<List<String>, Integer> calculateRecordMeasurementsByKey(CollectSurvey survey, final User user) {
		final List<AttributeDefinition> nonMeasurementKeyDefs = getNonMeasurementKeyDefs(survey);
		final Map<List<String>, Integer> measurementsByRecordKey = new HashMap<List<String>, Integer>();
		recordManager.visitSummaries(new RecordFilter(survey), null, new Visitor<CollectRecordSummary>() {
			public void visit(CollectRecordSummary summary) {
				if (summary.getCreatedBy().getId() != user.getId()) {
					List<String> keys = summary.getCurrentStepSummary().getRootEntityKeyValues();
					List<String> nonMeasurementKeys;
					if (keys.size() > nonMeasurementKeyDefs.size()) {
						nonMeasurementKeys = keys.subList(0, nonMeasurementKeyDefs.size() - 1);
					} else {
						nonMeasurementKeys = keys;
					}
					Integer measurements = measurementsByRecordKey.get(nonMeasurementKeys);
					if (measurements == null) {
						measurements = 1;
					} else {
						measurements += 1;
					}
					measurementsByRecordKey.put(nonMeasurementKeys, measurements);
				}
			}
		});
		List<AttributeDefinition> nonMeasurementKeyAttrDefs = getNonMeasurementKeyDefs(survey);
		
		SamplingDesignSummaries samplingPoints = samplingDesignManager.loadBySurvey(survey.getId(), nonMeasurementKeyAttrDefs.size());
		for (SamplingDesignItem item : samplingPoints.getRecords()) {
			if (item.getLevelCodes().size() == nonMeasurementKeyAttrDefs.size()) {
				List<String> key = item.getLevelCodes().subList(0, nonMeasurementKeyAttrDefs.size());
				Integer measurements = measurementsByRecordKey.get(key);
				if (measurements == null) {
					measurementsByRecordKey.put(key, 0);
				}
			}
		}
		return measurementsByRecordKey;
	}

	
	
	
}
