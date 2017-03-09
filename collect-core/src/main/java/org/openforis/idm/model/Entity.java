/**
 * 
 */
package org.openforis.idm.model;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.lang3.StringUtils;
import org.openforis.commons.collection.CollectionUtils;
import org.openforis.idm.metamodel.AttributeDefinition;
import org.openforis.idm.metamodel.CodeAttributeDefinition;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.metamodel.validation.ValidationResultFlag;
import org.openforis.idm.path.Path;

/**
 * @author G. Miceli
 * @author M. Togna
 * @author S. Ricci
 * 
 */
public class Entity extends Node<EntityDefinition> {

	private static final long serialVersionUID = 1L;
	
	List<Node<?>> children;
	Map<NodeDefinition, List<Node<?>>> childrenByDefinition;
	private ValidationState derivedStateCache;
	Map<NodeDefinition, State> childStates;
	
	public Entity(EntityDefinition definition) {
		super(definition);
		this.children = new ArrayList<Node<?>>();
		this.childrenByDefinition = new TreeMap<NodeDefinition, List<Node<?>>>();
		this.derivedStateCache = new ValidationState();
		this.childStates = new TreeMap<NodeDefinition, State>();
	}

	@Override
	protected void setRecord(Record record) {
		super.setRecord(record);
		List<Node<?>> children = getChildren();
		for (Node<?> node : children) {
			node.setRecord(record);
		}
	}
	
	public boolean isRoot() {
		return this == record.getRootEntity();
	}
	
	public void add(Node<?> node) {
		addInternal(node, null);
	}
	
	public void add(Node<?> node, int idx) {
		addInternal(node, idx);
	}
	
	public State getChildState(String childName) {
		return getChildState(definition.getChildDefinition(childName));
	}
		
	public State getChildState(NodeDefinition childDef) {
		State state = childStates.get(childDef);
		if (state == null) {
			state = new State();
			childStates.put(childDef, state);
		}
		return state;
	}

	public void setChildState(String childName, int intState) {
		setChildState(definition.getChildDefinition(childName), intState);
	}
	
	public void setChildState(NodeDefinition childDef, int intState) {
		State childState = getChildState(childDef);
		childState.set(intState);
	}
	
	public void setChildState(String childName, int position, boolean value) {
		setChildState(definition.getChildDefinition(childName), position, value);
	}
	
	public void setChildState(NodeDefinition childDef, int position, boolean value) {
		State childState = getChildState(childDef);
		childState.set(position, value);
	}
	
	public void setChildState(NodeDefinition childDef, State state) {
		childStates.put(childDef, state);
	}
		
	
	/**
	 * @return true if any descendant is a non-blank value
	 */
	@Override
	public boolean isEmpty() {
		for ( Node<?> child : children ) {
			if (! child.isEmpty()) {
				return false;
			}
		}
		return true;
	}
	
	@Override
	public boolean hasData() {
		for ( Node<?> child : children ) {
			if(child.hasData() ){
				return true;
			}
		}
		return false;
	}
	
	public <N extends Node<? extends NodeDefinition>> N getChild(String name) {
		NodeDefinition childDefn = definition.getChildDefinition(name);
		return getChild(childDefn);
	}
	
	public <N extends Node<? extends NodeDefinition>> N getChild(String name, int index) {
		NodeDefinition childDefn = definition.getChildDefinition(name);
		return getChild(childDefn, index);
	}
	
	public <N extends Node<? extends NodeDefinition>> N getChild(NodeDefinition childDef) {
		if ( childDef.isMultiple() ) {
			throw new IllegalArgumentException("Single child definition expected for " + childDef.getPath());
		}
		return getChild(childDef, 0);
	}
	
	@SuppressWarnings("unchecked")
	public <N extends Node<? extends NodeDefinition>> N getChild(NodeDefinition nodeDef, int index) {
		List<Node<?>> list = childrenByDefinition.get(nodeDef);
		if (list == null || index >= list.size()) {
			return null;
		} else {
			return (N) list.get(index);
		}
	}
	
	public <N extends Node<? extends NodeDefinition>> N getFirstChild(String childName) {
		NodeDefinition childDef = definition.getChildDefinition(childName);
		return getFirstChild(childDef);
	}
	
