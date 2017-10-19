package org.openforis.collect.io.metadata.collectearth.balloon;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author S. Ricci
 *
 */
public class CEFieldSet extends CEComponent {

	private List<CEComponent> children = new ArrayList<CEComponent>();
	
	public CEFieldSet(String name, String label ) {
		super(null, name, label, false, null);
	}

	public void addChild(CEComponent child) {
		children.add(child);
	}
	
	public List<CEComponent> getChildren() {
		return children;
	}
	
}
