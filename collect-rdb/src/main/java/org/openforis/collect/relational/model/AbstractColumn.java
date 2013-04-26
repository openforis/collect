package org.openforis.collect.relational.model;

/**
 * 
 * @author G. Miceli
 * 
 */
abstract class AbstractColumn<T> implements Column<T> {

	private String name;
	private int type;
	private String typeName;
	private Integer length;
	private boolean nullable;

	AbstractColumn(String name, int type, String typeName, Integer length, boolean nullable) {
		this.name = name;
		this.type = type;
		this.typeName = typeName;
		this.length = length;
		this.nullable = nullable;
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
	public String getTypeName() {
		return typeName;
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