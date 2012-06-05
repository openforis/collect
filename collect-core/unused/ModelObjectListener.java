/**
 * 
 */
package org.openforis.idm.model.impl;

import java.util.ArrayList;
import java.util.List;

import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.model.Node;

/**
 * @author M. Togna
 * 
 */
public class NodeListener {

	private List<Node<? extends NodeDefinition>> changedObjects;

	public NodeListener() {
		this.changedObjects = new ArrayList<Node<? extends NodeDefinition>>();
	}

	public void onStateChange(Node<? extends NodeDefinition> node) {
		this.changedObjects.add(node);
	}

	public void clear() {
		this.changedObjects = new ArrayList<Node<? extends NodeDefinition>>();
	}

}
