package org.openforis.collect.manager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.openforis.collect.model.CollectRecordSummary;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.RecordFilter;
import org.openforis.commons.collection.CollectionUtils;
import org.openforis.commons.collection.Predicate;
import org.openforis.idm.metamodel.AttributeDefinition;
import org.springframework.beans.factory.annotation.Autowired;

public class RandomRecordKeysGenerator {

	@Autowired
	private transient RecordManager recordManager;

	private static <T> List<T> generateRandomSubset(List<T> collection, float percentageOverTotal) {
		List<T> result = new ArrayList<T>();
		int numberOfRecordsToGenerate = (int) Math.ceil(((collection.size() * percentageOverTotal) / 100));
		List<T> currentCollection = new ArrayList<T>(collection);
		for (int i = 0; i < numberOfRecordsToGenerate; i++) {
			int existingRecordIndex = (int) Math.round(Math.random() * currentCollection.size());
			T item = currentCollection.get(existingRecordIndex);
			result.add(item);
			currentCollection.remove(existingRecordIndex);
		}
		return result;
	}
	
	private static <T> List<T> transformList(List<T> list, Transformer<T> transformer) {
		List<T> result = new ArrayList<T>();
		for (T item : list) {
			T itemTransformed = transformer.transform(item);
			result.add(itemTransformed);
		}
		return result;
	}

	public List<List<String>> generate(CollectSurvey survey, String currentMeasurementValue, String nextMeasurementValue, float percentageOverTotal) {
		AttributeDefinition measurementKeyDef = survey.getFirstMeasurementKeyDef();
		if (measurementKeyDef == null) return Collections.emptyList();
		
		List<AttributeDefinition> keyDefs = survey.getSchema().getFirstRootEntityDefinition().getKeyAttributeDefinitions();
		int measurementKeyDefIndex = keyDefs.indexOf(measurementKeyDef);
		
		List<CollectRecordSummary> recordSummaries = recordManager.loadSummaries(new RecordFilter(survey));
		CollectionUtils.filter(recordSummaries, new Predicate<CollectRecordSummary>() {
			public boolean evaluate(CollectRecordSummary recordSummary) {
				List<String> keyValues = recordSummary.getRootEntityKeyValues();
				String measurementKeyValue = keyValues.size() > measurementKeyDefIndex ? keyValues.get(measurementKeyDefIndex) : null;
				return currentMeasurementValue.equals(measurementKeyValue);
			};
		});
		List<List<String>> recordsKeys = new ArrayList<List<String>>();
		for (CollectRecordSummary recordSummary : recordSummaries) {
			recordsKeys.add(recordSummary.getRootEntityKeyValues());
		}
		List<List<String>> randomRecordsKeys = generateRandomSubset(recordsKeys, percentageOverTotal);
		List<List<String>> transformedRandomRecordsKeys = transformList(randomRecordsKeys, new Transformer<List<String>>() {
			public List<String> transform(List<String> keyValues) {
				List<String> newKeyValues = new ArrayList<String>(keyValues);
				newKeyValues.set(measurementKeyDefIndex, nextMeasurementValue);
				return newKeyValues;
			}
		});
		return transformedRandomRecordsKeys;
	}
	
	private interface Transformer<T> {
		T transform(T item);
	}
	
}
