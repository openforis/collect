package org.openforis.collect.metamodel.view;

import java.util.ArrayList;
import java.util.List;

public class CodeListView extends SurveyObjectView {
	
	private String name;
	private String label;
	private List<CodeListItemView> items = new ArrayList<CodeListItemView>();
	
	public void addItem(CodeListItemView item) {
		items.add(item);
	}

	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getLabel() {
		return label;
	}
	
	public void setLabel(String label) {
		this.label = label;
	}
	
	public List<CodeListItemView> getItems() {
		return items;
	}
	
	public void setItems(List<CodeListItemView> items) {
		this.items = items;
	}

	
}