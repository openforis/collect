package org.openforis.idm.db;

import java.util.List;

import liquibase.database.structure.Column;
import liquibase.database.structure.Table;

/**
 * 
 * @author G. Miceli
 *
 */
public abstract class AbstractIdmTableBuilder {

	private String tablePrefix; 
	private String tableSuffix;
	private Table table;

	public Table toTable() {
		// Create table
		String name = getName();
		this.table = new Table(name);
		createColumns();
		return table;
	}
	

	protected String getName() {
		String name = getBaseName();
		if ( tablePrefix != null ) {
			name = tablePrefix + name;
		}
		if ( tableSuffix != null ) {
			name = name + tableSuffix;
		}
		return name;
	}

	protected void addColumn(Column column) {
		List<Column> cols = table.getColumns();
		column.setTable(table);
		cols.add(column);
	}
	
	protected Column createIdColumn(String name) {
		Column col = new Column();
		col.setName(name);
//		col.setDataType(Types.INTEGER);
		col.setTypeName("integer");
		col.setNullable(false);
		return col;
	}

	protected abstract String getBaseName();
	
	protected abstract void createColumns();

	public String getTablePrefix() {
		return tablePrefix;
	}

	public void setTablePrefix(String tablePrefix) {
		this.tablePrefix = tablePrefix;
	}

	public String getTableSuffix() {
		return tableSuffix;
	}

	public void setTableSuffix(String tableSuffix) {
		this.tableSuffix = tableSuffix;
	}
}
