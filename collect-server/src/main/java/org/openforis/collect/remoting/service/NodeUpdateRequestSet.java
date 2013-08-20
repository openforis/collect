/**
 * 
 */
package org.openforis.collect.remoting.service;

import java.util.ArrayList;
import java.util.List;

import org.openforis.commons.collection.CollectionUtils;

/**
 * @author S. Ricci
 *
 */
public class NodeUpdateRequestSet {
	
	private List<NodeUpdateRequest> requests;
	
	public void addRequest(NodeUpdateRequest o) {
		if ( requests == null ) {
			requests = new ArrayList<NodeUpdateRequest>();
		}
		requests.add(o);
	}
	
	public List<NodeUpdateRequest> getRequests() {
		return CollectionUtils.unmodifiableList(requests);
	}

	public void setRequests(List<NodeUpdateRequest> requests) {
		this.requests = requests;
	}

}
