package org.openforis.collect.model;

import java.util.List;

import org.openforis.idm.model.Node;

/**
 * Change related to the delete of a Node in a Record.
 * 
 * @author S. Ricci
 *
 */
public class NodeDeleteChange extends NodeChange<Node<?>> {
	
	public NodeDeleteChange(Integer recordId, List<Integer> ancestoIds, Node<?> node) {
		super(recordId, ancestoIds, node);
	}
	
}