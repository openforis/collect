package org.openforis.collect.relational.model;

/**
 * 
 * @author G. Miceli
 *
 */
public class UniquenessConstraint extends Constraint {

	UniquenessConstraint(String name, Table<?> table, Column<?>... columns) {
		super(name, table, columns);
	}
}