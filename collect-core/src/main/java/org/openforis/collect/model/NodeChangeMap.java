/**
 * 
 */
package org.openforis.collect.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openforis.collect.model.CollectRecord.Step;
import org.openforis.idm.metamodel.validation.ValidationResultFlag;
import org.openforis.idm.metamodel.validation.ValidationResults;
import org.openforis.idm.model.Attribute;
import org.openforis.idm.model.Entity;
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
		this(null);
	}
	
	public NodeChangeMap(NodeChangeSet changeSet) {
		nodeIdToChange = new LinkedHashMap<Integer, NodeChange<?>>();
		if (changeSet != null) {
			if (changeSet instanceof NodeChangeMap) {
				nodeIdToChange.putAll(((NodeChangeMap) changeSet).nodeIdToChange);
			} else {
				addMergeChanges(changeSet);
			}
		}
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
	
	@Override
	public Set<Node<?>> getChangedNodes() {
		Set<Node<?>> result = new LinkedHashSet<Node<?>>();
		List<NodeChange<?>> changes = getChanges();
		for (NodeChange<?> nodeChange : changes) {
			if (nodeChange instanceof AttributeChange) {
				result.add(nodeChange.getNode());
			} else if (nodeChange instanceof EntityChange) {
				result.addAll(((EntityChange) nodeChange).extractChangedNodes());
			}
		}
		return result;
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
		if (c == null) {
			c = new EntityChange(entity);
			putChange(c);
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
		if (c == null) {
			c = new AttributeChange(attribute);
			addOrMergeChange(c);
		}
		return c;
	}
	
	/**
	 * Create a new NodeDeleteChange and puts it in the internal cache
	 * 
	 * @param recordId 
	 * @param ancestorIds 
	 * @param node
	 * @return
	 */
	public NodeDeleteChange addNodeDeleteChange(Integer recordId, Step recordStep, List<Integer> ancestorIds, Node<?> node) {
		NodeDeleteChange c = new NodeDeleteChange(recordId, recordStep, ancestorIds, node);
		putChange(c); //overwrite change if already present
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
		NodeChange<?> c = new EntityAddChange(entity);
		addChange(c);
		return c;
	}
	
	/**
	 * Returns the change already associated to the attribute or creates a new AttributeAddChange
	 * and puts it in the internal cache
	 */
	public NodeChange<?> addAttributeAddChange(Attribute<?, ?> attribute) {
		NodeChange<?> c = new AttributeAddChange(attribute);
		addChange(c);
		return c;
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
	 */
	public void addOrMergeChange(NodeChange<?> change) {
		Node<?> node = change.getNode();
		NodeChange<?> existingChange = getChange(node);
		if ( existingChange == null ) {
			putChange(change);
		} else if (! (existingChange instanceof NodeDeleteChange) ) {
			if ( existingChange instanceof AttributeChange && change instanceof AttributeChange ) {
				((AttributeChange) existingChange).merge((AttributeChange) change);
			} else if ( existingChange instanceof EntityChange && change instanceof EntityChange ) {
				((EntityChange) existingChange).merge((EntityChange) change);
			} else {
				putChange(change);
			}
		}
	}
	
	/**
	 * Puts a change into the nodeIdToChange map using the internal node id as key.
	 * 
	 * @return Previous node change associated to the node internal id, if already existing, 
	 * null if there was no mapping with node internal id.
	 */
	public NodeChange<?> putChange(NodeChange<?> change) {
		Node<?> node = change.getNode();
		Integer nodeId = node.getInternalId();
		return nodeIdToChange.put(nodeId, change);
	}
	
	public void addChange(NodeChange<?> change) {
		NodeChange<?> previousChange = putChange(change);
		if (previousChange != null) {
			Node<?> node = change.getNode();
			Integer nodeId = node.getInternalId();
			throw new IllegalStateException(String.format("%s already present for node with id: %d", change.getClass().getSimpleName(), nodeId));
		}
	}
	
	public void addValueChange(Attribute<?, ?> attribute) {
		AttributeChange change = prepareAttributeChange(attribute);
		change.setUpdatedFieldValues(attribute.getFieldValueByIndex());
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

	public void addMinCountChanges(Collection<NodePointer> pointers) {
		for (NodePointer nodePointer : pointers) {
			EntityChange change = prepareEntityChange(nodePointer.getEntity());
			change.setMinCount(nodePointer.getChildDefinitionId(), nodePointer.getNodesMinCount());
		}
	}

	public void addMaxCountChanges(Collection<NodePointer> pointers) {
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
