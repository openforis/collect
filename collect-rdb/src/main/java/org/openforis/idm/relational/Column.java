package org.openforis.idm.relational;

/**
 * 
 * @author G. Miceli
 * 
 */
public final class Column {

	private String name;
	private Class<?> type;
	private Integer length;
	private boolean allowNulls;

	Column(String name, Class<?> type, boolean allowNulls, Integer length) {
		this.name = name;
		this.type = type;
		this.length = length;
		this.allowNulls = allowNulls;
	}

	Column(String name, Class<?> type, boolean allowNulls) {
		this.name = name;
		this.type = type;
		this.allowNulls = allowNulls;
	}

	public String getName() {
		return name;
	}

	public Class<?> getType() {
		return type;
	}

	public Integer getLength() {
		return length;
	}

	public boolean isAllowNulls() {
		return allowNulls;
	}	
}