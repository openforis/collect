/**
 * 
 */
package org.openforis.collect.relational.model;

import java.sql.Types;

/**
 * @author S. Ricci
 *
 */
public abstract class IdColumn<T> extends AbstractColumn<T> {

	IdColumn(String name) {
		this(name, false);
	}
	
	IdColumn(String name, boolean nullable) {
		super(name, Types.BIGINT, "bigint", null, nullable);
	}
	
}
