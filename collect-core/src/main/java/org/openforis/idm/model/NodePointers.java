package org.openforis.idm.model;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.NodeDefinition;

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
	
	public static Set<NodePointer> pointersToDescendantPointers(Collection<NodePointer> pointers) {
		final Set<NodePointer> result = new HashSet<NodePointer>();

		NodeVisitor nodePointerCreatorVisitor = new NodeVisitor() {
			public void visit(Node<? extends NodeDefinition> descendant, int idx) {
				result.add(new NodePointer(descendant));
			}
		};

		for (NodePointer pointer : pointers) {
			if (pointer.getChildDefinition() instanceof EntityDefinition) {
				for (Node<?> node: pointer.getNodes()) {
					((Entity) node).traverseDescendants(nodePointerCreatorVisitor);
				}
			}
		}
		return result;
	}

}
