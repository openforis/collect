/**
 * 
 */
package org.openforis.collect.model;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

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
	
	public RecordUpdateResponse prepareResponse(Node<?> node) {
		Integer nodeId = node.getInternalId();
		RecordUpdateResponse response = nodeIdToResponse.get(nodeId);
		if(response == null){
			response = new RecordUpdateResponse(node);
			nodeIdToResponse.put(nodeId, response);
		}
		return response;
	}
	
	public Collection<RecordUpdateResponse> values() {
		return nodeIdToResponse.values();
	}

}
