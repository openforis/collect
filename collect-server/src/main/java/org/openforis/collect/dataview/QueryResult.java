package org.openforis.collect.dataview;

import java.util.ArrayList;
import java.util.List;

public class QueryResult {
	
	private List<QueryResultRow> rows = new ArrayList<QueryResultRow>();
	
	public void addRow(QueryResultRow row) {
		rows.add(row);
	}

	public List<QueryResultRow> getRows() {
		return rows;
	}

	public void setRows(List<QueryResultRow> rows) {
		this.rows = rows;
	}
}