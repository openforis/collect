/**
 * 
 */
package org.openforis.collect.persistence;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.NameValueEntry;
import org.openforis.collect.model.SamplingDesignItem;
import org.openforis.commons.collection.Visitor;
import org.openforis.idm.metamodel.CodeAttributeDefinition;
import org.openforis.idm.metamodel.CodeList;
import org.openforis.idm.metamodel.CodeListItem;
import org.openforis.idm.metamodel.CodeListLevel;
import org.openforis.idm.metamodel.ExternalCodeListItem;
import org.openforis.idm.metamodel.ExternalCodeListProvider;
import org.openforis.idm.metamodel.Survey;
import org.openforis.idm.model.Code;
import org.openforis.idm.model.CodeAttribute;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author M. Togna
 * @author S. Ricci
 * 
 */
@Transactional(readOnly=true, propagation=Propagation.SUPPORTS)
public class DatabaseExternalCodeListProvider implements
		ExternalCodeListProvider {

	private static final String ID_COLUMN_NAME = "id";
	private static final String DEFAULT_CODE_COLUMN_NAME = "code";
	private static final String LABEL_COLUMN_PREFIX = "label";
	
	private static final String SURVEY_ID_FIELD = "survey_id";

	@Autowired
	private DynamicTableDao dynamicTableDao;
	@Autowired
	private SamplingDesignDao samplingDesignDao;

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
		if (isSamplingDesignCodeList(list)) {
			return getItemFromSamplingDesign(attribute);
		} else {
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
			Map<String, String> row = dynamicTableDao.loadRow(list.getLookupTable(), 
					filters.toArray(new NameValueEntry[filters.size()]));
			if ( row == null ) {
				return null;
			} else {
				ExternalCodeListItem result = parseRow(row, list, level);
				return result;
			}
		}
	}

	private ExternalCodeListItem getItemFromSamplingDesign(CodeAttribute attribute) {
		CodeAttributeDefinition defn = attribute.getDefinition();
		CodeList list = defn.getList();
		Survey survey = defn.getSurvey();
		
		List<CodeAttribute> codeAncestors = attribute.getCodeAncestors();
		if (defn.getLevelIndex() == codeAncestors.size() && !attribute.isEmpty()) {
			String codeValue = attribute.getValue().getCode();
			List<String> codeAncestorCodes = getCodeValues(codeAncestors);
			List<String> parentKeys = new ArrayList<String>(codeAncestorCodes);
			parentKeys.add(codeValue);
			SamplingDesignItem samplingDesignItem = samplingDesignDao.loadItem(survey.getId(), parentKeys.toArray(new String[parentKeys.size()]));
			return samplingDesignItemToItem(samplingDesignItem, list, defn.getLevelPosition());
		} else {
			return null;
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
		Map<String, String> row = dynamicTableDao.loadRow(list.getLookupTable(), filters.toArray(new NameValueEntry[filters.size()]));
		return parseRow(row, list, parentLevel);
	}

	@Override
	public List<ExternalCodeListItem> getRootItems(CodeList list) {
		if (isSamplingDesignCodeList(list)) {
			List<SamplingDesignItem> samplingDesignItems = samplingDesignDao.loadChildItems(list.getSurvey().getId());
			return samplingDesignItemsToItems(list, samplingDesignItems, 1);
		} else {
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
	}

	private List<ExternalCodeListItem> samplingDesignItemsToItems(CodeList list, List<SamplingDesignItem> samplingDesignItems, int level) {
		List<ExternalCodeListItem> items = new ArrayList<ExternalCodeListItem>(samplingDesignItems.size());
		for (SamplingDesignItem samplingDesignItem : samplingDesignItems) {
			ExternalCodeListItem item = samplingDesignItemToItem(samplingDesignItem, list, level);
			items.add(item);
		}
		return items;
	}

	private ExternalCodeListItem samplingDesignItemToItem(SamplingDesignItem samplingDesignItem, CodeList list, int level) {
		if (samplingDesignItem == null)
			return null;
		
		Map<String, String> parentKeyByLevel = new HashMap<String, String>();
		for (int ancestorLevelIndex = 0; ancestorLevelIndex < level - 1; ancestorLevelIndex ++) {
			String ancestorLevelName = list.getHierarchy().get(ancestorLevelIndex).getName();
			parentKeyByLevel.put(ancestorLevelName, samplingDesignItem.getLevelCode(ancestorLevelIndex + 1));
		}
		int id = Long.valueOf(samplingDesignItem.getId()).intValue();
		ExternalCodeListItem item = new ExternalCodeListItem(list, id, parentKeyByLevel, level);
		item.setCode(samplingDesignItem.getLevelCode(level));
		return item;
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
		int itemLevel = item.getLevel();
		int childrenLevel = itemLevel + 1;
		if (childrenLevel > list.getHierarchy().size()) {
			return Collections.emptyList();
		}
		if (isSamplingDesignCodeList(list)) {
			List<String> ancestorKeys = new ArrayList<String>(item.getParentKeys());
			ancestorKeys.add(item.getCode());
			List<SamplingDesignItem> samplingDesignItems = samplingDesignDao.loadChildItems(list.getSurvey().getId(), ancestorKeys);
			return samplingDesignItemsToItems(list, samplingDesignItems, childrenLevel);
		}			
		List<NameValueEntry> filters = createChildItemsFilters(item);
		String childrenKeyColName = getLevelKeyColumnName(list, childrenLevel);
		String[] notNullColumns = new String[]{childrenKeyColName};
		List<Map<String, String>> rows = dynamicTableDao.loadRows(list.getLookupTable(), 
				filters.toArray(new NameValueEntry[filters.size()]),
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
		if (isSamplingDesignCodeList(list)) {
			List<String> ancestorKeys = new ArrayList<String>(item.getParentKeys());
			ancestorKeys.add(item.getCode());
			return samplingDesignDao.countChildItems(list.getSurvey().getId(), ancestorKeys) > 0;
		}
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
	
	@Override
	public void visitItems(final CodeList list, final Visitor<CodeListItem> visitor) {
		List<NameValueEntry> filters = new ArrayList<NameValueEntry>();
		addSurveyFilter(list, filters);
		List<NameValueEntry> emptyNextLevelsFilters = createEmptyNextLevelFilters(list, 1);
		filters.addAll(emptyNextLevelsFilters);
		dynamicTableDao.visitRows(list.getLookupTable(), 
			filters.toArray(new NameValueEntry[0]), (String[]) null, new Visitor<Map<String, String>>() {
				public void visit(Map<String, String> row) {
					ExternalCodeListItem item = parseRow(row, list, 1);
					visitor.visit(item);
					visitChildItems(item, visitor);
				}
			}
		);
	}
	
	@Override
	public void visitChildItems(final ExternalCodeListItem item, final Visitor<CodeListItem> visitor) {
		final CodeList list = item.getCodeList();
		int itemLevel = item.getLevel();
		final int childrenLevel = itemLevel + 1;
		if (childrenLevel > list.getHierarchy().size()) {
			return;
		}
		List<NameValueEntry> filters = createChildItemsFilters(item);
		String childrenKeyColName = getLevelKeyColumnName(list, childrenLevel);
		String[] notNullColumns = new String[]{childrenKeyColName};
		dynamicTableDao.visitRows(list.getLookupTable(), 
				filters.toArray(new NameValueEntry[filters.size()]),
				notNullColumns, new Visitor<Map<String,String>>() {
					public void visit(Map<String, String> row) {
						ExternalCodeListItem item = parseRow(row, list, childrenLevel);
						visitor.visit(item);
					}
				});
	}
	
	public int countRootItems(CodeList list) {
		if (isSamplingDesignCodeList(list)) {
			return samplingDesignDao.countItemsInLevel(list.getSurvey().getId(), 1);
		} else {
			throw new UnsupportedOperationException(
					String.format("Count items on dynamic table is not supported; code list '%s' lookup table '%s'",
							list.getName(), list.getLookupTable()));
		}
	}
	
	public int countMaxChildren(CodeList list, int level) {
		if (isSamplingDesignCodeList(list)) {
			return samplingDesignDao.countMaxByLevel(list.getSurvey().getId(), level);
		} else {
			throw new UnsupportedOperationException(
					String.format("Count items on dynamic table is not supported; code list '%s' lookup table '%s'",
							list.getName(), list.getLookupTable()));
		}
	}
	
	protected ExternalCodeListItem parseRow(Map<String, String> row, CodeList list, int levelIndex) {
		if ( row == null ) {
			return null;
		}
		String idValue = row.get(ID_COLUMN_NAME);
		Integer id = Integer.valueOf(idValue);
		
		Map<String, String> parentKeysByLevel = createParentKeyByLevelMap(list, row, levelIndex);
		ExternalCodeListItem item = new ExternalCodeListItem(list, id, parentKeysByLevel, levelIndex + 1);

		String currentLevelKeyColName = getLevelKeyColumnName(list, levelIndex);
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
			return new NameValueEntry(SURVEY_ID_FIELD, surveyId.toString());
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
	
	protected Map<String, String> createParentKeyByLevelMap(CodeList list, Map<String, String> row, int levelIndex) {
		Map<String, String> result = new HashMap<String, String>();
		List<CodeListLevel> hierarchy = list.getHierarchy();
		for (int i = 0; i < levelIndex - 1; i++) {
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
	
	private List<String> getCodeValues(List<CodeAttribute> attributes) {
		List<String> codes = new ArrayList<String>(attributes.size());
		for (CodeAttribute attribute: attributes) {
			if (attribute.isEmpty()) {
				codes.add(null);
			} else {
				codes.add(attribute.getValue().getCode());
			}
		}
		return codes;
	}

	private boolean isSamplingDesignCodeList(CodeList list) {
		return ((CollectSurvey) list.getSurvey()).isSamplingDesignCodeList(list);
	}

	public void setDynamicTableDao(DynamicTableDao dynamicTableDao) {
		this.dynamicTableDao = dynamicTableDao;
	}
	
	public void setSamplingDesignDao(SamplingDesignDao samplingDesignDao) {
		this.samplingDesignDao = samplingDesignDao;
	}

}