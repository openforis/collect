package org.openforis.collect.relational.model;

import java.util.List;

import org.openforis.idm.metamodel.CodeList;
import org.openforis.idm.metamodel.CodeListItem;

/**
 * 
 * @author S. Ricci
 * @author A. Sanchez-Paus
 *
 */
public class CodeListTable extends AbstractTable<CodeListItem> {
	
	private CodeList codeList;
	private CodeListTable parent;

	CodeListTable(String prefix, String baseName, CodeList codeList, CodeListTable parent) {
		super(prefix, baseName);
		this.codeList = codeList;
		this.parent = parent;
	}
	
	public CodeList getCodeList() {
		return codeList;
	}

	public CodeListTable getParent() {
		return parent;
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
	
	public Dataset extractData() {
		Dataset data = new Dataset();
		Integer levelIdx = getLevelIdx();
		List<CodeListItem> items = codeList.getItems(levelIdx);
		for (CodeListItem item : items) {
			Row row = extractRow(item);
			data.addRow(row);
		}
		return data;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
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
	
	

}
