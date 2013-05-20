package org.openforis.collect.model;

import org.openforis.idm.model.Node;

/**
 * Change related to the delete of a Node in a Record.
 * 
 * @author S. Ricci
 *
 */
public class NodeDeleteChange extends NodeChange<Node<?>> {
	
	public NodeDeleteChange(Node<?> node) {
		super(node);
	}
	
}