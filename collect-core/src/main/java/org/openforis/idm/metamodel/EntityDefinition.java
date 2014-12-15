/**
 *
 */
package org.openforis.idm.metamodel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.Stack;

import org.openforis.commons.collection.CollectionUtils;
import org.openforis.idm.model.Entity;
import org.openforis.idm.model.Node;

/**
 * @author G. Miceli
 * @author M. Togna
 * @author S. Ricci
 */
public class EntityDefinition extends NodeDefinition {

	private static final long serialVersionUID = 1L;

	private List<NodeDefinition> childDefinitions;
    private Map<String, NodeDefinition> childDefinitionByName;
    private Map<Integer, NodeDefinition> childDefinitionById;

	public enum TraversalType {
		DFS //depth first search
		,
		BFS //breadth first search
	}

	EntityDefinition(Survey survey, int id) {
		super(survey, id);
		childDefinitions = new ArrayList<NodeDefinition>();
		childDefinitionByName = new HashMap<String, NodeDefinition>();
        childDefinitionById = new HashMap<Integer, NodeDefinition>();
	}

	void renameChild(String oldName, String newName) {
		if ( childDefinitionByName.containsKey(newName) ) {
			throw new IllegalArgumentException(String.format("Definition with name %s already exist in entity %s", newName, this.getPath()));
		}
		NodeDefinition childDef = childDefinitionByName.get(oldName);
		childDefinitionByName.remove(oldName);
		childDefinitionByName.put(newName, childDef);
	}

	public boolean isRoot() {
		return getParentDefinition() == null;
	}

	public Set<String> getChildDefinitionNames() {
		return childDefinitionByName.keySet();
	}
	
	public List<NodeDefinition> getChildDefinitions() {
		return CollectionUtils.unmodifiableList(childDefinitions);
	}
	
	public List<NodeDefinition> getChildDefinitionsInVersion(ModelVersion version) {
		List<NodeDefinition> result = new ArrayList<NodeDefinition>(childDefinitions.size());
		for (NodeDefinition nodeDef : childDefinitions) {
			if (version == null || version.isApplicable(nodeDef)) {
				result.add(nodeDef);
			}
		}
		return result;
	}

	public NodeDefinition getChildDefinition(String name) {
        NodeDefinition nodeDefinition = childDefinitionByName.get(name);
        if (nodeDefinition != null) {
            return nodeDefinition;
        }
		throw new IllegalArgumentException("Child definition " + name + " not found in " + getPath());
	}

	public boolean containsChildDefinition(String name) {
        return childDefinitionByName.containsKey(name);
    }

	public NodeDefinition getChildDefinition(int id) {
		NodeDefinition childDef = childDefinitionById.get(id);
		if (childDef == null) {
			throw new IllegalArgumentException("Child definition with id " + id +
					" not found in " + getPath());
		} else {
			return childDef;
		}
	}

	/**
	 * Get child definition and cast to requested type
	 *
	 * @throws IllegalArgumentException if not defined in model or if not
	 * assignable from type defined in definitionClass
	 */
	@SuppressWarnings("unchecked")
	public <T extends NodeDefinition> T getChildDefinition(String name, Class<T> definitionClass) {
		NodeDefinition childDefinition = getChildDefinition(name);
		if (!childDefinition.getClass().isAssignableFrom(definitionClass)) {
			throw new IllegalArgumentException(childDefinition.getPath() +
					" is not a " + definitionClass.getSimpleName());
		}
		return (T) childDefinition;
	}

	public int getChildDefinitionIndex(NodeDefinition defn) {
		if ( childDefinitions != null ) {
			int result = childDefinitions.indexOf(defn);
			if ( result < 0 ) {
				throw new IllegalArgumentException(this.getPath() + "- child not found:" + defn.getName());
			}
			return result;
		} else {
			throw new IllegalArgumentException(this.getPath() + " has no children");
		}
	}

	public void addChildDefinition(NodeDefinition defn) {
		if ( defn.getName() == null ) {
			throw new NullPointerException("name");
		}

		if ( defn.isDetached() ) {
			throw new IllegalArgumentException("Detached definitions cannot be added");
		}
		childDefinitions.add(defn);
        childDefinitionByName.put(defn.getName(), defn);
        childDefinitionById.put(defn.getId(), defn);
		defn.setParentDefinition(this);
	}

	public void removeChildDefinition(int id) {
		NodeDefinition childDefn = getChildDefinition(id);
		removeChildDefinition(childDefn);
	}

	public void removeChildDefinition(NodeDefinition defn) {
		removeChildDefinition(defn, true);
	}

	protected void removeChildDefinition(NodeDefinition defn, boolean detach) {
		childDefinitions.remove(defn);
        childDefinitionByName.remove(defn.getName());
        childDefinitionById.remove(defn.getId());
		if ( detach ) {
			defn.detach();
		}
	}

	public void moveChildDefinition(int id, int index) {
		NodeDefinition defn = getChildDefinition(id);
		moveChildDefinition(defn, index);
	}

