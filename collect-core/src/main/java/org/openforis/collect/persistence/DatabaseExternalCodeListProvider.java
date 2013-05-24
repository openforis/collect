/**
 * 
 */
package org.openforis.collect.persistence;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.jooq.TableField;
import org.openforis.collect.manager.SurveyManager;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.persistence.jooq.tables.records.LookupRecord;
import org.openforis.idm.metamodel.CodeAttributeDefinition;
import org.openforis.idm.metamodel.CodeList;
import org.openforis.idm.metamodel.CodeListLevel;
import org.openforis.idm.metamodel.ExternalCodeListItem;
import org.openforis.idm.metamodel.ExternalCodeListProvider;
import org.openforis.idm.metamodel.StringKeyValuePair;
import org.openforis.idm.metamodel.Survey;
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
		String listName = list.getName();
		Object[] filters = addSurveyFilter(list, keys);
		Object object = dynamicTableDao.load(listName, attribute, filters);
		if (object == null) {
			return null;
		} else {
			return object.toString();
		}
	}

	@Override
	public String getCode(CodeList list, String attribute,
			StringKeyValuePair... filters) {
		String listName = list.getName();
		StringKeyValuePair[] newFilters = addSurveyFilter(list, filters);
		Object object = dynamicTableDao.load(listName, attribute, newFilters);
		if (object == null) {
			return null;
		} else {
			return object.toString();
		}
	}

	@Override
	public ExternalCodeListItem getItem(CodeList list, String codeColumn,
			StringKeyValuePair... filters) {
		String listName = list.getName();
		StringKeyValuePair[] newFilters = addSurveyFilter(list, filters);
		Map<String, String> row = dynamicTableDao.loadRow(listName, newFilters);
		return parseRow(list, codeColumn, row);
	}

	@Override
	public ExternalCodeListItem getParentItem(ExternalCodeListItem item) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<ExternalCodeListItem> getChildItems(CodeList list, String attribute) {
		
		List<StringKeyValuePair> filters = createEmptyValuesFilters(list, 1);
		CodeAttribute codeParent = codeAttribute.getCodeParent();
		int level = 0;
		while (codeParent != null) {
			String colName = getColumnName(codeParent);
			String codeValue = getCodeValue(codeParent);
			filters.add(new StringKeyValuePair(colName, codeValue));
			codeParent = codeParent.getCodeParent();
			level ++;
		}
		String colName = getColumnName(codeAttribute);
		String codeValue = getCodeValue(codeAttribute);
		filters.add(new StringKeyValuePair(colName, codeValue));
		
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<ExternalCodeListItem> getChildItems(ExternalCodeListItem item, String attribute) {
		// TODO Auto-generated method stub
		return null;
	}
	
	protected ExternalCodeListItem parseRow(CodeList list, String codeColumn, 
			Map<String, String> row) {
		String idValue = row.get(ID_COLUMN_NAME);
		String code = row.get(codeColumn);
		Integer id = Integer.valueOf(idValue);
		ExternalCodeListItem item = new ExternalCodeListItem(list, id);
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
		StringKeyValuePair[] newFilters = new StringKeyValuePair[filters.length + 1];
		newFilters[filters.length] = surveyFilter;
		return newFilters;
	}
	
	@Deprecated
	protected Object[] addSurveyFilter(CodeList list, Object... keys) {
		StringKeyValuePair surveyFilter = createSurveyFilter(list);
		Object[] filters = new Object[keys.length + 2];
		System.arraycopy(keys, 0, filters, 0, keys.length + 2);
		filters[keys.length] = surveyFilter.getKey();
		filters[keys.length + 1] = surveyFilter.getValue();
		return filters;
	}

	protected List<StringKeyValuePair> createEmptyValuesFilters(CodeList list, int level) {
		List<StringKeyValuePair> result = new ArrayList<StringKeyValuePair>();
		List<CodeListLevel> hierarchy = list.getHierarchy();
		for(int i = level+1; i<hierarchy.size(); i++){
			CodeListLevel codeListLevel = hierarchy.get(i);
			String name = codeListLevel.getName();
			result.add(new StringKeyValuePair(name, ""));
		}
		return result;
	}
}
