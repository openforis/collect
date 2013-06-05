/**
 * 
 */
package org.openforis.collect.model.proxy;

import org.openforis.collect.manager.CodeListManager;
import org.openforis.collect.manager.RecordFileManager;
import org.openforis.collect.manager.SessionManager;
import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.remoting.service.NodeUpdateRequest;
import org.openforis.collect.remoting.service.NodeUpdateRequest.AttributeAddRequest;
import org.openforis.idm.model.Entity;
import org.openforis.idm.model.Value;

/**
 * 
 * @author S. Ricci
 *
 */
public class AttributeAddRequestProxy extends BaseAttributeUpdateRequestProxy<AttributeAddRequest<?>> {
	
	private Integer parentEntityId;
	private String nodeName;
	
	@Override
	public AttributeAddRequest<?> toAttributeUpdateRequest(CodeListManager codeListManager, RecordFileManager fileManager, 
			SessionManager sessionManager, CollectRecord record) {
		Entity parentEntity = (Entity) record.getNodeByInternalId(parentEntityId);
		AttributeAddRequest<Value> result = new NodeUpdateRequest.AttributeAddRequest<Value>();
		result.setParentEntity(parentEntity);
		result.setNodeName(nodeName);
		result.setRemarks(remarks);
		result.setSymbol(symbol);
		if ( value != null ) {
			Value parsedValue = parseCompositeAttributeValue(codeListManager, parentEntity, nodeName, value);
			result.setValue(parsedValue);
		}
		return result;
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
