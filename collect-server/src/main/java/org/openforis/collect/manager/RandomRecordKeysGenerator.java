package org.openforis.collect.manager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.openforis.collect.metamodel.CollectAnnotations;
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

	private AttributeDefinition getFirstMeasurementKeyDef(CollectSurvey survey) {
		CollectAnnotations annotations = new CollectAnnotations(survey);
		List<AttributeDefinition> keyDefs = survey.getSchema().getFirstRootEntityDefinition()
				.getKeyAttributeDefinitions();
		List<AttributeDefinition> measurementKeyDefs = new ArrayList<AttributeDefinition>(keyDefs);
		CollectionUtils.filter(measurementKeyDefs, new Predicate<AttributeDefinition>() {
			public boolean evaluate(AttributeDefinition keyDef) {
				return annotations.isMeasurementAttribute(keyDef);
			}
		});
		return measurementKeyDefs.isEmpty() ? null : measurementKeyDefs.get(0);
	}

	public List<List<String>> generate(CollectSurvey survey, String currentMeasurementValue, String nextMeasurementValue, float percentage) {
		AttributeDefinition measurementKeyDef = getFirstMeasurementKeyDef(survey);
		if (measurementKeyDef == null) return Collections.emptyList();
		
		List<List<String>> result = new ArrayList<List<String>>();
		
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
		
		int numberOfRecordsToGenerate = (int) Math.ceil(((recordSummaries.size() * percentage) / 100));
		for (int i = 0; i < numberOfRecordsToGenerate; i++) {
			int existingRecordIndex = (int) Math.round(Math.random() * recordSummaries.size());
			CollectRecordSummary existingRecordSummary = recordSummaries.get(existingRecordIndex);
			List<String> keyValues = existingRecordSummary.getRootEntityKeyValues();
			List<String> newKeyValues = new ArrayList<String>(keyValues);
			newKeyValues.set(measurementKeyDefIndex, nextMeasurementValue);
			result.add(newKeyValues);
			recordSummaries.remove(existingRecordIndex);
		}
		return result;
	}
}
