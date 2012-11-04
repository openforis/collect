package org.openforis.collect.relational;

/**
 * 
 * @author G. Miceli
 * 
 */
abstract class AbstractColumn<T> implements Column<T> {

	private String name;
	private int type;
	private Integer length;
	private boolean nullable;

	AbstractColumn(String name, int type, Integer length, boolean nullable) {
		this.name = name;
		this.type = type;
		this.length = length;
		this.nullable = nullable;
	}

	AbstractColumn(String name, int type, boolean allowNulls) {
		this.name = name;
		this.type = type;
		this.nullable = allowNulls;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public int getType() {
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
}