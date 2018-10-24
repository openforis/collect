package org.openforis.collect.relational.data.internal;

import java.util.List;

import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.relational.data.DataExtractor;
import org.openforis.collect.relational.data.Row;
import org.openforis.collect.relational.model.CodeLabelColumn;
import org.openforis.collect.relational.model.CodeListCodeColumn;
import org.openforis.collect.relational.model.CodeListDescriptionColumn;
import org.openforis.collect.relational.model.CodeParentKeyColumn;
import org.openforis.collect.relational.model.CodePrimaryKeyColumn;
import org.openforis.collect.relational.model.CodeTable;
import org.openforis.collect.relational.model.Column;
import org.openforis.collect.relational.model.Table;
import org.openforis.idm.metamodel.CodeList;
import org.openforis.idm.metamodel.CodeListItem;
import org.openforis.idm.metamodel.CodeListService;
import org.openforis.idm.metamodel.PersistedCodeListItem;
import org.openforis.idm.metamodel.Survey;
import org.openforis.idm.metamodel.SurveyContext;

/**
 * 
 * @author S. Ricci
 *
 */
//TODO do not extract all code list items in level with a single query
public class CodeTableDataExtractor extends DataExtractor {

	public static final int DEFAULT_CODE_ROW_ID = -1;
	
	private CodeTable table;
	private List<CodeListItem> items;
	private int itemIndex;
	private int count;
	private int total;

	public CodeTableDataExtractor(CodeTable codeTable) {
		this.table = codeTable;
		CodeListService codeListService = getCodeListService();
		items = codeListService.loadItems(codeTable.getCodeList(), getEffectiveLevelIndex());
		itemIndex = 0;
		count = 0;
		total = items.size() + (isDefaultCodeRowToBeCreated() ? 1: 0);
	}
	
	@Override
	public Row next() {
		Row row;
		if ( isDefaultCodeRowToBeCreated() ) {
			row = createDefaultCodeRow();
		} else {
			CodeListItem item = items.get(itemIndex);
			row = extractRow(item);
			itemIndex ++;
		}
		count ++;
		return row;
	}

	@Override
	public boolean hasNext() {
		return count < total;
	}
	
	@Override
	public Table<?> getTable() {
		return table;
	}

	private boolean isDefaultCodeRowToBeCreated() {
		return count == 0 && table.getDefaultCode() != null && ! containsDefaultCodeItem();
	}

	private int getEffectiveLevelIndex() {
		Integer levelIdx = table.getLevelIdx();
		int level = levelIdx == null ? 1: levelIdx + 1;
		return level;
	}

	public int getTotal() {
		return total;
	}

	private CodeListService getCodeListService() {
		CollectSurvey survey = (CollectSurvey) table.getCodeList().getSurvey();
		SurveyContext context = survey.getContext();
		CodeListService codeListService = context.getCodeListService();
		return codeListService;
	}

	private boolean containsDefaultCodeItem() {
		for (CodeListItem item : items) {
			if ( item.getCode().equals(table.getDefaultCode()) ) {
				return true;
			}
		}
		return false;
	}
	
	public Row extractRow(CodeListItem source) {
		Row row = new Row(table);
		List<Column<?>> columns = table.getColumns();
		for (int i=0; i < columns.size(); i++) {
			Column<?> col = columns.get(i);
			Object val = extractValue(source, col);
			row.setValue(i, val);
		}
		return row;
	}

	private Object extractValue(CodeListItem item, Column<?> col) {
		if (col instanceof CodeLabelColumn) {
			String langCode = ((CodeLabelColumn) col).getLanguageCode();
			return item.getLabel(langCode, true);
		} else if (col instanceof CodeListCodeColumn) {
			return item.getCode();
		} else if (col instanceof CodeListDescriptionColumn) {
			String langCode = ((CodeListDescriptionColumn) col).getLanguageCode();
			return item.getDescription(langCode, true);
		} else if (col instanceof CodeParentKeyColumn) {
			CodeList list = item.getCodeList();
			if ( list.isExternal() ) {
				throw new UnsupportedOperationException(String.format(
						"External code list not supported (survey: %s, code list: %s)", 
						list.getSurvey().getName(), list.getName()));
			}
			CodeListItem parent;
			if (item instanceof PersistedCodeListItem) {
				parent = getCodeListService(list).loadParentItem((PersistedCodeListItem) item);
			} else {
				parent = item.getParentItem();
			}
			return parent.getId();
		} else if (col instanceof CodePrimaryKeyColumn) {
			return item.getId();
		} else {
			throw new UnsupportedOperationException("Code List Table Column type not supported: " + col.getClass().getName());
		}
	}
	
	protected Row createDefaultCodeRow() {
		Row row = new Row(table);
		List<Column<?>> columns = table.getColumns();
		for (int i=0; i < columns.size(); i++) {
			@SuppressWarnings("rawtypes")
			Column col = columns.get(i);
			Object val;
			if ( col instanceof CodePrimaryKeyColumn ) {
				val = DEFAULT_CODE_ROW_ID;
			} else if ( col instanceof CodeListCodeColumn ) {
				val = table.getDefaultCode();
			} else if ( col instanceof CodeLabelColumn ) {
				String defaultLanguage = ( (CollectSurvey) table.getCodeList().getSurvey()).getDefaultLanguage();
				val = table.getDefaultCodeLabel(((CodeLabelColumn) col).getLanguageCode(), defaultLanguage );
			} else {
				val = null;
			}
			row.setValue(i, val);
		}
		return row;
	}
	
	private CodeListService getCodeListService(CodeList list) {
		Survey survey = list.getSurvey();
		SurveyContext context = survey.getContext();
		CodeListService codeListService = context.getCodeListService();
		return codeListService;
	}

}
