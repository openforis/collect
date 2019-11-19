package org.openforis.collect.persistence;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openforis.idm.metamodel.CodeList;
import org.openforis.idm.metamodel.ModelVersion;
import org.openforis.idm.metamodel.PersistedCodeListItem;

/**
 * 
 * @author S. Ricci
 *
 */
public class CodeListItemCache {
	
//	private static final int MAX_ITEMS_PER_SURVEY = 10000;
	
	private Map<Integer, SurveyCodeListItemCache> cacheBySurveyId = new HashMap<Integer, CodeListItemCache.SurveyCodeListItemCache>();
	
	public PersistedCodeListItem getItem(CodeList codeList, Long parentId, String code, ModelVersion version) {
		List<PersistedCodeListItem> items = getItems(codeList, parentId);
		if (items == null) {
			return null;
		}
		for (PersistedCodeListItem item : items) {
			if (item.getCode().equals(code) && (version == null || version.isApplicable(item))) {
				return item;
			}
		}
		return null;
	}
	
	public List<PersistedCodeListItem> getItems(CodeList codeList, Long parentId) {
		int surveyId = codeList.getSurvey().getId();
		SurveyCodeListItemCache surveyCache = getSurveyCache(surveyId);
		if (surveyCache == null) {
			return null;
		}
		InternalCodeListItemCache codeListCache = surveyCache.getCodeListCache(codeList.getId());
		if (codeListCache == null) {
			return null;
		}
		List<PersistedCodeListItem> items = codeListCache.getItemsByParentId(parentId);
		return items;
	}

	public void putItems(CodeList codeList, Long parentId, List<PersistedCodeListItem> items) {
		int surveyId = codeList.getSurvey().getId();
		SurveyCodeListItemCache surveyCache = getOrCreateSurveyCache(surveyId);
		InternalCodeListItemCache codeListCache = surveyCache.getOrCreateCodeListCache(codeList.getId());
		codeListCache.putItems(parentId, items);
	}
	
	public void clear() {
		cacheBySurveyId.clear();
	}
	
	public void clearItems(CodeList codeList, Long parentId) {
		int surveyId = codeList.getSurvey().getId();
		SurveyCodeListItemCache surveyCache = getOrCreateSurveyCache(surveyId);
		InternalCodeListItemCache codeListCache = surveyCache.getOrCreateCodeListCache(codeList.getId());
		codeListCache.clearItems(parentId);
	}

	public void clearItemsBySurvey(int surveyId) {
		cacheBySurveyId.remove(surveyId);
	}
	
	public void clearItemsByCodeList(CodeList codeList) {
		Integer surveyId = codeList.getSurvey().getId();
		SurveyCodeListItemCache surveyCache = getSurveyCache(surveyId);
		if (surveyCache != null) {
			surveyCache.removeCodeListCache(codeList.getId());
		}
	}
	
	private SurveyCodeListItemCache getOrCreateSurveyCache(int surveyId) {
		SurveyCodeListItemCache surveyCache = getSurveyCache(surveyId);
		if (surveyCache == null) {
			surveyCache = new SurveyCodeListItemCache();
			cacheBySurveyId.put(surveyId, surveyCache);
		}
		return surveyCache;
	}

	private SurveyCodeListItemCache getSurveyCache(int surveyId) {
		return cacheBySurveyId.get(surveyId);
	}
	
	private static class SurveyCodeListItemCache {
		
		private Map<Integer, InternalCodeListItemCache> cacheByCodeListId = new HashMap<Integer, CodeListItemCache.InternalCodeListItemCache>();

		public InternalCodeListItemCache getCodeListCache(int codeListId) {
			return cacheByCodeListId.get(codeListId);
		}
		
		public InternalCodeListItemCache getOrCreateCodeListCache(int codeListId) {
			InternalCodeListItemCache cache = getCodeListCache(codeListId);
			if (cache == null) {
				cache = new InternalCodeListItemCache();
				cacheByCodeListId.put(codeListId, cache);
			}
			return cache;
		}
		
		public void removeCodeListCache(int codeListId) {
			cacheByCodeListId.remove(codeListId);
		}
		
	}
	
	private static class InternalCodeListItemCache {
		
		private Map<Long, List<PersistedCodeListItem>> itemsByParentId = new HashMap<Long, List<PersistedCodeListItem>>();
		
		public List<PersistedCodeListItem> getItemsByParentId(Long parentId) {
			return itemsByParentId.get(createMapKeyByParentId(parentId));
		}
		
		public void putItems(Long parentId, List<PersistedCodeListItem> items) {
			itemsByParentId.put(createMapKeyByParentId(parentId), items);
		}

		public void clearItems(Long parentId) {
			itemsByParentId.remove(parentId);
		}
		
		private Long createMapKeyByParentId(Long parentId) {
			return parentId == null ? 0 : parentId;
		}
		
	}

}