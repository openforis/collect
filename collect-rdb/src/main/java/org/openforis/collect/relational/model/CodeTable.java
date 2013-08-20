package org.openforis.collect.relational.model;

import java.util.List;

import org.openforis.collect.model.CollectSurvey;
import org.openforis.idm.metamodel.CodeList;
import org.openforis.idm.metamodel.CodeListItem;
import org.openforis.idm.metamodel.CodeListService;
import org.openforis.idm.metamodel.LanguageSpecificTextMap;
import org.openforis.idm.metamodel.SurveyContext;

/**
 * 
 * @author S. Ricci
 * @author A. Sanchez-Paus
 *
 */
public class CodeTable extends AbstractTable<CodeListItem> {
	
	private CodeList codeList;
	private CodeTable parent;
	private String defaultCode;
	private LanguageSpecificTextMap defaultCodeLabels;

	CodeTable(String prefix, String baseName, CodeList codeList, CodeTable parent, String defaultCode, LanguageSpecificTextMap defaultCodeLabels) {
		super(prefix, baseName);
		this.codeList = codeList;
		this.parent = parent;
		this.defaultCode = defaultCode;
		this.defaultCodeLabels = defaultCodeLabels;
	}
	
	public CodeList getCodeList() {
		return codeList;
	}

	public CodeTable getParent() {
		return parent;
	}
	
	public String getDefaultCode() {
		return defaultCode;
	}
	
	public LanguageSpecificTextMap getDefaultCodeLabels() {
		return defaultCodeLabels;
	}
	
	public String getDefaultCodeLabel(String langCode) {
		if ( defaultCodeLabels == null ) {
			return null;
		} else {
			return defaultCodeLabels.getText(langCode);
		}
	}
	
	public Integer getLevelIdx() {
		Integer result = null;
		if ( codeList.isHierarchical() ) {
			result = 0;
			CodeTable cp = parent;
			while ( cp != null ) {
				result++;
				cp = cp.parent;
			}
		}
		return result;
	}

	@Override
	public Dataset extractData(CodeListItem source) {
		throw new UnsupportedOperationException();
	}

	public Dataset extractData() {
		Dataset data = new Dataset();
		Integer levelIdx = getLevelIdx();
		CodeListService codeListService = getCodeListService();
		int level = levelIdx == null ? 1: levelIdx + 1;
		List<? extends CodeListItem> items = codeListService.loadItems(codeList, level);
		if ( defaultCode != null ) {
			addDefaultCodeRow(data, items);
		}
		for (CodeListItem item : items) {
			Row row = extractRow(item);
			data.addRow(row);
		}
		return data;
	}

	protected CodeListService getCodeListService() {
		CollectSurvey survey = (CollectSurvey) codeList.getSurvey();
		SurveyContext context = survey.getContext();
		CodeListService codeListService = context.getCodeListService();
		return codeListService;
	}

	protected void addDefaultCodeRow(Dataset data, List<? extends CodeListItem> items) {
		boolean containsDefaultAlready = false;
		for (CodeListItem item : items) {
			if ( item.getCode().equals(defaultCode) ) {
				containsDefaultAlready = true;
				break;
			}
		}
		if ( !containsDefaultAlready ) {
			Row defaultCodeRow = createDefaultCodeRow();
			data.addRow(defaultCodeRow);
		}
	}
	
	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Row extractRow(CodeListItem source) {
		Row row = new Row(this);
		List<Column<?>> columns = getColumns();
		for (int i=0; i < columns.size(); i++) {
			Column col = columns.get(i);
			Object val = col.extractValue(source);
			row.setValue(i, val);
		}
		return row;
	}
	
	protected Row createDefaultCodeRow() {
		Row row = new Row(this);
		List<Column<?>> columns = getColumns();
		for (int i=0; i < columns.size(); i++) {
			@SuppressWarnings("rawtypes")
			Column col = columns.get(i);
			Object val;
			if ( col instanceof CodePrimaryKeyColumn ) {
				val = -1;
			} else if ( col instanceof CodeListCodeColumn ) {
				val = defaultCode;
			} else if ( col instanceof CodeLabelColumn ) {
				val = getDefaultCodeLabel(((CodeLabelColumn) col).getLanguageCode());
			} else {
				val = null;
			}
			row.setValue(i, val);
		}
		return row;
	}

}
