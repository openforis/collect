/**
 * 
 */
package org.openforis.collect.persistence;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openforis.collect.manager.SurveyManager;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.idm.metamodel.CodeAttributeDefinition;
import org.openforis.idm.metamodel.CodeList;
import org.openforis.idm.metamodel.CodeListLevel;
import org.openforis.idm.metamodel.ExternalCodeListItem;
import org.openforis.idm.metamodel.ExternalCodeListProvider;
import org.openforis.idm.metamodel.StringKeyValuePair;
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
	private static final String LABEL_COLUMN_PREFIX = "label";
	
	private static final String SURVEY_ID_FIELD = "survey_id";
	private static final String SURVEY_WORK_ID_FIELD = "survey_work_id";

	@Autowired
	private DynamicTableDao dynamicTableDao;
	@Autowired
	private SurveyManager surveyManager;

	@Deprecated
	@Override
	public String getCode(CodeList list, String attribute, Object... keys) {
		String listName = list.getLookupTable();
		Object[] filters = addSurveyFilter(list, keys);
		Object object = dynamicTableDao.load(listName, attribute, filters);
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
		int level = defn.getCodeListLevelIndex();
		
		List<StringKeyValuePair> filters = new ArrayList<StringKeyValuePair>();
		addSurveyFilter(list, filters);
		CodeAttribute codeParent = attribute.getCodeParent();
		while (codeParent != null) {
			String colName = getLevelName(codeParent);
			String codeValue = getCodeValue(codeParent);
			filters.add(new StringKeyValuePair(colName, codeValue));
			codeParent = codeParent.getCodeParent();
		}
		String levelName = getLevelName(attribute);
		String codeValue = getCodeValue(attribute);
		filters.add(new StringKeyValuePair(levelName, codeValue));
		List<StringKeyValuePair> emptyNextLevelsFilters = createEmptyNextLevelFilters(list, level);
		filters.addAll(emptyNextLevelsFilters);
		Map<String, String> row = dynamicTableDao.loadRow(list.getLookupTable(), filters.toArray(new StringKeyValuePair[0]));
		ExternalCodeListItem result = parseRow(row, list, level);
		return result;
	}

	@Override
	public ExternalCodeListItem getParentItem(ExternalCodeListItem item) {
		List<StringKeyValuePair> filters = new ArrayList<StringKeyValuePair>();
		CodeList list = item.getCodeList();
		Collection<StringKeyValuePair> parentKeys = item.getParentKeys();
		filters.addAll(parentKeys);
		addSurveyFilter(list, filters);
		int level = parentKeys.size() + 1;
		int parentLevel = level - 1;
		List<StringKeyValuePair> emptyNextLevelsFilters = createEmptyNextLevelFilters(list, parentLevel);
		filters.addAll(emptyNextLevelsFilters);
		Map<String, String> row = dynamicTableDao.loadRow(list.getLookupTable(), filters.toArray(new StringKeyValuePair[0]));
		if ( row == null ) {
			return null;
		} else {
			return parseRow(row, list, parentLevel);
		}
	}

	@Override
	public List<ExternalCodeListItem> getChildItems(CodeList list) {
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

	@Override
	public List<ExternalCodeListItem> getChildItems(ExternalCodeListItem item) {
		List<StringKeyValuePair> filters = new ArrayList<StringKeyValuePair>();
		CodeList list = item.getCodeList();
		Collection<StringKeyValuePair> parentKeys = item.getParentKeys();
		filters.addAll(parentKeys);
		addSurveyFilter(list, filters);
		int itemLevelIdx = parentKeys.size();
		int itemLevelPos = itemLevelIdx + 1;
		int childrenLevelPos = itemLevelPos + 1;
		List<CodeListLevel> hierarchy = list.getHierarchy();
		CodeListLevel level = hierarchy.get(itemLevelIdx);
		filters.add(new StringKeyValuePair(level.getName(), item.getCode()));
		List<StringKeyValuePair> emptyNextLevelsFilters = createEmptyNextLevelFilters(list, childrenLevelPos);
		filters.addAll(emptyNextLevelsFilters);
		String[] notNullColumns = new String[]{getLevelName(list, childrenLevelPos)};
		List<Map<String, String>> rows = dynamicTableDao.loadRows(list.getLookupTable(), 
				filters.toArray(new StringKeyValuePair[0]),
				notNullColumns);
		List<ExternalCodeListItem> result = new ArrayList<ExternalCodeListItem>();
		for (Map<String, String> row : rows) {
			ExternalCodeListItem child = parseRow(row, list, childrenLevelPos);
			result.add(child);
		}
		return result;
	}
	
	protected ExternalCodeListItem parseRow(Map<String, String> row, CodeList list, int level) {
		String idValue = row.get(ID_COLUMN_NAME);
		Integer id = Integer.valueOf(idValue);
		
		Map<String, String> parentKeysByLevel = getParentKeysMap(list, row, level);
		ExternalCodeListItem item = new ExternalCodeListItem(list, id, parentKeysByLevel);

		List<CodeListLevel> hierarchy = list.getHierarchy();
		CodeListLevel currentLevel = hierarchy.get(level - 1);
		String currentLevelName = currentLevel.getName();
		String code = row.get(currentLevelName);
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
	
	@Deprecated
	protected Object[] addSurveyFilter(CodeList list, Object... keys) {
		StringKeyValuePair surveyFilter = createSurveyFilter(list);
		Object[] filters = Arrays.copyOf(keys, keys.length + 2);
		filters[keys.length] = surveyFilter.getKey();
		filters[keys.length + 1] = surveyFilter.getValue();
		return filters;
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
	
	protected Map<String, String> getParentKeysMap(CodeList list, Map<String, String> row, int level) {
		Map<String, String> parentKeysByLevel = new HashMap<String, String>();
		List<CodeListLevel> hierarchy = list.getHierarchy();
		for (int i = 0; i < level - 1; i++) {
			CodeListLevel l = hierarchy.get(i);
			String levelName = l.getName();
			parentKeysByLevel.put(levelName, row.get(levelName));
		}
		return parentKeysByLevel;
	}
	
	private String getCodeValue(CodeAttribute codeAttribute) {
		Code code = codeAttribute.getValue();
		return code == null ? "" : code.getCode();
	}

	private String getLevelName(CodeAttribute codeAttribute) {
		CodeAttributeDefinition defn = codeAttribute.getDefinition();
		CodeList list = defn.getList();
		int levelIdx = defn.getCodeListLevelIndex();
		return getLevelName(list, levelIdx + 1);
	}

	protected String getLevelName(CodeList list, int levelPosition) {
		List<CodeListLevel> hierarchy = list.getHierarchy();
		CodeListLevel level = hierarchy.get(levelPosition - 1);
		return level.getName();
	}
	
}