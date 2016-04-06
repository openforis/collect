/**
 * 
 */
package org.openforis.collect.relational.model;

import org.openforis.collect.relational.sql.RDBJdbcType;

/**
 * @author S. Ricci
 *
 */
public abstract class IdColumn<T> extends AbstractColumn<T> {

	IdColumn(String name) {
		this(name, false);
	}
	
	IdColumn(String name, boolean nullable) {
		super(name, RDBJdbcType.BIGINT, null, nullable);
	}
	
}
