package org.openforis.collect.relational.data;

import java.util.Iterator;

import org.openforis.collect.relational.model.Table;

/**
 * 
 * @author S. Ricci
 *
 */
public abstract class DataExtractor implements Iterator<Row> {

	public abstract Table<?> getTable();
	
	@Override
	public abstract boolean hasNext();

	@Override
	public abstract Row next();

	@Override
	public void remove() {
	}

}
