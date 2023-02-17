package org.openforis.idm.model;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class NodePointers {

	public static Set<Node<?>> pointersToNodes(Collection<NodePointer> pointers) {
		Set<Node<?>> result = new HashSet<Node<?>>();
		for (NodePointer pointer : pointers) {
			result.addAll(pointer.getNodes());
		}
		return result;
	}
	
	public static Set<NodePointer> nodesToPointers(Collection<? extends Node<?>> nodes) {
		Set<NodePointer> result = new HashSet<NodePointer>();
		for (Node<?> n : nodes) {
			result.add(new NodePointer(n));
		}
		return result;
	}

	public static boolean containNode(Collection<NodePointer> nodePointers, Node<?> node) {
		for (NodePointer np : nodePointers) {
			if (np.getNodes().contains(node)) {
				return true;
			}
		}
		return false;
	}

}