	public <N extends Node<? extends NodeDefinition>> N getFirstChild(NodeDefinition childDef) {
		return getChild(childDef, 0);
	}
	
	public <N extends Node<? extends NodeDefinition>> N getLastChild(String childName) {
		NodeDefinition childDef = definition.getChildDefinition(childName);
		return getLastChild(childDef);
	}
	
	public <N extends Node<? extends NodeDefinition>> N getLastChild(NodeDefinition childDef) {
		return getChild(childDef, getCount(childDef) - 1);
	}
	
	/**
	 * @deprecated Use {@link #getChild(String, int)}  instead.
	 */
	@Deprecated
	public Node<? extends NodeDefinition> get(String name, int index) {
		return getChild(name, index);
	}
	
	/**
	 * @deprecated Use {@link #getChild(NodeDefinition, int)}  instead.
	 */
	@Deprecated
	public Node<? extends NodeDefinition> get(NodeDefinition nodeDef, int index) {
		return getChild(nodeDef, index);
	}

	public List<Entity> findChildEntitiesByKeys(String childName, String... keys) {
		return findChildEntitiesByKeys((EntityDefinition) definition.getChildDefinition(childName), keys);
	}

	public List<Entity> findChildEntitiesByKeys(EntityDefinition childEntityDef, final Value... keyValues) {
		return findChildEntitiesByKeys(childEntityDef, Values.toStringValues(keyValues));
	}
	
	/**
	 * @deprecated Replaced by {@link #findChildEntitiesByKeys(EntityDefinition, Value...)}
	 */
	public List<Entity> findChildEntitiesByKeys(EntityDefinition childEntityDef, final String... keys) {
		@SuppressWarnings("unchecked")
		List<Entity> result = (List<Entity>) findChildren(childEntityDef, new NodePredicate() {
			public boolean evaluate(Node<?> node) {
				Value[] keyValues = ((Entity) node).getKeyAttributeValues();
				return compareKeys(Values.toStringValues(keyValues), keys) == 0;
			}
		});
		return result;
	}

	private int compareKeys(String[] keys1, String[] keys2) {
		if ( keys1 == keys2) {
			return 0;
		}
		if ( keys1 == null ) {
			return -1;
		}
		if ( keys2 == null ) {
			return 1;
		}
		if ( keys1.length != keys2.length ) {
			throw new IllegalArgumentException("Cannot compare keys of different length");
		}
		for (int i = 0; i < keys1.length; i++) {
			String key1 = StringUtils.trimToEmpty(keys1[i]);
			String key2 = StringUtils.trimToEmpty(keys2[i]);
			int compare = key1.compareTo(key2);
			if ( compare != 0 ) {
				return compare;
			}
		}
		return 0;
	}

	public Object getValue(String name, int index) {
		Node<?> node = getChild(name, index);
		if ( node instanceof Attribute ) {
			return ((Attribute<?,?>) node).getValue();
		} else if ( node == null ) {
			return null;
		} else {
			throw new IllegalArgumentException("Child "+name+" not an attribute"); 
		}
	}

	/**
	 * 
	 * @deprecated Replaced by {@link #getKeyAttributeValues}
	 */
	public String[] getKeyValues() {
		return Values.toStringValues(getKeyAttributeValues());
	}
	
	public Value[] getKeyAttributeValues() {
		EntityDefinition defn = getDefinition();
		List<AttributeDefinition> keyDefns = defn.getKeyAttributeDefinitions();
		if ( keyDefns.isEmpty() ) {
			return null;
		} else {
			Value[] result = new Value[keyDefns.size()];
			for (int i = 0; i < keyDefns.size(); i++) {
				AttributeDefinition keyDefn = keyDefns.get(i);
				Attribute<?, Value> keyAttr = getKeyAttribute(keyDefn);
				Value keyValue = keyAttr == null ? null: keyAttr.getValue();
				result[i] = keyValue;
			}
			return result;
		}
	}
	
	

