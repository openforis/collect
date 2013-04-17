/**
 * 
 */
package org.openforis.collect.model;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.openforis.collect.model.NodeUpdateResponse.AttributeUpdateResponse;
import org.openforis.collect.model.NodeUpdateResponse.EntityUpdateResponse;
import org.openforis.collect.model.NodeUpdateResponse.NodeDeleteResponse;
import org.openforis.idm.model.Attribute;
import org.openforis.idm.model.Entity;
import org.openforis.idm.model.Node;

/**
 * @author S. Ricci
 *
 */
public class NodeUpdateResponseMap {
	
	private Map<Integer, NodeUpdateResponse<?>> nodeIdToResponse;
	
	public NodeUpdateResponseMap() {
		nodeIdToResponse = new LinkedHashMap<Integer, NodeUpdateResponse<?>>();
	}
	
	public EntityUpdateResponse prepareEntityResponse(Entity entity) {
		EntityUpdateResponse response = (EntityUpdateResponse) getResponse(entity);
		if(response == null){
			response = new NodeUpdateResponse.EntityUpdateResponse(entity);
			putResponse(response);
		}
		return response;
	}

	public AttributeUpdateResponse prepareAttributeResponse(Attribute<?, ?> attribute) {
		AttributeUpdateResponse response = (AttributeUpdateResponse) getResponse(attribute);
		if(response == null){
			response = new NodeUpdateResponse.AttributeUpdateResponse(attribute);
			putResponse(response);
		}
		return response;
	}
	
	public NodeDeleteResponse prepareDeleteNodeResponse(Node<?> node) {
		NodeDeleteResponse response = new NodeUpdateResponse.NodeDeleteResponse(node);
		nodeIdToResponse.put(node.getInternalId(), response); //overwrite response if already present
		return response;
	}
	
	public NodeUpdateResponse<?> prepareAddEntityResponse(Entity node) {
		Integer nodeId = node.getInternalId();
		NodeUpdateResponse<?> response = getResponse(node);
		if ( response == null ) {
			response = new NodeUpdateResponse.EntityAddResponse(node);
			nodeIdToResponse.put(nodeId, response);
			return response;
		} else {
			throw new IllegalStateException("AddNodeResponse already present for node: " + nodeId);
		}
	}

	public NodeUpdateResponse<?> prepareAddAttributeResponse(Attribute<?, ?> node) {
		Integer nodeId = node.getInternalId();
		NodeUpdateResponse<?> response = getResponse(node);
		if ( response == null ) {
			response = new NodeUpdateResponse.AttributeAddResponse(node);
			nodeIdToResponse.put(nodeId, response);
			return response;
		} else {
			throw new IllegalStateException("AddNodeResponse already present for node: " + nodeId);
		}
	}
	
	public List<NodeUpdateResponse<?>> values() {
		return new ArrayList<NodeUpdateResponse<?>>(nodeIdToResponse.values());
	}

	protected NodeUpdateResponse<?> getResponse(Node<?> node) {
		Integer nodeId = node.getInternalId();
		NodeUpdateResponse<?> response = (NodeUpdateResponse<?>) nodeIdToResponse.get(nodeId);
		return response;
	}
	
	protected void putResponse(NodeUpdateResponse<?> response) {
		nodeIdToResponse.put(response.getNode().getInternalId(), response);
	}
	
}
