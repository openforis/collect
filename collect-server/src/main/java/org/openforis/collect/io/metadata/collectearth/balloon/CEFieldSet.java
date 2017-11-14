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
	
	public CEFieldSet(String name, String label, String tooltip) {
		super(null, name, label, tooltip, false);
	}

	public void addChild(CEComponent child) {
		children.add(child);
	}
	
	public List<CEComponent> getChildren() {
		return children;
	}
	
}
