package org.openforis.collect.relational.model;

import org.openforis.collect.relational.sql.RDBJdbcType;

/**
 * 
 * @author G. Miceli
 * 
 */
abstract class AbstractColumn<T> implements Column<T> {

	private String name;
	private RDBJdbcType type;
	private Integer length;
	private boolean nullable;

	AbstractColumn(String name, RDBJdbcType type, Integer length, boolean nullable) {
		this.name = name;
		this.type = type;
		this.length = length;
		this.nullable = nullable;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public RDBJdbcType getType() {
		return type;
	}

	@Override
	public Integer getLength() {
		return length;
	}

	@Override
	public boolean isNullable() {
		return nullable;
	}
	
	@Override
	public String toString() {
		return getName();
	}
}