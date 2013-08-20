/**
 * 
 */
package org.openforis.collect.model.proxy;

import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.remoting.service.NodeUpdateRequest;
import org.openforis.collect.remoting.service.NodeUpdateRequest.MissingValueApproveRequest;
import org.openforis.idm.model.Entity;

/**
 * 
 * @author S. Ricci
 *
 */
public class MissingValueApproveRequestProxy extends NodeUpdateRequestProxy<MissingValueApproveRequest> {
	
	private Integer parentEntityId;
	private String nodeName;
	
	@Override
	public MissingValueApproveRequest toNodeUpdateRequest(CollectRecord record) {
		MissingValueApproveRequest opts = new NodeUpdateRequest.MissingValueApproveRequest();
		Entity parentEntity = (Entity) record.getNodeByInternalId(parentEntityId);
		opts.setParentEntity(parentEntity);
		opts.setNodeName(nodeName);
		return opts;	
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
