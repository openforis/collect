package org.openforis.collect.relational;

/**
 * 
 * @author G. Miceli
 * @author M. Togna
 *
 */
public class PrimaryKeyConstraint extends UniquenessConstraint {

	PrimaryKeyConstraint(String name, Table<?> table, Column<?>... columns) {
		super(name, table, columns);
	}
}