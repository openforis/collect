/**
 * 
 */
package org.openforis.collect.model.proxy;

import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.RecordUpdateRequest;
import org.openforis.collect.model.RecordUpdateRequest.EntityAddRequest;

/**
 * 
 * @author S. Ricci
 *
 */
public class EntityAddRequestProxy extends RecordUpdateRequestProxy<EntityAddRequest> {
	
	private Integer parentEntityId;
	private String nodeName;
	
	@Override
	public EntityAddRequest toUpdateRequest(CollectRecord record) {
		EntityAddRequest request = new RecordUpdateRequest.EntityAddRequest();
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
