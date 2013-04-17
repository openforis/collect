/**
 * 
 */
package org.openforis.collect.model.proxy;

import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.RecordUpdateRequest;
import org.openforis.collect.model.RecordUpdateRequest.MissingValueApproveRequest;

/**
 * 
 * @author S. Ricci
 *
 */
public class MissingValueApproveRequestProxy extends RecordUpdateRequestProxy<MissingValueApproveRequest> {
	
	private Integer parentEntityId;
	private String nodeName;
	
	@Override
	public MissingValueApproveRequest toUpdateRequest(CollectRecord record) {
		MissingValueApproveRequest request = new RecordUpdateRequest.MissingValueApproveRequest();
		request.setParentEntityId(parentEntityId);
		request.setNodeName(nodeName);
		return request;	
	}
	
	public Integer getParentEntityId() {
		return parentEntityId;
	}
	
	public void setParentEntityId(Integer parentEntityId) {
		this.parentEntityId = parentEntityId;
	}
	
	public String getNodeName() {
		return nodeName;
	}
	
	public void setNodeName(String nodeName) {
		this.nodeName = nodeName;
	}
	
}
