package org.openforis.collect.relational.data;

import java.util.Iterator;

/**
 * 
 * @author S. Ricci
 *
 */
public abstract class DataExtractor implements Iterator<Row> {

	@Override
	public abstract boolean hasNext();

	@Override
	public abstract Row next();

	@Override
	public void remove() {
	}

}
