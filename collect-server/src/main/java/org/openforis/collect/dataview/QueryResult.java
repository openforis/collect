package org.openforis.collect.dataview;

import java.util.ArrayList;
import java.util.List;

public class QueryResult {
	
	private List<QueryResultRow> rows = new ArrayList<QueryResultRow>();
	private int totalRecords;
	
	public void addRow(QueryResultRow row) {
		rows.add(row);
	}

	public List<QueryResultRow> getRows() {
		return rows;
	}

	public void setRows(List<QueryResultRow> rows) {
		this.rows = rows;
	}

	public int getTotalRecords() {
		return totalRecords;
	}
	
	public void setTotalRecords(int totalRecords) {
		this.totalRecords = totalRecords;
	}
}