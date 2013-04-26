package org.openforis.collect.relational.model;

/**
 * 
 * @author G. Miceli
 *
 */
public class ReferentialConstraint extends Constraint {

	public UniquenessConstraint referencedKey;
	
    ReferentialConstraint(String name, Table<?> table, UniquenessConstraint referencedKey, Column<?>... columns) {
		super(name, table, columns);
		this.referencedKey = referencedKey;
	}

    public UniquenessConstraint getReferencedKey() {
		return referencedKey;
	}
}