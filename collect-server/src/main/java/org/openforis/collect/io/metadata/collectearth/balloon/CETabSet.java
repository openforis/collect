package org.openforis.collect.io.metadata.collectearth.balloon;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author S. Ricci
 *
 */
public class CETabSet extends CEFieldSet {

	private List<CETab> tabs;
	
	public CETabSet(String name, String label) {
		super(name, label, null);
		tabs = new ArrayList<CETab>();
	}
	
	public void addTab(CETab tab) {
		tabs.add(tab);
	}
	
	public List<CETab> getTabs() {
		return tabs;
	}
}
