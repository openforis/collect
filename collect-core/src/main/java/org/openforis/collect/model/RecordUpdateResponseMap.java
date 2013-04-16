/**
 * 
 */
package org.openforis.collect.model;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.openforis.collect.model.RecordUpdateResponse.AttributeUpdateResponse;
import org.openforis.collect.model.RecordUpdateResponse.DeleteNodeResponse;
import org.openforis.collect.model.RecordUpdateResponse.EntityUpdateResponse;
import org.openforis.collect.model.RecordUpdateResponse.NodeUpdateResponse;
import org.openforis.idm.model.Attribute;
import org.openforis.idm.model.Entity;
import org.openforis.idm.model.Node;

/**
 * @author S. Ricci
 *
 */
public class RecordUpdateResponseMap {
	
	private Map<Integer, RecordUpdateResponse> nodeIdToResponse;
	
	public RecordUpdateResponseMap() {
		nodeIdToResponse = new HashMap<Integer, RecordUpdateResponse>();
	}
	
	public EntityUpdateResponse prepareEntityResponse(Entity entity) {
		EntityUpdateResponse response = (EntityUpdateResponse) getNodeResponse(entity);
		if(response == null){
			response = new RecordUpdateResponse.EntityUpdateResponse(entity);
			putResponse(response);
		}
		return response;
	}

	public AttributeUpdateResponse prepareAttributeResponse(Attribute<?, ?> attribute) {
		AttributeUpdateResponse response = (AttributeUpdateResponse) getNodeResponse(attribute);
		if(response == null){
			response = new RecordUpdateResponse.AttributeUpdateResponse(attribute);
			putResponse(response);
		}
		return response;
	}
	
	public DeleteNodeResponse prepareDeleteNodeResponse(Node<?> node) {
		Integer nodeId = node.getInternalId();
		DeleteNodeResponse response = new RecordUpdateResponse.DeleteNodeResponse();
		response.setDeletedNodeId(nodeId);
		nodeIdToResponse.put(nodeId, response); //overwrite response if already present
		return response;
	}
	
	public NodeUpdateResponse<?> prepareAddEntityResponse(Entity node) {
		Integer nodeId = node.getInternalId();
		NodeUpdateResponse<?> response = getNodeResponse(node);
		if ( response == null ) {
			response = new RecordUpdateResponse.AddEntityResponse(node);
			nodeIdToResponse.put(nodeId, response);
			return response;
		} else {
			throw new IllegalStateException("AddNodeResponse already present for node: " + nodeId);
		}
	}

	public NodeUpdateResponse<?> prepareAddAttributeResponse(Attribute<?, ?> node) {
		Integer nodeId = node.getInternalId();
		NodeUpdateResponse<?> response = getNodeResponse(node);
		if ( response == null ) {
			response = new RecordUpdateResponse.AddAttributeResponse(node);
			nodeIdToResponse.put(nodeId, response);
			return response;
		} else {
			throw new IllegalStateException("AddNodeResponse already present for node: " + nodeId);
		}
	}
	
	public Collection<RecordUpdateResponse> values() {
		return nodeIdToResponse.values();
	}

	protected NodeUpdateResponse<?> getNodeResponse(Node<?> node) {
		Integer nodeId = node.getInternalId();
		NodeUpdateResponse<?> response = (NodeUpdateResponse<?>) nodeIdToResponse.get(nodeId);
		return response;
	}
	
	protected void putResponse(NodeUpdateResponse<?> response) {
		nodeIdToResponse.put(response.getNode().getInternalId(), response);
	}
	
}
