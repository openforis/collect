/**
 * 
 */
package org.openforis.collect.model.proxy;

import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.RecordUpdateRequest;
import org.openforis.collect.model.RecordUpdateRequest.AttributeAddRequest;
import org.openforis.idm.metamodel.AttributeDefinition;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.model.Entity;
import org.openforis.idm.model.Value;

/**
 * 
 * @author S. Ricci
 *
 */
public class AttributeAddRequestProxy extends BaseAttributeRequestProxy<AttributeAddRequest<?>> {
	
	private Integer parentEntityId;
	private String nodeName;
	
	@Override
	public AttributeAddRequest<?> toUpdateRequest(CollectRecord record) {
		Entity parentEntity = (Entity) record.getNodeByInternalId(parentEntityId);
		EntityDefinition parentEntityDefn = parentEntity.getDefinition();
		NodeDefinition childDefn = parentEntityDefn.getChildDefinition(nodeName);
		AttributeAddRequest<Value> request = new RecordUpdateRequest.AttributeAddRequest<Value>();
		request.setParentEntityId(parentEntityId);
		request.setNodeName(nodeName);
		request.setRemarks(remarks);
		request.setSymbol(symbol);
		if ( value != null ) {
			Value parsedValue = parseCompositeAttributeValue(parentEntity, (AttributeDefinition) childDefn, value);
			request.setValue(parsedValue);
		}
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
