package org.openforis.collect.manager;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.CollectSurvey;

public class CachedRecordProvider implements RecordProvider {

	private RecordManager recordManager;
	private Map<CollectSurvey, RecordCache> recordsBySurvey = new HashMap<CollectSurvey, RecordCache>();
	
	public CachedRecordProvider(RecordManager recordManager) {
		super();
		this.recordManager = recordManager;
	}

	@Override
	public CollectRecord provide(CollectSurvey survey, int recordId) {
		RecordCache recordCache = recordsBySurvey.get(survey);
		if (recordCache == null) {
			recordCache = new RecordCache();
		}
		CollectRecord record = recordCache.get(recordId);
		if (record == null) {
			record = recordManager.load(survey, recordId);
			recordCache.put(recordId, record);
		}
		return record;
	}
	
	private static class RecordCache extends LinkedHashMap<Integer, CollectRecord> {

		private static final long serialVersionUID = 1L;

		private static final int MAX_ENTRIES = 50;

		@Override
		protected boolean removeEldestEntry(Entry<Integer, CollectRecord> eldest) {
			return size() > MAX_ENTRIES;
		}

	}

}
