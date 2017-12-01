package org.openforis.collect.metamodel.view;

import java.util.ArrayList;
import java.util.List;

public class CodeListView extends SurveyObjectView {
	
	private String name;
	List<CodeListItemView> items = new ArrayList<CodeListItemView>();
	
	public List<CodeListItemView> getItems() {
		return items;
	}
	
	public void setItems(List<CodeListItemView> items) {
		this.items = items;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
}