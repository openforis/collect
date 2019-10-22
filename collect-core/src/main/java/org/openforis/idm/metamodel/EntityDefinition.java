/**
 *
 */
package org.openforis.idm.metamodel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import org.apache.commons.lang3.ArrayUtils;
import org.openforis.collect.metamodel.CollectAnnotations;
import org.openforis.collect.model.CollectSurvey;
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
	private boolean virtual; //if true, the entity data comes from another entity
	/**
	 * Used when "virtual" is true to generate the data belonging to this entity.
	 * The expression must always give a node set of entities with attributes compatible
	 * with the attributes defined as child nodes of this entity.
	 */
	private String generatorExpression; 
	
	//cache
    private Map<String, NodeDefinition> childDefinitionByName;
    private Map<Integer, NodeDefinition> childDefinitionById;
    private String[] childDefinitionNames;
    
	public enum TraversalType {
		DFS //depth first search
		,
		BFS //breadth first search
	}

	EntityDefinition(Survey survey, int id) {
		super(survey, id);
		initializeCache();
	}

	EntityDefinition(Survey survey, EntityDefinition def, int nextId, String... ignoreChildDefinitions) {
		super(survey, def, nextId);
		initializeCache();
		this.virtual = def.virtual;
		this.generatorExpression = def.generatorExpression;
		List<NodeDefinition> childDefs = def.getChildDefinitions();
		List<String> ignoreChildDefList;
		if (ignoreChildDefinitions == null) {
			ignoreChildDefList = Collections.emptyList();
		} else {
			ignoreChildDefList = Arrays.asList(ignoreChildDefinitions);
		}
		for (NodeDefinition childDef : childDefs) {
			if (! ignoreChildDefList.contains(childDef.getName())) {
				addChildDefinition(survey.getSchema().cloneDefinition(childDef));
			}
		}
	}
	
	private void initializeCache() {
		childDefinitionNames = new String[0];
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
		updateChildDefinitionNames();
	}
	
	@Override
	protected void resetPath() {
		super.resetPath();
		for (NodeDefinition child : getChildDefinitions()) {
			child.resetPath();
		}
	}
	
	@Override
	public boolean isAlwaysRequired() {
		return isRoot() ? true : super.isAlwaysRequired();
	}

	public boolean isRoot() {
		return getParentDefinition() == null;
	}

	public String[] getChildDefinitionNames() {
		return childDefinitionNames;
	}
	
	public List<NodeDefinition> getChildDefinitions() {
		return CollectionUtils.unmodifiableList(childDefinitions);
	}
	
	public List<NodeDefinition> getChildDefinitionsInVersion(ModelVersion version) {
		if (version == null) {
			return childDefinitions;
		}
		List<NodeDefinition> result = new ArrayList<NodeDefinition>(childDefinitions.size());
		for (NodeDefinition nodeDef : childDefinitions) {
			if (version.isApplicable(nodeDef)) {
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
		String name = defn.getName();
		if ( name == null ) {
			throw new NullPointerException("name");
		}

		if ( defn.isDetached() ) {
			throw new IllegalArgumentException("Detached definitions cannot be added");
		}
		childDefinitionNames = ArrayUtils.add(childDefinitionNames, name);
		childDefinitions.add(defn);
        childDefinitionByName.put(name, defn);
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
		defn.setParentDefinition(null);
		updateChildDefinitionNames();
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
		return findDescendantDefinitions(new NodeDefinitionVerifier() {
			public boolean verify(NodeDefinition definition) {
				return definition instanceof KeyAttributeDefinition && ((KeyAttributeDefinition) definition).isKey();
			}
		}, false, true);
	}
	
	public <N extends NodeDefinition> N findDescendantDefinition(NodeDefinitionVerifier verifier) {
		@SuppressWarnings("unchecked")
		List<N> nodeDefns = (List<N>) findDescendantDefinitions(verifier, true);
		return nodeDefns.isEmpty() ? null: nodeDefns.get(0);
	}
	
	public <N extends NodeDefinition> List<N> findDescendantDefinitions(NodeDefinitionVerifier verifier) {
		return findDescendantDefinitions(verifier, false);
	}
	
	public <N extends NodeDefinition> List<N> findDescendantDefinitions(NodeDefinitionVerifier verifier, boolean stopAfterFirstFound) {
		return findDescendantDefinitions(verifier, stopAfterFirstFound, false);
	}
	
	@SuppressWarnings("unchecked")
	public <N extends NodeDefinition> List<N> findDescendantDefinitions(NodeDefinitionVerifier verifier, 
			boolean stopAfterFirstFound, boolean onlyInsideSingleEntities) {
		Deque<NodeDefinition> stack = new LinkedList<NodeDefinition>();
		List<N> foundNodeDefns = new ArrayList<N>();
		stack.addAll(childDefinitions);
		while (! stack.isEmpty()) {
			NodeDefinition defn = stack.pop();
			if (verifier.verify(defn)) {
				foundNodeDefns.add((N) defn);
				if (stopAfterFirstFound) {
					break;
				}
			}
			if (defn instanceof EntityDefinition && (!onlyInsideSingleEntities || !defn.isMultiple())) {
				stack.addAll(((EntityDefinition) defn).childDefinitions);
			}
		}
		return foundNodeDefns;
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
		dfsTraverse(visitor, false);
	}
	
	protected void dfsTraverse(NodeDefinitionVisitor visitor, boolean onlyInsideSingleEntities) {
		// Initialize stack with root entity
		Deque<NodeDefinition> stack = new LinkedList<NodeDefinition>();
		stack.push(this);
		// While there are still nodes to insert
		while (!stack.isEmpty()) {
			// Pop the next list of nodes to insert
			NodeDefinition defn = stack.pop();

			// Pre-order operation
			visitor.visit(defn);

			// For entities, add existing child nodes to the stack
			if (defn instanceof EntityDefinition && !(onlyInsideSingleEntities && defn.isMultiple())) {
				stack.addAll(((EntityDefinition) defn).getChildDefinitions());
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
	
	/**
	 * Returns attributes inside current entity and in nested single entities
	 */
	public List<AttributeDefinition> getNestedAttributes() {
		final List<AttributeDefinition> result = new ArrayList<AttributeDefinition>();
		Queue<EntityDefinition> queue = new LinkedList<EntityDefinition>();
		queue.add(this);
		while (! queue.isEmpty()) {
			EntityDefinition entityDef = queue.poll();
			List<NodeDefinition> childDefinitions = entityDef.getChildDefinitions();
			for (NodeDefinition nodeDef : childDefinitions) {
				if (nodeDef instanceof AttributeDefinition) {
					result.add((AttributeDefinition) nodeDef);
				} else if (! nodeDef.isMultiple()) {
					queue.add((EntityDefinition) nodeDef);
				}
			}
		}
		return result;
	}

	@Override
	public Node<?> createNode() {
		return new Entity(this);
	}

	@Override
	void detach() {
		if ( childDefinitions != null ) {
			for (NodeDefinition child : childDefinitions) {
				child.detach();
			}
		}
		super.detach();
	}

	/**
	 *
	 * @return true if the entity definition has key attributes only of type code (not lookup)
	 */
	public boolean isEnumerable() {
		CodeAttributeDefinition enumeratingKeyCodeAttribute = getEnumeratingKeyCodeAttribute();
		return enumeratingKeyCodeAttribute != null;
	}
	
	public boolean isEnumerate() {
		//TODO don't use annotations
		CollectSurvey survey = getSurvey();
		CollectAnnotations annotations = survey.getAnnotations();
		return annotations.isEnumerate(this);
	}
	
	public void setEnumerate(boolean enumerate) {
		CollectSurvey survey = getSurvey();
		CollectAnnotations annotations = survey.getAnnotations();
		annotations.setEnumerate(this, enumerate);
	}

	public CodeAttributeDefinition getEnumeratingKeyCodeAttribute() {
		return getEnumeratingKeyCodeAttribute(null);
	}

	public CodeAttributeDefinition getEnumeratingKeyCodeAttribute(ModelVersion version) {
		List<AttributeDefinition> keyDefns = getKeyAttributeDefinitions();
		for (AttributeDefinition keyDefn: keyDefns) {
			if (version == null || version.isApplicable(keyDefn) ) {
				if ( keyDefn instanceof CodeAttributeDefinition && !((CodeAttributeDefinition) keyDefn).getList().isExternal() ) {
					return (CodeAttributeDefinition) keyDefn;
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
	
	private void updateChildDefinitionNames() {
		Set<String> names = childDefinitionByName.keySet();
		childDefinitionNames = names.toArray(new String[names.size()]);
	}
	
	public Set<EntityDefinition> calculateDependingVirtualEntities() {
		final Set<EntityDefinition> result = new HashSet<EntityDefinition>();
		traverse(new NodeDefinitionVisitor() {
			public void visit(NodeDefinition definition) {
				if (definition instanceof AttributeDefinition) {
					AttributeDefinition referencedAttribute = ((AttributeDefinition) definition).getReferencedAttribute();
					if (referencedAttribute != null) {
						EntityDefinition ancestorMultipleEntity = referencedAttribute.getNearestAncestorMultipleEntity();
						List<NodeDefinition> childDefinitions = ancestorMultipleEntity.getChildDefinitions();
						for (NodeDefinition childDef : childDefinitions) {
							if (childDef instanceof EntityDefinition && ((EntityDefinition) childDef).isVirtual()) {
								result.add((EntityDefinition) childDef);
							}
							
						}
					}
				}
			}
		});
		return result;
	}
	
	public boolean isVirtual() {
		return virtual;
	}
	
	public void setVirtual(boolean virtual) {
		this.virtual = virtual;
	}
	
	public String getGeneratorExpression() {
		return generatorExpression;
	}
	
	public void setGeneratorExpression(String generatorExpression) {
		this.generatorExpression = generatorExpression;
	}

	@Override
	public boolean deepEquals(Object obj) {
		if (this == obj)
			return true;
		if (!super.deepEquals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		EntityDefinition other = (EntityDefinition) obj;
		if (childDefinitions == null) {
			if (other.childDefinitions != null)
				return false;
		} else if (! CollectionUtils.deepEquals(childDefinitions, other.childDefinitions))
			return false;
		return true;
	}

}
