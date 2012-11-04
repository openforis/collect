package org.openforis.collect.relational;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * 
 * @author G. Miceli
 *
 */
public final class Dataset {
	private List<Row> rows;
	
	Dataset() {
		rows = new ArrayList<Row>();
	}
	
	public List<Row> getRows() {
		return Collections.unmodifiableList(rows);
	}
	
	void addRow(Row row) {
		rows.add(row);
	}
	
	void printDebug(PrintStream out) {
		for (Row row : rows) {
			row.printDebug(out);
		}
	}
}