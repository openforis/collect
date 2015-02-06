package org.openforis.collect.io.metadata.collectearth.balloon;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author S. Ricci
 * @author A. Sanchez-Paus Diaz
 *
 */
class CEEntity extends CEComponent {

	private List<CEComponent> children = new ArrayList<CEComponent>();
	
	public CEEntity(String name, String label, boolean multiple) {
		super(null, name, label, multiple);
	}
	
	public void addChild(CEComponent child) {
		children.add(child);
	}
	
	public List<CEComponent> getChildren() {
		return children;
	}
	
}