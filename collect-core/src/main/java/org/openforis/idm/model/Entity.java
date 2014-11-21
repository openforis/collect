/**
 * 
 */
package org.openforis.idm.model;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Stack;

import org.apache.commons.lang3.StringUtils;
import org.openforis.commons.collection.CollectionUtils;
import org.openforis.idm.metamodel.AttributeDefinition;
import org.openforis.idm.metamodel.CodeAttributeDefinition;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.metamodel.validation.ValidationResultFlag;

/**
 * @author G. Miceli
 * @author M. Togna
 * @author S. Ricci
 * 
 */
public class Entity extends Node<EntityDefinition> {

	private static final long serialVersionUID = 1L;
	
	Map<String, List<Node<?>>> childrenByName;
	private ValidationState derivedStateCache;
	Map<String, State> childStates;
	
	public Entity(EntityDefinition definition) {
		super(definition);
		this.childrenByName = new HashMap<String, List<Node<?>>>();
		this.derivedStateCache = new ValidationState();
		this.childStates = new HashMap<String, State>();
	}

	@Override
	protected void setRecord(Record record) {
		super.setRecord(record);
		List<Node<?>> children = getChildren();
		for (Node<?> node : children) {
			node.setRecord(record);
		}
	}
	
	public void add(Node<?> node) {
		addInternal(node, null);
	}
	
	public void add(Node<?> node, int idx) {
		addInternal(node, idx);
	}
	
	public void setChildState(String childName, int intState) {
		State childState = getChildState(childName);
		childState.set(intState);
	}
	
	public void setChildState(String childName, int position, boolean value) {
		State childState = getChildState(childName);
		childState.set(position, value);
	}
	
	public State getChildState(String childName){
		State state = childStates.get(childName);
		if (state == null) {
			state = new State();
			childStates.put(childName, state);
		}
		return state;
	}
	
	/**
	 * @return true if any descendant is a non-blank value
	 */
	@Override
	public boolean isEmpty() {
		Collection<List<Node<?>>> childLists = childrenByName.values();
		for (List<Node<?>> list : childLists) {
			for (Node<?> node : list) {
				if (!node.isEmpty()) {
					return false;
				}
			}
		}
		return true;
	}
	
	@Override
	public boolean hasData() {
		List<Node<?>> children = getChildren();
		for ( Node<?> child : children ) {
			if( child.hasData() ){
				return true;
			}
		}
		return false;
	}

	public Node<? extends NodeDefinition> get(String name, int index) {
		List<Node<?>> list = childrenByName.get(name);
		if (list == null || index >= list.size()) {
			return null;
		} else {
			return list.get(index);
		}
	}
	
	public Node<? extends NodeDefinition> getChild(String name) {
		NodeDefinition childDefn = definition.getChildDefinition(name);
		if ( childDefn.isMultiple() ) {
			throw new IllegalArgumentException("Single child definition expected for " + getDefinition().getPath() + "/" + name);
		}
		return get(name, 0);
	}
	
	public List<Entity> findChildEntitiesByKeys(String childName, String... keys) {
		List<Entity> result = new ArrayList<Entity>();
		List<Node<?>> siblings = getAll(childName);
		for (Node<?> sibling : siblings) {
			String[] keyValues = ((Entity) sibling).getKeyValues();
			if ( compareKeys(keyValues, keys) == 0 ) {
				result.add((Entity) sibling);
			}
		}
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
		Node<?> node = get(name, index);
		if ( node instanceof Attribute ) {
			return ((Attribute<?,?>) node).getValue();
		} else if ( node == null ) {
			return null;
		} else {
			throw new IllegalArgumentException("Child "+name+" not an attribute"); 
		}
	}

