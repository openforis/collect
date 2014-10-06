package org.openforis.collect.model;


import org.openforis.idm.model.Node;

/**
 * Change related to a Node in a Record.
 * 
 * 
 * @author S. Ricci
 */
public abstract class NodeChange<T extends Node<?>> {

	protected T node;
	
	public NodeChange(T node) {
		this.node = node;
	}

	public T getNode() {
		return node;
	}

	@Override
	public String toString() {
		return String.format("%s for node %s", 
				this.getClass().getSimpleName(), 
				node.getPath());
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((node == null) ? 0 : node.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		NodeChange<?> other = (NodeChange<?>) obj;
		if (node == null) {
			if (other.node != null)
				return false;
		} else if (!node.equals(other.node))
			return false;
		return true;
	}
	
}