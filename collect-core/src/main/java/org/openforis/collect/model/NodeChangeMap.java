/**
 * 
 */
package org.openforis.collect.model;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.openforis.collect.model.NodeChange.AttributeChange;
import org.openforis.collect.model.NodeChange.EntityChange;
import org.openforis.collect.model.NodeChange.NodeDeleteChange;
import org.openforis.idm.model.Attribute;
import org.openforis.idm.model.Entity;
import org.openforis.idm.model.Node;

/**
 * @author S. Ricci
 *
 */
public class NodeChangeMap {
	
	private Map<Integer, NodeChange<?>> nodeIdToChange;
	
	public NodeChangeMap() {
		nodeIdToChange = new LinkedHashMap<Integer, NodeChange<?>>();
	}
	
	public EntityChange prepareEntityChange(Entity entity) {
		EntityChange c = (EntityChange) getChange(entity);
		if(c == null){
			c = new NodeChange.EntityChange(entity);
			putChange(c);
		}
		return c;
	}

	public AttributeChange prepareAttributeChange(Attribute<?, ?> attribute) {
		AttributeChange c = (AttributeChange) getChange(attribute);
		if(c == null){
			c = new NodeChange.AttributeChange(attribute);
			putChange(c);
		}
		return c;
	}
	
	public NodeDeleteChange prepareDeleteNodeChange(Node<?> node) {
		NodeDeleteChange c = new NodeChange.NodeDeleteChange(node);
		nodeIdToChange.put(node.getInternalId(), c); //overwrite change if already present
		return c;
	}
	
	public NodeChange<?> prepareAddEntityChange(Entity node) {
		Integer nodeId = node.getInternalId();
		NodeChange<?> c = getChange(node);
		if ( c == null ) {
			c = new NodeChange.EntityAddChange(node);
			nodeIdToChange.put(nodeId, c);
			return c;
		} else {
			throw new IllegalStateException("AddNodeChange already present for node: " + nodeId);
		}
	}

	public NodeChange<?> prepareAddAttributeChange(Attribute<?, ?> node) {
		Integer nodeId = node.getInternalId();
		NodeChange<?> c = getChange(node);
		if ( c == null ) {
			c = new NodeChange.AttributeAddChange(node);
			nodeIdToChange.put(nodeId, c);
			return c;
		} else {
			throw new IllegalStateException("AddNodeChange already present for node: " + nodeId);
		}
	}
	
	public List<NodeChange<?>> values() {
		return new ArrayList<NodeChange<?>>(nodeIdToChange.values());
	}

	protected NodeChange<?> getChange(Node<?> node) {
		Integer nodeId = node.getInternalId();
		NodeChange<?> c = (NodeChange<?>) nodeIdToChange.get(nodeId);
		return c;
	}
	
	protected void putChange(NodeChange<?> change) {
		nodeIdToChange.put(change.getNode().getInternalId(), change);
	}
	
}
