package org.openforis.collect.relational.model;

import org.openforis.idm.model.Node;

/**
 * 
 * @author G. Miceli
 * @author S. Ricci
 *
 */
public class DataAncestorFKColumn extends IdColumn<Node<?>> {

	private final int ancestorDefinitionId;
	
	DataAncestorFKColumn(String name, int ancestorDefinitionId) {
		super(name);
		this.ancestorDefinitionId = ancestorDefinitionId;
	}
	
	public int getAncestorDefinitionId() {
		return ancestorDefinitionId;
	}

}
