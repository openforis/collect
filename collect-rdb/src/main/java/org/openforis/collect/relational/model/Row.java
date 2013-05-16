package org.openforis.collect.relational.model;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * 
 * @author G. Miceli
 * 
 */
public final class Row {

	private Table<?> table;
	private List<Object> values;

	Row(Table<?> table) {
		this.table = table;
		int cnt = table.getColumns().size();
		this.values = Arrays.asList(new Object[cnt]);
	}
	
	public Table<?> getTable() {
		return table;
	}
	
	public List<Object> getValues() {
		return Collections.unmodifiableList(values);
	}
	
	void setValue(int i, Object o) {
		values.set(i, o);
	}
	
	void printDebug(PrintStream out) {
		List<Column<?>> cols = table.getColumns();
		out.print(table.getName());
		out.print(": ");
		for (int i=0; i < cols.size(); i++) {
			if ( i>0 ) out.print(", ");
			Column<?> col = cols.get(i);
			Object value = values.get(i);
			out.printf("%s=%s", col.getName(), value);
		}
		out.println();
	}
}