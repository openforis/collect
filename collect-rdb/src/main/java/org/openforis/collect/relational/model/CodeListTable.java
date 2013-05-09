package org.openforis.collect.relational.model;

import java.util.List;

import org.openforis.collect.relational.DatabaseExporterConfig;
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

	@Override
	public Dataset extractData(CodeListItem source) {
		return extractData();
	}

	@Override
	public Dataset extractData(DatabaseExporterConfig config,
			CodeListItem source) {
		return extractData(config);
	}
	
	public Dataset extractData() {
		return extractData(DatabaseExporterConfig.createDefault());
	}
	
	public Dataset extractData(DatabaseExporterConfig config) {
		Dataset data = new Dataset();
		Integer levelIdx = getLevelIdx();
		List<CodeListItem> items = codeList.getItems(levelIdx);
		String defaultCode = config.getDefaultCode();
		if ( defaultCode != null ) {
			addDefaultCodeRow(config, data, items);
		}
		for (CodeListItem item : items) {
			Row row = extractRow(config, item);
			data.addRow(row);
		}
		return data;
	}

	protected void addDefaultCodeRow(DatabaseExporterConfig config,
			Dataset data, List<CodeListItem> items) {
		boolean containsDefaultAlready = false;
		for (CodeListItem item : items) {
			if ( item.getCode().equals(config.getDefaultCode()) ) {
				containsDefaultAlready = true;
				break;
			}
		}
		if ( !containsDefaultAlready ) {
			Row defaultCodeRow = createDefaultCodeRow(config);
			data.addRow(defaultCodeRow);
		}
	}
	
	@Override
	public Row extractRow(CodeListItem source) {
		return extractRow(DatabaseExporterConfig.createDefault(), source);
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public Row extractRow(DatabaseExporterConfig config, CodeListItem source) {
		Row row = new Row(this);
		List<Column<?>> columns = getColumns();
		for (int i=0; i < columns.size(); i++) {
			Column col = columns.get(i);
			Object val = col.extractValue(source);
			row.setValue(i, val);
		}
		return row;
	}
	
	protected Row createDefaultCodeRow(DatabaseExporterConfig config) {
		Row row = new Row(this);
		List<Column<?>> columns = getColumns();
		for (int i=0; i < columns.size(); i++) {
			@SuppressWarnings("rawtypes")
			Column col = columns.get(i);
			Object val;
			if ( col instanceof CodeListPrimaryKeyColumn ) {
				val = -1;
			} else if ( col instanceof CodeListCodeColumn ) {
				val = config.getDefaultCode();
			} else if ( col instanceof CodeListItemLabelColumn ) {
				val = config.getDefaultCodeLabel(((CodeListItemLabelColumn) col).getLanguageCode());
			} else {
				val = null;
			}
			row.setValue(i, val);
		}
		return row;
	}

}
