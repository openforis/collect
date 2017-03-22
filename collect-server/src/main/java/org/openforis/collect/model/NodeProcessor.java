package org.openforis.collect.model;

import org.openforis.idm.model.Node;

/**
 * 
 * @author S. Ricci
 *
 */
public abstract interface NodeProcessor {
	
	void process(Node<?> node) throws Exception;
	
}