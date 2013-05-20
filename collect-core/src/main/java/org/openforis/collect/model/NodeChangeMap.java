/**
 * 
 */
package org.openforis.collect.model;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.openforis.idm.model.Attribute;
import org.openforis.idm.model.Entity;
import org.openforis.idm.model.Node;

/**
 * Map of NodeChange objects.
 * 
 * @author S. Ricci
 *
 */
public class NodeChangeMap {
	
	private Map<Integer, NodeChange<?>> nodeIdToChange;
	
	public NodeChangeMap() {
		nodeIdToChange = new LinkedHashMap<Integer, NodeChange<?>>();
	}
	
	public int size() {
		return nodeIdToChange.size();
	}
	
	public boolean isEmpty() {
		return nodeIdToChange.isEmpty();
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
	public NodeDeleteChange prepareDeleteNodeChange(Node<?> node) {
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
	public NodeChange<?> prepareAddEntityChange(Entity entity) {
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
	public NodeChange<?> prepareAddAttributeChange(Attribute<?, ?> node) {
		Integer nodeId = node.getInternalId();
		NodeChange<?> c = getChange(node);
		if ( c == null ) {
			c = new AttributeAddChange(node);
			nodeIdToChange.put(nodeId, c);
			return c;
		} else {
			throw new IllegalStateException("AddNodeChange already present for node: " + nodeId);
		}
	}
	
	public List<NodeChange<?>> getChanges() {
		return new ArrayList<NodeChange<?>>(nodeIdToChange.values());
	}

	public NodeChange<?> getChange(Node<?> node) {
		Integer nodeId = node.getInternalId();
		return getChange(nodeId);
	}

	protected NodeChange<?> getChange(Integer nodeId) {
		NodeChange<?> c = nodeIdToChange.get(nodeId);
		return c;
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
	
}
