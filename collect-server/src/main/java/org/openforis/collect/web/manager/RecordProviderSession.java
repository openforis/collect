package org.openforis.collect.web.manager;

import java.util.HashMap;
import java.util.Map;

import org.openforis.collect.manager.CachedRecordProvider;
import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.CollectRecord.Step;
import org.openforis.collect.model.CollectSurvey;

public class RecordProviderSession extends CachedRecordProvider {

	private CachedRecordProvider delegate;
	private Map<Integer, CollectRecord> recordsPreviewBySurvey = new HashMap<Integer, CollectRecord>();

	public RecordProviderSession(CachedRecordProvider cachedRecordProvider) {
		super(null);
		this.delegate = cachedRecordProvider;
	}

	@Override
	public CollectRecord provide(CollectSurvey survey, Integer recordId, Step recordStep) {
		if (recordId == null) {
			// Preview record
			return this.recordsPreviewBySurvey.get(survey.getId());
		} else {
			return delegate.provide(survey, recordId, recordStep);
		}
	}

	public void putRecord(CollectRecord record) {
		if (record.isPreview()) {
			this.recordsPreviewBySurvey.put(record.getSurvey().getId(), record);
		} else {
			delegate.putRecord(record);
		}
	}

	@Override
	public void clearRecords(int surveyId) {
		delegate.clearRecords(surveyId);
		this.recordsPreviewBySurvey.remove(surveyId);
	}

}
