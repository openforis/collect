package org.openforis.collect.manager;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.CollectRecord.Step;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.idm.metamodel.Survey;

public class CachedRecordProvider implements RecordProvider {

	private RecordManager recordManager;
	private Map<CollectSurvey, RecordCache> recordsBySurvey = new HashMap<CollectSurvey, RecordCache>();
	
	public CachedRecordProvider(RecordManager recordManager) {
		super();
		this.recordManager = recordManager;
	}

	@Override
	public CollectRecord provide(CollectSurvey survey, Integer recordId, Step recordStep) {
		RecordCache recordCache = getRecordCache(survey);
		RecordCacheKey key = new RecordCacheKey(recordId, recordStep);
		CollectRecord record = recordCache.get(key);
		if (record == null) {
			record = recordManager.load(survey, recordId, recordStep);
			recordCache.put(key, record);
		}
		return record;
	}

	public void putRecord(CollectRecord record) {
		RecordCacheKey key = new RecordCacheKey(record.getId(), record.getDataStep());
		RecordCache recordCache = getRecordCache(record.getSurvey());
		recordCache.put(key, record);
	}
	
	private RecordCache getRecordCache(Survey survey) {
		RecordCache recordCache = recordsBySurvey.get(survey);
		if (recordCache == null) {
			recordCache = new RecordCache();
		}
		return recordCache;
	}
	
	private static class RecordCache extends LinkedHashMap<RecordCacheKey, CollectRecord> {

		private static final long serialVersionUID = 1L;

		private static final int MAX_ENTRIES = 50;

		@Override
		protected boolean removeEldestEntry(Entry<RecordCacheKey, CollectRecord> eldest) {
			return size() > MAX_ENTRIES;
		}

	}
	
	private static class RecordCacheKey {
		private Integer id;
		private Step step;
		
		public RecordCacheKey(Integer id, Step step) {
			super();
			this.id = id;
			this.step = step;
		}
		
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((id == null) ? 0 : id.hashCode());
			result = prime * result + ((step == null) ? 0 : step.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			RecordCacheKey other = (RecordCacheKey) obj;
			if (id == null) {
				if (other.id != null)
					return false;
			} else if (!id.equals(other.id))
				return false;
			if (step != other.step)
				return false;
			return true;
		}
		
	}

}
