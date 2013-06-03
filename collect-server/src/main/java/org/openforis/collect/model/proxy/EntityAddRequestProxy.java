/**
 * 
 */
package org.openforis.collect.model.proxy;

import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.remoting.service.NodeUpdateRequest;
import org.openforis.collect.remoting.service.NodeUpdateRequest.EntityAddRequest;
import org.openforis.idm.model.Entity;

/**
 * 
 * @author S. Ricci
 *
 */
public class EntityAddRequestProxy extends NodeUpdateRequestProxy<EntityAddRequest> {
	
	private Integer parentEntityId;
	private String nodeName;
	
	@Override
	public EntityAddRequest toNodeUpdateRequest(CollectRecord record) {
		EntityAddRequest opts = new NodeUpdateRequest.EntityAddRequest();
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
