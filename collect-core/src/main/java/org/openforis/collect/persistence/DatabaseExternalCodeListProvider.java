/**
 * 
 */
package org.openforis.collect.persistence;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.openforis.collect.manager.SurveyManager;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.StringKeyValuePair;
import org.openforis.idm.metamodel.CodeAttributeDefinition;
import org.openforis.idm.metamodel.CodeList;
import org.openforis.idm.metamodel.CodeListLevel;
import org.openforis.idm.metamodel.ExternalCodeListItem;
import org.openforis.idm.metamodel.ExternalCodeListProvider;
import org.openforis.idm.metamodel.Survey;
import org.openforis.idm.model.Code;
import org.openforis.idm.model.CodeAttribute;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author M. Togna
 * @author S. Ricci
 * 
 */
public class DatabaseExternalCodeListProvider implements
		ExternalCodeListProvider {

	private static final String ID_COLUMN_NAME = "id";
	private static final String DEFAULT_CODE_COLUMN_NAME = "code";
	private static final String LABEL_COLUMN_PREFIX = "label";
	
	private static final String SURVEY_ID_FIELD = "survey_id";
	private static final String SURVEY_WORK_ID_FIELD = "survey_work_id";

	@Autowired
	private DynamicTableDao dynamicTableDao;
	@Autowired
	private SurveyManager surveyManager;
	private boolean active;

	@Override
	public boolean isActive() {
		return active;
	}
	
	@Override
	public void setActive(boolean active) {
		this.active = active;
	}
	
	@Override
	@Deprecated
	public String getCode(CodeList list, String attribute, Object... keys) {
		StringKeyValuePair[] filters = StringKeyValuePair.fromKeyValuePairs(keys);
		addSurveyFilter(list, filters);
		String listName = list.getLookupTable();
		Object object = dynamicTableDao.loadValue(listName, attribute, filters);
		if (object == null) {
			return null;
		} else {
			return object.toString();
		}
	}
	
	@Override
	public ExternalCodeListItem getItem(CodeAttribute attribute) {
		CodeAttributeDefinition defn = attribute.getDefinition();
		CodeList list = defn.getList();
		
		List<StringKeyValuePair> filters = new ArrayList<StringKeyValuePair>();
		addSurveyFilter(list, filters);
		CodeAttribute codeParent = attribute.getCodeParent();
		while (codeParent != null) {
			String colName = getLevelKeyColumnName(codeParent);
			String codeValue = getCodeValue(codeParent);
			filters.add(new StringKeyValuePair(colName, codeValue));
			codeParent = codeParent.getCodeParent();
		}
		String colName = getLevelKeyColumnName(attribute);
		String codeValue = getCodeValue(attribute);
		filters.add(new StringKeyValuePair(colName, codeValue));
		int level = defn.getLevelPosition();
		List<StringKeyValuePair> emptyNextLevelsFilters = createEmptyNextLevelFilters(list, level);
		filters.addAll(emptyNextLevelsFilters);
		Map<String, String> row = dynamicTableDao.loadRow(list.getLookupTable(), filters.toArray(new StringKeyValuePair[0]));
		if ( row == null ) {
			return null;
		} else {
			ExternalCodeListItem result = parseRow(row, list, level);
			return result;
		}
	}

	public ExternalCodeListItem getParentItem(ExternalCodeListItem item) {
		List<StringKeyValuePair> filters = new ArrayList<StringKeyValuePair>();
		CodeList list = item.getCodeList();
		Collection<StringKeyValuePair> parentKeys = getParentKeys(item);
		filters.addAll(parentKeys);
		addSurveyFilter(list, filters);
		int level = parentKeys.size() + 1;
		int parentLevel = level - 1;
		List<StringKeyValuePair> emptyNextLevelsFilters = createEmptyNextLevelFilters(list, parentLevel);
		filters.addAll(emptyNextLevelsFilters);
		Map<String, String> row = dynamicTableDao.loadRow(list.getLookupTable(), filters.toArray(new StringKeyValuePair[0]));
		return parseRow(row, list, parentLevel);
	}

	public List<ExternalCodeListItem> getRootItems(CodeList list) {
		List<StringKeyValuePair> filters = new ArrayList<StringKeyValuePair>();
		addSurveyFilter(list, filters);
		List<StringKeyValuePair> emptyNextLevelsFilters = createEmptyNextLevelFilters(list, 1);
		filters.addAll(emptyNextLevelsFilters);
		List<Map<String, String>> rows = dynamicTableDao.loadRows(list.getLookupTable(), 
				filters.toArray(new StringKeyValuePair[0]));
		List<ExternalCodeListItem> result = new ArrayList<ExternalCodeListItem>();
		for (Map<String, String> row : rows) {
			ExternalCodeListItem item = parseRow(row, list, 1);
			result.add(item);
		}
		return result;
	}

	public ExternalCodeListItem getRootItem(CodeList list, String code) {
		List<StringKeyValuePair> filters = new ArrayList<StringKeyValuePair>();
		addSurveyFilter(list, filters);
		List<StringKeyValuePair> emptyNextLevelsFilters = createEmptyNextLevelFilters(list, 1);
		filters.addAll(emptyNextLevelsFilters);
		String firstLevelKeyColName = getLevelKeyColumnName(list, 1);
		StringKeyValuePair itemFilter = new StringKeyValuePair(firstLevelKeyColName, code);
		filters.add(itemFilter);
		Map<String, String> row = dynamicTableDao.loadRow(list.getLookupTable(), filters.toArray(new StringKeyValuePair[0]));
		return parseRow(row, list, 1);
	}
	
	public List<ExternalCodeListItem> getChildItems(ExternalCodeListItem item) {
		CodeList list = item.getCodeList();
		List<StringKeyValuePair> filters = createChildItemsFilters(item);
		int itemLevel = item.getLevel();
		int childrenLevel = itemLevel + 1;
		String childrenKeyColName = getLevelKeyColumnName(list, childrenLevel);
		String[] notNullColumns = new String[]{childrenKeyColName};
		List<Map<String, String>> rows = dynamicTableDao.loadRows(list.getLookupTable(), 
				filters.toArray(new StringKeyValuePair[0]),
				notNullColumns);
		List<ExternalCodeListItem> result = new ArrayList<ExternalCodeListItem>();
		for (Map<String, String> row : rows) {
			ExternalCodeListItem child = parseRow(row, list, childrenLevel);
			result.add(child);
		}
		return result;
	}
	
	public ExternalCodeListItem getChildItem(ExternalCodeListItem item,
			String code) {
		CodeList list = item.getCodeList();
		List<StringKeyValuePair> filters = createChildItemsFilters(item);
		int itemLevel = item.getLevel();
		int childrenLevel = itemLevel + 1;
		String childrenLevelColName = getLevelKeyColumnName(list, childrenLevel);
		StringKeyValuePair codeFilter = new StringKeyValuePair(childrenLevelColName, code);
		filters.add(codeFilter);
		Map<String, String> row = dynamicTableDao.loadRow(list.getLookupTable(), 
				filters.toArray(new StringKeyValuePair[0]));
		return parseRow(row, list, childrenLevel);
	}
	
	protected List<StringKeyValuePair> createChildItemsFilters(ExternalCodeListItem item) {
		List<StringKeyValuePair> filters = new ArrayList<StringKeyValuePair>();
		CodeList list = item.getCodeList();
		Collection<StringKeyValuePair> parentKeys = getParentKeys(item);
		filters.addAll(parentKeys);
		addSurveyFilter(list, filters);
		int itemLevel = item.getLevel();
		int childrenLevel = itemLevel + 1;
		String itemKeyColName = getLevelKeyColumnName(list, itemLevel);
		filters.add(new StringKeyValuePair(itemKeyColName, item.getCode()));
		List<StringKeyValuePair> emptyNextLevelsFilters = createEmptyNextLevelFilters(list, childrenLevel);
		filters.addAll(emptyNextLevelsFilters);
		return filters;
	}
	
	protected ExternalCodeListItem parseRow(Map<String, String> row, CodeList list, int level) {
		if ( row == null ) {
			return null;
		}
		String idValue = row.get(ID_COLUMN_NAME);
		Integer id = Integer.valueOf(idValue);
		
		Map<String, String> parentKeysByLevel = createParentKeyByLevelMap(list, row, level);
		ExternalCodeListItem item = new ExternalCodeListItem(list, id, parentKeysByLevel);

		String currentLevelKeyColName = getLevelKeyColumnName(list, level);
		String code = row.get(currentLevelKeyColName);
		item.setCode(code);
		
		Survey survey = list.getSurvey();
		List<String> languages = survey.getLanguages();
		for (int i = 0; i < languages.size(); i++) {
			String langCode = languages.get(i);
			String colName = LABEL_COLUMN_PREFIX + (i+1);
			String label = row.get(colName);
			if ( label != null ) {
				item.setLabel(langCode, label);
			}
		}
		return item;
	}

	protected StringKeyValuePair createSurveyFilter(CodeList list) {
		CollectSurvey survey = (CollectSurvey) list.getSurvey();
		Integer surveyId = survey.getId();
		boolean surveyWork = surveyManager.isSurveyWork(survey);
		if ( surveyId != null ) {
			String surveyIdFieldName = surveyWork ? SURVEY_WORK_ID_FIELD : SURVEY_ID_FIELD;
			StringKeyValuePair keyValue = new StringKeyValuePair(surveyIdFieldName, surveyId.toString());
			return keyValue;
		} else {
			return null;
		}
	}

	protected StringKeyValuePair[] addSurveyFilter(CodeList list,
			StringKeyValuePair... filters) {
		StringKeyValuePair surveyFilter = createSurveyFilter(list);
		StringKeyValuePair[] newFilters = Arrays.copyOf(filters, filters.length + 1);
		newFilters[filters.length] = surveyFilter;
		return newFilters;
	}
	
	protected void addSurveyFilter(CodeList list,
			List<StringKeyValuePair> filters) {
		StringKeyValuePair surveyFilter = createSurveyFilter(list);
		filters.add(surveyFilter);
	}
	
	protected List<StringKeyValuePair> createEmptyNextLevelFilters(CodeList list, int level) {
		List<StringKeyValuePair> result = new ArrayList<StringKeyValuePair>();
		List<CodeListLevel> hierarchy = list.getHierarchy();
		for(int i = level; i < hierarchy.size(); i++){
			CodeListLevel codeListLevel = hierarchy.get(i);
			String name = codeListLevel.getName();
			result.add(new StringKeyValuePair(name, ""));
		}
		return result;
	}
	
	protected Collection<StringKeyValuePair> getParentKeys(ExternalCodeListItem item) {
		Set<StringKeyValuePair> result = new HashSet<StringKeyValuePair>();
		Map<String, String> parentKeyByLevel = item.getParentKeyByLevel();
		Set<Entry<String,String>> entrySet = parentKeyByLevel.entrySet();
		for (Entry<String, String> entry : entrySet) {
			StringKeyValuePair keyValuePair = new StringKeyValuePair(entry);
			result.add(keyValuePair);
		}
		return result;
	}
	
	protected Map<String, String> createParentKeyByLevelMap(CodeList list, Map<String, String> row, int level) {
		Map<String, String> result = new HashMap<String, String>();
		List<CodeListLevel> hierarchy = list.getHierarchy();
		for (int i = 0; i < level - 1; i++) {
			CodeListLevel l = hierarchy.get(i);
			String levelName = l.getName();
			result.put(levelName, row.get(levelName));
		}
		return result;
	}
	
	protected String getCodeValue(CodeAttribute codeAttribute) {
		Code code = codeAttribute.getValue();
		return code == null ? "" : code.getCode();
	}

	protected String getLevelKeyColumnName(CodeAttribute codeAttribute) {
		CodeAttributeDefinition defn = codeAttribute.getDefinition();
		CodeList list = defn.getList();
		if ( list.getHierarchy().isEmpty() ) {
			return defn.getName();
		} else {
			int level = defn.getLevelPosition();
			return getLevelKeyColumnName(list, level);
		}
	}

	protected String getLevelKeyColumnName(CodeList list, int levelPosition) {
		String levelName = getLevelName(list, levelPosition);
		return levelName == null ? DEFAULT_CODE_COLUMN_NAME: levelName;
	}

	protected String getLevelName(CodeList list, int levelPosition) {
		List<CodeListLevel> hierarchy = list.getHierarchy();
		if ( levelPosition > 0 && levelPosition <= hierarchy.size()) {
			CodeListLevel level = hierarchy.get(levelPosition - 1);
			return level.getName();
		} else {
			return null;
		}
	}
	
	public SurveyManager getSurveyManager() {
		return surveyManager;
	}
	
	public void setSurveyManager(SurveyManager surveyManager) {
		this.surveyManager = surveyManager;
	}
	
}