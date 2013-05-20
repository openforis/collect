package org.openforis.collect.model;

import org.openforis.idm.model.Entity;

/**
 * Change related to an Entity that has just been added to a Record.
 * 
 * @author S. Ricci
 *
 */
public class EntityAddChange extends EntityChange implements NodeAddChange {
	
	public EntityAddChange(Entity node) {
		super(node);
	}
	
}