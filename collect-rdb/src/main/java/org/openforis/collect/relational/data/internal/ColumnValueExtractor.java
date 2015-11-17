package org.openforis.collect.relational.data.internal;

import org.openforis.collect.relational.model.Column;
import org.openforis.collect.relational.model.Table;
import org.openforis.idm.model.Node;

/**
 * 
 * @author S. Ricci
 *
 */
public abstract class ColumnValueExtractor<T extends Table<?>, C extends Column<?>> {

	protected T table;
	protected C column;
	
	public ColumnValueExtractor(T table, C column) {
		this.table = table;
		this.column = column;
	}
	
	public abstract Object extractValue(Node<?> context);
	
}
