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
		User user = loadUser(parameters.getUserId(), parameters.getUsername());

		RecordKey recordKey = provideRandomRecordKey(survey, user, parameters.isOnlyUnanalyzedSamplingPoints());
		if (recordKey == null) {
			return null;
		}
		return this.generate(survey, parameters, recordKey);
	}

	private RecordKey provideRandomRecordKey(CollectSurvey survey, User user, boolean onlyUnanalyzed) {
		Map<RecordKey, Integer> recordMeasurementsByKey = calculateRecordMeasurementsByKey(survey, user);
		
		if (recordMeasurementsByKey.isEmpty()) {
			throw new IllegalStateException(String.format("Sampling design data not defined for survey %s", survey.getName()));
		}
		int minMeasurements = onlyUnanalyzed ? 0 : Collections.min(recordMeasurementsByKey.values());
		return provideRandomRecordKey(survey, recordMeasurementsByKey, minMeasurements);
	}

	private RecordKey provideRandomRecordKey(CollectSurvey survey, Map<RecordKey, Integer> recordMeasurementsByKey, int measurements) {
		//do not consider measurements different from specified number of measurement
		Map<RecordKey, Integer> filteredRecordMeasurementsByKey = new HashMap<RecordKey, Integer>(recordMeasurementsByKey);
		Iterator<Entry<RecordKey, Integer>> iterator = filteredRecordMeasurementsByKey.entrySet().iterator();
		while (iterator.hasNext()) {
			Entry<RecordKey, Integer> entry = iterator.next();
			if (entry.getValue() != measurements) {
				iterator.remove();
			}
		}
		if (filteredRecordMeasurementsByKey.isEmpty()) {
			return null;
		}
		//randomly select one record key among the ones with minimum measurements
		List<RecordKey> recordKeys = new ArrayList<RecordKey>(filteredRecordMeasurementsByKey.keySet());
		int recordKeyIdx = Double.valueOf(Math.floor(Math.random() * recordKeys.size())).intValue();
		RecordKey randomRecordKey = recordKeys.get(recordKeyIdx);
		List<AttributeDefinition> measurementKeyDefs = getMeasurementKeyDefs(survey);
		String measurementAttrPath = measurementKeyDefs.get(0).getPath();
		randomRecordKey.putValue(measurementAttrPath, String.valueOf(measurements + 1));
		return randomRecordKey;
	}

	private Map<RecordKey, Integer> calculateRecordMeasurementsByKey(CollectSurvey survey, final User user) {
		final List<AttributeDefinition> nonMeasurementKeyDefs = getNonMeasurementKeyDefs(survey);
		final Map<RecordKey, Integer> measurementsByRecordKey = new HashMap<RecordKey, Integer>();
		recordManager.visitSummaries(new RecordFilter(survey), null, new Visitor<CollectRecordSummary>() {
			public void visit(CollectRecordSummary summary) {
				if (summary.getCreatedBy().getId() != user.getId()) {
					List<String> keys = summary.getCurrentStepSummary().getRootEntityKeyValues();
					RecordKey nonMeasurementKey = new RecordKey(nonMeasurementKeyDefs, keys);
					Integer measurements = measurementsByRecordKey.get(nonMeasurementKey);
					if (measurements == null) {
						measurements = 1;
					} else {
						measurements += 1;
					}
					measurementsByRecordKey.put(nonMeasurementKey, measurements);
				}
			}
		});
		samplingDesignManager.visitItems(survey.getId(), 1, new Visitor<SamplingDesignItem>() {
			public void visit(SamplingDesignItem item) {
				RecordKey key = new RecordKey(nonMeasurementKeyDefs, item.getLevelCodes());
				Integer measurements = measurementsByRecordKey.get(key);
				if (measurements == null) {
					measurementsByRecordKey.put(key, 0);
				}
			}
		});
		return measurementsByRecordKey;
	}

}