	public String[] getKeyValues() {
		EntityDefinition defn = getDefinition();
		List<AttributeDefinition> keyDefns = defn.getKeyAttributeDefinitions();
		if ( keyDefns.size() > 0 ) {
			String[] result = new String[keyDefns.size()];
			for (int i = 0; i < keyDefns.size(); i++) {
				AttributeDefinition keyDefn = keyDefns.get(i);
				Attribute<?, Value> keyAttr = getKeyAttribute(keyDefn);
				String keyValue = keyAttr == null ? null: getKeyValue(keyAttr);
				result[i] = keyValue;
			}
			return result;
		} else {
			return null;
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
			String[] parts = relativePath.split("/");
			for (String part : parts) {
				keyAttrParent = (Entity) keyAttrParent.get(part, 0);
			}
		} else {
			keyAttrParent = this;
		}
		@SuppressWarnings("unchecked")
		Attribute<?, Value> keyAttr = (Attribute<?, Value>) keyAttrParent.getChild(keyDefn.getName());
		return keyAttr;
	}
	
	private String getKeyValue(Attribute<?, Value> keyAttr) {
		Value value = keyAttr.getValue();
		if ( value == null ) {
			return null;
		} else if ( value instanceof Code ) {
			return ((Code) value).getCode();
		} else if ( value instanceof TextValue ) {
			return ((TextValue) value).getValue();
		} else if ( value instanceof NumberValue ) {
			Number num = ((NumberValue<?>) value).getValue();
			return num == null ? null : num.toString();
		} else {
			AttributeDefinition defn = keyAttr.getDefinition();
			throw new IllegalArgumentException("Attribute is not a KeyAttribute: " + defn.getClass().getName());
		}
	}

    public Entity getEnumeratedEntity(EntityDefinition childEntityDefn,
			CodeAttributeDefinition enumeratingCodeAttributeDef, String enumeratingCode) {
		List<Node<?>> children = getAll(childEntityDefn.getName());
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
		Node<?> node = get(def.getName(), 0);
		return node == null ? null: ((CodeAttribute)node).getValue();
	}
	
	/*
	 * public Set<String> getChildNames() { Set<String> childNames = childrenByName.keySet(); return Collections.unmodifiableSet(childNames); }
	 */
	public int getCount(String name) {
		List<Node<?>> list = childrenByName.get(name);
		return list == null ? 0 : list.size();
	}
	
	public int getNonEmptyCount(String name) {
		int count = 0;
		List<Node<?>> list = childrenByName.get(name);
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
		int minCount = getEffectiveMinCount(name);
		int specified = getNonEmptyCount(name);
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
		return childrenByName.size();
	}

	public void move(String name, int oldIndex, int newIndex) {
		List<Node<?>> list = childrenByName.get(name);
		if (list != null) {
			Node<?> obj = list.remove(oldIndex);
			decreaseNodeIndexes(list, oldIndex);
			list.add(newIndex, obj);
			increaseNodeIndexes(list, newIndex);
		}
	}