	protected Attribute<?, Value> getKeyAttribute(AttributeDefinition keyDefn) {
		if ( ! keyDefn.isDescendantOf(this.getDefinition()) ) {
			String message = String.format("Key attribute definition %s must be descendant of this entity (%s)", keyDefn.getPath(), this.getDefinition().getPath());
			throw new IllegalArgumentException(message);
		}
		Entity keyAttrParent;
		if ( keyDefn.getParentDefinition() != this.getDefinition() ) {
			// key definition is inside single entities
			String relativePath = this.getDefinition().getRelativePath(keyDefn.getParentDefinition());
			keyAttrParent = this;
			String[] parts = relativePath.split(Path.SEPARATOR_REGEX);
			for (String part : parts) {
				keyAttrParent = (Entity) keyAttrParent.getChild(part, 0);
			}
		} else {
			keyAttrParent = this;
		}
		@SuppressWarnings("unchecked")
		Attribute<?, Value> keyAttr = (Attribute<?, Value>) keyAttrParent.getChild(keyDefn);
		return keyAttr;
	}
	
    public Entity getEnumeratedEntity(EntityDefinition childEntityDefn,
			CodeAttributeDefinition enumeratingCodeAttributeDef, String enumeratingCode) {
		List<Node<?>> children = getChildren(childEntityDefn);
		for (Node<?> child : children) {
			Entity entity = (Entity) child;
			Code code = entity.getCodeAttributeValue(enumeratingCodeAttributeDef);
			if(code != null && enumeratingCode.equals(code.getCode())) {
				return entity;
			}
		}
		return null;
	}
	
	private Code getCodeAttributeValue(CodeAttributeDefinition def) {
		Node<?> node = getChild(def, 0);
		return node == null ? null: ((CodeAttribute)node).getValue();
	}
	
	public int getCount(String name) {
		NodeDefinition childDef = definition.getChildDefinition(name);
		return getCount(childDef);
	}
	
	public int getCount(NodeDefinition childDef) {
		List<Node<?>> list = childrenByDefinition.get(childDef);
		return list == null ? 0 : list.size();
	}
	
	public int getNonEmptyCount(String name) {
		NodeDefinition childDef = definition.getChildDefinition(name);
		return getNonEmptyCount(childDef);
	}
	
	public int getNonEmptyCount(NodeDefinition childDef) {
		int count = 0;
		List<Node<?>> list = childrenByDefinition.get(childDef);
		if(list != null && list.size() > 0) {
			for (Node<?> node : list) {
				if(! node.isEmpty()) {
					count ++;
				}
			}
		}
		return count;
	}

	public int getMissingCount(String name) {
		NodeDefinition childDef = definition.getChildDefinition(name);
		return getMissingCount(childDef);
	}
	
	public int getMissingCount(NodeDefinition childDef) {
		int minCount = getMinCount(childDef);
		int specified = getNonEmptyCount(childDef);
		if(minCount > specified) {
			return minCount - specified;
		} else {
			return 0;
		}
	}

	/**
	 * Returns the number of children
	 * @return
	 */
	public int size(){
		return children.size();
	}

	public void move(String name, int oldIndex, int newIndex) {
		NodeDefinition childDef = definition.getChildDefinition(name);
		move(childDef, oldIndex, newIndex);
	}

	public void move(NodeDefinition childDef, int oldIndex, int newIndex) {
		List<Node<?>> list = childrenByDefinition.get(childDef);
		if (list != null) {
			Node<?> obj = list.remove(oldIndex);
			decreaseNodeIndexes(list, oldIndex);
			list.add(newIndex, obj);
			increaseNodeIndexes(list, newIndex);
		}
	}

	public Node<? extends NodeDefinition> remove(String name, int index) {
		NodeDefinition childDef = definition.getChildDefinition(name);
		return remove(childDef, index);
	}

	public Node<? extends NodeDefinition> remove(NodeDefinition childDef, int index) {
		List<Node<?>> list = childrenByDefinition.get(childDef);
		if (list == null) {
			return null;
		} else {
			Node<?> node = list.remove(index);
			
			this.children.remove(node);
			
			decreaseNodeIndexes(list, index);
			
			if( node instanceof Entity ) {
				Entity entity = (Entity)node;
				entity.traverse(new NodeVisitor() {
					@Override
					public void visit(Node<? extends NodeDefinition> node, int idx) {
						record.remove(node);
					}
				});
			} else if ( node instanceof Attribute ){
				record.remove(node);
			}
			return node;
		}
	}

