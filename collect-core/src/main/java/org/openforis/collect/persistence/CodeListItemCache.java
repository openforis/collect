package org.openforis.collect.persistence;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.openforis.collect.model.CollectSurvey;
import org.openforis.idm.metamodel.CodeList;
import org.openforis.idm.metamodel.PersistedCodeListItem;

/**
 * 
 * @author S. Ricci
 *
 */
public class CodeListItemCache {
	
	private static final int MAX_ITEMS_PER_SURVEY = 10000;
	
	private Map<Integer, Map<Integer, Map<CodeListCacheItemKey, PersistedCodeListItem>>> itemsBySurveyAndCodeList;
	
	public CodeListItemCache() {
		itemsBySurveyAndCodeList = new HashMap<Integer, Map<Integer,Map<CodeListCacheItemKey, PersistedCodeListItem>>>();
	}
	
	public synchronized PersistedCodeListItem getItem(int surveyId, int listId, Integer parentItemId, String code) {
		CodeListCacheItemKey key = new CodeListCacheItemKey(parentItemId, code);
		Map<CodeListCacheItemKey, PersistedCodeListItem> items = getItems(surveyId, listId, false);
		if ( items == null ) {
			return null;
		} else {
			PersistedCodeListItem item = items.get(key);
			return item;
		}
	}

	private Map<CodeListCacheItemKey, PersistedCodeListItem> getItems(int surveyId, int listId, boolean createIfNull) {
		Map<Integer, Map<CodeListCacheItemKey, PersistedCodeListItem>> itemsByCodeList = getItemsByCodeList(surveyId, createIfNull);
		if ( itemsByCodeList == null ) {
			return null;
		} else {
			Map<CodeListCacheItemKey, PersistedCodeListItem> items = itemsByCodeList.get(listId);
			if ( items == null && createIfNull ) {
				items = new HashMap<CodeListCacheItemKey, PersistedCodeListItem>();
				itemsByCodeList.put(listId, items);
			}
			return items;
		}
	}

	protected Map<Integer, Map<CodeListCacheItemKey, PersistedCodeListItem>> getItemsByCodeList(int surveyId, boolean createIfNull) {
		Map<Integer, Map<CodeListCacheItemKey, PersistedCodeListItem>> itemsByCodeList = itemsBySurveyAndCodeList.get(surveyId);
		if ( itemsByCodeList == null && createIfNull ) {
			itemsByCodeList = new HashMap<Integer, Map<CodeListCacheItemKey, PersistedCodeListItem>>();
			Map<Integer, Map<Integer, Map<CodeListCacheItemKey, PersistedCodeListItem>>> itemsBySurvey = itemsBySurveyAndCodeList;
			itemsBySurvey.put(surveyId, itemsByCodeList);
		}
		return itemsByCodeList;
	}
	
	public synchronized void addItem(CodeList list, Integer parentItemId, String code, PersistedCodeListItem item) {
		CollectSurvey survey = (CollectSurvey) list.getSurvey();
		Integer surveyId = survey.getId();
		Map<CodeListCacheItemKey, PersistedCodeListItem> items = getItems(surveyId, list.getId(), true);
		if ( countItemsPerSurvey(surveyId) > MAX_ITEMS_PER_SURVEY ) {
			//remove first item
			removeFirstItemInCache(surveyId);
		} else {
			CodeListCacheItemKey key = new CodeListCacheItemKey(parentItemId, code);
			items.put(key, item);
		}
	}
	
	private void removeFirstItemInCache(Integer surveyId) {
		Map<Integer, Map<CodeListCacheItemKey, PersistedCodeListItem>> itemsPerCodeList = getItemsByCodeList(surveyId, false);
		Collection<Map<CodeListCacheItemKey,PersistedCodeListItem>> values = itemsPerCodeList.values();
		for (Map<CodeListCacheItemKey, PersistedCodeListItem> items : values) {
			if ( ! items.isEmpty() ) {
				Iterator<Entry<CodeListCacheItemKey, PersistedCodeListItem>> it = items.entrySet().iterator();
				it.next();
				it.remove();
			}
		}
	}

	private int countItemsPerSurvey(Integer surveyId) {
		int count = 0;
		Map<Integer, Map<CodeListCacheItemKey, PersistedCodeListItem>> itemsPerCodeList = getItemsByCodeList(surveyId, false);
		if ( itemsPerCodeList != null ) {
			for (Map<CodeListCacheItemKey, PersistedCodeListItem> itemsByKey : itemsPerCodeList.values()) {
				count += itemsByKey.size();
			}
		}
		return count;
	}

	public synchronized void removeItemsBySurvey(int surveyId) {
		Map<Integer, Map<Integer, Map<CodeListCacheItemKey, PersistedCodeListItem>>> itemsBySurvey = itemsBySurveyAndCodeList;
		if ( itemsBySurvey != null ) {
			Map<Integer, Map<CodeListCacheItemKey, PersistedCodeListItem>> items = itemsBySurvey.get(surveyId);
			if ( items != null ) {
				items.clear();
				itemsBySurvey.remove(surveyId);
			}
		}
	}

	public synchronized void removeItemsByCodeList(int surveyId, int codeListId) {
		Map<Integer, Map<CodeListCacheItemKey, PersistedCodeListItem>> itemsPerCodeList = getItemsByCodeList(surveyId, false);
		if ( itemsPerCodeList != null ) {
			Map<CodeListCacheItemKey, PersistedCodeListItem> items = itemsPerCodeList.get(codeListId);
			if ( items != null ) {
				items.clear();
				itemsPerCodeList.remove(codeListId);
			}
		}
	}
	
	private static class CodeListCacheItemKey {
		
		private Integer parentItemId;
		private String code;
		
		public CodeListCacheItemKey(Integer parentItemId, String code) {
			super();
			this.parentItemId = parentItemId;
			this.code = code;
		}
		
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((code == null) ? 0 : code.hashCode());
			result = prime * result
					+ ((parentItemId == null) ? 0 : parentItemId.hashCode());
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
			CodeListCacheItemKey other = (CodeListCacheItemKey) obj;
			if (code == null) {
				if (other.code != null)
					return false;
			} else if (!code.equals(other.code))
				return false;
			if (parentItemId == null) {
				if (other.parentItemId != null)
					return false;
			} else if (!parentItemId.equals(other.parentItemId))
				return false;
			return true;
		}
		
	}
	
}