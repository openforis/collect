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

import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.NameValueEntry;
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

	@Override
	@Deprecated
	public String getCode(CodeList list, String attribute, Object... keys) {
		NameValueEntry[] filters = NameValueEntry.fromKeyValuePairs(keys);
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
		
		List<NameValueEntry> filters = new ArrayList<NameValueEntry>();
		addSurveyFilter(list, filters);
		CodeAttribute codeParent = attribute.getCodeParent();
		while (codeParent != null) {
			String colName = getLevelKeyColumnName(codeParent);
			String codeValue = getCodeValue(codeParent);
			filters.add(new NameValueEntry(colName, codeValue));
			codeParent = codeParent.getCodeParent();
		}
		String colName = getLevelKeyColumnName(attribute);
		String codeValue = getCodeValue(attribute);
		filters.add(new NameValueEntry(colName, codeValue));
		int level = defn.getLevelPosition();
		List<NameValueEntry> emptyNextLevelsFilters = createEmptyNextLevelFilters(list, level);
		filters.addAll(emptyNextLevelsFilters);
		Map<String, String> row = dynamicTableDao.loadRow(list.getLookupTable(), filters.toArray(new NameValueEntry[0]));
		if ( row == null ) {
			return null;
		} else {
			ExternalCodeListItem result = parseRow(row, list, level);
			return result;
		}
	}

	public ExternalCodeListItem getParentItem(ExternalCodeListItem item) {
		List<NameValueEntry> filters = new ArrayList<NameValueEntry>();
		CodeList list = item.getCodeList();
		Collection<NameValueEntry> parentKeys = getParentKeys(item);
		filters.addAll(parentKeys);
		addSurveyFilter(list, filters);
		int level = parentKeys.size() + 1;
		int parentLevel = level - 1;
		List<NameValueEntry> emptyNextLevelsFilters = createEmptyNextLevelFilters(list, parentLevel);
		filters.addAll(emptyNextLevelsFilters);
		Map<String, String> row = dynamicTableDao.loadRow(list.getLookupTable(), filters.toArray(new NameValueEntry[0]));
		return parseRow(row, list, parentLevel);
	}

	@Override
	public List<ExternalCodeListItem> getRootItems(CodeList list) {
		List<NameValueEntry> filters = new ArrayList<NameValueEntry>();
		addSurveyFilter(list, filters);
		List<NameValueEntry> emptyNextLevelsFilters = createEmptyNextLevelFilters(list, 1);
		filters.addAll(emptyNextLevelsFilters);
		List<Map<String, String>> rows = dynamicTableDao.loadRows(list.getLookupTable(), 
				filters.toArray(new NameValueEntry[0]));
		List<ExternalCodeListItem> result = new ArrayList<ExternalCodeListItem>();
		for (Map<String, String> row : rows) {
			ExternalCodeListItem item = parseRow(row, list, 1);
			result.add(item);
		}
		return result;
	}

	public ExternalCodeListItem getRootItem(CodeList list, String code) {
		List<NameValueEntry> filters = new ArrayList<NameValueEntry>();
		addSurveyFilter(list, filters);
		List<NameValueEntry> emptyNextLevelsFilters = createEmptyNextLevelFilters(list, 1);
		filters.addAll(emptyNextLevelsFilters);
		String firstLevelKeyColName = getLevelKeyColumnName(list, 1);
		NameValueEntry itemFilter = new NameValueEntry(firstLevelKeyColName, code);
		filters.add(itemFilter);
		Map<String, String> row = dynamicTableDao.loadRow(list.getLookupTable(), filters.toArray(new NameValueEntry[0]));
		return parseRow(row, list, 1);
	}
	
	@Override
	public List<ExternalCodeListItem> getChildItems(ExternalCodeListItem item) {
		CodeList list = item.getCodeList();
		List<NameValueEntry> filters = createChildItemsFilters(item);
		int itemLevel = item.getLevel();
		int childrenLevel = itemLevel + 1;
		String childrenKeyColName = getLevelKeyColumnName(list, childrenLevel);
		String[] notNullColumns = new String[]{childrenKeyColName};
		List<Map<String, String>> rows = dynamicTableDao.loadRows(list.getLookupTable(), 
				filters.toArray(new NameValueEntry[0]),
				notNullColumns);
		List<ExternalCodeListItem> result = new ArrayList<ExternalCodeListItem>();
		for (Map<String, String> row : rows) {
			ExternalCodeListItem child = parseRow(row, list, childrenLevel);
			result.add(child);
		}
		return result;
	}
	
	public boolean hasChildItems(ExternalCodeListItem item) {
		CodeList list = item.getCodeList();
		List<NameValueEntry> filters = createChildItemsFilters(item);
		int itemLevel = item.getLevel();
		int childrenLevel = itemLevel + 1;
		String childrenKeyColName = getLevelKeyColumnName(list, childrenLevel);
		String[] notNullColumns = new String[]{childrenKeyColName};
		return dynamicTableDao.exists(list.getLookupTable(), 
				filters.toArray(new NameValueEntry[0]),
				notNullColumns);
	}
	
	public ExternalCodeListItem getChildItem(ExternalCodeListItem item,
			String code) {
		CodeList list = item.getCodeList();
		List<NameValueEntry> filters = createChildItemsFilters(item);
		int itemLevel = item.getLevel();
		int childrenLevel = itemLevel + 1;
		String childrenLevelColName = getLevelKeyColumnName(list, childrenLevel);
		NameValueEntry codeFilter = new NameValueEntry(childrenLevelColName, code);
		filters.add(codeFilter);
		Map<String, String> row = dynamicTableDao.loadRow(list.getLookupTable(), 
				filters.toArray(new NameValueEntry[0]));
		return parseRow(row, list, childrenLevel);
	}
	
	protected List<NameValueEntry> createChildItemsFilters(ExternalCodeListItem item) {
		List<NameValueEntry> filters = new ArrayList<NameValueEntry>();
		CodeList list = item.getCodeList();
		Collection<NameValueEntry> parentKeys = getParentKeys(item);
		filters.addAll(parentKeys);
		addSurveyFilter(list, filters);
		int itemLevel = item.getLevel();
		int childrenLevel = itemLevel + 1;
		String itemKeyColName = getLevelKeyColumnName(list, itemLevel);
		filters.add(new NameValueEntry(itemKeyColName, item.getCode()));
		List<NameValueEntry> emptyNextLevelsFilters = createEmptyNextLevelFilters(list, childrenLevel);
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

	protected NameValueEntry createSurveyFilter(CodeList list) {
		CollectSurvey survey = (CollectSurvey) list.getSurvey();
		Integer surveyId = survey.getId();
		if ( surveyId != null ) {
			String surveyIdFieldName = survey.isWork() ? SURVEY_WORK_ID_FIELD : SURVEY_ID_FIELD;
			NameValueEntry keyValue = new NameValueEntry(surveyIdFieldName, surveyId.toString());
			return keyValue;
		} else {
			return null;
		}
	}

	protected NameValueEntry[] addSurveyFilter(CodeList list,
			NameValueEntry... filters) {
		NameValueEntry surveyFilter = createSurveyFilter(list);
		NameValueEntry[] newFilters = Arrays.copyOf(filters, filters.length + 1);
		newFilters[filters.length] = surveyFilter;
		return newFilters;
	}
	
	protected void addSurveyFilter(CodeList list,
			List<NameValueEntry> filters) {
		NameValueEntry surveyFilter = createSurveyFilter(list);
		filters.add(surveyFilter);
	}
	
	protected List<NameValueEntry> createEmptyNextLevelFilters(CodeList list, int level) {
		List<NameValueEntry> result = new ArrayList<NameValueEntry>();
		List<CodeListLevel> hierarchy = list.getHierarchy();
		for(int i = level; i < hierarchy.size(); i++){
			CodeListLevel codeListLevel = hierarchy.get(i);
			String name = codeListLevel.getName();
			result.add(new NameValueEntry(name, ""));
		}
		return result;
	}
	
	protected Collection<NameValueEntry> getParentKeys(ExternalCodeListItem item) {
		Set<NameValueEntry> result = new HashSet<NameValueEntry>();
		Map<String, String> parentKeyByLevel = item.getParentKeyByLevel();
		Set<Entry<String,String>> entrySet = parentKeyByLevel.entrySet();
		for (Entry<String, String> entry : entrySet) {
			NameValueEntry keyValuePair = new NameValueEntry(entry);
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
	
}