	public Node<? extends NodeDefinition> remove(String name, int index) {
		List<Node<?>> list = childrenByName.get(name);
		if (list == null) {
			return null;
		} else {
			Node<?> node = list.remove(index);
			
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
		verifyChildCanBeAdded(child);
		String name = child.getName();

		// Get or create list containing children
		List<Node<?>> children = childrenByName.get(name);
		if (children == null) {
			children = new ArrayList<Node<?>>();
			childrenByName.put(name, children);
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
		
		if ( record != null ) {
			record.put(child);
		}
		return child;
	}

	private <T extends Node<?>> void verifyChildCanBeAdded(T o) {
		NodeDefinition defn = o.getDefinition();
		String name = defn.getName();
		// Get child's definition and check schema object definition is the same
		NodeDefinition childDefn = getDefinition().getChildDefinition(name);
		if ( childDefn == null ) {
			throw new IllegalArgumentException("'"+name+"' not allowed in '"+getName()+"'");			
		} else if (defn != childDefn) {
			throw new IllegalArgumentException("'"+name+"' in '"+getName()+"' wrong type");
		}
	}

	public List<Node<? extends NodeDefinition>> getAll(String name) {
		List<Node<?>> children = childrenByName.get(name);
		return CollectionUtils.unmodifiableList(children);
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
			List<Node<?>> children = childrenByName.get(defn.getName());
			if (children != null) {
				for (Node<?> child : children) {
					child.write(sw, indent + 1);
					sw.append("\n");
				}
			}
		}
	}

	// Pre-order depth-first traversal from here down
	public void traverse(NodeVisitor visitor) {
		// Initialize stack with root entity
		NodeStack stack = new NodeStack(this);
		// While there are still nodes to insert
		while (!stack.isEmpty()) {
			// Pop the next list of nodes to insert
			List<Node<?>> nodes = stack.pop();
			// Insert this list in order
			for (int i = 0; i < nodes.size(); i++) {
				Node<?> node = nodes.get(i);

				if ( node == null ) {
					throw new IllegalStateException("Null node in entity children list");
				}
				visitor.visit(node, i);

				// For entities, add existing child nodes to the stack
				if (node instanceof Entity) {
					Entity entity = (Entity) node;
					EntityDefinition defn = entity.getDefinition();
					List<NodeDefinition> childDefns = defn.getChildDefinitions();
					for (NodeDefinition childDefn : childDefns) {
						String childName = childDefn.getName();
						List<Node<?>> children = entity.getAll(childName);
						if (children != null) {
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
	
	private class NodeStack extends Stack<List<Node<? extends NodeDefinition>>> {
		private static final long serialVersionUID = 1L;

		public NodeStack(Entity root) {
			List<Node<?>> rootList = new ArrayList<Node<?>>(1);
			rootList.add(root);
			push(rootList);
		}
	}

	public boolean isRelevant(String childName) {
		return derivedStateCache.isRelevant(childName);
	}
	
	public Boolean getRelevance(String childName) {
		return derivedStateCache.getRelevance(childName);
	}

	public void setRelevant(String childName, boolean relevant) {
		derivedStateCache.setRelevant(childName, relevant);
	}
	
	public boolean isRequired(String childName) {
		return derivedStateCache.getRequired(childName);
	}
	
	public void setRequired(String childName, boolean required) {
		derivedStateCache.setRequired(childName, required);
	}

	public ValidationResultFlag getMinCountValidationResult(String childName) {
		return derivedStateCache.getMinCountValidationResult(childName);
	}
	
	public void setMinCountValidationResult(String childName, ValidationResultFlag value) {
		derivedStateCache.setMinCountValidationResult(childName, value);
	}
	
	public ValidationResultFlag getMaxCountValidationResult(String childName) {
		return derivedStateCache.getMaxCountValidationResult(childName);
	}
	
	public void setMaxCountValidationResult(String childName, ValidationResultFlag value) {
		derivedStateCache.setMaxCountValidationResult(childName, value);
	}
	
	/**
	 * 
	 * @param childName
	 * @return minimum number of non-empty child nodes, based on minCount, required and 
	 * requiredExpression properties
	 */
	public int getEffectiveMinCount(String childName) {
		NodeDefinition defn = definition.getChildDefinition(childName);
		Integer minCount = defn.getMinCount();
		// requiredExpression is only considered if minCount and required are not set
		if ( minCount==null ) {
			return isRequired(childName) ? 1 : 0;
		} else {
			return minCount;
		}
	}

	/**
	 * 
	 * @return Unmodifiable list of child instances, sorted by their schema
	 *         order.
	 */
	public List<Node<? extends NodeDefinition>> getChildren() {
		List<Node<?>> result = new ArrayList<Node<?>>();
		List<NodeDefinition> definitions = getDefinition().getChildDefinitions();
		for (NodeDefinition defn : definitions) {
			String childName = defn.getName();
			result.addAll(getChildren(childName));
		}
		return Collections.unmodifiableList(result);
	}

	public List<Node<?>> getChildren(String childName) {
		List<Node<?>> result = new ArrayList<Node<?>>();
		List<Node<?>> children = childrenByName.get(childName);
		if (children != null) {
			for (Node<?> child : children) {
				result.add(child);
			}
		}
		return result;
	}

	/**
	 * Returns all the descendants in depth-first order
	 */
	public List<Node<?>> getDescendants() {
		List<Node<?>> result = new Stack<Node<?>>();
		Stack<Node<?>> stack = new Stack<Node<?>>();
		stack.addAll(this.getChildren());
		while(!stack.isEmpty()){
			Node<?> n = stack.pop();
			result.add(0, n);
			if(n instanceof Entity){
				Entity entity = (Entity) n;
				List<Node<?>> children = entity.getChildren();
				for (Node<?> child : children) {
					stack.push(child);
				}
			}
		}
		return result;
	}

	public void clearChildStates() {
		this.childStates = new HashMap<String, State>();
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
		if (childrenByName == null && other.childrenByName != null || 
				childrenByName != null && other.childrenByName == null) {
			return false;
		} else {
			//custom check
			normalizeChildrenByName();
			other.normalizeChildrenByName();
			if ( childrenByName.size() != other.childrenByName.size() ) {
				return false;
			}
			Set<String> childNames = childrenByName.keySet();
			for (String childName : childNames) {
				List<Node<?>> children = childrenByName.get(childName);
				List<Node<?>> otherChildren = other.childrenByName.get(childName);
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
			Set<Entry<String,State>> entrySet = childStates.entrySet();
			Iterator<Entry<String, State>> iterator = entrySet.iterator();
			while ( iterator.hasNext() ) {
				Entry<String, State> entry = iterator.next();
				State childState = entry.getValue();
				if ( childState.intValue() == 0 ) {
					iterator.remove();
				}
			}
		}
	}
	
	protected void normalizeChildrenByName() {
		Set<Entry<String, List<Node<?>>>> entrySet = childrenByName.entrySet();
		Iterator<Entry<String, List<Node<?>>>> iterator = entrySet.iterator();
		while ( iterator.hasNext() ) {
			Entry<String, List<Node<?>>> entry = iterator.next();
			List<Node<?>> nodes = entry.getValue();
			if ( nodes == null || nodes.isEmpty()) {
				iterator.remove();
			}
		}
	}

	private static class ValidationState {
		/** Set of children dynamic required states */
		private Map<String, Boolean> childRequiredStates;
		/** Set of children relevance states */
		private Map<String, Boolean> childRelevance;
		
		private Map<String, ValidationResultFlag> minCountValidationResult;
		private Map<String, ValidationResultFlag> maxCountValidationResult;

		public ValidationState() {
			childRequiredStates = new HashMap<String, Boolean>();
			childRelevance = new HashMap<String, Boolean>();
			minCountValidationResult = new HashMap<String, ValidationResultFlag>();
			maxCountValidationResult = new HashMap<String, ValidationResultFlag>();
		}

		private boolean getRequired(String childName) {
			Boolean value = childRequiredStates.get(childName);
			return value == null ? false: value;
		}

		private void setRequired(String childName, boolean flag) {
			childRequiredStates.put(childName, flag);
		}

		private boolean isRelevant(String childName) {
			Boolean value = childRelevance.get(childName);
			return value == null ? true: value;
		}
		
		private Boolean getRelevance(String childName) {
			return childRelevance.get(childName);
		}

		private void setRelevant(String childName, boolean flag) {
			childRelevance.put(childName, flag);
		}
		
		private ValidationResultFlag getMinCountValidationResult(String childName) {
			return minCountValidationResult.get(childName);
		}

		private ValidationResultFlag setMinCountValidationResult(String childName, ValidationResultFlag value) {
			return minCountValidationResult.put(childName, value);
		}

		private ValidationResultFlag getMaxCountValidationResult(String childName) {
			return maxCountValidationResult.get(childName);
		}

		private ValidationResultFlag setMaxCountValidationResult(String childName, ValidationResultFlag value) {
			return maxCountValidationResult.put(childName, value);
		}

	}

}
