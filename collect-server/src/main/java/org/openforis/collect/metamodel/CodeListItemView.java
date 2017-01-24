package org.openforis.collect.metamodel;

import java.util.ArrayList;
import java.util.List;

public class CodeListItemView extends SurveyObjectView {
	
	String code;
	String label;
	private String color;
	
	List<CodeListItemView> items = new ArrayList<CodeListItemView>();

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getColor() {
		return color;
	}

	public void setColor(String color) {
		this.color = color;
	}

	public List<CodeListItemView> getItems() {
		return items;
	}

	public void setItems(List<CodeListItemView> items) {
		this.items = items;
	}
	
}