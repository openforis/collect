package org.openforis.collect.relational.data;

import org.openforis.collect.relational.model.Column;

/***
 * 
 * @author S. Ricci
 *
 */
public class ColumnValuePair<C extends Column<?>, T> {

	private C column;
	private T value;
	
	public ColumnValuePair(C column, T value) {
		super();
		this.column = column;
		this.value = value;
	}
	
	public C getColumn() {
		return column;
	}
	
	public T getValue() {
		return value;
	}
	
	@Override
	public String toString() {
		return column + " = " + value;
	}
}
