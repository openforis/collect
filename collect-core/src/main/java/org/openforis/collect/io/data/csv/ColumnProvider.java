package org.openforis.collect.io.data.csv;

import java.util.List;

import org.openforis.idm.model.Node;

/**
 * @author G. Miceli
 */
public interface ColumnProvider {
	
	List<Column> getColumns();
	
	List<Object> extractValues(Node<?> n);
	
}
