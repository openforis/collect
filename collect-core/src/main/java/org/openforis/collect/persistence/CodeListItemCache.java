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
	
	private Map<Integer, Map<Integer, Map<CodeListItemCache.CodeListCacheItemKey, PersistedCodeListItem>>> itemsBySurveyAndCodeList;
	private Map<Integer, Map<Integer, Map<CodeListItemCache.CodeListCacheItemKey, PersistedCodeListItem>>> itemsBySurveyWorkAndCodeList;
	
	public CodeListItemCache() {
		itemsBySurveyAndCodeList = new HashMap<Integer, Map<Integer,Map<CodeListItemCache.CodeListCacheItemKey, PersistedCodeListItem>>>();
		itemsBySurveyWorkAndCodeList = new HashMap<Integer, Map<Integer, Map<CodeListItemCache.CodeListCacheItemKey, PersistedCodeListItem>>>();
	}
	
	public synchronized PersistedCodeListItem getItem(int surveyId, boolean surveyWork, int listId, Integer parentItemId, String code) {
		CodeListItemCache.CodeListCacheItemKey key = new CodeListCacheItemKey(parentItemId, code);
		Map<CodeListItemCache.CodeListCacheItemKey, PersistedCodeListItem> items = getItems(surveyId, surveyWork, listId, false);
		if ( items == null ) {
			return null;
		} else {
			PersistedCodeListItem item = items.get(key);
			return item;
		}
	}

	private Map<CodeListItemCache.CodeListCacheItemKey, PersistedCodeListItem> getItems(int surveyId, boolean surveyWork, int listId, boolean createIfNull) {
		Map<Integer, Map<CodeListItemCache.CodeListCacheItemKey, PersistedCodeListItem>> itemsByCodeList = getItemsByCodeList(surveyId, surveyWork, createIfNull);
		if ( itemsByCodeList == null ) {
			return null;
		} else {
			Map<CodeListItemCache.CodeListCacheItemKey, PersistedCodeListItem> items = itemsByCodeList.get(listId);
			if ( items == null && createIfNull ) {
				items = new HashMap<CodeListItemCache.CodeListCacheItemKey, PersistedCodeListItem>();
				itemsByCodeList.put(listId, items);
			}
			return items;
		}
	}

	protected Map<Integer, Map<CodeListItemCache.CodeListCacheItemKey, PersistedCodeListItem>> getItemsByCodeList(int surveyId, boolean surveyWork, boolean createIfNull) {
		Map<Integer, Map<CodeListItemCache.CodeListCacheItemKey, PersistedCodeListItem>> itemsByCodeList = 
				surveyWork ? itemsBySurveyWorkAndCodeList.get(surveyId) : itemsBySurveyAndCodeList.get(surveyId);
		if ( itemsByCodeList == null && createIfNull ) {
			itemsByCodeList = new HashMap<Integer, Map<CodeListItemCache.CodeListCacheItemKey,PersistedCodeListItem>>();
			Map<Integer, Map<Integer, Map<CodeListItemCache.CodeListCacheItemKey, PersistedCodeListItem>>> itemsBySurvey = 
					surveyWork ? itemsBySurveyWorkAndCodeList: itemsBySurveyAndCodeList;
			itemsBySurvey.put(surveyId, itemsByCodeList);
		}
		return itemsByCodeList;
	}
	
	public synchronized void addItem(CodeList list, Integer parentItemId, String code, PersistedCodeListItem item) {
		CollectSurvey survey = (CollectSurvey) list.getSurvey();
		Integer surveyId = survey.getId();
		boolean surveyWork = survey.isWork();
		Map<CodeListItemCache.CodeListCacheItemKey, PersistedCodeListItem> items = getItems(surveyId, surveyWork, list.getId(), true);
		if ( countItemsPerSurvey(surveyId, surveyWork) > MAX_ITEMS_PER_SURVEY ) {
			//remove first item
			removeFirstItemInCache(surveyId, surveyWork);
		} else {
			CodeListItemCache.CodeListCacheItemKey key = new CodeListCacheItemKey(parentItemId, code);
			items.put(key, item);
		}
	}
	
	private void removeFirstItemInCache(Integer surveyId, boolean surveyWork) {
		Map<Integer, Map<CodeListItemCache.CodeListCacheItemKey, PersistedCodeListItem>> itemsPerCodeList = getItemsByCodeList(surveyId, surveyWork, false);
		Collection<Map<CodeListItemCache.CodeListCacheItemKey,PersistedCodeListItem>> values = itemsPerCodeList.values();
		for (Map<CodeListItemCache.CodeListCacheItemKey, PersistedCodeListItem> items : values) {
			if ( ! items.isEmpty() ) {
				Iterator<Entry<CodeListItemCache.CodeListCacheItemKey, PersistedCodeListItem>> it = items.entrySet().iterator();
				it.next();
				it.remove();
			}
		}
	}

	private int countItemsPerSurvey(Integer surveyId, boolean surveyWork) {
		int count = 0;
		Map<Integer, Map<CodeListItemCache.CodeListCacheItemKey, PersistedCodeListItem>> itemsPerCodeList = getItemsByCodeList(surveyId, surveyWork, false);
		if ( itemsPerCodeList != null ) {
			for (Map<CodeListItemCache.CodeListCacheItemKey, PersistedCodeListItem> itemsByKey : itemsPerCodeList.values()) {
				count += itemsByKey.size();
			}
		}
		return count;
	}

	public synchronized void removeItemsBySurvey(int surveyId, boolean surveyWork) {
		Map<Integer, Map<Integer, Map<CodeListItemCache.CodeListCacheItemKey, PersistedCodeListItem>>> itemsBySurvey = surveyWork ? itemsBySurveyWorkAndCodeList: itemsBySurveyAndCodeList;
		if ( itemsBySurvey != null ) {
			Map<Integer, Map<CodeListItemCache.CodeListCacheItemKey, PersistedCodeListItem>> items = itemsBySurvey.get(surveyId);
			if ( items != null ) {
				items.clear();
				itemsBySurvey.remove(surveyId);
			}
		}
	}

	public synchronized void removeItemsByCodeList(int surveyId, boolean surveyWork, int codeListId) {
		Map<Integer, Map<CodeListItemCache.CodeListCacheItemKey, PersistedCodeListItem>> itemsPerCodeList = getItemsByCodeList(surveyId, surveyWork, false);
		if ( itemsPerCodeList != null ) {
			Map<CodeListItemCache.CodeListCacheItemKey, PersistedCodeListItem> items = itemsPerCodeList.get(codeListId);
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
			CodeListItemCache.CodeListCacheItemKey other = (CodeListItemCache.CodeListCacheItemKey) obj;
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