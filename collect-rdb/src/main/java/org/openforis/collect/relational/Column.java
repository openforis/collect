package org.openforis.collect.relational;

public interface Column<T> {

	public abstract String getName();

	public abstract int getType();

	public abstract Integer getLength();

	public abstract boolean isAllowNulls();
	
	public Object extractValue(T source);

}