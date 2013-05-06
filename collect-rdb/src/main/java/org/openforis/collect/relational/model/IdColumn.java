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
		super(name, Types.BIGINT, "bigint", null, false);
	}
	
}