	/**
	 * Adds an item at the specified index. Assumed o has already been checked to be of the appropriate type. All added entities or attributes pass
	 * through this method
	 * 
	 * @param child
	 * @param idx
	 */
	private <T extends Node<?>> T addInternal(T child, Integer idx) {
		NodeDefinition def = child.getDefinition();

		// Get or create list containing children
		List<Node<?>> children = childrenByDefinition.get(def);
		if (children == null) {
			children = new ArrayList<Node<?>>();
			childrenByDefinition.put(def, children);
		}
		// Add item
		if (idx == null) {
			children.add(child);
			child.index = children.size() -1;
		} else {
			children.add(idx, child);
			child.index = idx;
			increaseNodeIndexes(children, idx + 1);
		}
		child.setParent(this);
		
		this.children.add(child);
		
		if ( record != null ) {
			record.put(child);
		}
		return child;
	}

	/**
	 * @deprecated Use {@link Entity#getChildren()} instead.
	 * @return Unmodifiable list of child instances, sorted by their schema
	 *         order.
	 */
	@Deprecated
	public List<Node<? extends NodeDefinition>> getAll() {
		return getChildren();
	}
	
	/**
	 * @deprecated Use {@link Entity#getChildren(NodeDefinition)} instead.
	 */
	@Deprecated
	public List<Node<? extends NodeDefinition>> getAll(NodeDefinition childDef) {
		return getChildren(childDef);
	}

	/**
	 * @deprecated Use {@link Entity#getChildren(String)} instead.
	 */
	@Deprecated
	public List<Node<? extends NodeDefinition>> getAll(String name) {
		return getChildren(name);
	}

	public List<Node<? extends NodeDefinition>> getChildren() {
		return CollectionUtils.unmodifiableList(children);
	}
	
	/**
	 * Returns a list of children with the same sorting as the children node definitions
	 * @return List of children
	 */
	public List<Node<? extends NodeDefinition>> getSortedChildren() {
		List<Node<?>> result = new ArrayList<Node<?>>();
		List<NodeDefinition> childDefinitions = getDefinition().getChildDefinitions();
		for (NodeDefinition childDefn : childDefinitions) {
			List<Node<?>> tempChildren = childrenByDefinition.get(childDefn);
			if (tempChildren != null) {
				result.addAll(tempChildren);
			}
		}
		return result;
	}
	
	public List<Node<? extends NodeDefinition>> getChildren(NodeDefinition childDef) {
		List<Node<?>> children = childrenByDefinition.get(childDef);
		return CollectionUtils.unmodifiableList(children);
	}
	
	public void visitChildren(NodeDefinition childDef, NodeVisitor visitor) {
		List<Node<?>> children = childrenByDefinition.get(childDef);
		if (children != null) {
			for (Node<?> child : children) {
				visitor.visit(child, child.getIndex());
			}
		}
	}
	
	public void visitChildren(NodeVisitor visitor) {
		visitChildren(visitor, false);
	}
	
	public void visitChildren(NodeVisitor visitor, boolean includeSingleEntitiesDescendants) {
		Deque<Node<?>> stack = new LinkedList<Node<?>>();
		stack.addAll(this.children);
		while (! stack.isEmpty()) {
			Node<?> node = stack.pop();
			visitor.visit(node, node.getIndex());
			if (node instanceof Entity && ! node.getDefinition().isMultiple() && includeSingleEntitiesDescendants) {
				stack.addAll(((Entity) node).children);
			}
		}
	}
	
	public List<? extends Node<? extends NodeDefinition>> findChildren(NodeDefinition childDef, final NodePredicate predicate) {
		List<Node<?>> children = childrenByDefinition.get(childDef);
		if (children == null || children.isEmpty()) {
			return Collections.emptyList();
		} else {
			List<Node<? extends NodeDefinition>> result = new ArrayList<Node<? extends NodeDefinition>>(children.size());
			for (Node<?> child : children) {
				if (predicate.evaluate(child)) {
					result.add(child);
				}
			}
			return result;
		}
	}

	/**
	 * Returns the children related to the child definition with the given name.
	 * It's preferable to use {@link #getChildren(NodeDefinition)} because it's more efficient.
	 */
	public List<Node<? extends NodeDefinition>> getChildren(String name) {
		NodeDefinition childDef = definition.getChildDefinition(name);
		return getChildren(childDef);
	}

