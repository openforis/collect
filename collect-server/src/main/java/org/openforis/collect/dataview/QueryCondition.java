package org.openforis.collect.dataview;

import java.util.ArrayList;
import java.util.List;

public class QueryCondition {

	public enum Type {
		EQ, //equal to value
		IN, //in (list of values)
		BTW, //between (min and max)
		GT, //greater than value
		GE, //greater or equal to value
		LT, //less than value
		LE //less or equal to value
	}
	
	private Type type;
	private String value;
	private List<String> inValues = new ArrayList<String>();
	private String min;
	private String max;
	
	public Type getType() {
		return type;
	}
	
	public void setType(Type type) {
		this.type = type;
	}
	
	public String getValue() {
		return value;
	}
	
	public void setValue(String value) {
		this.value = value;
	}

	public List<String> getInValues() {
		return inValues;
	}
	
	public void setInValues(List<String> inValues) {
		this.inValues = inValues;
	}

	public String getMin() {
		return min;
	}

	public void setMin(String min) {
		this.min = min;
	}

	public String getMax() {
		return max;
	}

	public void setMax(String max) {
		this.max = max;
	}
}
