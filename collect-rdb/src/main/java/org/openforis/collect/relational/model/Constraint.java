package org.openforis.collect.relational.model;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * 
 * @author G. Miceli
 * 
 */
public abstract class Constraint {
	private String name;
	private Table<?> table;
	private List<Column<?>> columns;
	
	Constraint(String name, Table<?> table, Column<?>... columns) {
		this.name = name;
		this.table = table;
		this.columns = Arrays.asList(columns);
	}

	public String getName() {
		return name;
	}

	public Table<?> getTable() {
		return table;
	}

	public List<Column<?>> getColumns() {
		return Collections.unmodifiableList(columns);
	}
}