	@Override
	protected void write(StringWriter sw, int indent) {
		for (int i = 0; i < indent; i++) {
			sw.append('\t');
		}
		if ( indent == 0 ) {
			sw.append(getPath());
		} else {
			sw.append(getName());
			if ( this.getDefinition().isMultiple() ) { 
				sw.append("[");
				sw.append(String.valueOf(getIndex() + 1));
				sw.append("]");
			}
		}
		sw.append(":\n");
		List<NodeDefinition> definitions = getDefinition().getChildDefinitions();
		for (NodeDefinition defn : definitions) {
			List<Node<?>> children = childrenByDefinition.get(defn);
			if (children != null) {
				for (Node<?> child : children) {
					child.write(sw, indent + 1);
					sw.append("\n");
				}
			}
		}
	}

	public void traverse(NodeVisitor visitor) {
		visitor.visit(this, this.getIndex());
		traverseDescendants(visitor);
	}
	
	public void traverseDescendants(NodeVisitor visitor) {
		Deque<Node<?>> stack = new LinkedList<Node<?>>();
		stack.addAll(this.children);
		while (! stack.isEmpty()) {
			Node<?> node = stack.pop();
			visitor.visit(node, node.getIndex());
			if (node instanceof Entity) {
				stack.addAll(((Entity) node).children);
			}
		}
	}
	
	// Pre-order depth-first traversal from here down
	public void dfsTraverse(NodeVisitor visitor) {
		// Initialize stack with root entity
		NodeStack stack = new NodeStack(this);
		// While there are still nodes to insert
		while (!stack.isEmpty()) {
			// Pop the next list of nodes to insert
			List<Node<?>> nodes = stack.pop();
			// Insert this list in order
			Iterator<Node<?>> it = nodes.iterator();
			while (it.hasNext()) {
				Node<?> node = it.next();
				visitor.visit(node, node.getIndex());

				// For entities, add existing child nodes to the stack
				if (node instanceof Entity) {
					Entity entity = (Entity) node;
					EntityDefinition defn = entity.getDefinition();
					List<NodeDefinition> childDefns = defn.getChildDefinitions();
					for (NodeDefinition childDefn : childDefns) {
						List<Node<?>> children = entity.childrenByDefinition.get(childDefn);
						if (children != null && ! children.isEmpty()) {
							stack.push(children);
						}
					}
				}
			}
		}
	}
	
	/**
	 * Decrease nodes' index for each node after the specified index (included)
	 */
	private void decreaseNodeIndexes(List<Node<?>> nodes, int afterIndexInclusive) {
		for(int i = afterIndexInclusive; i < nodes.size(); i++) {
			Node<?> n = nodes.get(i);
			n.setIndex(n.getIndex() - 1);
		}
	}
	
	/**
	 * Increment nodes' index for each node after the specified index
	 */
	private void increaseNodeIndexes(List<Node<?>> nodes, int afterIndexInclusive) {
		for(int i = afterIndexInclusive; i < nodes.size(); i++) {
			Node<?> n = nodes.get(i);
			n.setIndex(n.getIndex() + 1);
		}
	}

	@Override
	protected void resetPath() {
		super.resetPath();
		for (Node<?> child : getChildren()) {
			child.resetPath();
		}
	}
	
	private class NodeStack extends LinkedList<List<Node<? extends NodeDefinition>>> {
		private static final long serialVersionUID = 1L;

		public NodeStack(Entity root) {
			push(Collections.<Node<? extends NodeDefinition>>singletonList(root));
		}
	}

	public boolean isRelevant(String childName) {
		return isRelevant(definition.getChildDefinition(childName));
	}
	
	public boolean isRelevant(NodeDefinition childDef) {
		return derivedStateCache.isRelevant(childDef);
	}
	
	public Boolean getRelevance(NodeDefinition childDef) {
		return derivedStateCache.getRelevance(childDef);
	}

	public void setRelevant(String childName, boolean relevant) {
		setRelevant(definition.getChildDefinition(childName), relevant);
	}
	
	public void setRelevant(NodeDefinition childDef, boolean relevant) {
		derivedStateCache.setRelevant(childDef, relevant);
	}
	
