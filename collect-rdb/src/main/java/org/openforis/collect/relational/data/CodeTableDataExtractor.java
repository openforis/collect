package org.openforis.collect.relational.data;

import java.util.List;

import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.relational.model.CodeLabelColumn;
import org.openforis.collect.relational.model.CodeListCodeColumn;
import org.openforis.collect.relational.model.CodePrimaryKeyColumn;
import org.openforis.collect.relational.model.CodeTable;
import org.openforis.collect.relational.model.Column;
import org.openforis.idm.metamodel.CodeListItem;
import org.openforis.idm.metamodel.CodeListService;
import org.openforis.idm.metamodel.SurveyContext;

/**
 * 
 * @author ste
 *
 */
//TODO do not extract all code list items in level with a single query
public class CodeTableDataExtractor extends DataExtractor {

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
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Row extractRow(CodeListItem source) {
		Row row = new Row(table);
		List<Column<?>> columns = table.getColumns();
		for (int i=0; i < columns.size(); i++) {
			Column col = columns.get(i);
			Object val = col.extractValue(source);
			row.setValue(i, val);
		}
		return row;
	}
	
	protected Row createDefaultCodeRow() {
		Row row = new Row(table);
		List<Column<?>> columns = table.getColumns();
		for (int i=0; i < columns.size(); i++) {
			@SuppressWarnings("rawtypes")
			Column col = columns.get(i);
			Object val;
			if ( col instanceof CodePrimaryKeyColumn ) {
				val = -1;
			} else if ( col instanceof CodeListCodeColumn ) {
				val = table.getDefaultCode();
			} else if ( col instanceof CodeLabelColumn ) {
				val = table.getDefaultCodeLabel(((CodeLabelColumn) col).getLanguageCode());
			} else {
				val = null;
			}
			row.setValue(i, val);
		}
		return row;
	}

}
