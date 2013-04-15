/**
 * 
 */
package org.openforis.collect.model;

import java.util.List;

import org.openforis.commons.collection.CollectionUtils;

/**
 * @author S. Ricci
 *
 */
public class RecordUpdateRequestSet {
	
	private List<RecordUpdateRequest> requests;

	public List<RecordUpdateRequest> getRequests() {
		return CollectionUtils.unmodifiableList(requests);
	}

	public void setRequests(List<RecordUpdateRequest> requests) {
		this.requests = requests;
	}

}