	public boolean isRequired(String childName) {
		return isRequired(definition.getChildDefinition(childName));
	}
	
	public boolean isRequired(NodeDefinition def) {
		Integer minCount = getMinCount(def);
		return minCount != null && minCount > 0;
	}
	
	public Integer getMinCount(NodeDefinition defn) {
		Integer fixedCount = defn.getFixedMinCount();
		return fixedCount == null ? derivedStateCache.getMinCount(defn): fixedCount;
	}
	
	public void setMinCount(NodeDefinition childDefn, int count) {
		derivedStateCache.setMinCount(childDefn, count);
	}

	public Integer getMaxCount(NodeDefinition defn) {
		Integer fixedCount = defn.getFixedMaxCount();
		return fixedCount == null ? derivedStateCache.getMaxCount(defn): fixedCount;
	}
	
	public void setMaxCount(NodeDefinition childDefn, int count) {
		derivedStateCache.setMaxCount(childDefn, count);
	}

	public ValidationResultFlag getMinCountValidationResult(String childName) {
		return getMinCountValidationResult(definition.getChildDefinition(childName));
	}
	
	public ValidationResultFlag getMinCountValidationResult(NodeDefinition childDef) {
		return derivedStateCache.getMinCountValidationResult(childDef);
	}
	
	public void setMinCountValidationResult(String childName, ValidationResultFlag value) {
		setMinCountValidationResult(definition.getChildDefinition(childName), value);
	}
	
	public void setMinCountValidationResult(NodeDefinition childDef, ValidationResultFlag value) {
		derivedStateCache.setMinCountValidationResult(childDef, value);
	}
	
	public ValidationResultFlag getMaxCountValidationResult(String childName) {
		return getMaxCountValidationResult(definition.getChildDefinition(childName));
	}
	
	public ValidationResultFlag getMaxCountValidationResult(NodeDefinition childDef) {
		return derivedStateCache.getMaxCountValidationResult(childDef);
	}
	
	public void setMaxCountValidationResult(String childName, ValidationResultFlag value) {
		setMaxCountValidationResult(definition.getChildDefinition(childName), value);
	}
	
	public void setMaxCountValidationResult(NodeDefinition childDef, ValidationResultFlag value) {
		derivedStateCache.setMaxCountValidationResult(childDef, value);
	}
	
	/**
	 * 
	 * @param childName
	 * @return minimum number of non-empty child nodes, based on minCount, required and 
	 * requiredExpression properties
	 */
	public int getMinCount(String childName) {
		NodeDefinition defn = definition.getChildDefinition(childName);
		return getMinCount(defn);
	}

	/**
	 * Returns all the descendants in depth-first order
	 */
	public List<Node<?>> getDescendants() {
		final List<Node<?>> result = new ArrayList<Node<?>>();
		this.traverseDescendants(new NodeVisitor() {
			public void visit(Node<?> node, int idx) {
				if (node != Entity.this) {
					result.add(0, node);
				}
			}
		});
		return result;
	}

	public void clearChildStates() {
		this.childStates.clear();
	}