	public void moveChildDefinition(NodeDefinition defn, int newIndex) {
		CollectionUtils.shiftItem(childDefinitions, defn, newIndex);
	}

	/**
	 * Returns all key attribute definitions for this entity.
	 * The key attribute definitions can even be defined inside nested single entities.
	 */
	public List<AttributeDefinition> getKeyAttributeDefinitions() {
		List<AttributeDefinition> result = new LinkedList<AttributeDefinition>();
		Queue<NodeDefinition> queue = new LinkedList<NodeDefinition>(getChildDefinitions());
		while ( ! queue.isEmpty() ) {
			NodeDefinition nodeDefn = queue.remove();
			if ( nodeDefn instanceof KeyAttributeDefinition && ((KeyAttributeDefinition) nodeDefn).isKey() ) {
				result.add((AttributeDefinition) nodeDefn);
			} else if ( nodeDefn instanceof EntityDefinition && ! nodeDefn.isMultiple() ) {
				queue.addAll(((EntityDefinition) nodeDefn).getChildDefinitions());
			}
		}
		return Collections.unmodifiableList(result);
	}

	public void traverse(NodeDefinitionVisitor visitor) {
		//use depth first search by default
		traverse(visitor, TraversalType.DFS);
	}

	public void traverse(NodeDefinitionVisitor visitor, TraversalType traversalType) {
		switch ( traversalType ) {
		case BFS:
			bfsTraverse(visitor);
			break;
		default:
			dfsTraverse(visitor);
		}
	}

	/**
	 *
	 * Pre-order depth-first traversal from here down.
	 */
	protected void dfsTraverse(NodeDefinitionVisitor visitor) {
		// Initialize stack with root entity
		Stack<NodeDefinition> stack = new Stack<NodeDefinition>();
		stack.push(this);
		// While there are still nodes to insert
		while (!stack.isEmpty()) {
			// Pop the next list of nodes to insert
			NodeDefinition defn = stack.pop();

			// Pre-order operation
			visitor.visit(defn);

			// For entities, add existing child nodes to the stack
			if (defn instanceof EntityDefinition ) {
				List<NodeDefinition> children = ((EntityDefinition) defn).getChildDefinitions();
				if ( ! children.isEmpty() ) {
					@SuppressWarnings({ "rawtypes", "unchecked" })
					List<NodeDefinition> reversedChildren = new ArrayList(children);
					Collections.reverse(reversedChildren);
					stack.addAll(reversedChildren);
				}
			}
		}
	}

	/**
	 * Breadth-First traversal from here down.
	 *
	 */
	protected void bfsTraverse(NodeDefinitionVisitor visitor) {
		Queue<NodeDefinition> queue = new LinkedList<NodeDefinition>();
		queue.add(this);
		while ( ! queue.isEmpty() ) {
			NodeDefinition defn = queue.poll();

			visitor.visit(defn);

			// For entities, add existing child nodes to the stack
			if (defn instanceof EntityDefinition) {
				queue.addAll(((EntityDefinition) defn).getChildDefinitions());
			}
		}
	}

	@Override
	public Node<?> createNode() {
		return new Entity(this);
	}

	@Override
	public void detach() {
		if ( childDefinitions != null ) {
			for (NodeDefinition child : childDefinitions) {
				child.detach();
			}
		}
		super.detach();
	}

	/**
	 *
	 * @return true if entities with only keys of type internal code (not lookup)
	 */
	public boolean isEnumerable() {
		CodeAttributeDefinition enumeratingKeyCodeAttribute = getEnumeratingKeyCodeAttribute();
		return enumeratingKeyCodeAttribute != null;
	}

	public CodeAttributeDefinition getEnumeratingKeyCodeAttribute() {
		return getEnumeratingKeyCodeAttribute(null);
	}

	public CodeAttributeDefinition getEnumeratingKeyCodeAttribute(ModelVersion version) {
		List<AttributeDefinition> keyDefns = getKeyAttributeDefinitions();
		for (AttributeDefinition keyDefn: keyDefns) {
			if (version == null || version.isApplicable(keyDefn) ) {
				if ( keyDefn instanceof CodeAttributeDefinition ) {
					CodeAttributeDefinition codeDefn = (CodeAttributeDefinition) keyDefn;
					CodeList list = codeDefn.getList();
					if ( ! list.isExternal() ) {
						return codeDefn;
					}
				} else {
					return null;
				}
			}
		}
		return null;
	}

	public boolean isAncestorOf(NodeDefinition nodeDefn) {
		EntityDefinition parent = nodeDefn.getParentEntityDefinition();
		while ( parent != null ) {
			if ( parent == this ) {
				return true;
			}
			parent = parent.getParentEntityDefinition();
		}
		return false;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((childDefinitions == null) ? 0 : childDefinitions.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		EntityDefinition other = (EntityDefinition) obj;
		if (childDefinitions == null) {
			if (other.childDefinitions != null)
				return false;
		} else if (!childDefinitions.equals(other.childDefinitions))
			return false;
		return true;
	}

}
