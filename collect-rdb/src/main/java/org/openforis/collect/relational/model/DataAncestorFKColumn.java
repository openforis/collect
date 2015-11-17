package org.openforis.collect.relational.model;

import org.openforis.idm.model.Node;

/**
 * 
 * @author S. Ricci
 *
 */
public class DataAncestorFKColumn extends IdColumn<Node<?>> {

	private final int ancestorDefinitionId;
	private int level;
	
	DataAncestorFKColumn(String name, int ancestorDefinitionId, int level) {
		super(name);
		this.ancestorDefinitionId = ancestorDefinitionId;
		this.level = level;
	}
	
	public boolean isParentFKColumn() {
		return level == 1;
	}
	
	public int getAncestorDefinitionId() {
		return ancestorDefinitionId;
	}
	
	public int getLevel() {
		return level;
	}

}
