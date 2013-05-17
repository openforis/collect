/**
 * 
 */
package org.openforis.collect.model.proxy;

import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.remoting.service.NodeUpdateRequest;
import org.openforis.collect.remoting.service.NodeUpdateRequest.AttributeAddRequest;
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
public class AttributeAddRequestProxy extends BaseAttributeUpdateRequestProxy<AttributeAddRequest<?>> {
	
	private Integer parentEntityId;
	private String nodeName;
	
	@Override
	public AttributeAddRequest<?> toNodeUpdateOptions(CollectRecord record) {
		Entity parentEntity = (Entity) record.getNodeByInternalId(parentEntityId);
		EntityDefinition parentEntityDefn = parentEntity.getDefinition();
		NodeDefinition childDefn = parentEntityDefn.getChildDefinition(nodeName);
		AttributeAddRequest<Value> result = new NodeUpdateRequest.AttributeAddRequest<Value>();
		result.setParentEntity(parentEntity);
		result.setNodeName(nodeName);
		result.setRemarks(remarks);
		result.setSymbol(symbol);
		if ( value != null ) {
			Value parsedValue = parseCompositeAttributeValue(parentEntity, (AttributeDefinition) childDefn, value);
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