	@Override
	public boolean deepEquals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Entity other = (Entity) obj;
		//custom check
		if (! isChildStatesEquals(other) ) {
			return false;
		}
		if (childrenByDefinition == null && other.childrenByDefinition != null || 
				childrenByDefinition != null && other.childrenByDefinition == null) {
			return false;
		} else {
			//custom check
			removeEmptyChildren();
			other.removeEmptyChildren();
			if ( childrenByDefinition.size() != other.childrenByDefinition.size() ) {
				return false;
			}
			Set<NodeDefinition> childDefs = childrenByDefinition.keySet();
			for (NodeDefinition childDef : childDefs) {
				List<Node<?>> children = childrenByDefinition.get(childDef);
				List<Node<?>> otherChildren = other.childrenByDefinition.get(childDef);
				if ( children.size() != otherChildren.size() ) {
					return false;
				}
				for (int i = 0; i < children.size(); i++) {
					Node<?> child = children.get(i);
					Node<?> otherChild = otherChildren.get(i);
					if ( ! child.deepEquals(otherChild) ) {
						return false;
					}
				}
			}
		}
		return true;
	}

	private boolean isChildStatesEquals(Entity other) {
		if (childStates == null) {
			if (other.childStates != null) {
				return false;
			}
		} else {
			removeEmptyChildStates();
			other.removeEmptyChildStates();
			if (!childStates.equals(other.childStates)) {
				return false;
			}
		}
		return true;
	}
	
	protected void removeEmptyChildStates() {
		if ( childStates != null ) {
			Set<Entry<NodeDefinition, State>> entrySet = childStates.entrySet();
			Iterator<Entry<NodeDefinition, State>> iterator = entrySet.iterator();
			while ( iterator.hasNext() ) {
				Entry<NodeDefinition, State> entry = iterator.next();
				State childState = entry.getValue();
				if ( childState.intValue() == 0 ) {
					iterator.remove();
				}
			}
		}
	}
	
	protected void removeEmptyChildren() {
		Set<Entry<NodeDefinition, List<Node<?>>>> entrySet = childrenByDefinition.entrySet();
		Iterator<Entry<NodeDefinition, List<Node<?>>>> iterator = entrySet.iterator();
		while ( iterator.hasNext() ) {
			Entry<NodeDefinition, List<Node<?>>> entry = iterator.next();
			List<Node<?>> nodes = entry.getValue();
			if ( nodes == null || nodes.isEmpty()) {
				iterator.remove();
				children.removeAll(nodes);
			}
		}
	}

	private static class ValidationState {
		/** Set of children dynamic min count */
		private Map<NodeDefinition, Integer> minCountByChildDefinition;
		/** Set of children dynamic max count */
		private Map<NodeDefinition, Integer> maxCountByChildDefinition;
		/** Set of children relevance states */
		private Map<NodeDefinition, Boolean> relevanceByChildDefinition;
		
		private Map<NodeDefinition, ValidationResultFlag> minCountValidationResultByChildDefinition;
		private Map<NodeDefinition, ValidationResultFlag> maxCountValidationResultByChildDefinition;

		public ValidationState() {
			minCountByChildDefinition = new TreeMap<NodeDefinition, Integer>();
			maxCountByChildDefinition = new TreeMap<NodeDefinition, Integer>();
			relevanceByChildDefinition = new TreeMap<NodeDefinition, Boolean>();
			minCountValidationResultByChildDefinition = new TreeMap<NodeDefinition, ValidationResultFlag>();
			maxCountValidationResultByChildDefinition = new TreeMap<NodeDefinition, ValidationResultFlag>();
		}

		private Integer getMinCount(NodeDefinition childDefinition) {
			return minCountByChildDefinition.get(childDefinition);
		}
		
		private void setMinCount(NodeDefinition childDefinition, int count) {
			minCountByChildDefinition.put(childDefinition, count);
		}
		
		private Integer getMaxCount(NodeDefinition childDefinition) {
			return maxCountByChildDefinition.get(childDefinition);
		}
		
		private void setMaxCount(NodeDefinition childDefinition, Integer count) {
			maxCountByChildDefinition.put(childDefinition, count);
		}
		
		private boolean isRelevant(NodeDefinition childDefinition) {
			Boolean value = relevanceByChildDefinition.get(childDefinition);
			return value == null ? true: value;
		}
		
		private Boolean getRelevance(NodeDefinition childDefinition) {
			return relevanceByChildDefinition.get(childDefinition);
		}

		private void setRelevant(NodeDefinition childDefinition, boolean flag) {
			relevanceByChildDefinition.put(childDefinition, flag);
		}
		
		private ValidationResultFlag getMinCountValidationResult(NodeDefinition childDefinition) {
			return minCountValidationResultByChildDefinition.get(childDefinition);
		}

		private ValidationResultFlag setMinCountValidationResult(NodeDefinition childDefinition, ValidationResultFlag value) {
			return minCountValidationResultByChildDefinition.put(childDefinition, value);
		}

		private ValidationResultFlag getMaxCountValidationResult(NodeDefinition childDefinition) {
			return maxCountValidationResultByChildDefinition.get(childDefinition);
		}

		private ValidationResultFlag setMaxCountValidationResult(NodeDefinition childDefinitionId, ValidationResultFlag value) {
			return maxCountValidationResultByChildDefinition.put(childDefinitionId, value);
		}

	}

}
