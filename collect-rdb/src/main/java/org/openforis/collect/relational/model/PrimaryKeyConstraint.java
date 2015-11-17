package org.openforis.collect.relational.model;

/**
 * 
 * @author G. Miceli
 * @author M. Togna
 *
 */
public class PrimaryKeyConstraint extends UniquenessConstraint {

	PrimaryKeyConstraint(String name, Table<?> table, Column<?> column) {
		super(name, table, column);
	}
	
	public Column<?> getPrimaryKeyColumn() {
		return getColumns().get(0);
	}
}