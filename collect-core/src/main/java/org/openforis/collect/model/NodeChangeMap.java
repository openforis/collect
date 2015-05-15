/**
 * 
 */
package org.openforis.collect.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openforis.idm.metamodel.validation.ValidationResultFlag;
import org.openforis.idm.metamodel.validation.ValidationResults;
import org.openforis.idm.model.Attribute;
import org.openforis.idm.model.Entity;
import org.openforis.idm.model.Field;
import org.openforis.idm.model.Node;
import org.openforis.idm.model.NodePointer;

/**
 * Map of NodeChange objects.
 * 
 * @author S. Ricci
 *
 */
public class NodeChangeMap implements NodeChangeSet {
	
	private Map<Integer, NodeChange<?>> nodeIdToChange;
	
	public NodeChangeMap() {
		nodeIdToChange = new LinkedHashMap<Integer, NodeChange<?>>();
	}
	
	@Override
	public int size() {
		return nodeIdToChange.size();
	}
	
	@Override
	public boolean isEmpty() {
		return nodeIdToChange.isEmpty();
	}
	
	@Override
	public List<NodeChange<?>> getChanges() {
		return new ArrayList<NodeChange<?>>(nodeIdToChange.values());
	}
	
	@Override
	public NodeChange<?> getChange(Node<?> node) {
		Integer nodeId = node.getInternalId();
		return getChange(nodeId);
	}

	@Override
	public NodeChange<?> getChange(int nodeId) {
		NodeChange<?> c = nodeIdToChange.get(nodeId);
		return c;
	}
	
	/**
	 * Returns the change already associated to the entity or creates a new EntityChange
	 * and puts it in the internal cache
	 *  
	 * @param entity
	 * @return
	 */
	public EntityChange prepareEntityChange(Entity entity) {
		EntityChange c = (EntityChange) getChange(entity);
		if(c == null){
			c = new EntityChange(entity);
			addOrMergeChange(c);
		}
		return c;
	}

	/**
	 * Returns the change already associated to the attribute or creates a new AttributeChange
	 * and puts it in the internal cache
	 * 
	 * @param attribute
	 * @return
	 */
	public AttributeChange prepareAttributeChange(Attribute<?, ?> attribute) {
		AttributeChange c = (AttributeChange) getChange(attribute);
		if(c == null){
			c = new AttributeChange(attribute);
			addOrMergeChange(c);
		}
		return c;
	}
	
	/**
	 * Create a new NodeDeleteChange and puts it in the internal cache
	 * 
	 * @param node
	 * @return
	 */
	public NodeDeleteChange addNodeDeleteChange(Node<?> node) {
		NodeDeleteChange c = new NodeDeleteChange(node);
		nodeIdToChange.put(node.getInternalId(), c); //overwrite change if already present
		return c;
	}
	
	/**
	 * Returns the change already associated to the entity or creates a new EntityAddChange
	 * and puts it in the internal cache
	 * 
	 * @param entity
	 * @return
	 */
	public NodeChange<?> addEntityAddChange(Entity entity) {
		Integer nodeId = entity.getInternalId();
		NodeChange<?> c = getChange(entity);
		if ( c == null ) {
			c = new EntityAddChange(entity);
			nodeIdToChange.put(nodeId, c);
			return c;
		} else {
			throw new IllegalStateException("AddNodeChange already present for node: " + nodeId);
		}
	}

	/**
	 * Returns the change already associated to the attribute or creates a new AttributeAddChange
	 * and puts it in the internal cache
	 * 
	 * @param attribute
	 * @return
	 */
	public NodeChange<?> addAttributeAddChange(Attribute<?, ?> attribute) {
		Integer nodeId = attribute.getInternalId();
		NodeChange<?> c = getChange(attribute);
		if ( c == null ) {
			c = new AttributeAddChange(attribute);
			nodeIdToChange.put(nodeId, c);
			return c;
		} else {
			throw new IllegalStateException("AddNodeChange already present for node: " + nodeId);
		}
	}
	
	public void addMergeChanges(NodeChangeSet changeSet) {
		for (NodeChange<?> change : changeSet.getChanges()) {
			addOrMergeChange(change);
		}
	}

	/**
	 * Puts a change into the internal cache.
	 * If a change associated to the node already exists and it is not a NodeDeleteChange,
	 * merges the old change with the new one.
	 * 
	 * @param change
	 */
	public void addOrMergeChange(NodeChange<?> change) {
		Node<?> node = change.getNode();
		Integer nodeId = node.getInternalId();
		NodeChange<?> oldItem = nodeIdToChange.get(nodeId);
		if ( oldItem == null ) {
			nodeIdToChange.put(nodeId, change);
		} else if (! (oldItem instanceof NodeDeleteChange) ) {
			if ( oldItem instanceof AttributeChange && change instanceof AttributeChange ) {
				((AttributeChange) oldItem).merge((AttributeChange) change);
			} else if ( oldItem instanceof EntityChange && change instanceof EntityChange ) {
				((EntityChange) oldItem).merge((EntityChange) change);
			} else {
				nodeIdToChange.put(nodeId, change);
			}
		}
	}

	public void addValueChange(Attribute<?, ?> attribute) {
		AttributeChange change = prepareAttributeChange(attribute);
		
		Map<Integer, Object> fieldValues = new HashMap<Integer, Object>();
		int fieldCount = attribute.getFieldCount();
		for (int idx = 0; idx < fieldCount; idx ++) {
			Field<?> field = attribute.getField(idx);
			fieldValues.put(idx, field.getValue());
		}
		
		change.setUpdatedFieldValues(fieldValues);
	}

	public void addValueChanges(Collection<? extends Attribute<?, ?>> attributes) {
		for (Attribute<?, ?> attribute : attributes) {
			addValueChange(attribute);
		}
	}

	public void addRelevanceChanges(Set<NodePointer> pointers) {
		for (NodePointer nodePointer : pointers) {
			EntityChange change = prepareEntityChange(nodePointer.getEntity());
			change.setRelevance(nodePointer.getChildName(), nodePointer.areNodesRelevant());
		}
	}

	public void addMinCountChanges(Set<NodePointer> pointers) {
		for (NodePointer nodePointer : pointers) {
			EntityChange change = prepareEntityChange(nodePointer.getEntity());
			change.setMinCount(nodePointer.getChildDefinitionId(), nodePointer.getNodesMinCount());
		}
	}

	public void addMaxCountChanges(Set<NodePointer> pointers) {
		for (NodePointer nodePointer : pointers) {
			EntityChange change = prepareEntityChange(nodePointer.getEntity());
			change.setMaxCount(nodePointer.getChildDefinitionId(), nodePointer.getNodesMaxCount());
		}
	}

	public void addMinCountValidationResultChange(NodePointer nodePointer, ValidationResultFlag minCountResult) {
		EntityChange change = prepareEntityChange(nodePointer.getEntity());
		change.setMinCountValidation(nodePointer.getChildName(), minCountResult);
	}

	public void addMaxCountValidationResultChange(NodePointer nodePointer, ValidationResultFlag maxCountResult) {
		EntityChange change = prepareEntityChange(nodePointer.getEntity());
		change.setMaxCountValidation(nodePointer.getChildName(), maxCountResult);
	}

	public void addValidationResultChange(Attribute<?, ?> attribute, ValidationResults validationResults) {
		AttributeChange change = prepareAttributeChange(attribute);
		change.setValidationResults(validationResults);
	}
	
	@Override
	public String toString() {
		return "Node change map for these nodes: " + nodeIdToChange.toString();
	}

}
