package org.openforis.idm.relational;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 
 * @author G. Miceli
 * 
 */
public final class Row {

	private Table table;
	private List<Object> values;

	Row(Table table) {
		this.table = table;
		this.values = new ArrayList<Object>(table.getColumns().size());
	}
	
	public Table getTable() {
		return table;
	}
	
	public List<Object> getValues() {
		return Collections.unmodifiableList(values);
	}
}