package org.openforis.collect.relational;

public interface Column<T> {

	public abstract String getName();

	public abstract int getType();

	public abstract Integer getLength();

	public abstract boolean isNullable();
	
	public Object extractValue(T source);

}