package org.openforis.collect.relational.model;

import java.io.PrintStream;
import java.util.List;

import org.openforis.idm.metamodel.CodeList;
import org.openforis.idm.metamodel.CodeListItem;
import org.openforis.idm.metamodel.LanguageSpecificTextMap;

/**
 * 
 * @author S. Ricci
 * @author A. Sanchez-Paus
 *
 */
// TODO Rename to CodeListTable
public class CodeListTable extends AbstractTable<CodeListItem> {
	
	private CodeList codeList;
	private CodeListTable parent;
	private String defaultCode;
	private LanguageSpecificTextMap defaultCodeLabels;

	CodeListTable(String prefix, String baseName, String suffix, CodeList codeList, CodeListTable parent, 
			String defaultCode, LanguageSpecificTextMap defaultCodeLabels) {
		super(prefix, baseName, suffix);
		this.codeList = codeList;
		this.parent = parent;
		this.defaultCode = defaultCode;
		this.defaultCodeLabels = defaultCodeLabels;
	}
	
	public CodeList getCodeList() {
		return codeList;
	}

	public CodeListTable getParent() {
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
			CodeListTable cp = parent;
			while ( cp != null ) {
				result++;
				cp = cp.parent;
			}
		}
		return result;
	}

	@Override
	public Dataset extractData(CodeListItem source) {
		return extractData();
	}

	public Dataset extractData() {
		Dataset data = new Dataset();
		Integer levelIdx = getLevelIdx();
		List<CodeListItem> items = codeList.getItems(levelIdx);
		if ( defaultCode != null ) {
			addDefaultCodeRow(data, items);
		}
		for (CodeListItem item : items) {
			Row row = extractRow(item);
			data.addRow(row);
		}
		return data;
	}

	protected void addDefaultCodeRow(Dataset data, List<CodeListItem> items) {
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

	@Override
	public void print(PrintStream out) {
		out.printf("%-43s%s\n", getName(), getCodeList().getName());
		printColumns(out);
	}
}
