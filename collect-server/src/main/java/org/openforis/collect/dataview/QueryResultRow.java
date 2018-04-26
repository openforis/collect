package org.openforis.collect.dataview;

import java.util.ArrayList;
import java.util.List;

public class QueryResultRow {
	
	private List<String> values = new ArrayList<String>();
	private int recordId;
	
	public void addValue(String value) {
		values.add(value);
	}

	public List<String> getValues() {
		return values;
	}
	
	public void setValues(List<String> values) {
		this.values = values;
	}

	public int getRecordId() {
		return recordId;
	}
	
	public void setRecordId(int recordId) {
		this.recordId = recordId;
	}
}